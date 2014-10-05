/**
 * 
 */
package gov.miamidade.hgowl.plugin.owl.model;

import gov.miamidade.hgowl.plugin.owlapi.apibinding.PHGDBOWLManager;
import gov.miamidade.hgowl.plugin.owlapi.apibinding.PHGDBOntologyManagerImpl;

import java.io.File;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
import org.coode.xml.XMLWriterPreferences;
import org.hypergraphdb.app.owl.HGDBIRIMapper;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.owl.model.MissingImportHandler;
import org.protege.editor.owl.model.MissingImportHandlerImpl;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLModelManagerImpl;
import org.protege.editor.owl.model.SaveErrorHandler;
import org.protege.editor.owl.model.XMLWriterPrefs;
import org.protege.editor.owl.model.cache.OWLEntityRenderingCache;
import org.protege.editor.owl.model.cache.OWLEntityRenderingCacheImpl;
import org.protege.editor.owl.model.cache.OWLObjectRenderingCache;
import org.protege.editor.owl.model.classexpression.anonymouscls.AnonymousDefinedClassManager;
import org.protege.editor.owl.model.entity.CustomOWLEntityFactory;
import org.protege.editor.owl.model.entity.OWLEntityFactory;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.model.find.OWLEntityFinderImpl;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyManager;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyManagerImpl;
import org.protege.editor.owl.model.history.HistoryManager;
import org.protege.editor.owl.model.history.HistoryManagerImpl;
import org.protege.editor.owl.model.inference.OWLReasonerManager;
import org.protege.editor.owl.model.inference.OWLReasonerManagerImpl;
import org.protege.editor.owl.model.inference.ReasonerPreferences;
import org.protege.editor.owl.model.io.AutoMappedRepositoryIRIMapper;
import org.protege.editor.owl.model.io.IOListener;
import org.protege.editor.owl.model.io.IOListenerEvent;
import org.protege.editor.owl.model.io.OntologySourcesManager;
import org.protege.editor.owl.model.io.UserResolvedIRIMapper;
import org.protege.editor.owl.model.io.WebConnectionIRIMapper;
import org.protege.editor.owl.model.library.OntologyCatalogManager;
import org.protege.editor.owl.model.selection.ontologies.ImportsClosureOntologySelectionStrategy;
import org.protege.editor.owl.model.selection.ontologies.OntologySelectionStrategy;
import org.protege.editor.owl.model.util.ListenerManager;
import org.protege.editor.owl.ui.OWLObjectComparator;
import org.protege.editor.owl.ui.OWLObjectRenderingComparator;
import org.protege.editor.owl.ui.clsdescriptioneditor.ManchesterOWLExpressionCheckerFactory;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionCheckerFactory;
import org.protege.editor.owl.ui.error.OntologyLoadErrorHandler;
import org.protege.editor.owl.ui.explanation.ExplanationManager;
import org.protege.editor.owl.ui.renderer.OWLEntityRendererImpl;
import org.protege.editor.owl.ui.renderer.OWLEntityRendererListener;
import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
import org.protege.editor.owl.ui.renderer.OWLObjectRenderer;
import org.protege.editor.owl.ui.renderer.OWLObjectRendererImpl;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.protege.editor.owl.ui.renderer.plugin.RendererPlugin;
import org.protege.owlapi.model.ProtegeOWLOntologyManager;
import org.protege.xmlcatalog.XMLCatalog;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyLoaderListener;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;

/**
 * HGModelManagerImpl. Rationale: We copied most of the code here from
 * OWLModelManagerImpl (by Matthew Horridge). Even though we wanted to make a
 * subclass to it, we found no way to set another ProtegeOWLOntologyManager as
 * manager instance variable in a subclass. The Fraunhofer OWLDB implementation
 * (v2.0) relied on a setOWLModelManager method. *
 * 
 * The <code>OWLModelManager</code> acts as a controller over a collection of
 * ontologies (ontologies that are related to each other via owl:imports) and
 * the various UI components that are used to access the ontology.
 * 
 * @author Thomas Hilpold based on Matthew Horridge.
 */
