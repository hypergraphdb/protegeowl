package gov.miamidade.hgowl.plugin.owl;

import gov.miamidade.hgowl.plugin.owl.model.HGOntologyRepositoryEntry;
import gov.miamidade.hgowl.plugin.owl.model.HGOwlModelManagerImpl;
import gov.miamidade.hgowl.plugin.owlapi.apibinding.PHGDBOntologyManagerImpl;
import gov.miamidade.hgowl.plugin.ui.CreateHGOntologyWizard;
import gov.miamidade.hgowl.plugin.ui.HGOntologyFormatPanel;
import gov.miamidade.hgowl.plugin.ui.repository.RepositoryViewPanel;

import java.io.File;
import java.net.ProtocolException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;






//import org.apache.log4j.Logger;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyFormat;
import org.hypergraphdb.app.owl.HGDBOntologyImpl;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.OntologyDatabase;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByDocumentIRIException;
import org.osgi.framework.ServiceRegistration;
import org.protege.editor.core.OntologyRepository;
import org.protege.editor.core.OntologyRepositoryEntry;
import org.protege.editor.core.OntologyRepositoryManager;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.editorkit.EditorKitDescriptor;
import org.protege.editor.core.editorkit.RecentEditorKitManager;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.core.ui.wizard.Wizard;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.OWLEditorKitFactory;
import org.protege.editor.owl.ProtegeOWL;
import org.protege.editor.owl.model.SaveErrorHandler;
import org.protege.editor.owl.model.io.IOListenerPlugin;
import org.protege.editor.owl.model.io.IOListenerPluginInstance;
import org.protege.editor.owl.model.io.IOListenerPluginLoader;
//import org.protege.editor.owl.model.search.SearchManager;
//import org.protege.editor.owl.model.search.SearchMetadataImportManager;
import org.protege.editor.owl.ui.SaveConfirmationPanel;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.error.OntologyLoadErrorHandlerUI;
import org.protege.editor.owl.ui.explanation.ExplanationManager;
import org.protege.editor.owl.ui.ontology.imports.missing.MissingImportHandlerUI;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyDocumentAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.VersionInfo;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

/**
 * HGOwlEditorKit contains UI functions for editing database-backed ontologies.
 * 
 * Here, the crucial connection between Protege Editor and our OWL Api with
 * Hypergraph backend is established (see initialize). The connection point to
 * the Hypergraph OWL-API implementation is HGOWLModelManager.
 * 
 * 2012.02.06 added: use HGDB ontology after import (reload)
 * 
 * @author Thomas Hilpold
 */
public class HGOwlEditorKit extends OWLEditorKit
{
	public static boolean DBG = false;

	public static final String ID = "HGOwlEditorKit";

	private ServiceRegistration registration;

	public HGOwlEditorKit(OWLEditorKitFactory editorKitFactory)
	{
		super(editorKitFactory);
	}

	private HGOwlModelManagerImpl modelManager;
	private OWLOntologyChangeListener ontologyChangeListener;
	private boolean modifiedDocument = false;
	
	@SuppressWarnings("deprecation")
	protected void initialise()
	{
		// DO NOT DO THIS: super.initialise();
		// THIS SETS OUR MODEL MANAGER
		modelManager = new HGOwlModelManagerImpl();
		System.out.println("Using OWL API version " + VersionInfo.getVersionInfo().getVersion());
		// logger.info("Using OWL API version " +
		// VersionInfo.getVersionInfo().getVersion());
		modelManager.setExplanationManager(new ExplanationManager(this));
		modelManager.setMissingImportHandler(new MissingImportHandlerUI(this));
		modelManager.setSaveErrorHandler(new SaveErrorHandler()
		{
			public void handleErrorSavingOntology(OWLOntology ont, URI physicalURIForOntology, OWLOntologyStorageException e)
					throws Exception
			{
				if (e.getCause() != null && e.getCause() instanceof ProtocolException)
				{
					hghandleSaveAs(ont);
				}
				else
				{
					throw e;
				}
			}
		});
		modelManager.setLoadErrorHandler(new OntologyLoadErrorHandlerUI(this));

//		searchManager = new SearchManager(this, new SearchMetadataImportManager());

		this.getWorkspace().getStatusArea();
		setOWLModelManager(modelManager);

		ontologyChangeListener = new OWLOntologyChangeListener()
		{
			public void ontologiesChanged(List<? extends OWLOntologyChange> owlOntologyChanges) throws OWLException
			{
				modifiedDocument = true;
			}
		};
		modelManager.addOntologyChangeListener(ontologyChangeListener);

		OntologyLoadErrorHandlerUI loadErrorHandler = new OntologyLoadErrorHandlerUI(this);
		modelManager.setLoadErrorHandler(loadErrorHandler);

		IOListenerPluginLoader loader = new IOListenerPluginLoader(this);
		for (IOListenerPlugin pl : loader.getPlugins())
		{
			try
			{
				IOListenerPluginInstance instance = pl.newInstance();
				getModelManager().addIOListener(instance);
			}
			catch (Throwable e)
			{
				ProtegeApplication.getErrorLog().logError(e);
			}
		}

		// loadIOListenerPlugins();
		registration = ProtegeOWL.getBundleContext().registerService(EditorKit.class.getCanonicalName(), this,
				new Hashtable<String, Object>());

		getWorkspace().refreshComponents();
	}
	
