package gov.miamidade.hgowl.plugin.owl;

import gov.miamidade.hgowl.plugin.owl.model.HGOntologyRepositoryEntry;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.VDRenderer;

import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.ClientCentralizedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.DistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.PeerDistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.ServerCentralizedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
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
public class VDHGOwlOntologyRepository implements OntologyRepository {

	public static boolean SHOW_EXTENDED_COLUMNS = false;
	
	public static final String VERSION_URI = "Version URI";
	
	public static final String PHYSICAL_URI = "Physical URI";

	public static final String HEAD_REVISION = "Last Revision";

	//public static final String LAST_COMMIT = "Last Commit";

	public static final String DISTRIBUTED_INFO = "Team Info";

	public static final List<Object> METADATA_KEYS = Arrays.asList(new Object[]{ DISTRIBUTED_INFO, HEAD_REVISION, });

	public static final List<Object> METADATA_KEYS_EXT = Arrays.asList(new Object[]{ VERSION_URI, PHYSICAL_URI, HEAD_REVISION, DISTRIBUTED_INFO});
	
    private String repositoryName;

    private VDHGDBOntologyRepository dbRepository;

    private List<VDHGDBRepositoryEntry> entries;

    private OWLOntologyIRIMapper iriMapper;

    public VDHGOwlOntologyRepository(String repositoryName, VDHGDBOntologyRepository dbRepository) {
        this.repositoryName = repositoryName;
        this.dbRepository = dbRepository;
        entries = new ArrayList<VDHGDBRepositoryEntry>();
        iriMapper = new RepositoryIRIMapper();
    }

    public void initialise() throws Exception {
    }

    public String getName() {
        return repositoryName;
    }

    public String getLocation() {
        return HGDBOntologyRepository.getHypergraphDBLocation();
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
    	return SHOW_EXTENDED_COLUMNS? METADATA_KEYS_EXT : METADATA_KEYS;
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
            entries.add(new VDHGDBRepositoryEntry(o));
        }
    }

    public class VDHGDBRepositoryEntry implements HGOntologyRepositoryEntry {

        private String shortName;

        private URI ontologyURI;

        private String ontologyVersionURI;

        private URI physicalURI;

        private OWLOntologyID ontologyID;

        private String headRevision;

		private String distributedInfo;
		
		private HGDBOntology ontology;

        public VDHGDBRepositoryEntry(HGDBOntology o) {
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
            	DistributedOntology dOnto = dbRepository.getDistributedOntology(o);
            	if (dOnto != null) {
            		if (dOnto instanceof ClientCentralizedOntology) {
            			ClientCentralizedOntology cco = (ClientCentralizedOntology) dOnto;
            			distributedInfo = "Client (" + VDRenderer.render(cco.getServerPeer()) + ")";
            		} else if (dOnto instanceof ServerCentralizedOntology) {
            			distributedInfo = "Server";
            		} else if (dOnto instanceof PeerDistributedOntology) { 
            			distributedInfo = "Peer";
            		} else {
            			throw new IllegalStateException("Distributed Ontology Type not recognized " + dOnto);
            		}
            	} else {
                	distributedInfo = "Local Versioning";
            	}
            	headRevision = VDRenderer.render(vo.getHeadRevision(), !vo.getWorkingSetChanges().isEmpty());
            } else {
            	distributedInfo = "Not Versioned";
            	headRevision = "";
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
		 * @return the distributedInfo
		 */
		public String getUncommittedChanges() {
			return distributedInfo;
		}

		public String getEditorKitId() {
            return HGOwlEditorKitFactory.ID;
        }

        public String getMetaData(Object key) {
        	if (key.equals(VERSION_URI)) {
        		return "" + getOntologyVersionURI();
        	} else if (key.equals(PHYSICAL_URI)) {
        		return "" + getPhysicalURI();
        	} else if (key.equals(HEAD_REVISION)) {
        		return getHeadRevision();
        	} else if (key.equals(DISTRIBUTED_INFO)) {
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
            for(VDHGDBRepositoryEntry entry : entries) {
                if(entry.getOntologyURI().equals(iri.toURI())) {
                    return IRI.create(entry.getPhysicalURI());
                }
            }
            return null;
        }
    }
}