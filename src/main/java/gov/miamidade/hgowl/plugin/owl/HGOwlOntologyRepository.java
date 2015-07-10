package gov.miamidade.hgowl.plugin.owl;

import gov.miamidade.hgowl.plugin.owl.model.HGOntologyRepositoryEntry;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.protege.editor.core.OntologyRepository;
import org.protege.editor.core.OntologyRepositoryEntry;
import org.protege.editor.core.editorkit.EditorKit;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;

/**
 * A protege repository implementation backed by a Hypergraph Ontology Repository instance.
 * This enabled Protege to show Hypergraph ontologies at startup. 
 * 
 * @author Thomas Hilpold
 */
public class HGOwlOntologyRepository implements OntologyRepository {

	public static final String VERSION_URI = "Version URI";
	
	public static final String PHYSICAL_URI = "Physical URI";

	public static final String AXIOM_COUNT = "Nr Axioms";
	
	public static final String ATOM_COUNT = "Nr HGDB Atoms";
	
	public static final List<Object> METADATA_KEYS = Arrays.asList(new Object[]{VERSION_URI, PHYSICAL_URI, AXIOM_COUNT, ATOM_COUNT});
	
    private String repositoryName;

    private HGDBOntologyRepository dbRepository;

    private List<HGDBRepositoryEntry> entries;

    private OWLOntologyIRIMapper iriMapper;

    public HGOwlOntologyRepository(String repositoryName, HGDBOntologyRepository dbRepository) {
        this.repositoryName = repositoryName;
        this.dbRepository = dbRepository;
        entries = new ArrayList<HGDBRepositoryEntry>();
        iriMapper = new RepositoryIRIMapper();
    }

    public void initialise() throws Exception {
    }

    public String getName() {
        return repositoryName;
    }

    public String getLocation() {
        return dbRepository.getHyperGraph().getLocation();
    }

    public void refresh() {
        fillRepository();
    }

    public Collection<OntologyRepositoryEntry> getEntries() {
        List<OntologyRepositoryEntry> ret = new ArrayList<OntologyRepositoryEntry>();
        ret.addAll(entries);
        return ret;
    }

    public List<Object> getMetaDataKeys() {
        return METADATA_KEYS;
    }

    public void dispose() throws Exception {
    	dbRepository.dispose();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Implementation details

    private void fillRepository() {
        entries.clear();
        List<HGDBOntology>  l = dbRepository.getOntologies();
        for(HGDBOntology o : l) {
            entries.add(new HGDBRepositoryEntry(o));
        }
    }

    public class HGDBRepositoryEntry implements HGOntologyRepositoryEntry {

        private String shortName;

        private URI ontologyURI;

        private URI ontologyVersionURI = null;

        private URI physicalURI;

        private OWLOntologyID ontologyID;

        private int nrOfAxioms;

		private int nrOfAtoms;
		
		private HGDBOntology ontology;

        public HGDBRepositoryEntry(HGDBOntology o) {
        	ontologyID = o.getOntologyID();
            this.shortName = ontologyID.getOntologyIRI().getFragment();
            this.ontologyURI = URI.create(ontologyID.getOntologyIRI().toString());
            if (ontologyID.getVersionIRI() != null) {
            	this.ontologyVersionURI = URI.create(o.getOntologyID().getVersionIRI().toString());
            }
            OntologyIRIShortFormProvider sfp = new OntologyIRIShortFormProvider();
            shortName = sfp.getShortForm(o);
            physicalURI = URI.create(o.getDocumentIRI().toString());
            nrOfAxioms = o.getAxiomCount();
            nrOfAtoms = (int)o.count(hg.all());
            ontology = o;
            
        }

        public String getOntologyShortName() {
            return shortName;
        }

        public URI getOntologyURI() {
            return ontologyURI;
        }

        /**
		 * @return the ontologyVersionURI or null if none.
		 */
		public URI getOntologyVersionURI() {
			return ontologyVersionURI;
		}

		public URI getPhysicalURI() {
            return physicalURI;
        }

        public OWLOntologyID getOntologyID() {
            return ontologyID;
        }

		public int getNrOfAxioms() {
			return nrOfAxioms;
		}

		/**
		 * @return the nrOfAtoms that are in the ontology subgraph.
		 */
		public int getNrOfAtoms() {
			return nrOfAtoms;
		}

        public String getEditorKitId() {
            return HGOwlEditorKitFactory.ID;
        }

        public String getMetaData(Object key) {
        	if (key.equals(VERSION_URI)) {
        		return "" + getOntologyVersionURI();
        	} else if (key.equals(PHYSICAL_URI)) {
        		return "" + getPhysicalURI();
        	} else if (key.equals(AXIOM_COUNT)) {
        		return "" + getNrOfAxioms();
        	} else if (key.equals(ATOM_COUNT)) {
        		return "" + getNrOfAtoms();
        	} else {
        		throw new IllegalArgumentException("Key unknown.");
        	}
        }

        public void configureEditorKit(EditorKit editorKit) {
            ((HGOwlEditorKit) editorKit).getOWLModelManager().getOWLOntologyManager().addIRIMapper(iriMapper);
        }

        public void restoreEditorKit(EditorKit editorKit) {
            ((HGOwlEditorKit) editorKit).getOWLModelManager().getOWLOntologyManager().removeIRIMapper(iriMapper);

        }

		/* (non-Javadoc)
		 * @see gov.miamidade.hgowl.plugin.owl.model.HGOntologyRepositoryEntry#getOntology()
		 */
		@Override
		public HGDBOntology getOntology() {
			return ontology;
		}
    }

    private class RepositoryIRIMapper implements OWLOntologyIRIMapper {

        public IRI getDocumentIRI(IRI iri) {
            for(HGDBRepositoryEntry entry : entries) {
                if(entry.getOntologyURI().equals(iri.toURI())) {
                    return IRI.create(entry.getPhysicalURI());
                }
            }
            return null;
        }
    }
}