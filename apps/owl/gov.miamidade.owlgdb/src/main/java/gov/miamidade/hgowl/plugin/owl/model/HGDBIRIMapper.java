package gov.miamidade.hgowl.plugin.owl.model;

import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
/**
 * HGDBIRIMapper will try to find the ontology by IRI in the current HGDB repository 
 * and return a DocumentIRI with a hgdb schema on success; null otherwise.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Dec 20, 2011
 */
public class HGDBIRIMapper implements OWLOntologyIRIMapper {
	
	public static boolean DBG = true;
	
	HGDBOntologyRepository ontologyRepository;
	
	public HGDBIRIMapper(HGDBOntologyRepository ontologyRepository) {
		this.ontologyRepository = ontologyRepository;
	}
	
    public IRI getDocumentIRI(IRI ontologyIRI) {
    	String iriNoScheme = ontologyIRI.toString();
    	String scheme = ontologyIRI.getScheme();
    	iriNoScheme = iriNoScheme.substring(scheme.length());
    	IRI docIRI = IRI.create("hgdb" + iriNoScheme);  
    	if (DBG) { 
    		System.out.println("HGDBIRIMapper: " + ontologyIRI + " -> " + docIRI);
    	}
    	if (ontologyRepository.existsOntologyByDocumentIRI(docIRI)) {
    		return docIRI;
    	} else {
    		return null;
    	}
    }
}