	@Override
	public HGOwlModelManagerImpl getModelManager()
	{
		return modelManager;
	}

	protected void initialiseCompleted()
	{
		super.initialiseCompleted();
	}

	/**
	 * Gets the <code>EditorKit</code> Id. This can be used to identify the type
	 * of <code>EditorKit</code>.
	 * 
	 * @return A <code>String</code> that represents the <code>EditorKit</code>
	 *         Id.
	 */
	public String getId()
	{
		return "OWLEditorKit";
	}

	/**
	 * This method gets called when protege starts up always, to open a 
	 * new ontology. But we don't want to always create a new ontology, if
	 * we are working on something, we want to open the last one automatically.
	 */
	public boolean handleNewRequest() throws Exception
	{
		List<EditorKitDescriptor> L = RecentEditorKitManager.getInstance().getDescriptors();
		if (L.isEmpty() || ProtegeManager.getInstance().getEditorKitManager().getEditorKitCount() > 0)
			return super.handleNewRequest();
		else // if user opted to load most recent
		{
			boolean loaded = false;
			for (EditorKitDescriptor descriptor : L)
			{
				URI uri = descriptor.getURI(URI_KEY);				
				if (getModelManager().getOWLOntologyManager()
						.getOntologyRepository().getOntologyByDocumentIRI(IRI.create(uri)) != null)
				{
					this.handleLoadRecentRequest(descriptor);
					loaded = true;
				}
			}
			if (!loaded)
				return super.handleNewRequest();
			return true;
		}
//		boolean handleNewSuccess = false;		
//		CreateHGOntologyWizard w = new CreateHGOntologyWizard(null, this);
//		int result = w.showModalDialog();
//		if (result == Wizard.FINISH_RETURN_CODE)
//		{
//			OWLOntologyID oid = w.getOntologyID();
//			if (oid != null)
//			{
//				HGOwlModelManagerImpl mm = (HGOwlModelManagerImpl) getOWLModelManager();
//				// check if already exists
//				// we are catching specific exceptions here instead of checking
//				// the cases before the
//				// call to mm.createNewOntology.
//				try
//				{
//					mm.createNewOntology(oid, w.getLocationURI());
//					// addToRecent(URI.create(prop.getProperty("hibernate.connection.url")));
//					addRecent(w.getLocationURI());
//					handleNewSuccess = true;
//				}
//				catch (OWLOntologyAlreadyExistsException e)
//				{
//					showNewExistsOntologyIDMessage(oid, e.getDocumentIRI());
//				}
//				catch (OWLOntologyDocumentAlreadyExistsException e)
//				{
//					showNewExistsDocumentLoadedMessage(oid, e.getOntologyDocumentIRI());
//				}
//				catch (HGDBOntologyAlreadyExistsByDocumentIRIException e)
//				{
//					showNewExistsDocumentRepoMessage(oid, e.getOntologyDocumentIRI());
//				}
//			}
//		}
//		return handleNewSuccess;
	}