public class HGOwlModelManagerImpl extends OWLModelManagerImpl implements OWLModelManager, OWLEntityRendererListener,
		OWLOntologyChangeListener, OWLOntologyLoaderListener
{

	/**
	 * If this is set to true, all modifications to ontologies will be
	 * channelled through the AWT Event Thread. This would cause deadlocks when
	 * activities modify the ontology through our PHGDBOntologyManager and the
	 * activities result is waited for by the Swing thread.
	 */
	public static boolean USE_SWING_INVOKE_AND_WAIT = false;

	// private static final Logger logger =
	// Logger.getLogger(HGOwlModelManagerImpl.class);

	private HistoryManager historyManager;

	private OWLModelManagerEntityRenderer entityRenderer;

	private OWLObjectRenderer objectRenderer;

	private OWLOntology activeOntology;

	private OWLEntityRenderingCache owlEntityRenderingCache;

	/**
	 * P4 repeatedly asks for the same rendering multiple times in a row because
	 * of the components listening to mouse events etc so cache a small number
	 * of objects we have just rendered
	 */
	private OWLObjectRenderingCache owlObjectRenderingCache;

	private OWLEntityFinder entityFinder;

	private OWLReasonerManager owlReasonerManager;

	/**
	 * Dirty ontologies are ontologies that have been edited and not saved.
	 */
	private Set<OWLOntology> dirtyOntologies;

	/**
	 * The <code>OWLConnection</code> that we use to manage ontologies.
	 */
	private PHGDBOntologyManagerImpl manager;

	private OntologyCatalogManager ontologyLibraryManager;

	private ExplanationManager explanationManager;

	private OWLEntityFactory entityFactory;

	/**
	 * A cache for the imports closure. Originally, we just requested this each
	 * time from the OWLOntologyManager, but this proved to be expensive in
	 * terms of time.
	 */
	private Set<OWLOntology> activeOntologies;

	private Set<OntologySelectionStrategy> ontSelectionStrategies;

	private OntologySelectionStrategy activeOntologiesStrategy;

	private OWLExpressionCheckerFactory owlExpressionCheckerFactory;

	// error handlers

	private SaveErrorHandler saveErrorHandler;

	private OntologyLoadErrorHandler loadErrorHandler;

	private AutoMappedRepositoryIRIMapper autoMappedRepositoryIRIMapper;

	private UserResolvedIRIMapper userResolvedIRIMapper;

	// listeners

	private List<OWLModelManagerListener> modelManagerChangeListeners;

	private ListenerManager<OWLModelManagerListener> modelManagerListenerManager;

	private ListenerManager<OWLOntologyChangeListener> changeListenerManager;

	private List<IOListener> ioListeners;

	protected List<IOListener> getIoListeners()
	{
		if (ioListeners == null)
			ioListeners = new ArrayList<IOListener>();
		return ioListeners;
	}

	protected OWLObjectRenderingCache getOwlObjectRenderingCache()
	{
		if (owlObjectRenderingCache == null)
			owlObjectRenderingCache = new OWLObjectRenderingCache(this);
		return owlObjectRenderingCache;
	}

	protected OWLEntityRenderingCache getOwlEntityRenderingCache()
	{
		if (owlEntityRenderingCache == null)
		{
			owlEntityRenderingCache = new OWLEntityRenderingCacheImpl();
			owlEntityRenderingCache.setOWLModelManager(this);
		}
		return owlEntityRenderingCache;
	}

	protected ListenerManager<OWLOntologyChangeListener> getChangeListenerManager()
	{
		if (changeListenerManager == null)
			changeListenerManager = new ListenerManager<OWLOntologyChangeListener>();
		return changeListenerManager;
	}

	protected ListenerManager<OWLModelManagerListener> getModelManagerListenerManager()
	{
		if (modelManagerListenerManager == null)
			modelManagerListenerManager = new ListenerManager<OWLModelManagerListener>();
		return modelManagerListenerManager;
	}

	protected List<OWLModelManagerListener> getModelManagerChangeListeners()
	{
		if (modelManagerChangeListeners == null)
			modelManagerChangeListeners = new ArrayList<OWLModelManagerListener>();
		return modelManagerChangeListeners;
	}

	// public HGOwlModelManagerImpl() {
	// super();
	// //old manager
	// //hilpold check if that or even more cleanup is necessary
	// manager = super.getOWLOntologyManager();
	// manager.removeOntologyChangeListener(this);
	// manager.removeOntologyLoaderListener(this);
	// //this.
	// //new manager
	// manager =
	// HGOWLManager.createOWLOntologyManager(OWLDataFactoryImpl.getInstance());
	// //HGManager.createOWLOntologyManager(OWLDataFactoryImpl.getInstance());
	// manager.addOntologyChangeListener(this);
	// manager.addOntologyLoaderListener(this);
	//
	// super.setOWL
	// }

	public HGOwlModelManagerImpl()
	{
		// super();

		//
		// This is the reason why we had to copy the whole sourcecode from
		// OWLModelManagerImpl:
		// we wanted to use a subclass of ProtegeOWLManager that relies on our
		// Hypergraph
		// implementation of the OWL API.
		//
		// manager = ProtegeOWLManager.createOWLOntologyManager();
		manager = getOWLOntologyManager(); // PHGDBOWLManager.createOWLOntologyManager();

		System.out.println("Created HGHGHGH MANAGER: " + manager);
		manager.setUseWriteSafety(true);
		manager.setUseSwingThread(USE_SWING_INVOKE_AND_WAIT);
		manager.setSilentMissingImportsHandling(true);
		manager.addOntologyChangeListener(this);
		manager.addOntologyLoaderListener(this);

		// URI mappers for loading - added in reverse order
		autoMappedRepositoryIRIMapper = new AutoMappedRepositoryIRIMapper(this);
		userResolvedIRIMapper = new UserResolvedIRIMapper(new MissingImportHandlerImpl());
		manager.clearIRIMappers();
		HGDBOntologyRepository repository = manager.getOntologyRepository();
		manager.addIRIMapper(userResolvedIRIMapper);
		manager.addIRIMapper(new WebConnectionIRIMapper());
		manager.addIRIMapper(autoMappedRepositoryIRIMapper);
		// HGDBIRIMapper will be the first to resolve
		manager.addIRIMapper(new HGDBIRIMapper(repository));

		dirtyOntologies = new HashSet<OWLOntology>();
		ontSelectionStrategies = new HashSet<OntologySelectionStrategy>();

		objectRenderer = new OWLObjectRendererImpl(this);

		owlExpressionCheckerFactory = new ManchesterOWLExpressionCheckerFactory(this);

		// needs to be initialized
		activeOntologiesStrategy = new ImportsClosureOntologySelectionStrategy(this);

		// force the renderer to be created
		// to prevent double cache rebuild once ontologies loaded
		getOWLEntityRenderer();

		XMLWriterPreferences.getInstance().setUseNamespaceEntities(XMLWriterPrefs.getInstance().isUseEntities());

		// put(AnonymousDefinedClassManager.ID, new
		// AnonymousDefinedClassManager(this));

		put(OntologySourcesManager.ID, new OntologySourcesManager(this));
	}

	public void dispose()
	{
		super.dispose();

		OntologySourcesManager sourcesMngr = get(OntologySourcesManager.ID);
		removeIOListener(sourcesMngr);

		try
		{
			// Empty caches
			getOwlEntityRenderingCache().dispose();
			getOwlObjectRenderingCache().dispose();

			if (entityRenderer != null)
			{
				entityRenderer.dispose();
			}

			owlReasonerManager.dispose();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err); // logger.err(e.getMessage() + "\n",
											// e);
		}

		// Name and shame plugins that do not (or can't be bothered to) clean up
		// their listeners!
		// modelManagerListenerManager.dumpWarningForAllListeners(logger,
		// Level.ERROR,
		// "(Listeners should be removed in the plugin dispose method!)");
		//
		// changeListenerManager.dumpWarningForAllListeners(logger, Level.ERROR,
		// "(Listeners should be removed in the plugin dispose method!)");
	}

	public boolean isDirty()
	{
		return !dirtyOntologies.isEmpty();
	}

	public PHGDBOntologyManagerImpl getOWLOntologyManager()
	{
		if (manager == null)
			manager = PHGDBOWLManager.createOWLOntologyManager();
		return manager;
	}

	public OntologyCatalogManager getOntologyCatalogManager()
	{
		if (ontologyLibraryManager == null)
		{
			ontologyLibraryManager = new OntologyCatalogManager();
		}
		return ontologyLibraryManager;
	}

	public OWLHierarchyManager getOWLHierarchyManager()
	{
		OWLHierarchyManager hm = get(OWLHierarchyManager.ID);
		if (hm == null)
		{
			hm = new OWLHierarchyManagerImpl(this);
			put(OWLHierarchyManager.ID, hm);
		}
		return hm;
	}

	public ExplanationManager getExplanationManager()
	{
		return explanationManager;
	}

	public void setExplanationManager(ExplanationManager explanationManager)
	{
		this.explanationManager = explanationManager;
	}

	// /////////////////////////////////////////////////////////////////////////////////////
	//
	// Loading
	//
	// /////////////////////////////////////////////////////////////////////////////////////

	/**
	 * A convenience method that loads an ontology from a file The location of
	 * the file is specified by the URI argument.
	 */
	public boolean loadOntologyFromPhysicalURI(URI uri)
	{
		if (uri.getScheme() != null && uri.getScheme().equals("file"))
		{
			// Load the URIs of other ontologies that are contained in the same
			// folder.
			addRootFolder(new File(uri).getParentFile());
		}
		OWLOntology ontology = null;
		try
		{
			ontology = manager.loadOntologyFromOntologyDocument(IRI.create(uri));
			setActiveOntology(ontology);
			fireEvent(EventType.ONTOLOGY_LOADED);
			OWLOntologyID id = ontology.getOntologyID();
			if (!id.isAnonymous())
			{
				manager.addIRIMapper(new SimpleIRIMapper(id.getDefaultDocumentIRI(), IRI.create(uri)));
			}
		}
		catch (OWLOntologyCreationException ooce)
		{
			System.out.println("LOAD FROM PHYSICAL:" + ooce); // will be handled
																// by the
																// loadErrorHandler,
																// so ignore
		}
		return ontology != null;
	}

	public void startedLoadingOntology(LoadingStartedEvent event)
	{
		System.out.println("loading " + event.getOntologyID() + " from " + event.getDocumentIRI());
		// logger.info("loading " + event.getOntologyID() + " from " +
		// event.getDocumentIRI());
		fireBeforeLoadEvent(event.getOntologyID(), event.getDocumentIRI().toURI());
	}

	public void finishedLoadingOntology(LoadingFinishedEvent event)
	{
		if (!event.isSuccessful())
		{
			Exception e = event.getException();
			if (loadErrorHandler != null)
			{
				try
				{
					loadErrorHandler.handleErrorLoadingOntology(event.getOntologyID(), event.getDocumentIRI().toURI(), e);
				}
				catch (Throwable e1)
				{
					// if, for any reason, the loadErrorHandler cannot report
					// the error
					ErrorLogPanel.showErrorDialog(e1);
				}
			}
		}
		fireAfterLoadEvent(event.getOntologyID(), event.getDocumentIRI().toURI());
	}

	public XMLCatalog addRootFolder(File dir)
	{
		return ontologyLibraryManager.addFolder(dir);
	}

	private void fireBeforeLoadEvent(OWLOntologyID ontologyID, URI physicalURI)
	{
		for (IOListener listener : new ArrayList<IOListener>(ioListeners))
		{
			try
			{
				listener.beforeLoad(new IOListenerEvent(ontologyID, physicalURI));
			}
			catch (Throwable e)
			{
				ProtegeApplication.getErrorLog().logError(e);
			}
		}
	}

	private void fireAfterLoadEvent(OWLOntologyID ontologyID, URI physicalURI)
	{
		for (IOListener listener : new ArrayList<IOListener>(ioListeners))
		{
			try
			{
				listener.afterLoad(new IOListenerEvent(ontologyID, physicalURI));
			}
			catch (Throwable e)
			{
				ProtegeApplication.getErrorLog().logError(e);
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Ontology URI to Physical URI mapping
	//
	// //////////////////////////////////////////////////////////////////////////////////////

	public URI getOntologyPhysicalURI(OWLOntology ontology)
	{
		return manager.getOntologyDocumentIRI(ontology).toURI();
	}

	public void setPhysicalURI(OWLOntology ontology, URI physicalURI)
	{
		manager.setOntologyDocumentIRI(ontology, IRI.create(physicalURI));
	}

	public OWLOntology createNewOntology(OWLOntologyID ontologyID, URI physicalURI) throws OWLOntologyCreationException
	{
		manager.addIRIMapper(new SimpleIRIMapper(ontologyID.getDefaultDocumentIRI(), IRI.create(physicalURI)));
		OWLOntology ont = manager.createOntology(ontologyID);
		// 2011.12.08 hilpold keep new HGDB ontologies clean.
		if (!(ont instanceof HGDBOntology))
		{
			dirtyOntologies.add(ont);
		}
		else
		{
			// Do not add HGDBOntologys to dirtyset. They will be backed by
			// Hypergraph.
		}
		setActiveOntology(ont);
		if (physicalURI != null)
		{
			try
			{
				File containingDirectory = new File(physicalURI).getParentFile();
				if (containingDirectory.exists())
				{
					getOntologyCatalogManager().addFolder(containingDirectory);
				}
			}
			catch (IllegalArgumentException iae)
			{
				System.out.println("Cannot generate ontology catalog for ontology at " + physicalURI);
				// logger.info("Cannot generate ontology catalog for ontology at "
				// + physicalURI);
			}
		}
		fireEvent(EventType.ONTOLOGY_CREATED);
		return ont;
	}

	public OWLOntology reload(OWLOntology ont) throws OWLOntologyCreationException
	{
		if (ont instanceof HGDBOntology)
		{
			// 2011.12.08 A reload of a database backed ontology shall be
			// transparently skipped.
			// GUI dialogs should not allow this to the user.
			// 2012.03.26 We want to refresh everything here as this is the only
			// public method to allow us to do it
			// after a pull from remote for example.
			rebuildActiveOntologiesCache();
			refreshRenderer();
			fireEvent(EventType.ONTOLOGY_RELOADED);
			return ont;
		}
		IRI ontologyDocumentIRI = IRI.create(getOntologyPhysicalURI(ont));
		manager.removeOntology(ont);
		boolean wasTheActiveOntology = false;
		if (ont.equals(activeOntology))
		{
			wasTheActiveOntology = true;
			activeOntology = null;
		}
		dirtyOntologies.remove(ont);
		try
		{
			ont = manager.loadOntologyFromOntologyDocument(ontologyDocumentIRI);
		}
		catch (Throwable t)
		{
			((OWLOntologyManagerImpl) manager).ontologyCreated(ont); // put it
																		// back
																		// - a
																		// hack
																		// but
																		// it
																		// works
			manager.setOntologyDocumentIRI(ont, ontologyDocumentIRI);
			throw (t instanceof OWLOntologyCreationException) ? (OWLOntologyCreationException) t
					: new OWLOntologyCreationException(t);
		}
		if (wasTheActiveOntology)
		{
			activeOntology = ont;
		}
		rebuildActiveOntologiesCache();
		refreshRenderer();
		fireEvent(EventType.ONTOLOGY_RELOADED);
		return ont;
	}

	public boolean removeOntology(OWLOntology ont)
	{
		if (manager.contains(ont.getOntologyID()) && manager.getOntologies().size() > 1)
		{

			boolean resetActiveOntologyRequired = ont.equals(activeOntology);
			activeOntologies.remove(ont);
			dirtyOntologies.remove(ont);
			manager.removeOntology(ont);

			if (resetActiveOntologyRequired)
			{
				OWLOntology newActiveOnt = null;
				if (!activeOntologies.isEmpty())
				{
					newActiveOnt = activeOntologies.iterator().next();
				}
				if (newActiveOnt == null && !manager.getOntologies().isEmpty())
				{
					newActiveOnt = manager.getOntologies().iterator().next();
				}

				setActiveOntology(newActiveOnt, true);
			}
			else
			{
				setActiveOntology(activeOntology, true);
			}
			return true;
		}
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Saving
	//
	// /////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Save all of the ontologies that are editable and that have been modified.
	 */
	public void save() throws OWLOntologyStorageException
	{
		for (OWLOntology ont : new HashSet<OWLOntology>(dirtyOntologies))
		{
			// 2011.12.07 HGDB never dirty //hilpold 2011.09.27 do not save hgdb
			// backed ontologies.
			// if (!(ont instanceof HGDBOntology)) {
			save(ont);
			// } else {
			// hilpold still simulate effect of successful save.
			// dirtyOntologies.remove(ont);
			// }
		}
	}

	public void save(OWLOntology ont) throws OWLOntologyStorageException
	{
		final URI physicalURI = manager.getOntologyDocumentIRI(ont).toURI();

		try
		{
			fireBeforeSaveEvent(ont.getOntologyID(), physicalURI);

			try
			{
				// hilpold why not EXPORT??
				if (!physicalURI.getScheme().equals("file") && !physicalURI.getScheme().equals("hgdb"))
				{
					throw new ProtocolException("Cannot save file to remote location: " + physicalURI);
				}
				// TODO if (!physicalURI.getScheme().equals("hgdb")){
				// throw new
				// ProtocolException("Cannot save ontology into Hypergraph: " +
				// physicalURI);
				// }

				OWLOntologyFormat format = manager.getOntologyFormat(ont);
				/*
				 * Using the addMissingTypes call here for RDF/XML files can
				 * result in OWL Full output and can also result in data
				 * corruption.
				 * 
				 * See http://protegewiki.stanford.edu/wiki/
				 * OWL2RDFParserDeclarationRequirement
				 */
				manager.saveOntology(ont, format, IRI.create(physicalURI));

				manager.setOntologyDocumentIRI(ont, IRI.create(physicalURI));
			}
			catch (IOException e)
			{
				throw new OWLOntologyStorageException("Error while saving ontology " + ont.getOntologyID() + " to " + physicalURI,
						e);
			}

			System.out.println("Saved " + getRendering(ont) + " to " + physicalURI);
			// logger.info("Saved " + getRendering(ont) + " to " + physicalURI);

			dirtyOntologies.remove(ont);

			fireEvent(EventType.ONTOLOGY_SAVED);
			fireAfterSaveEvent(ont.getOntologyID(), physicalURI);
		}
		catch (OWLOntologyStorageException e)
		{
			if (saveErrorHandler != null)
			{
				try
				{
					saveErrorHandler.handleErrorSavingOntology(ont, physicalURI, e);
				}
				catch (Exception e1)
				{
					throw new OWLOntologyStorageException(e1);
				}
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * @deprecated - this method would require user interaction - use
	 *             <code>OWLEditorKit.saveAs()</code> instead
	 * @throws OWLOntologyStorageException
	 *             if a problem occurs during the save
	 */
	@Deprecated
	public void saveAs() throws OWLOntologyStorageException
	{
		save();
	}

	private void fireBeforeSaveEvent(OWLOntologyID ontologyID, URI physicalURI)
	{
		for (IOListener listener : new ArrayList<IOListener>(ioListeners))
		{
			try
			{
				listener.beforeSave(new IOListenerEvent(ontologyID, physicalURI));
			}
			catch (Throwable e)
			{
				ProtegeApplication.getErrorLog().logError(e);
			}
		}
	}

	private void fireAfterSaveEvent(OWLOntologyID ontologyID, URI physicalURI)
	{
		for (IOListener listener : new ArrayList<IOListener>(ioListeners))
		{
			try
			{
				listener.afterSave(new IOListenerEvent(ontologyID, physicalURI));
			}
			catch (Throwable e)
			{
				ProtegeApplication.getErrorLog().logError(e);
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Ontology Management
	//
	// /////////////////////////////////////////////////////////////////////////////////////

	public Set<OWLOntology> getOntologies()
	{
		return manager.getOntologies();
	}

	public Set<OWLOntology> getDirtyOntologies()
	{
		return new HashSet<OWLOntology>(dirtyOntologies);
	}

	/**
	 * Forces the system to believe that an ontology has been modified.
	 * 
	 * @param ontology
	 *            The ontology to be made dirty.
	 */
	public void setDirty(OWLOntology ontology)
	{
		if (!(ontology instanceof HGDBOntology))
		{
			dirtyOntologies.add(ontology);
		}
		else
		{
			System.out.println("Tried to set a HGDBOntology dirty. Ignored.");
		}
	}

	public OWLOntology getActiveOntology()
	{
		return activeOntology;
	}

	public OWLDataFactory getOWLDataFactory()
	{
		return manager.getOWLDataFactory();
	}

	public Set<OWLOntology> getActiveOntologies()
	{
		if (activeOntologies == null)
			activeOntologies = new HashSet<OWLOntology>();
		return activeOntologies;
	}

	public boolean isActiveOntologyMutable()
	{
		return isMutable(getActiveOntology());
	}

	public boolean isMutable(OWLOntology ontology)
	{
		// Assume all ontologies are editable - even ones
		// that have been loaded from non-editable locations e.g.
		// the web. The reason for this is that feedback from users
		// has indicated that it is a pain when an ontology isn't editable
		// just because it has been downloaded from a web because
		// they can't experiment with adding or removing axioms.
		return true;
	}

	public void setActiveOntology(OWLOntology activeOntology)
	{
		setActiveOntology(activeOntology, false);
	}

	public void setActiveOntologiesStrategy(OntologySelectionStrategy strategy)
	{
		activeOntologiesStrategy = strategy;
		setActiveOntology(getActiveOntology(), true);
		fireEvent(EventType.ONTOLOGY_VISIBILITY_CHANGED);
	}

	public OntologySelectionStrategy getActiveOntologiesStrategy()
	{
		return activeOntologiesStrategy;
	}

	public Set<OntologySelectionStrategy> getActiveOntologiesStrategies()
	{
		return ontSelectionStrategies;
	}

	/**
	 * Sets the active ontology (and hence the set of active ontologies).
	 * 
	 * @param activeOntology
	 *            The ontology to be set as the active ontology.
	 * @param force
	 *            By default, if the specified ontology is already the active
	 *            ontology then no changes will take place. This flag can be
	 *            used to force the active ontology to be reset and listeners
	 *            notified of a change in the state of the active ontology.
	 */
	private void setActiveOntology(OWLOntology activeOntology, boolean force)
	{
		if (!force)
		{
			if (this.activeOntology != null)
			{
				if (this.activeOntology.equals(activeOntology))
				{
					return;
				}
			}
		}
		this.activeOntology = activeOntology;
		System.out.println("Setting active ontology to " + activeOntology.getOntologyID());
		// logger.info("Setting active ontology to " +
		// activeOntology.getOntologyID());
		rebuildActiveOntologiesCache();
		// Rebuild entity indices
		entityRenderer.ontologiesChanged();
		rebuildEntityIndices();
		// Inform our listeners
		fireEvent(EventType.ACTIVE_ONTOLOGY_CHANGED);
		System.out.println("... active ontology changed");
		// logger.info("... active ontology changed");
	}

	public void registerOntologySelectionStrategy(OntologySelectionStrategy strategy)
	{
		ontSelectionStrategies.add(strategy);
	}

	private void rebuildActiveOntologiesCache()
	{
		activeOntologies.clear();
		activeOntologies.addAll(activeOntologiesStrategy.getOntologies());
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Ontology history management
	//
	// /////////////////////////////////////////////////////////////////////////////////////////////////

	public void applyChange(OWLOntologyChange change)
	{
		try
		{
			AnonymousDefinedClassManager adcManager = get(AnonymousDefinedClassManager.ID);
			if (adcManager != null)
			{
				change = adcManager.getChangeRewriter().rewriteChange(change);
			}
			manager.applyChange(change);
		}
		catch (OWLOntologyChangeException e)
		{
			throw new OWLRuntimeException(e);
		}
	}

	public void applyChanges(List<? extends OWLOntologyChange> changes)
	{
		try
		{
			AnonymousDefinedClassManager adcManager = get(AnonymousDefinedClassManager.ID);
			if (adcManager != null)
			{
				changes = adcManager.getChangeRewriter().rewriteChanges(changes);
			}
			manager.applyChanges(changes);
		}
		catch (OWLOntologyChangeException e)
		{
			throw new OWLRuntimeException(e);
		}
	}

	public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
	{
		getHistoryManager().logChanges(changes);
		boolean refreshActiveOntology = false;
		for (OWLOntologyChange change : changes)
		{
			// hilpold 2011.12.08 Keep HGDB backed ontologies out of dirty set.
			// OWLOntology changedOnto = change.getOntology();
			if (!(change.getOntology() instanceof HGDBOntology))
			{
				dirtyOntologies.add(change.getOntology());
			} // else {
				// do not add HGDBOntologys to dirty set on change.
			// }
			if (change.isImportChange())
			{
				refreshActiveOntology = true;
			}
		}
		if (refreshActiveOntology)
		{
			setActiveOntology(getActiveOntology(), true);
		}
	}

	public boolean isChangedEntity(OWLEntity entity)
	{
		return false;
	}

	public HistoryManager getHistoryManager()
	{
		if (historyManager == null)
		{
			historyManager = new HistoryManagerImpl(this);
		}
		return historyManager;
	}

	// ArrayList mylisteners = new ArrayList();

	public void addOntologyChangeListener(OWLOntologyChangeListener listener)
	{
		getOWLOntologyManager().addOntologyChangeListener(listener);
		// if (listener == null)
		// return;
		// if (manager == null)
		// mylisteners.add(listener);
		// else
		// manager.addOntologyChangeListener(listener);
		getChangeListenerManager().recordListenerAdded(listener);
	}

	public void removeOntologyChangeListener(OWLOntologyChangeListener listener)
	{
		manager.removeOntologyChangeListener(listener);
		getChangeListenerManager().recordListenerRemoved(listener);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////

	public void addListener(OWLModelManagerListener listener)
	{
		getModelManagerChangeListeners().add(listener);
		getModelManagerListenerManager().recordListenerAdded(listener);
	}

	public void removeListener(OWLModelManagerListener listener)
	{
		getModelManagerChangeListeners().remove(listener);
		getModelManagerListenerManager().recordListenerRemoved(listener);
	}

	public void fireEvent(EventType type)
	{
		OWLModelManagerChangeEvent event = new OWLModelManagerChangeEvent(this, type);
		for (OWLModelManagerListener listener : new ArrayList<OWLModelManagerListener>(getModelManagerChangeListeners()))
		{
			try
			{
				listener.handleChange(event);
			}
			catch (Throwable e)
			{
				System.err
						.println("Exception thrown by listener: " + listener.getClass().getName() + ".  Detatching bad listener!");
				// logger.warn("Exception thrown by listener: " +
				// listener.getClass().getName() +
				// ".  Detatching bad listener!");
				ProtegeApplication.getErrorLog().logError(e);
				getModelManagerChangeListeners().remove(listener);
			}
		}
	}

	public void addIOListener(IOListener listener)
	{
		getIoListeners().add(listener);
	}

	public void removeIOListener(IOListener listener)
	{
		getIoListeners().remove(listener);
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	//
	// Entity rendering classes
	//
	// ////////////////////////////////////////////////////////////////////////////////////

	public OWLModelManagerEntityRenderer getOWLEntityRenderer()
	{
		if (entityRenderer == null)
		{
			try
			{
				OWLRendererPreferences preferences = OWLRendererPreferences.getInstance();
				RendererPlugin plugin = preferences.getRendererPlugin();
				entityRenderer = plugin.newInstance();
				loadRenderer();
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace(System.err);
				// logger.error(e.getMessage());
			}
			catch (InstantiationException e)
			{
				e.printStackTrace(System.err);
				// logger.error(e.getMessage());
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace(System.err);
				// logger.error(e.getMessage());
			}
			if (entityRenderer == null)
			{
				entityRenderer = new OWLEntityRendererImpl();
				loadRenderer();
			}
		}
		return entityRenderer;
	}

	public String getRendering(OWLObject object)
	{
		// Look for a cached version of the rendering first!
		if (object instanceof OWLEntity)
		{
			AnonymousDefinedClassManager adcManager = get(AnonymousDefinedClassManager.ID);
			if (adcManager != null && object instanceof OWLClass && adcManager.isAnonymous((OWLClass) object))
			{
				return getOwlObjectRenderingCache().getRendering(adcManager.getExpression((OWLClass) object),
						getOWLObjectRenderer());
			}
			else
			{
				getOWLEntityRenderer();
				String rendering = getOwlEntityRenderingCache().getRendering((OWLEntity) object);
				if (rendering != null)
				{
					return rendering;
				}
				else
				{
					return getOWLEntityRenderer().render((OWLEntity) object);
				}
			}
		}

		return getOwlObjectRenderingCache().getRendering(object, getOWLObjectRenderer());
	}

	public void renderingChanged(OWLEntity entity, final OWLModelManagerEntityRenderer renderer)
	{
		getOwlEntityRenderingCache().updateRendering(entity);
		getOwlObjectRenderingCache().clear();
		// We should inform listeners
		for (OWLModelManagerListener listener : new ArrayList<OWLModelManagerListener>(getModelManagerChangeListeners()))
		{
			listener.handleChange(new OWLModelManagerChangeEvent(this, EventType.ENTITY_RENDERING_CHANGED));
		}
	}

	public void refreshRenderer()
	{
		if (entityRenderer != null)
		{
			entityRenderer.removeListener(this);
			try
			{
				entityRenderer.dispose();
			}
			catch (Exception e)
			{
				ProtegeApplication.getErrorLog().logError(e);
			}
		}
		entityRenderer = null;
		getOWLEntityRenderer();
		loadRenderer();
	}

	private void loadRenderer()
	{
		entityRenderer.addListener(this);
		entityRenderer.setup(this);
		entityRenderer.initialise();
		rebuildEntityIndices();
		fireEvent(EventType.ENTITY_RENDERER_CHANGED);
	}

	public void setOWLEntityRenderer(OWLModelManagerEntityRenderer renderer)
	{
		refreshRenderer();
	}

	public OWLObjectRenderer getOWLObjectRenderer()
	{
		return objectRenderer;
	}

	public OWLExpressionCheckerFactory getOWLExpressionCheckerFactory()
	{
		return owlExpressionCheckerFactory;
	}

	public OWLEntityFactory getOWLEntityFactory()
	{
		if (entityFactory == null)
		{
			entityFactory = new CustomOWLEntityFactory(this);
		}
		return entityFactory;
	}

	public void setOWLEntityFactory(OWLEntityFactory owlEntityFactory)
	{
		this.entityFactory = owlEntityFactory;
	}

	public OWLEntityFinder getOWLEntityFinder()
	{
		if (entityFinder == null)
		{
			entityFinder = new OWLEntityFinderImpl(this, getOwlEntityRenderingCache());
		}
		return entityFinder;
	}

	public Comparator<OWLObject> getOWLObjectComparator()
	{
		OWLObjectComparator<OWLObject> comparator = get(OWL_OBJECT_COMPARATOR_KEY);
		if (comparator == null)
		{
			comparator = new OWLObjectRenderingComparator<OWLObject>(this);
			put(OWL_OBJECT_COMPARATOR_KEY, comparator);
		}
		return comparator;
	}

	private void rebuildEntityIndices()
	{
		System.out.println("Rebuilding entity indices...");
		// logger.info("Rebuilding entity indices...");
		long t0 = System.currentTimeMillis();
		getOwlEntityRenderingCache().rebuild();
		getOwlObjectRenderingCache().clear();
		System.out.println("... rebuilt in " + (System.currentTimeMillis() - t0) + " ms");
		// logger.info("... rebuilt in " + (System.currentTimeMillis() - t0) +
		// " ms");
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	//
	// Reasoner
	//
	// ////////////////////////////////////////////////////////////////////////////////////

	public OWLReasonerManager getOWLReasonerManager()
	{
		if (owlReasonerManager == null)
		{
			owlReasonerManager = new OWLReasonerManagerImpl(this);
		}
		return owlReasonerManager;
	}

	public OWLReasoner getReasoner()
	{
		return getOWLReasonerManager().getCurrentReasoner();
	}

	public ReasonerPreferences getReasonerPreferences()
	{
		return getOWLReasonerManager().getReasonerPreferences();
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	//
	// Error handling
	//

	// ////////////////////////////////////////////////////////////////////////////////////

	public void setMissingImportHandler(MissingImportHandler missingImportHandler)
	{
		userResolvedIRIMapper.setMissingImportHandler(missingImportHandler);
	}

	public void setSaveErrorHandler(SaveErrorHandler handler)
	{
		this.saveErrorHandler = handler;
	}

	public void setLoadErrorHandler(OntologyLoadErrorHandler handler)
	{
		this.loadErrorHandler = handler;
	}
}