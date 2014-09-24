package gov.miamidade.hgowl.plugin.owl;

import gov.miamidade.hgowl.plugin.owl.model.HGOntologyRepositoryEntry;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.VDRenderer;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
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
public class VHGOwlOntologyRepository implements OntologyRepository {

	public static final String VERSION_URI = "Version URI";
	
	public static final String PHYSICAL_URI = "Physical URI";

	public static final String HEAD_REVISION = "Head Revision";

	public static final String LAST_COMMIT = "Last Commit";

	public static final String UNCOMMITTED_CHANGES = "Uncommitted Changes";

	public static final List<Object> METADATA_KEYS = Arrays.asList(new Object[]{VERSION_URI, PHYSICAL_URI, HEAD_REVISION, LAST_COMMIT, UNCOMMITTED_CHANGES});
	
    private String repositoryName;

    private VHGDBOntologyRepository dbRepository;

    private List<VHGDBRepositoryEntry> entries;

    private OWLOntologyIRIMapper iriMapper;

    public VHGOwlOntologyRepository(String repositoryName, VHGDBOntologyRepository dbRepository) {
        this.repositoryName = repositoryName;
        this.dbRepository = dbRepository;
        entries = new ArrayList<VHGDBRepositoryEntry>();
        iriMapper = new RepositoryIRIMapper();
    }

    public void initialise() throws Exception {
    }

    public String getName() {
        return repositoryName;
    }

    public String getLocation() {
        return this.dbRepository.getHyperGraph().getLocation();
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
            entries.add(new VHGDBRepositoryEntry(o));
        }
    }

    public class VHGDBRepositoryEntry implements HGOntologyRepositoryEntry {

        private String shortName;

        private URI ontologyURI;

        private String ontologyVersionURI;

        private URI physicalURI;

        private OWLOntologyID ontologyID;

        private String headRevision;

		private String lastCommitTime;

		private String uncommittedChanges;
		
		private HGDBOntology ontology;

        public VHGDBRepositoryEntry(HGDBOntology o) {
        	ontologyID = o.getOntologyID();
            shortName = ontologyID.getOntologyIRI().getFragment();
            ontologyURI = URI.create(ontologyID.getOntologyIRI().toString());
            if (ontologyID.getVersionIRI() != null) {
            	ontologyVersionURI = o.getOntologyID().getVersionIRI().toString();
            } else {
            	ontologyVersionURI = "";
            }
            OntologyIRIShortFormProvider sfp = new OntologyIRIShortFormProvider();
            shortName = sfp.getShortForm(o);
            physicalURI = URI.create(o.getDocumentIRI().toString());
            if (dbRepository.isVersionControlled(o)) {
            	VersionedOntology vo = dbRepository.getVersionControlledOntology(o);
            	headRevision = "" + vo.getHeadRevision().getRevision();
            	lastCommitTime = VDRenderer.render(vo.getHeadRevision().getTimeStamp());
            	//Format.getDateTimeInstance().format(vo.getWorkingSetChanges().getCreatedDate());
            	uncommittedChanges = "" + vo.getWorkingSetChanges().size(); 
            } else {
            	headRevision = "Not Versioned";
            	lastCommitTime = "";
            	uncommittedChanges = "";
            }
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
		public String getOntologyVersionURI() {
			return ontologyVersionURI;
		}

		public URI getPhysicalURI() {
            return physicalURI;
        }

        public OWLOntologyID getOntologyID() {
            return ontologyID;
        }

		/**
		 * @return the headRevision
		 */
		public String getHeadRevision() {
			return headRevision;
		}

		/**
		 * @return the lastCommitTime
		 */
		public String getLastCommitTime() {
			return lastCommitTime;
		}

        /**
		 * @return the uncommittedChanges
		 */
		public String getUncommittedChanges() {
			return uncommittedChanges;
		}

		public String getEditorKitId() {
            return HGOwlEditorKitFactory.ID;
        }

        public String getMetaData(Object key) {
        	if (key.equals(VERSION_URI)) {
        		return "" + getOntologyVersionURI();
        	} else if (key.equals(PHYSICAL_URI)) {
        		return "" + getPhysicalURI();
        	} else if (key.equals(LAST_COMMIT)) {
        		return getLastCommitTime();
        	} else if (key.equals(HEAD_REVISION)) {
        		return getHeadRevision();
        	} else if (key.equals(UNCOMMITTED_CHANGES)) {
        		return getUncommittedChanges();
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
            for(VHGDBRepositoryEntry entry : entries) {
                if(entry.getOntologyURI().equals(iri.toURI())) {
                    return IRI.create(entry.getPhysicalURI());
                }
            }
            return null;
        }
    }
}