	/**
	 * 
	 * @param ontoId
	 * @param documentIri
	 *            docIRI or null
	 * @param ontologyIRI
	 *            ontolog
	 */
	protected void showNewExistsDocumentLoadedMessage(OWLOntologyID ontoId, IRI existingDocumentIRI)
	{
		String message = "Cannot create an ontology, because another ontology is currently loaded with the same document IRI. \n"
				+ "    The ontology ID was: Ontology IRI " + ontoId.getOntologyIRI() + "\n "
				+ "                         Version  IRI " + ontoId.getVersionIRI() + "\n " + "    Reason: Existing Document URI: "
				+ existingDocumentIRI + "\n \n" + " You should check File/loaded ontology sources...";
		JOptionPane.showMessageDialog(getWorkspace(), message, "Create Hypergraph DB Ontology - "
				+ "Already exists in loaded ontologies.", JOptionPane.ERROR_MESSAGE);
	}

	protected void showNewExistsOntologyIDMessage(OWLOntologyID ontoId, IRI existingOntologyIRI)
	{
		String message = "Cannot create an ontology, because another ontology is currently loaded with the same ontology ID. \n"
				+ "    The ontology ID was: Ontology IRI " + ontoId.getOntologyIRI() + "\n "
				+ "                         Version  IRI " + ontoId.getVersionIRI() + "\n " + "    Reason: Existing Ontology IRI: "
				+ existingOntologyIRI + "\n \n" + " You should check File/loaded ontology sources...";
		JOptionPane.showMessageDialog(getWorkspace(), message, "Create Hypergraph DB Ontology - "
				+ "Already exists in loaded ontologies.", JOptionPane.ERROR_MESSAGE);
	}

	protected void showNewExistsDocumentRepoMessage(OWLOntologyID ontoId, IRI repoDocumentIRI)
	{
		String message = "Cannot create an ontology, because another ontology with the same document IRI exists in the repository. \n"
				+ "    The ontology ID was: Ontology IRI "
				+ ontoId.getOntologyIRI()
				+ "\n "
				+ "                         Version  IRI "
				+ ontoId.getVersionIRI()
				+ "\n "
				+ "    Reason: Existing Repository Document IRI: "
				+ repoDocumentIRI
				+ "\n \n"
				+ " You should check the repository and maybe consider using Version IRI, in case you want another version of the same ontology.";
		JOptionPane.showMessageDialog(getWorkspace(), message,
				"Create Hypergraph DB Ontology - " + "Already exists in repository.", JOptionPane.ERROR_MESSAGE);
	}

	public boolean handleLoadRecentRequest(EditorKitDescriptor descriptor) throws Exception
	{
		if (DBG)
			System.out.println("HG handleLoadRecentRequest " + descriptor.getURI(URI_KEY));
		HGDBOntologyManager m = (HGDBOntologyManager) this.getModelManager().getOWLOntologyManager();
		m.getOntologyRepository().printStatistics();
		boolean retValue = super.handleLoadRecentRequest(descriptor);
		m.getOntologyRepository().printStatistics();
		return retValue;
	}

