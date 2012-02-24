package gov.miamidade.hgowl.plugin.owl;

import gov.miamidade.hgowl.plugin.owl.model.HGOntologyRepositoryEntry;
import gov.miamidade.hgowl.plugin.owl.model.HGOwlModelManagerImpl;
import gov.miamidade.hgowl.plugin.owlapi.apibinding.PHGDBOntologyManagerImpl;
import gov.miamidade.hgowl.plugin.ui.render.VHGOwlIconProviderImpl;
import gov.miamidade.hgowl.plugin.ui.repository.VOntologyViewPanel;
import gov.miamidade.hgowl.plugin.ui.repository.VRepositoryViewPanel;
import gov.miamidade.hgowl.plugin.ui.versioning.VHGCommitDialog;

import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.protege.editor.core.OntologyRepository;
import org.protege.editor.core.OntologyRepositoryEntry;
import org.protege.editor.core.OntologyRepositoryManager;
import org.protege.editor.owl.OWLEditorKitFactory;
import org.protege.editor.owl.model.event.EventType;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import java.util.Collection;
import javax.swing.JOptionPane;


/**
 * VHGOwlEditorKit.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 24, 2012
 */
public class VHGOwlEditorKit extends HGOwlEditorKit {


	public VHGOwlEditorKit(OWLEditorKitFactory editorKitFactory) {
		super(editorKitFactory);
	}
	
	protected void initialise(){
		super.initialise();
		getWorkspace().setOWLIconProvider(new VHGOwlIconProviderImpl(modelManager, this));
	}

    public boolean handleAddVersionControlRequest() throws Exception {
    	System.out.println("VHG HandleAddVersionControlRequest");
    	boolean success;
        // Find our Repository 
        OntologyRepository repository = getProtegeRepository();
        if (repository == null) throw new IllegalStateException("No HGOwlOntologyRepository registered with Protege.");
        // Open Repository delete dialog 
        HGOntologyRepositoryEntry ontologyEntry = VRepositoryViewPanel.showAddToVersionControlDialog(getWorkspace(), repository);        
        if (ontologyEntry != null) {
        	// User wants to add ontology to version control.
        	// ?Ontology not versioned
        	// ?Ontolgy in repository
        	HGDBOntology onto = ontologyEntry.getOntology();
        	if (this.getVersionedRepository().isVersionControlled(onto)) {
        		success = false;
                JOptionPane.showMessageDialog(getWorkspace(),
                        "The selected ontology is already under version control.",
                        "Hypergraph Versioning - Add Ontology ",
                        JOptionPane.INFORMATION_MESSAGE);
        	} else {
        		String user = getUserName();
        		getVersionedRepository().addVersionControl(onto, user);
        		causeViewUpdate();
        		success = true;
                JOptionPane.showMessageDialog(getWorkspace(),
                        "The selected ontology: " + ontologyEntry.getOntologyURI() 
                        + "\r\n was sucessfully added to version control."
                        + "\r\n The icon for version controlled ontologies is a \"V\" inside a red diamond.",
                        "Hypergraph Versioning - Add Ontology ",
                        JOptionPane.INFORMATION_MESSAGE);
        	}
        } else {
        	success = false;
        }
        return success;
    }

    public boolean handleRemoveVersionControlRequest() throws Exception {
    	System.out.println("VHG HandleRemoveVersionControlRequest");
    	boolean success;
        // Find our Repository 
        OntologyRepository repository = getProtegeRepository();
        if (repository == null) throw new IllegalStateException("Cannot handle remove version control from repository. No HGOwlOntologyRepository registered with Protege.");
        // Open Repository delete dialog 
        HGOntologyRepositoryEntry ontologyEntry = VRepositoryViewPanel.showRemoveFromVersionControlDialog(getWorkspace(), repository);        
        if (ontologyEntry != null) {
        	// User wants to remove ontology from version control.
        	VersionedOntology vo = getVersionControlledOntologyBy(ontologyEntry);        	
        	if (vo != null) {
        		int userConfirm = JOptionPane.showConfirmDialog(getWorkspace(),
                        "All past revisions and changesets for ontology: " + ontologyEntry.getOntologyURI() 
                        + "\r\n will be deleted. This cannot be undone. " 
                        + "\r\n Only the last revision (head) will remain available. "
                        + "\r\n  Do you wish to continue?",
                        "Hypergraph Versioning - Remove Confirm",
                        JOptionPane.YES_NO_OPTION);
        		if (userConfirm == JOptionPane.YES_OPTION) {
	        		getVersionedRepository().removeVersionControl(vo);
	        		causeViewUpdate();
	        		success = true;
	                JOptionPane.showMessageDialog(getWorkspace(),
	                        "The selected ontology: " + ontologyEntry.getOntologyURI() 
	                        + "\r\n was sucessfully removed from version control.",
	                        "Hypergraph Versioning - Remove ",
	                        JOptionPane.INFORMATION_MESSAGE);
        		} else {
        			//user abort
	                JOptionPane.showMessageDialog(getWorkspace(),
	                        "The selected ontology: " + ontologyEntry.getOntologyURI() 
	                        + "\r\n was NOT removed from version control.",
	                        "Hypergraph Versioning - Remove abort",
	                        JOptionPane.INFORMATION_MESSAGE);
            		success = false;
        		}
        	} else {
                JOptionPane.showMessageDialog(getWorkspace(),
                        "The selected ontology is not under version control.",
                        "Hypergraph Versioning - Remove ",
                        JOptionPane.INFORMATION_MESSAGE);
        		success = false;
        	}
        } else {
        	success = false;
        }
        return success;
    }
    
