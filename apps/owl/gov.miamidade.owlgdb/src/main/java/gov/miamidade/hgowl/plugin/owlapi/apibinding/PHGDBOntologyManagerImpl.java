package gov.miamidade.hgowl.plugin.owlapi.apibinding;

import java.io.File;

import org.hypergraphdb.app.owl.HGDBApplication;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyFormat;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.core.AddPrefixChange;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.core.PrefixChange;
import org.hypergraphdb.app.owl.core.PrefixChangeListener;
import org.hypergraphdb.app.owl.core.RemovePrefixChange;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.protege.owlapi.model.ProtegeOWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;

/**
 * Manages multiple Ontologies. Based on OWL-API OWLOntologyManagerImpl and ProtegeOWLOntologyManager.
 * For use with Protege 4.1.
 * 
 * The implementation of this class must match HGDBOntologyManagerImpl.
 * 
 * PHGDBOntologyManagerImpl for protege integration.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 */
public class PHGDBOntologyManagerImpl extends ProtegeOWLOntologyManager implements HGDBOntologyManager, PrefixChangeListener {

	HGDBOntologyRepository ontologyRepository;
	
	public PHGDBOntologyManagerImpl(OWLDataFactoryHGDB dataFactory) {
		super(dataFactory);						
		//Make sure there is an application, a graph, et.c.
		if (HGDBApplication.DISTRIBUTED) {
			ontologyRepository = VDHGDBOntologyRepository.getInstance();
			((VDHGDBOntologyRepository)ontologyRepository).setOntologyManager(this);
			this.addOntologyChangeListener(((VDHGDBOntologyRepository)ontologyRepository));
			
		} else if (HGDBApplication.VERSIONING) {
			ontologyRepository = VHGDBOntologyRepository.getInstance();
			this.addOntologyChangeListener(((VHGDBOntologyRepository)ontologyRepository));
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
	
	@Override
    public void setOntologyFormat(OWLOntology ontology, OWLOntologyFormat format) {
    	if (format instanceof HGDBOntologyFormat) {
    		((HGDBOntologyFormat)format).addPrefixChangeListener(this);
    	}
    	super.setOntologyFormat(ontology, format);
    }

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.core.PrefixChangeListener#prefixChanged(org.hypergraphdb.app.owl.core.PrefixChange)
	 */
	@Override
	public void prefixChanged(PrefixChange e) {
		//We get notfied here if anybody modifies prefixes.
		//We will have to look up the ontology and call for a change to be applied
		HGDBOntology ho = getOntologyForFormat(e.getFormat());
		if (e instanceof AddPrefixChange) {
			applyChange(new AddPrefixChange(ho, e.getPrefixName(), e.getPrefix()));
		} else if (e instanceof RemovePrefixChange) {
			applyChange(new RemovePrefixChange(ho, e.getPrefixName(), e.getPrefix()));
		} else {
			throw new IllegalArgumentException("Unknown prefixchange: " + e + "" + e.getClass());
		}
	}
	
	public HGDBOntology getOntologyForFormat(HGDBOntologyFormat f) {
		for (OWLOntology o : getOntologies()) {
			if (o instanceof HGDBOntology) {
				OWLOntologyFormat candidate = getOntologyFormat(o);
				if (f == candidate) {
					return (HGDBOntology)o;
				}
			}
		}
		return null;
	}
}