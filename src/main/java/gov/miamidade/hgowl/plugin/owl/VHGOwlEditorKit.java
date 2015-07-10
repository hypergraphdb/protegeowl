package gov.miamidade.hgowl.plugin.owl;

import gov.miamidade.hgowl.plugin.owl.model.HGOwlModelManagerImpl;
import gov.miamidade.hgowl.plugin.owlapi.apibinding.PHGDBOntologyManagerImpl;
import gov.miamidade.hgowl.plugin.ui.render.VHGOwlIconProviderImpl;
import gov.miamidade.hgowl.plugin.ui.versioning.RollbackDialog;
import gov.miamidade.hgowl.plugin.ui.versioning.VHGCommitDialog;
import gov.miamidade.hgowl.plugin.ui.versioning.VHGHistoryDialog;
import gov.miamidade.hgowl.plugin.ui.versioning.VHGRevertDialog;

import java.util.Collection;

import javax.swing.JOptionPane;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.newver.VersionManager;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.protege.editor.core.OntologyRepository;
import org.protege.editor.core.OntologyRepositoryEntry;
import org.protege.editor.core.OntologyRepositoryManager;
import org.protege.editor.owl.OWLEditorKitFactory;
import org.protege.editor.owl.model.event.EventType;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * VHGOwlEditorKit contains UI functions for editing versioned ontologies.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 24, 2012
 */
public class VHGOwlEditorKit extends HGOwlEditorKit
{
	public static boolean DBG = true;
 
	/**
	 * Return the HyperGraphDB handle of the active ontology if there is an
	 * active ontology that is stored in the database or null otherwise.
	 */
	public HGHandle activeOntology()
	{
		OWLOntology activeOntology = getActiveOntology();
		if (activeOntology == null)
			return null;
		else if (!(activeOntology instanceof HGDBOntology))
			return null;
		return ((HGDBOntology)activeOntology).getAtomHandle();		
	}
	
	public VersionManager versionManager()
	{
		return this.getModelManager().getOWLOntologyManager().getVersionManager();		
	}
	
	public VHGOwlEditorKit(OWLEditorKitFactory editorKitFactory)
	{
		super(editorKitFactory);
	}

	protected void initialise()
	{
		super.initialise();
		initializeIconProvider();
	}

	protected void initializeIconProvider()
	{
		getWorkspace().setOWLIconProvider(
				new VHGOwlIconProviderImpl(getModelManager(), this));
	}
	
