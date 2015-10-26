package gov.miamidade.hgowl.plugin.owl;

import static gov.miamidade.hgowl.plugin.Singles.*;
import gov.miamidade.hgowl.plugin.Singles;
import gov.miamidade.hgowl.plugin.owl.model.HGOwlModelManagerImpl;
import gov.miamidade.hgowl.plugin.owlapi.apibinding.PHGDBOntologyManagerImpl;
import gov.miamidade.hgowl.plugin.ui.render.VHGOwlIconProviderImpl;
import gov.miamidade.hgowl.plugin.ui.versioning.RevisionDialog;
import gov.miamidade.hgowl.plugin.ui.versioning.RollbackDialog;
import gov.miamidade.hgowl.plugin.ui.versioning.VHGCommitDialog;

import java.util.Collection;

import javax.swing.JOptionPane;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
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

	public boolean commitActiveOntology()
	{
		HGHandle hActive = activeOntology();
		if (hActive == null)
			return false;
		else if (!versionManager().isVersioned(hActive))
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"Cannot commit: Active ontology not version controlled.",
					Singles.bundles().value("commit.dialog.title"),
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		VersionedOntology versioned = versionManager().versioned(hActive);
		if (versioned.changes().isEmpty())
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"Cannot commit: No pending changes",
					Singles.bundles().value("commit.dialog.title"),
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		// COMMIT WHAT WHO INCREMENT OK CANCEL
		VHGCommitDialog dlg = VHGCommitDialog.showDialog(getWorkspace(), versioned, false);
		if (dlg.isCommitOK())
		{
			// DO IT
			versioned.commit(getSystemUserName(), 
							 dlg.getCommitComment());
		}
		return true;
	}

	public boolean commitNewBranch()
	{
		HGHandle hActive = activeOntology();
		if (hActive == null)
			return false;
		else if (!versionManager().isVersioned(hActive))
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"Cannot create branch on a non version controlled ontology.",
					Singles.bundles().value("newbranch.dialog.title"),
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		VersionedOntology versioned = versionManager().versioned(hActive);
		if (!versioned.changes().isEmpty())
		{
			if (JOptionPane.showConfirmDialog(getWorkspace(),
					"A new branch will be created with current working changes as a first commit. Proceed?",
					Singles.bundles().value("newbranch.dialog.title"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
				return false;
		}
		VHGCommitDialog dlg = VHGCommitDialog.showDialog(getWorkspace(), versioned, true);
		if (dlg.isCommitOK())
		{
			if (dlg.getBranchName() == null || dlg.getBranchName().length() == 0)
			{
				JOptionPane.showMessageDialog(getWorkspace(),
						"No branch name specified.",
						Singles.bundles().value("newbranch.dialog.title"),
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}	
			else if (versioned.metadata().findBranch(dlg.getBranchName()) != null)
			{
				JOptionPane.showMessageDialog(getWorkspace(),
						"Branch name already exists. To commit to that branch, first go to its head revision.",
						Singles.bundles().value("newbranch.dialog.title"),
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
			// DO IT
			versioned.commit(getSystemUserName(), 
							 dlg.getCommitComment(),
							 dlg.getBranchName());
		}
		return true;
	}
	
	public boolean undoLocalChanges()
	{ 
		String actionTitle = bundles().value("undoworking.action.title","Drop Working Changes");
		HGHandle hActive = activeOntology();
		if (hActive == null || !versionManager().isVersioned(hActive))
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"The currently active ontology does not appear under version control.",
					actionTitle,
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		VersionedOntology ver = versionManager().versioned(hActive);
		if (ver.changes().isEmpty())
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"The selected ontology does not have any pending changes to roll back",
					actionTitle,
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		RollbackDialog dlg = RollbackDialog.showDialog(actionTitle, 
													   getWorkspace(), 
													   ver, 
													   this);		
		if (dlg.isRollbackOK() && 
				areYouSure(actionTitle,
					"Your pending changes will be lost. \r\n Are you sure to undo them?"))
		{
			ver.undo();
			causeViewUpdate();
			getModelManager().getHistoryManager().getLoggedChanges().clear();
			return true;			
		}
		else
			return false;
	}
	
	public void showHistoryActive()
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
			new RevisionDialog("Hypergraph Versioning - History of " + vo, 
									 getWorkspace(), 
									 vo, 
									 this).build().showDialog();
		}
	}
	
	public void revertActive()
	{
		HGHandle hActive = activeOntology();
		if (hActive == null || !versionManager().isVersioned(hActive))
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"The selected ontology is not under version control.",
					"Hypergraph Versioning - Revert",
					JOptionPane.INFORMATION_MESSAGE);	
			return;
		}
		final VersionedOntology vo = versionManager().versioned(hActive);
		final RevisionDialog dlg = new RevisionDialog(
				"Hypergraph Versioning - Revert " + vo, 
				 getWorkspace(), 
				 vo, 
				 this);
		Runnable revertAction = new Runnable() { public void run() {
			Revision rev = dlg.getSelectedRevision();
			if (!rev.children().isEmpty())
			{
				JOptionPane.showMessageDialog(getWorkspace(),
						"The selected revision is not a head revision and cannot be removed.",
						"Hypergraph Versioning - Revert  ",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			else if (JOptionPane.showConfirmDialog(getWorkspace(),
					"The selected head revision will be deleted from the version history.Proceed?", 						
					"CAUTION: Irreversible Operation",
					JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
				return;
			vo.dropHeadRevision(rev.getAtomHandle());
			dlg.ontologyView().getTableModel().deleteRevison(rev);
			
		}};	
		dlg.action("Revert To Previous Revision", revertAction)
		   .build()
		   .showDialog();
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