package gov.miamidade.hgowl.plugin.owl;

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
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;

/**
 * A protege repository implementation backed by a Hypergraph Ontology Repository instance.
 * This enabled Protege to show Hypergraph ontologies at startup. 
 * 
 * @author Thomas Hilpold
 */
public class HGOwlOntologyRepository implements OntologyRepository {

	public static final String PHYSICAL_URI = "Physical URI";
	
	public static final String AXIOM_COUNT = "Nr Axioms";
	
	public static final String ATOM_COUNT = "Nr HGDB Atoms";
	
	public static final List<Object> METADATA_KEYS = Arrays.asList(new Object[]{PHYSICAL_URI, AXIOM_COUNT, ATOM_COUNT});
	
    private String repositoryName;

    private HGDBOntologyRepository dbRepository;

    private List<RepositoryEntry> entries;

    private OWLOntologyIRIMapper iriMapper;

    public HGOwlOntologyRepository(String repositoryName, HGDBOntologyRepository dbRepository) {
        this.repositoryName = repositoryName;
        this.dbRepository = dbRepository;
        entries = new ArrayList<RepositoryEntry>();
        iriMapper = new RepositoryIRIMapper();
    }

    public void initialise() throws Exception {
    }

    public String getName() {
        return repositoryName;
    }

    public String getLocation() {
        return "Hypergraph Repository at " + HGDBOntologyRepository.getHypergraphDBLocation();
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
            entries.add(new RepositoryEntry(o));
        }
    }

    private class RepositoryEntry implements OntologyRepositoryEntry {

        private String shortName;

        private URI ontologyURI;

        private URI physicalURI;

        private int nrOfAxioms;

		private int nrOfAtoms;

        public RepositoryEntry(HGDBOntology o) {
        	this.shortName = o.getOntologyID().getOntologyIRI().getFragment();
            this.ontologyURI = URI.create(o.getOntologyID().getOntologyIRI().toString());
            OntologyIRIShortFormProvider sfp = new OntologyIRIShortFormProvider();
            shortName = sfp.getShortForm(o);
            physicalURI = URI.create(o.getDocumentIRI().toString());
            nrOfAxioms = o.getAxiomCount();
            nrOfAtoms = (int)o.count(hg.all());
        }

        public String getOntologyShortName() {
            return shortName;
        }

        public URI getOntologyURI() {
            return ontologyURI;
        }

        public URI getPhysicalURI() {
            return physicalURI;
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
        	if (key.equals(PHYSICAL_URI)) {
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
    }

    private class RepositoryIRIMapper implements OWLOntologyIRIMapper {

        public IRI getDocumentIRI(IRI iri) {
            for(RepositoryEntry entry : entries) {
                if(entry.getOntologyURI().equals(iri.toURI())) {
                    return IRI.create(entry.getPhysicalURI());
                }
            }
            return null;
        }
    }
}