    public boolean handleCommitActiveRequest() throws Exception {
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		//PHGDBOntologyManagerImpl  hom = (PHGDBOntologyManagerImpl)hmm.getOWLOntologyManager(); 
		OWLOntology activeOnto = hmm.getActiveOntology();
		VHGDBOntologyRepository vor = getVersionedRepository();
		if (vor.isVersionControlled(activeOnto)) {
			VersionedOntology vo = vor.getVersionControlledOntology(activeOnto);
			int pendingChanges = vo.getHeadChangeSet().size();
			if (pendingChanges == 0) {
				// NO PENDING CHANGES OK
				System.out.println("No pending changes.");
                JOptionPane.showMessageDialog(getWorkspace(),
                        "Cannot commit: No pending changes",
                        "Hypergraph Versioning - No Changes",
                        JOptionPane.INFORMATION_MESSAGE);
			} else {
				// 	COMMIT WHAT WHO INCREMENT OK CANCEL
				VHGCommitDialog dlg = VHGCommitDialog.showDialog(getWorkspace(), vo, activeOnto);
				if (dlg.isCommitOK()) {
					//DO IT 
					vo.commit(getUserName(), Revision.REVISION_INCREMENT, dlg.getCommitComment());
					// NEW REVISION OK
				}
			}
		} else {
			System.out.println("Active ontology not version controlled.");
            JOptionPane.showMessageDialog(getWorkspace(),
                    "Cannot commit: Active ontology not version controlled: \r\n" + activeOnto.getOntologyID(),
                    "Hypergraph Versioning - Active not versioned",
                    JOptionPane.INFORMATION_MESSAGE);
		}
    	return true;
    	
    }
    
    public VHGCommitDialog showUserCommitDialog(VersionedOntology vo, OWLOntology onto) {
//		int nrOfRevisions = vo.getNrOfRevisions();
//		Revision headRevision = vo.getHeadRevision();
//		int pendingChanges = vo.getHeadChangeSet().size();
//    	String message = "Do you want to commit " + pendingChanges + " change" 
//    		+ ((pendingChanges > 1)? "s" : "") + ":\n" 
//          	+ "    Last Revision    : " + headRevision.getRevision() + "\n"
//          	+ "    Created          : " + DateFormat.getDateTimeInstance().format(headRevision.getTimeStamp()) + "\n"
//          	+ "    By               : " + headRevision.getUser() + "\n" 
//    		+ "    Total Revisions  : " + nrOfRevisions + "\n" 
//    		+ "    Ontology ID : " + headRevision.getOntologyID() + "\n \n"; 
//        int userInput = JOptionPane.showConfirmDialog(getWorkspace(),
//                                      message,
//                                      "Commit Versioned HGDB Ontology - Confirm Commit",
//                                      JOptionPane.YES_NO_OPTION);
//        
//        return (userInput == JOptionPane.YES_OPTION);
    	return VHGCommitDialog.showDialog(getWorkspace(), vo, onto);
    	//return vhgc.getUserOK();
    }

    public boolean handleRollbackActiveRequest() throws Exception {
    	return true;
    }
    
