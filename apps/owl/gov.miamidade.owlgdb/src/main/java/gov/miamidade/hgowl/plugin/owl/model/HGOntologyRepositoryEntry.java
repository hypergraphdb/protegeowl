package gov.miamidade.hgowl.plugin.owl.model;

import org.hypergraphdb.app.owl.HGDBOntology;
import org.protege.editor.core.OntologyRepositoryEntry;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * HGOntologyRepositoryEntry.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 31, 2012
 */
public interface HGOntologyRepositoryEntry extends OntologyRepositoryEntry {
	
	public OWLOntologyID getOntologyID();
	
	public HGDBOntology getOntology();
	
}