	public boolean startVersioningActive()
	{
		OWLOntology activeOntology = getActiveOntology();
		if (!(activeOntology instanceof HGDBOntology))
		{
			JOptionPane.showMessageDialog(
				getWorkspace(),
				"The active ontology is file based. It needs to be imported \r\n into the repositorty before Versioning can be used.",
				"Hypergraph Versioning - Add Ontology ",
				JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		HGDBOntology hgdbOntology = (HGDBOntology)activeOntology;
		if (versionManager().isVersioned(hgdbOntology.getAtomHandle()))
		{
			JOptionPane.showMessageDialog(
				getWorkspace(),
				"The active ontology is already under version control.",
				"Hypergraph Versioning - Add Ontology ",
				JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		versionManager().versioned(hgdbOntology.getAtomHandle());
		causeViewUpdate();
		JOptionPane.showMessageDialog(
			getWorkspace(),
			"The active ontology: "
					+ activeOntology.getOntologyID()
					+ "\r\n was sucessfully added to version control."
					+ "\r\n The icon for version controlled ontologies is a \"V\" inside a red diamond.",
			"Hypergraph Versioning - Add Ontology ",
			JOptionPane.INFORMATION_MESSAGE);		
		return true;
	}
	
	public boolean stopVersioningActive()
	{
		OWLOntology activeOntology = getActiveOntology();
		if (activeOntology == null)
			return false;
		else if (!(activeOntology instanceof HGDBOntology))
			return false;
		HGHandle ontoHandle = ((HGDBOntology)activeOntology).getAtomHandle();
		if (!versionManager().isVersioned(ontoHandle))
		{
			JOptionPane.showMessageDialog(getWorkspace(),
				"The selected ontology is not under version control.",
				"Hypergraph Versioning - Remove ",
				JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		int userConfirm = JOptionPane.showConfirmDialog(
			getWorkspace(),
			"All past revisions and changesets for ontology: "
					+ activeOntology.getOntologyID()
					+ "\r\n will be deleted. This cannot be undone locally. "
					+ "\r\n Only the last revision (head) will remain available. "
					+ "\r\n  Do you wish to continue?",
			"Hypergraph Versioning - Remove Confirm",
			JOptionPane.YES_NO_OPTION);
		if (userConfirm == JOptionPane.YES_OPTION)
		{
			versionManager().removeVersioning(ontoHandle);
			causeViewUpdate();
			JOptionPane.showMessageDialog(
				getWorkspace(),
				"The selected ontology: "
						+ activeOntology.getOntologyID()
						+ "\r\n was sucessfully removed from version control.",
				"Hypergraph Versioning - Remove ",
				JOptionPane.INFORMATION_MESSAGE);
			return true;
		}
		else
		{
			// user abort
			JOptionPane.showMessageDialog(
				getWorkspace(),
				"The selected ontology: "
						+ activeOntology.getOntologyID()
						+ "\r\n was NOT removed from version control.",
				"Hypergraph Versioning - Remove abort",
				JOptionPane.INFORMATION_MESSAGE);
			return false;
		}			
	}

	public boolean commitActiveOntology() throws Exception
	{
		HGHandle hActive = activeOntology();
		if (hActive == null)
			return false;
		else if (!versionManager().isVersioned(hActive))
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"Cannot commit: Active ontology not version controlled.",
					"Hypergraph Versioning - Active not versioned",
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		VersionedOntology versioned = versionManager().versioned(hActive);
		if (versioned.changes().isEmpty())
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"Cannot commit: No pending changes",
					"Hypergraph Versioning - No Changes",
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		// COMMIT WHAT WHO INCREMENT OK CANCEL
		VHGCommitDialog dlg = VHGCommitDialog.showDialog(getWorkspace(), versioned);
		if (dlg.isCommitOK())
		{
			// DO IT
			versioned.commit(getSystemUserName(), 
							 dlg.getCommitComment());
		}
		return true;
	}

	public boolean undoLocalChanges()
	{
		HGHandle hActive = activeOntology();
		if (hActive == null || !versionManager().isVersioned(hActive))
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"The currently active ontology does not appear under version control.",
					"Hypergraph Versioning - Rollback ",
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		VersionedOntology ver = versionManager().versioned(hActive);
		if (ver.changes().isEmpty())
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"The selected ontology does not have any pending changes to roll back",
					"Hypergraph Versioning - Rollback ",
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		RollbackDialog dlg = RollbackDialog.showDialog("Hypergraph Versioning - Rollback", 
													   getWorkspace(), 
													   ver, 
													   this);		
		if (dlg.isRollbackOK() && 
				areYouSure("Hypergraph Versioning - Rollback",
					"Your pending changes will be lost. \r\n Are you sure to rollback?"))
		{
			ver.undo();
			causeViewUpdate();
			getModelManager().getHistoryManager().getLoggedChanges().clear();
			return true;			
		}
		else
			return false;
	}
	
	public void handleShowHistoryActiveRequest()
	{
		HGHandle hActive = activeOntology();
		if (hActive == null || !versionManager().isVersioned(hActive))
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"No History: Active ontology not version controlled",
					"Hypergraph Versioning - Active not versioned",
					JOptionPane.INFORMATION_MESSAGE);			
		}
		else
		{
			VersionedOntology vo = versionManager().versioned(hActive);
			VHGHistoryDialog.showDialog(
				"Hypergraph Versioning - History of " + vo, 
				getWorkspace(), vo, this);
		}
	}
	
	public boolean handleRevertActiveRequest() throws Exception
	{
		HGHandle hActive = activeOntology();
		if (hActive == null || !versionManager().isVersioned(hActive))
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"The selected ontology is not under version control.",
					"Hypergraph Versioning - Revert  ",
					JOptionPane.INFORMATION_MESSAGE);	
			return false;
		}
		VersionedOntology vo = versionManager().versioned(hActive);
		VHGRevertDialog dlg = VHGRevertDialog.showDialog(
				"Hypergraph Versioning - Revert " + vo, getWorkspace(), vo, this);
		if (dlg.isUserConfirmedRevert())
		{
			//Revision selectedRevision = dlg.getSelectedRevision();
			return true;
		}
		return false;
//		if (activeOntology != null)
//		{
//			VersionedOntology vo = getVersionedRepository()
//					.getVersionControlledOntology(activeOntology);
//			if (vo != null)
//			{
//				// ?Head == Base?, cannot do it
//				// not on pending changes!
//				// Allow working set changes
//				VHGRevertDialog dlg = VHGRevertDialog.showDialog(
//						"Hypergraph Versioning - Revert "
//								+ VDRenderer.render(vo), getWorkspace(), vo,
//						this);
//				if (dlg.isUserConfirmedRevert())
//				{
//					Revision selectedRevision = dlg.getSelectedRevision();
//					if (selectedRevision != null)
//					{
//						if (!selectedRevision.equals(vo.getHeadRevision()))
//						{
//							if (vo.getNrOfRevisions() > 1)
//							{
//								if (areYouSure(
//										"Hypergraph Versioning - Revert",
//										"Are you sure to revert ontololgy?"))
//								{
//									vo.revertHeadTo(selectedRevision, true);
//									causeViewUpdate();
//									// Clear undo/redo history on revert
//									getModelManager().getHistoryManager()
//											.getLoggedChanges().clear();
//									success = true;
//									if (vo.getWorkingSetConflicts().isEmpty())
//									{
//										JOptionPane
//												.showMessageDialog(
//														getWorkspace(),
//														"This ontology was sucessfully reverted. "
//																+ "Head is now: \r\n"
//																+ VDRenderer
//																		.render(vo
//																				.getHeadRevision())
//																+ "\r\n  ("
//																+ (vo.getHeadRevision()
//																		.getRevisionComment())
//																+ ")"
//																+ "\r\n Reapplied uncommitted changes: "
//																+ vo.getWorkingSetChanges()
//																		.size(),
//														"Hypergraph Versioning - Revert Completed",
//														JOptionPane.INFORMATION_MESSAGE);
//									}
//									else
//									{
//										// Conflicts.
//										JOptionPane
//												.showMessageDialog(
//														getWorkspace(),
//														"This ontology was sucessfully reverted, but conflicts occured with your uncommitted changes. "
//																+ "Head is now: \r\n"
//																+ VDRenderer
//																		.render(vo
//																				.getHeadRevision())
//																+ "\r\n  ("
//																+ (vo.getHeadRevision()
//																		.getRevisionComment())
//																+ ")"
//																+ "\r\n Reapplied uncommitted changes: "
//																+ vo.getWorkingSetChanges()
//																		.size()
//																+ "\r\n Conflicts: "
//																+ vo.getWorkingSetConflicts()
//																		.size()
//																+ "\r\n Please open Team/History for details on conflicts.",
//														"Hypergraph Versioning - Revert Completed with Conflicts",
//														JOptionPane.WARNING_MESSAGE);
//									}
//								}
//								else
//								{
//									success = false;
//								}
//							}
//							else
//							{
//								// cannot revert beyond base
//								JOptionPane
//										.showMessageDialog(
//												getWorkspace(),
//												"This ontology was not reverted, because "
//														+ " it only has one revision: \r\n"
//														+ VDRenderer.render(vo),
//												"Hypergraph Versioning - Revert Nothing to do",
//												JOptionPane.INFORMATION_MESSAGE);
//								success = false;
//							}
//						}
//						else
//						{
//							// head selected, nothing to do
//							JOptionPane
//									.showMessageDialog(
//											getWorkspace(),
//											"This ontology cannot be reverted, because "
//													+ " the user selected the head revision: \r\n"
//													+ VDRenderer.render(vo),
//											"Hypergraph Versioning - Revert Nothing to do",
//											JOptionPane.WARNING_MESSAGE);
//							success = false;
//						}
//					}
//					else
//					{
//						JOptionPane
//								.showMessageDialog(
//										getWorkspace(),
//										"The selected ontology: \r\n "
//												+ VDRenderer.render(vo)
//												+ "\r\n was NOT reverted, because no revision to revert to was selected.",
//										"Hypergraph Versioning - Revert Abort",
//										JOptionPane.WARNING_MESSAGE);
//						success = false;
//					}
//				}
//				else
//				{
//					// User abort: no dialog
//					success = false;
//				}
//			}
//			else
//			{
//				JOptionPane.showMessageDialog(getWorkspace(),
//						"The selected ontology is not under version control: \r\n"
//								+ VDRenderer.render(activeOntology),
//						"Hypergraph Versioning - Revert  ",
//						JOptionPane.INFORMATION_MESSAGE);
//				success = false;
//			}
//		}
//		else
//		{
//			success = false;
//		}
//		return success;
	}

	/**
	 * Returns the OntologyRepository implementation of our plugin (Protege
	 * Interface). Will find the versioned repository.
	 * 
	 * @return the found VHGOwlOntologyRepository or null.
	 */
	public OntologyRepository getProtegeRepository()
	{
		Collection<OntologyRepository> repositories = OntologyRepositoryManager
				.getManager().getOntologyRepositories();
		for (OntologyRepository cur : repositories)
		{
			if (cur instanceof VHGOwlOntologyRepository)
			{
				// current implementation uses first one found
				return cur;
			}
		}
		return null;
	}

	public OWLOntology getLoadedOntologyBy(OntologyRepositoryEntry ontologyEntry)
	{
		OWLOntologyID oID = ((VHGOwlOntologyRepository.VHGDBRepositoryEntry) ontologyEntry)
				.getOntologyID();
		if (oID == null)
			throw new IllegalStateException();
		HGOwlModelManagerImpl hmm = (HGOwlModelManagerImpl) getOWLModelManager();
		PHGDBOntologyManagerImpl hom = (PHGDBOntologyManagerImpl) hmm
				.getOWLOntologyManager();
		return hom.getOntology(oID);
	}
	
	public boolean isActiveOntologyVersioned()
	{
		OWLOntology activeOnto = getActiveOntology();
		return activeOnto instanceof HGDBOntology && 
				versionManager().isVersioned(((HGDBOntology)activeOnto).getAtomHandle());
	}

	protected void causeViewUpdate()
	{
		HGOwlModelManagerImpl hmm = (HGOwlModelManagerImpl) getOWLModelManager();
		hmm.fireEvent(EventType.ONTOLOGY_RELOADED);
		this.getWorkspace().refreshComponents();
	}

	String getSystemUserName()
	{
		return System.getProperty("user.name");
	}

	public boolean areYouSure(String title, String txt)
	{
		int confirm = JOptionPane.showConfirmDialog(getWorkspace(), txt, title,
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		return (confirm == JOptionPane.OK_OPTION);
	}

	public String getRenderedActivityException(Throwable e)
	{
		Throwable ex = e;
		while (ex instanceof RuntimeException && ex.getCause() != null)
		{
			ex = ex.getCause();
		}
		System.err.println(ex);
		return "Error : " + ex.getClass() + "\r\n" + ex.getMessage();
	}

}