    public boolean handleCommitRequest() throws Exception {
    	System.out.println("VHG HandleRemoveVersionControlRequest");
    	boolean success;
        // Find our Repository 
        OntologyRepository repository = getProtegeRepository();
        if (repository == null) throw new IllegalStateException("Cannot handle delete from repository. No HGOwlOntologyRepository registered with Protege.");
        // Open Repository delete dialog 
        HGOntologyRepositoryEntry ontologyEntry = VRepositoryViewPanel.showCommitDialog(getWorkspace(), repository);        
        if (ontologyEntry != null) {
        	// User wants to commit ontology.
        	VersionedOntology vo = getVersionControlledOntologyBy(ontologyEntry);
        	if (vo != null) {
        		VHGCommitDialog dlg = showUserCommitDialog(vo, vo.getHeadRevisionData());
        		if (dlg.isCommitOK()) {
        			//Clear undo/redo history on revert
        			getModelManager().getHistoryManager().clear();
        			vo.commit(getUserName(), dlg.getCommitComment());
        			success = true;
        		} else {
        			success = false;
        		}
        	} else {
        		//a given ontology entry was not a versioned ontology or 
        		success = false;
        	}
        } else {
        	//none selected
        	success = false;
        }
        return success;
    }

    public boolean handleRollbackRequest() throws Exception {
    	System.out.println("VHG HandleRemoveVersionControlRequest");
    	boolean success;
        // Find our Repository 
        OntologyRepository repository = getProtegeRepository();
        if (repository == null) throw new IllegalStateException("Cannot handle delete from repository. No HGOwlOntologyRepository registered with Protege.");
        // Open Repository delete dialog 
        HGOntologyRepositoryEntry ontologyEntry = VRepositoryViewPanel.showRollbackDialog(getWorkspace(), repository);        
        if (ontologyEntry != null) {
        	// User wants to add ontology to version control.
        	VersionedOntology vo = getVersionControlledOntologyBy(ontologyEntry);
        	if (vo != null) {
        		//? Head Changes.size() == 0, nothing to do
        		if (vo.getHeadChangeSet().size() > 0) { 
        			vo.rollback();
        			//Update Protege
        			causeViewUpdate();
        			//Clear undo/redo history on revert
        			getModelManager().getHistoryManager().clear();
        			success = true;
        		} else {
        			//nothing to do
                    JOptionPane.showMessageDialog(getWorkspace(),
                            "The selected ontology does not have any pending changes to roll back: \r\n" + ontologyEntry.getOntologyURI(),
                            "Hypergraph Versioning - Rollback ",
                            JOptionPane.INFORMATION_MESSAGE);
            		success = false;
        		}
        	} else {
                JOptionPane.showMessageDialog(getWorkspace(),
                        "The selected ontology is not under version control: \r\n" + ontologyEntry.getOntologyURI(),
                        "Hypergraph Versioning - Rollback ",
                        JOptionPane.INFORMATION_MESSAGE);
        		success = false;
        	}
        } else {
        	success = false;
        }
        return success;
    }