	public boolean handleLoadRequest() throws Exception
	{
		if (DBG)
			System.out.println("HG HandleLoadRequest");
		boolean success;
		HGDBOntologyManager m = (HGDBOntologyManager) this.getModelManager().getOWLOntologyManager();
		m.getOntologyRepository().printStatistics();
		Object[] possibleValues = { "Open From Hypergraph Repository", "Open From File" };
		Object selectedValue = JOptionPane.showInputDialog(getWorkspace(), "Choose open method", "Open Method Selection",
				JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
		if (selectedValue == possibleValues[0])
		{
			success = handleLoadFromRepositoryRequest();
		}
		else if (selectedValue == possibleValues[1])
		{
			success = super.handleLoadRequest();
		}
		else
		{ // null
			success = false;
		}
		m.getOntologyRepository().printStatistics();
		return success;
	}

	boolean handleLoadFromRepositoryRequest() throws Exception
	{
		if (DBG)
			System.out.println("HG HandleLoadFromRepositoryRequest");
		boolean success;
		HGDBOntologyManager m = (HGDBOntologyManager) this.getModelManager().getOWLOntologyManager();
		OntologyRepository repository = getProtegeRepository();
		if (repository == null)
			throw new IllegalStateException(
					"Cannot handle load from repository. No HGOwlOntologyRepository registered with Protege.");
		// Open Repository open Dlg
		OntologyRepositoryEntry ontologyEntry = RepositoryViewPanel.showOpenDialog(repository);
		if (ontologyEntry != null)
		{
			if (isLoadedOntologyFromLocation(ontologyEntry.getPhysicalURI()))
			{
				// Dialog: database backed ontology already loaded, no reload
				// necessary.
				success = false;
			}
			else
			{
				success = handleLoadFrom(ontologyEntry.getPhysicalURI());
			}
		}
		else
		{
			success = false;
		}
		//
		m.getOntologyRepository().printStatistics();
		return success;
	}

	public boolean handleDeleteFromRepositoryRequest() throws Exception
	{
		if (DBG)
			System.out.println("HG HandleDeleteFromRepositoryRequest");
		boolean success;
		HGDBOntologyManager m = (HGDBOntologyManager) this.getModelManager().getOWLOntologyManager();
		m.getOntologyRepository().printStatistics();
		// Find our Repository
		OntologyRepository repository = getProtegeRepository();
		if (repository == null)
			throw new IllegalStateException(
					"Cannot handle delete from repository. No HGOwlOntologyRepository registered with Protege.");
		// Open Repository delete dialog
		OntologyRepositoryEntry ontologyEntry = RepositoryViewPanel.showDeleteDialog(repository);
		if (ontologyEntry != null)
		{
			// User wants to delete ontology.
			// Do not allow deletion of any active ontology:
//			if (isLoadedOntologyFromLocation(ontologyEntry.getPhysicalURI()))
//			{
//				// Dialog, cannot delete active ontology. Remove from sources
//				// first.
//				showDeleteCannotDeleteLoaded(ontologyEntry.getPhysicalURI());
//				success = false;
//			}
//			else
//			{
				success = handleDeleteFrom(ontologyEntry);
				if (success)
				{
					this.handleNewRequest();
				}
//			}
		}
		else
		{
			success = false;
		}
		//
		if (DBG)
			m.getOntologyRepository().printStatistics();
		return success;
	}

	/**
	 * Returns the OntologyRepository implementation of our plugin (Protege
	 * Interface).
	 * 
	 * @return
	 */
	public OntologyRepository getProtegeRepository()
	{
		Collection<OntologyRepository> repositories = OntologyRepositoryManager.getManager().getOntologyRepositories();
		for (OntologyRepository cur : repositories)
		{
			if (cur instanceof HGOwlOntologyRepository)
			{
				// current implementation uses first one found
				return cur;
			}
		}
		return null;
	}

	/**
	 * @param physicalURI
	 * @return
	 */
	protected boolean isLoadedOntologyFromLocation(URI physicalURI)
	{
		Set<OWLOntology> loadedOntos = getOWLModelManager().getOntologies();// getOWLOntologyManager().getOntologies();
		for (OWLOntology onto : loadedOntos)
		{
			URI curURI = getOWLModelManager().getOntologyPhysicalURI(onto);
			if (curURI != null && curURI.equals(physicalURI))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Deletes an ontology from Hypergraph Repository.
	 * 
	 * @param physicalURI
	 * @return
	 */
	protected boolean handleDeleteFrom(OntologyRepositoryEntry ontologyEntry)
	{
		// A) Check, if the ontology is already loaded and/or managed and
		// whether it can be found in
		// the repository.
		OWLOntologyID oID = ((HGOntologyRepositoryEntry) ontologyEntry).getOntologyID();
		if (oID == null)
			throw new IllegalStateException();
		HGOwlModelManagerImpl hmm = (HGOwlModelManagerImpl) getOWLModelManager();
		PHGDBOntologyManagerImpl hom = (PHGDBOntologyManagerImpl) hmm.getOWLOntologyManager();
		OWLOntology loadedOntoToDelete = hom.getOntology(oID);
		hmm.removeOntology(loadedOntoToDelete);
		hom.removeOntology(loadedOntoToDelete);
		// will be null if not loaded.
		// getOntologyCatalogManager().Ontologies()
		// B) Provide a confirmation Dialog with as much information as
		// possible.
		boolean userConfirmsDelete = showDeleteConfirmation(oID, ontologyEntry.getPhysicalURI(), loadedOntoToDelete);
		if (userConfirmsDelete)
		{
			// C) Actual Removal:
			// C-A) if ontology managed, remove from OwlModelManager,
			// Owlontologymanager
			if (loadedOntoToDelete != null)
			{
				if (!(hom.getOntologyFormat(loadedOntoToDelete) instanceof HGDBOntologyFormat))
				{
					hmm.removeOntology(loadedOntoToDelete);
				}
				else
				{
					System.out.println("File based ontology not unloaded :" + loadedOntoToDelete.getOntologyID());
				}
			}
			// C-B) delete in repository
			boolean repoDeleteOk = hom.getOntologyRepository().deleteOntology(oID);
			showDeleteSuccessOrFailure(repoDeleteOk, oID, ontologyEntry.getPhysicalURI());
			return repoDeleteOk;
		}
		else
		{
			JOptionPane.showMessageDialog(getWorkspace(), "Delete cancelled by user.                                ",
					"Delete Hypergraph Database Backed Ontology - Cancelled", JOptionPane.WARNING_MESSAGE);
			return false;
		}
	}

	/**
	 * 
	 * @param oId
	 * @param physicalURI
	 * @param loadedOntology
	 *            or null, if not loaded.
	 * @return
	 */
	public boolean showDeleteConfirmation(OWLOntologyID oID, URI physicalURI, OWLOntology loadedOntology)
	{
		String message = "Do you really want to delete the following ontology: \n" + "    OntologyIRI: " + oID.getOntologyIRI()
				+ "\n" + "    VersionIRI : " + oID.getVersionIRI() + "\n" + "    PhysicalURI: " + physicalURI + "\n \n"
				+ "    This action cannot be undone! Make sure you have a current backup of the ontology. \n";
		int userInput = JOptionPane.showConfirmDialog(getWorkspace(), message,
				"Delete Hypergraph Database Backed Ontology - Confirm Deletion", JOptionPane.YES_NO_OPTION);
		return (userInput == JOptionPane.YES_OPTION);
	}

	public void showDeleteCannotDeleteLoaded(URI physicalURI)
	{
		String message = "Cannot delete currently loaded ontology from location : \n" + "    PhysicalURI: " + physicalURI + "\n \n"
				+ ("Please remove the ontology from loaded sources first.");
		JOptionPane.showMessageDialog(getWorkspace(), message, "Delete Hypergraph Database Backed Ontology - "
				+ "Cannot delete loaded.", JOptionPane.ERROR_MESSAGE);

	}

	public void showDeleteSuccessOrFailure(boolean success, OWLOntologyID oID, URI physicalURI)
	{
		String message = "The following ontology : \n" + "    OntologyIRI: " + oID.getOntologyIRI() + "\n" + "    VersionIRI : "
				+ oID.getVersionIRI() + "\n" + "    PhysicalURI: " + physicalURI + "\n \n"
				+ (success ? " was sucessfully deleted from the repository." : " COULD NOT BE DELETED !");
		JOptionPane.showMessageDialog(getWorkspace(), message, "Delete Hypergraph Database Backed Ontology - "
				+ (success ? "Result" : " FAILED"), success ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
	}

	public boolean handleLoadFrom(URI uri) throws Exception
	{
		HGDBOntologyManager m = (HGDBOntologyManager) this.getModelManager().getOWLOntologyManager();
		m.getOntologyRepository().printStatistics();
		boolean success = ((HGOwlModelManagerImpl) getModelManager()).loadOntologyFromPhysicalURI(uri);
		if (success)
		{
			addRecent(uri);
		}
		m.getOntologyRepository().printStatistics();

		return success;
	}

	public void handleSave() throws Exception
	{
		if (DBG)
			System.out.println("HG handleSave ");
		OWLOntology ont = getModelManager().getActiveOntology();
		if (ont instanceof HGDBOntologyImpl)
		{
			String message = "This ontology is database backed and does not need to be saved to the database again.\n"
					+ "All changes to it are instantly persisted in the Hypergraph Ontology Repository.";
			System.err.println(message);
			// logger.warn(message);
			JOptionPane
					.showMessageDialog(getWorkspace(), message, "Hypergraph Database Backed Ontology", JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			super.handleSave();
		}
	}

	public void handleSaveAs() throws Exception
	{
		OWLOntology ont = getModelManager().getActiveOntology();
		if (hghandleSaveAs(ont))
		{
			ont = getModelManager().getActiveOntology();
			SaveConfirmationPanel.showDialog(this, Collections.singleton(ont));
		}
	}

	/**
	 * Saves an ontology using HGDBOntologyFormat into the repository.
	 * 
	 * @param ont
	 */
	public boolean handleAnImportRequest(OWLOntology ont)
	{
		try
		{
			return hghandleSaveAs(ont, new HGDBOntologyFormat());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * This should only save the specified ontology
	 * 
	 * @param ont
	 *            the ontology to save
	 * @throws Exception
	 */
	protected boolean hghandleSaveAs(OWLOntology ont) throws Exception
	{
		PHGDBOntologyManagerImpl man = (PHGDBOntologyManagerImpl) getModelManager().getOWLOntologyManager();
		OWLOntologyFormat oldFormat = man.getOntologyFormat(ont);
		// IRI oldDocumentIRI = man.getOntologyDocumentIRI(ont);
		// HGDBOntologyRepository repo = man.getOntologyRepository();
		OWLOntologyFormat format = HGOntologyFormatPanel.showDialog(this, oldFormat, "Choose a format to use when saving the "
				+ getModelManager().getRendering(ont) + " ontology");
		if (format == null)
		{
			System.err.println("Please select a valid format");
			// logger.warn("Please select a valid format");
			return false;
		}
		return hghandleSaveAs(ont, format);
	}

	protected boolean hghandleSaveAs(OWLOntology ont, OWLOntologyFormat format) throws Exception
	{
		PHGDBOntologyManagerImpl man = (PHGDBOntologyManagerImpl) getModelManager().getOWLOntologyManager();
		OWLOntologyFormat oldFormat = man.getOntologyFormat(ont);
		IRI oldDocumentIRI = man.getOntologyDocumentIRI(ont);
		OntologyDatabase repo = man.getOntologyRepository();
		if (oldFormat instanceof PrefixOWLOntologyFormat && format instanceof PrefixOWLOntologyFormat)
		{
			PrefixOWLOntologyFormat oldPrefixes = (PrefixOWLOntologyFormat) oldFormat;
			for (String name : oldPrefixes.getPrefixNames())
			{
				((PrefixOWLOntologyFormat) format).setPrefix(name, oldPrefixes.getPrefix(name));
			}
		}
		if (format instanceof HGDBOntologyFormat)
		{
			// Case A1) OntololgyHGDB -> Repository Same Name: Already in
			// repository
			// Case A2) OntololgyHGDB -> Repository: Copy Ontology in Repository
			// NOT CURRENTLY SUPPORTED DIALOLG
			if (ont instanceof HGDBOntology)
			{
				String message = "This ontology is database backed and does not need to be saved to the database again.\n"
						+ "All changes to it are instantly persisted in the Hypergraph Ontology Repository.\n"
						+ "A copy operation to a different name in the repository is currently not supported.";
				System.err.println(message);
				// logger.warn(message);
				JOptionPane.showMessageDialog(getWorkspace(), message, "Hypergraph Database Backed Ontology",
						JOptionPane.ERROR_MESSAGE);

				return false;
			}
			else
			{
				// IN MEMORY ONTOLOGY
				// Case B) OntololgyImpl -> Repository: Import
				String message = "This in-memory ontology will be imported into the Hypergraph Ontology Repository.\n"
						+ "This process is estimated to take one minute per 35000 Axioms. \n" + ont.getOntologyID().toString()
						+ " has " + ont.getAxiomCount() + " Axioms. \n"
						+ "Please be patient. A Success Dialog will pop up when the process is finished.";
				System.err.println(message);
				// logger.info(message);
				JOptionPane.showMessageDialog(getWorkspace(), message, "Hypergraph Database Import",
						JOptionPane.INFORMATION_MESSAGE);
				System.out.println("IMPORTING INTO HYPERGRAPH " + ont.getOntologyID());
				// logger.info("IMPORTING INTO HYPERGRAPH " +
				// ont.getOntologyID());
				long startTime = System.currentTimeMillis();
				man.setOntologyFormat(ont, format);
				// TODO OPEN A DIALOG FOR SELECTING A documentIRI
				IRI documentIri;
				if (ont.getOntologyID().isAnonymous())
				{
					int i = 0;
					do
					{
						documentIri = IRI.create("hgdb://" + "anonymous-" + i);
						i++;
					}
					while (repo.existsOntologyByDocumentIRI(documentIri));

				}
				else
				{
					// HGDBOntologyFormat.convertToHGDBDocumentIRI(ontologyIRI)
					// IRI defaultIri =
					// ont.getOntologyID().getDefaultDocumentIRI();
					// String defaultIriStr = defaultIri.toString();
					// int schemaLength = defaultIri.getScheme().length();
					// String hgdbIRIStr = "hgdb" +
					// defaultIriStr.toString().substring(schemaLength);
					documentIri = HGDBOntologyFormat.convertToHGDBDocumentIRI(ont.getOntologyID().getDefaultDocumentIRI());
					//
					// Check if exists by ID or Document IRI
					//
					if (repo.existsOntology(ont.getOntologyID()))
					{
						JOptionPane.showMessageDialog(
								getWorkspace(),
								"An ontology with the same ID already exists in the hypergraph repository." + "\r\n "
										+ ont.getOntologyID()
										+ "\r\n If you wish to replace, delete the old one now using: HypergraphDB/Delete",
								"Hypergraph Database Import - Failed", JOptionPane.ERROR_MESSAGE);
						return false;
					}
					else if (repo.existsOntologyByDocumentIRI(documentIri))
					{
						JOptionPane.showMessageDialog(getWorkspace(),
								"An ontology with the same documentIRI already exists in the hypergraph repository." + "\r\n "
										+ documentIri
										+ "\r\n If you wish to replace, delete the old one now using: HypergraphDB/Delete",
								"Hypergraph Database Import - Failed", JOptionPane.ERROR_MESSAGE);
						return false;
					} // else continue import
				}
				System.out.println("Saving with documentIRI: " + documentIri);
				// logger.info("Saving with documentIRI: " + documentIri);
				// + ont.getOntologyID().getOntologyIRI().getFragment());

				man.setOntologyDocumentIRI(ont, documentIri);
				getModelManager().save(ont);
				int durationSecs = (int) (System.currentTimeMillis() - startTime) / 1000;
				message = "Hypergraph Database Import Success.\n" + "Saving took " + durationSecs + " seconds for "
						+ ont.getAxiomCount() + " Axioms. \n" + "You are still working with the in-memory ontology. \n "
						+ "Do you wish to use the database backed ontology now?";
				int useHGOnto = JOptionPane.showConfirmDialog(getWorkspace(), message, "Hypergraph Database Import Success",
						JOptionPane.YES_NO_OPTION);
				addRecent(documentIri.toURI());
				if (useHGOnto == JOptionPane.YES_OPTION)
				{
					// load the ontology from hypergraph and close
					getModelManager().reload(ont);
				}
				else
				{
					man.setOntologyFormat(ont, oldFormat);
					man.setOntologyDocumentIRI(ont, oldDocumentIRI);
				}
				return true;
			}
		}
		else
		{
			// FILE BASED FORMAT
			File file = getSaveAsOWLFile(ont);
			if (file != null)
			{
				// Do the following only if not database backed.
				man.setOntologyFormat(ont, format);
				man.setOntologyDocumentIRI(ont, IRI.create(file));
				try
				{
					getModelManager().save(ont);
					addRecent(file.toURI());
				}
				finally
				{
					if (ont instanceof HGDBOntology)
					{
						man.setOntologyFormat(ont, oldFormat);
						man.setOntologyDocumentIRI(ont, oldDocumentIRI);
					}
				}
				return true;
			}
			else
			{
				System.err.println("No valid file specified for the save as operation - quitting");
				// logger.warn("No valid file specified for the save as operation - quitting");
				return false;
			}
		}
	}

	private File getSaveAsOWLFile(OWLOntology ont)
	{
		UIHelper helper = new UIHelper(this);
		File file = helper.saveOWLFile("Please select a location in which to save: " + getModelManager().getRendering(ont));
		if (file != null)
		{
			int extensionIndex = file.toString().lastIndexOf('.');
			if (extensionIndex == -1)
			{
				file = new File(file.toString() + ".owl");
			}
			else if (extensionIndex != file.toString().length() - 4)
			{
				file = new File(file.toString() + ".owl");
			}
		}
		return file;
	}

	public OWLOntology getActiveOntology()
	{
		HGOwlModelManagerImpl m = (HGOwlModelManagerImpl) getOWLModelManager();
		return m.getActiveOntology();
	}

    public boolean hasModifiedDocument() 
    {
        return modifiedDocument;
    }
	@Override
	public void dispose()
	{
		//
		// start copy & paste of super.dispose()
		getModelManager().removeOntologyChangeListener(ontologyChangeListener);
//		getSearchManager().dispose();
		getWorkspace().dispose();
		try
		{
			getModelManager().dispose();
		}
		catch (Exception e)
		{
			ErrorLogPanel.showErrorDialog(e);
		}

		if (registration != null)
		{
			registration.unregister();
			registration = null;
		}
	}

}