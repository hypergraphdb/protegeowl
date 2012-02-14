package gov.miamidade.hgowl.plugin.owl.model;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

public class HGDBIRIMapper implements OWLOntologyIRIMapper {

    public IRI getDocumentIRI(IRI ontologyIRI) {
    	if (ontologyIRI.getScheme().equalsIgnoreCase("hgdb")) 
    		return IRI.create(ontologyIRI.toString());
    	else 
    		return null;
    }
}