    public boolean handleRevertOneRequest() throws Exception {
    	System.out.println("VHG HandleRemoveVersionControlRequest");
    	boolean success;
        // Find our Repository 
        OntologyRepository repository = getProtegeRepository();
        if (repository == null) throw new IllegalStateException("Cannot handle revert one from repository. No HGOwlOntologyRepository registered with Protege.");
        // Open Repository delete dialog 
        HGOntologyRepositoryEntry ontologyEntry = VRepositoryViewPanel.showRevertOneDialog(getWorkspace(), repository);        
        if (ontologyEntry != null) {
        	// User wants to add ontology to version control.
        	VersionedOntology vo = getVersionControlledOntologyBy(ontologyEntry);
        	if (vo != null) {
        		// ?Head == Base?, cannot do it
        		//not on pending changes!
        		if (vo.getHeadChangeSet().size() == 0) {
        			if (vo.getNrOfRevisions() > 1) {
        				//Confirm
                		int userConfirm = JOptionPane.showConfirmDialog(getWorkspace(),
                                "The most recent revision and changeset for ontology: " + ontologyEntry.getOntologyURI() 
                                + "\r\n will be deleted. This cannot be undone. " 
                                + "\r\n  Do you wish to continue?",
                                "Hypergraph Versioning - Revert One Confirm",
                                JOptionPane.YES_NO_OPTION);
                		if (userConfirm == JOptionPane.YES_OPTION) {
	            			vo.revertHeadOneRevision();
	            			causeViewUpdate();
	            			//Clear undo/redo history on revert
	            			getModelManager().getHistoryManager().clear();
	            			success = true;
                		} else {
                			//Abort
        	                JOptionPane.showMessageDialog(getWorkspace(),
        	                        "The selected ontology: " + ontologyEntry.getOntologyURI() 
        	                        + "\r\n was NOT reverted.",
        	                        "Hypergraph Versioning - Revert One Abort",
        	                        JOptionPane.INFORMATION_MESSAGE);
                    		success = false;
                		}
        			} else {
        				//cannot revert beyond base
                        JOptionPane.showMessageDialog(getWorkspace(),
                                "This ontology cannot be reverted, because " 
                        		+ " it only has one revision: \r\n" 
                        		+ ontologyEntry.getOntologyURI() + " \r\n",
                                "Hypergraph Versioning - Revert One Revision",
                                JOptionPane.WARNING_MESSAGE);
            			success = false;
        			}
        		} else {
                    JOptionPane.showMessageDialog(getWorkspace(),
                            "This ontology cannot be reverted, because " 
                    		+ vo.getHeadChangeSet().size() + " pending changes exist: \r\n" 
                    		+ ontologyEntry.getOntologyURI() + " \r\n"
                    		+ " Rollback or Commit those changes first.",
                            "Hypergraph Versioning - Revert One Revision",
                            JOptionPane.WARNING_MESSAGE);
        			success = false;
        		}
        	} else {
                JOptionPane.showMessageDialog(getWorkspace(),
                        "The selected ontology is not under version control: \r\n" + ontologyEntry.getOntologyURI(),
                        "Hypergraph Versioning - Rollback ",
                        JOptionPane.INFORMATION_MESSAGE);
        		success = false;
        	}
        } else {
        	success = false;
        }
        return success;
    }

    /**
     * Returns the OntologyRepository implementation of our plugin (Protege Interface).
     * Will find the versioned repository.
     * 
     * @return the found VHGOwlOntologyRepository or null.
     */
    public OntologyRepository getProtegeRepository() {
        Collection<OntologyRepository> repositories = OntologyRepositoryManager.getManager().getOntologyRepositories();
    	for (OntologyRepository  cur: repositories) {
        	if (cur instanceof VHGOwlOntologyRepository) {
        		//current implementation uses first one found
        		return cur;
        	}
        }
        return null;
    }

    public VHGDBOntologyRepository getVersionedRepository() {
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		PHGDBOntologyManagerImpl  hom = (PHGDBOntologyManagerImpl)hmm.getOWLOntologyManager(); 
		return (VHGDBOntologyRepository)hom.getOntologyRepository();
    }
    
    public OWLOntology getLoadedOntologyBy(OntologyRepositoryEntry ontologyEntry) {
		OWLOntologyID oID = ((VHGOwlOntologyRepository.HGDBRepositoryEntry)ontologyEntry).getOntologyID();
		if (oID == null) throw new IllegalStateException();		
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		PHGDBOntologyManagerImpl  hom = (PHGDBOntologyManagerImpl)hmm.getOWLOntologyManager(); 
		return hom.getOntology(oID);
    }
    
    public VersionedOntology getVersionControlledOntologyBy(HGOntologyRepositoryEntry ontologyEntry) {
    	return getVersionedRepository().getVersionControlledOntology(ontologyEntry.getOntology());
    }
    
    void causeViewUpdate() {
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		hmm.fireEvent(EventType.ONTOLOGY_RELOADED);
		//this.workspace.refreshComponents();
    }
    
    String getUserName() {
    	return System.getProperty("user.name");
    }

	/**
	 * 
	 */
	public void handleShowHistoryActiveRequest() {
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		//PHGDBOntologyManagerImpl  hom = (PHGDBOntologyManagerImpl)hmm.getOWLOntologyManager(); 
		OWLOntology activeOnto = hmm.getActiveOntology();
		VHGDBOntologyRepository vor = getVersionedRepository();
		if (vor.isVersionControlled(activeOnto)) {
			VersionedOntology vo = vor.getVersionControlledOntology(activeOnto);
			VOntologyViewPanel.showRevisionDialog(getWorkspace(), vo);
		} else {
            JOptionPane.showMessageDialog(getWorkspace(),
                    "No History: Active ontology not version controlled",
                    "Hypergraph Versioning - Active not versioned",
                    JOptionPane.INFORMATION_MESSAGE);
		}
	} 
}