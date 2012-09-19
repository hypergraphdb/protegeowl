package gov.miamidade.hgowl.plugin.owlapi.apibinding;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBApplication;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGDBOntologyManagerImpl;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByDocumentIRIException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyIDException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyUUIDException;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.distributed.activity.ActivityUtils;
import org.protege.owlapi.model.ProtegeOWLOntologyManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * Manages multiple Ontologies. Based on OWL-API OWLOntologyManagerImpl and ProtegeOWLOntologyManager.
 * For use with Protege 4.1.
 * 
 * PHGDBOntologyManagerImpl for protege integration.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 */
public class PHGDBOntologyManagerImpl extends ProtegeOWLOntologyManager implements HGDBOntologyManager {

	HGDBOntologyRepository ontologyRepository;
	
	public PHGDBOntologyManagerImpl(OWLDataFactoryHGDB dataFactory) {
		super(dataFactory);						
		//Make sure there is an application, a graph, et.c.
		if (HGDBApplication.DISTRIBUTED) {
			ontologyRepository = VDHGDBOntologyRepository.getInstance();
			((VDHGDBOntologyRepository)ontologyRepository).setOntologyManager(this);
			this.addImpendingOntologyChangeListener(((VDHGDBOntologyRepository)ontologyRepository));
			
		} else if (HGDBApplication.VERSIONING) {
			ontologyRepository = VHGDBOntologyRepository.getInstance();
			this.addImpendingOntologyChangeListener(((VHGDBOntologyRepository)ontologyRepository));
		} else {
			ontologyRepository = HGDBOntologyRepository.getInstance();
		}
		dataFactory.setHyperGraph(ontologyRepository.getHyperGraph());
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyManager#getOntologyRepository()
	 */
	@Override
	public HGDBOntologyRepository getOntologyRepository() {
		return ontologyRepository;
	}
	
	/**
	 * Imports a full versionedOntology from a VOWLXMLFormat file.
	 * Throws one of: 
	 * OWLOntologyChangeException, UnloadableImportException, HGDBOntologyAlreadyExistsByDocumentIRIException, HGDBOntologyAlreadyExistsByOntologyIDException, HGDBOntologyAlreadyExistsByOntologyUUIDException, OWLParserException, IOException 
	 * wrapped as cause of a RuntimeException.
	 */
	public VersionedOntology importVersionedOntology(File vowlxmlFile) throws RuntimeException {
		throw new IllegalStateException("Not yet implemented.");
	}

	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyManager#hasInMemoryOntology()
	 */
	@Override
	public boolean hasInMemoryOntology() {
		for (OWLOntology onto : getOntologies()) {
			if (!(onto instanceof HGDBOntology)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyManager#getCurrentTaskSize()
	 */
	@Override
	public int getCurrentTaskSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyManager#getCurrentTaskProgress()
	 */
	@Override
	public int getCurrentTaskProgress() {
		// TODO Auto-generated method stub
		return 0;
	}
}