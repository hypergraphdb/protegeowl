package gov.miamidade.hgowl.plugin.owl;

import gov.miamidade.hgowl.plugin.owl.model.HGOntologyRepositoryEntry;
import gov.miamidade.hgowl.plugin.owl.model.HGOwlModelManagerImpl;
import gov.miamidade.hgowl.plugin.owlapi.apibinding.PHGDBOntologyManagerImpl;
import gov.miamidade.hgowl.plugin.ui.render.VHGOwlIconProviderImpl;
import gov.miamidade.hgowl.plugin.ui.repository.VRepositoryViewPanel;
import gov.miamidade.hgowl.plugin.ui.versioning.RollbackDialog;
import gov.miamidade.hgowl.plugin.ui.versioning.VHGCommitDialog;
import gov.miamidade.hgowl.plugin.ui.versioning.VHGHistoryDialog;
import gov.miamidade.hgowl.plugin.ui.versioning.VHGRevertDialog;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.VDRenderer;

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

	public static boolean DBG = true;

	public VHGOwlEditorKit(OWLEditorKitFactory editorKitFactory) {
		super(editorKitFactory);
	}
	
	protected void initialise(){
		super.initialise();
		initializeIconProvider();
	}
	
	protected void initializeIconProvider() {
		getWorkspace().setOWLIconProvider(new VHGOwlIconProviderImpl(modelManager, this));
	}

    public boolean handleAddActiveToVersionControlRequest() throws Exception {
    	boolean success;
        OWLOntology  activeOntology = getActiveOntology();
        if (activeOntology != null) {
        	if (activeOntology instanceof HGDBOntology) {
		        	// ?Ontology not versioned
		        	// ?Ontolgy in repository
		        	if (this.getVersionedRepository().isVersionControlled(activeOntology)) {
		        		success = false;
		                JOptionPane.showMessageDialog(getWorkspace(),
		                        "The active ontology is already under version control.",
		                        "Hypergraph Versioning - Add Ontology ",
		                        JOptionPane.INFORMATION_MESSAGE);
		        	} else {
		        		String user = getSystemUserName();
		        		getVersionedRepository().addVersionControl((HGDBOntology)activeOntology, user);
		        		causeViewUpdate();
		        		success = true;
		                JOptionPane.showMessageDialog(getWorkspace(),
		                        "The active ontology: " + activeOntology.getOntologyID() 
		                        + "\r\n was sucessfully added to version control."
		                        + "\r\n The icon for version controlled ontologies is a \"V\" inside a red diamond.",
		                        "Hypergraph Versioning - Add Ontology ",
		                        JOptionPane.INFORMATION_MESSAGE);
		        	}
        	} else {
        		//file based
                JOptionPane.showMessageDialog(getWorkspace(),
                        "The active ontology is file based. It needs to be imported \r\n into the repositorty before Versioning can be used.",
                        "Hypergraph Versioning - Add Ontology ",
                        JOptionPane.INFORMATION_MESSAGE);
                success = false;
        	}
        } else {
        	success = false;
        }
        return success;
    }

    public boolean handleRemoveActiveFromVersionControlRequest() throws Exception {
    	boolean success;
        OWLOntology  activeOntology = getActiveOntology();
        if (activeOntology != null) {
        	// User wants to remove ontology from version control.
        	VersionedOntology vo = getVersionedRepository().getVersionControlledOntology(activeOntology);        	
        	if (vo != null) {
        		int userConfirm = JOptionPane.showConfirmDialog(getWorkspace(),
                        "All past revisions and changesets for ontology: " + activeOntology.getOntologyID() 
                        + "\r\n will be deleted. This cannot be undone locally. " 
                        + "\r\n Only the last revision (head) will remain available. "
                        + "\r\n  Do you wish to continue?",
                        "Hypergraph Versioning - Remove Confirm",
                        JOptionPane.YES_NO_OPTION);
        		if (userConfirm == JOptionPane.YES_OPTION) {
	        		getVersionedRepository().removeVersionControl(vo);
	        		causeViewUpdate();
	        		success = true;
	                JOptionPane.showMessageDialog(getWorkspace(),
	                        "The selected ontology: " + activeOntology.getOntologyID() 
	                        + "\r\n was sucessfully removed from version control.",
	                        "Hypergraph Versioning - Remove ",
	                        JOptionPane.INFORMATION_MESSAGE);
        		} else {
        			//user abort
	                JOptionPane.showMessageDialog(getWorkspace(),
	                        "The selected ontology: " + activeOntology.getOntologyID() 
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
			int pendingChanges = vo.getWorkingSetChanges().size();
			if (pendingChanges == 0) {
				// NO PENDING CHANGES OK
				//System.out.println("No pending changes.");
                JOptionPane.showMessageDialog(getWorkspace(),
                        "Cannot commit: No pending changes",
                        "Hypergraph Versioning - No Changes",
                        JOptionPane.INFORMATION_MESSAGE);
			} else {
				// 	COMMIT WHAT WHO INCREMENT OK CANCEL
				VHGCommitDialog dlg = VHGCommitDialog.showDialog(getWorkspace(), vo, activeOnto);
				if (dlg.isCommitOK()) {
					//DO IT 
					vo.commit(getSystemUserName(), Revision.REVISION_INCREMENT, dlg.getCommitComment());
					// NEW REVISION OK
				}
			}
		} else {
			//System.out.println("Active ontology not version controlled.");
            JOptionPane.showMessageDialog(getWorkspace(),
                    "Cannot commit: Active ontology not version controlled: \r\n" + activeOnto.getOntologyID(),
                    "Hypergraph Versioning - Active not versioned",
                    JOptionPane.INFORMATION_MESSAGE);
		}
    	return true;
    }
    
    public VHGCommitDialog showUserCommitDialog(VersionedOntology vo, OWLOntology onto) {
    	return VHGCommitDialog.showDialog(getWorkspace(), vo, onto);
    }

//    public boolean handleCommitRequest() throws Exception {
//    	System.out.println("VHG HandleRemoveVersionControlRequest");
//    	boolean success;
//        // Find our Repository 
//        OntologyRepository repository = getProtegeRepository();
//        if (repository == null) throw new IllegalStateException("Cannot handle delete from repository. No HGOwlOntologyRepository registered with Protege.");
//        // Open Repository delete dialog 
//        HGOntologyRepositoryEntry ontologyEntry = VRepositoryViewPanel.showCommitDialog(getWorkspace(), repository);        
//        if (ontologyEntry != null) {
//        	// User wants to commit ontology.
//        	VersionedOntology vo = getVersionControlledOntologyBy(ontologyEntry);
//        	if (vo != null) {
//        		VHGCommitDialog dlg = showUserCommitDialog(vo, vo.getWorkingSetData());
//        		if (dlg.isCommitOK()) {
//        			//Clear undo/redo history on revert
//        			getModelManager().getHistoryManager().clear();
//        			vo.commit(getUserName(), dlg.getCommitComment());
//        			success = true;
//        		} else {
//        			success = false;
//        		}
//        	} else {
//        		//a given ontology entry was not a versioned ontology or 
//        		success = false;
//        	}
//        } else {
//        	//none selected
//        	success = false;
//        }
//        return success;
//    }

    public boolean handleRollbackActiveRequest() throws Exception {
    	boolean success;
    	OWLOntology activeOntology = getActiveOntology();
        if (activeOntology != null) {
        	// User wants to add ontology to version control.
        	VersionedOntology vo = getVersionedRepository().getVersionControlledOntology(activeOntology);
        	if (vo != null) {
        		//? Head Changes.size() == 0, nothing to do
        		if (vo.getWorkingSetChanges().size() > 0) {
        			RollbackDialog dlg = RollbackDialog.showDialog("Hypergraph Versioning - Rollback", getWorkspace(), vo);
        			if (dlg.isRolbackOK()) {
//            		int userConfirm = JOptionPane.showConfirmDialog(getWorkspace(),
//                            "All currently pending changes for : \r\n" + VDRenderer.render(activeOntology) 
//                            + "\r\n will be deleted. This cannot be undone locally. " 
//                            + "\r\n The number of changes to be rolled back is : " +  vo.getWorkingSetChanges().size()
//                            + "\r\n  Do you wish to roll back ?",
//                            "Hypergraph Versioning - Rollback Confirm",
//                            JOptionPane.YES_NO_OPTION);
//            		if (userConfirm == JOptionPane.YES_OPTION) { 
            			vo.rollback();
            			//Update Protege
            			causeViewUpdate();
            			//Clear undo/redo history on revert
            			getModelManager().getHistoryManager().clear();
            			success = true;
            		} else {
            			//User abort: no dialog
            			success = false;
            			//Show aborted dialog.
            		}
        		} else {
        			//nothing to do
                    JOptionPane.showMessageDialog(getWorkspace(),
                            "The selected ontology does not have any pending changes to roll back: \r\n" + VDRenderer.render(activeOntology),
                            "Hypergraph Versioning - Rollback ",
                            JOptionPane.INFORMATION_MESSAGE);
            		success = false;
        		}
        	} else {
                JOptionPane.showMessageDialog(getWorkspace(),
                        "The selected ontology is not under version control: \r\n" + VDRenderer.render(activeOntology),
                        "Hypergraph Versioning - Rollback ",
                        JOptionPane.INFORMATION_MESSAGE);
        		success = false;
        	}
        } else {
        	success = false;
        }
        return success;
    }

    public boolean handleRevertActiveRequest() throws Exception {
    	boolean success;
        // Find our Repository 
    	OWLOntology activeOntology = getActiveOntology();
        if (activeOntology != null) {
        	VersionedOntology vo = getVersionedRepository().getVersionControlledOntology(activeOntology);
        	if (vo != null) {
        		// ?Head == Base?, cannot do it
        		//not on pending changes!
        		// Allow working set changes
        		VHGRevertDialog dlg = VHGRevertDialog.showDialog("Hypergraph Versioning - Revert " + VDRenderer.render(vo), getWorkspace(), vo);
        		if (dlg.isUserConfirmedRevert()) {
        			Revision selectedRevision = dlg.getSelectedRevision();
        			if (selectedRevision != null) {
        				if (!selectedRevision.equals(vo.getHeadRevision())) {
	        				if (vo.getNrOfRevisions() > 1) {
		            			vo.revertHeadTo(selectedRevision, true);
		            			causeViewUpdate();
		            			//Clear undo/redo history on revert
		            			getModelManager().getHistoryManager().clear();
		            			success = true;
		                        JOptionPane.showMessageDialog(getWorkspace(),
		                                "This ontology was sucessfully  reverted. " 
		                        		+ "Head is now: \r\n" 
		                        		+ VDRenderer.render(vo.getHeadRevision()) 
		                        		+ "\r\n  (" + (vo.getHeadRevision().getRevisionComment()) + ")"
		                        		+ "\r\n Reapplied uncommitted changes: " + vo.getWorkingSetChanges().size(),
		                                "Hypergraph Versioning - Revert Completed",
		                                JOptionPane.INFORMATION_MESSAGE);
		        			} else {
		        				//cannot revert beyond base
		                        JOptionPane.showMessageDialog(getWorkspace(),
		                                "This ontology was not reverted, because " 
		                        		+ " it only has one revision: \r\n" 
		                        		+ VDRenderer.render(vo),
		                                "Hypergraph Versioning - Revert Nothing to do",
		                                JOptionPane.INFORMATION_MESSAGE);
		            			success = false;
		        			}
        				} else {
	        				//head selected, nothing to do
	                        JOptionPane.showMessageDialog(getWorkspace(),
	                                "This ontology cannot be reverted, because " 
	                        		+ " the user selected the head revision: \r\n" 
	                        		+ VDRenderer.render(vo),
	                                "Hypergraph Versioning - Revert Nothing to do",
	                                JOptionPane.WARNING_MESSAGE);
	            			success = false;
	        			}
            		} else {
    	                JOptionPane.showMessageDialog(getWorkspace(),
    	                        "The selected ontology: \r\n " + VDRenderer.render(vo) 
    	                        + "\r\n was NOT reverted, because no revision to revert to was selected.",
    	                        "Hypergraph Versioning - Revert Abort",
    	                        JOptionPane.WARNING_MESSAGE);
                		success = false;
            		}
        		} else {
        			//User abort: no dialog
        			success = false;
        		}
        	} else {
                JOptionPane.showMessageDialog(getWorkspace(),
                        "The selected ontology is not under version control: \r\n" 
                		+ VDRenderer.render(activeOntology)
                        ,"Hypergraph Versioning - Revert  ",
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
		OWLOntologyID oID = ((VHGOwlOntologyRepository.VHGDBRepositoryEntry)ontologyEntry).getOntologyID();
		if (oID == null) throw new IllegalStateException();		
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		PHGDBOntologyManagerImpl  hom = (PHGDBOntologyManagerImpl)hmm.getOWLOntologyManager(); 
		return hom.getOntology(oID);
    }
    
    public VersionedOntology getVersionControlledOntologyBy(HGOntologyRepositoryEntry ontologyEntry) {
    	return getVersionedRepository().getVersionControlledOntology(ontologyEntry.getOntology());
    }

    public boolean isActiveOntologyVersioned() {
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		OWLOntology activeOnto = hmm.getActiveOntology();
    	return getVersionedRepository().getVersionControlledOntology(activeOnto) != null;
    }

    protected void causeViewUpdate() {
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		hmm.fireEvent(EventType.ONTOLOGY_RELOADED);
		//this.workspace.refreshComponents();
    }
    
    String getSystemUserName() {
    	return System.getProperty("user.name");
    }

	public void handleShowHistoryActiveRequest() {
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		//PHGDBOntologyManagerImpl  hom = (PHGDBOntologyManagerImpl)hmm.getOWLOntologyManager(); 
		OWLOntology activeOnto = hmm.getActiveOntology();
		VHGDBOntologyRepository vor = getVersionedRepository();
		if (vor.isVersionControlled(activeOnto)) {
			VersionedOntology vo = vor.getVersionControlledOntology(activeOnto);
			//VOntologyViewPanel.showRevisionDialog(getWorkspace(), vo);
			VHGHistoryDialog.showDialog("Hypergraph Versioning - History of " + VDRenderer.render(vo), getWorkspace(), vo);
		} else {
            JOptionPane.showMessageDialog(getWorkspace(),
                    "No History: Active ontology not version controlled",
                    "Hypergraph Versioning - Active not versioned",
                    JOptionPane.INFORMATION_MESSAGE);
		}
	} 

	public String getRenderedActivityException(Throwable e) {
    	Throwable ex = e;
    	while (ex instanceof RuntimeException && ex.getCause() != null) {
    		ex = ex.getCause();
    	}
    	System.err.println(ex);
    	return "Error : " + ex.getClass() + "\r\n" + ex.getMessage();
    }

}