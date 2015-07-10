package gov.miamidade.hgowl.plugin.ui;

import java.awt.Frame;
import java.net.URI;

import org.hypergraphdb.app.owl.HGDBOntologyFormat;
import org.protege.editor.core.ui.wizard.Wizard;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * Wizard for Hypergraph ontology specification by a user.
 * Currently, only Ontology IRI and optional Version IRI can be specified.
 * 
 * @author Thomas Hilpold
 */
public class CreateHGOntologyWizard extends Wizard {

    private OntologyIDPanel ontologyIDPanel;

    public CreateHGOntologyWizard(Frame owner, OWLEditorKit editorKit) {
        super(owner);
        setTitle("Create Hypergraph Ontology");
        registerWizardPanel(OntologyIDPanel.ID, ontologyIDPanel = new OntologyIDPanel(editorKit));
        setCurrentPanel(OntologyIDPanel.ID);
    }

    public OWLOntologyID getOntologyID() {
        return ontologyIDPanel.getOntologyID();
    }

    public URI getLocationURI() {
    	// e.g. hgdb
    	OWLOntologyID oid = getOntologyID();
    	URI hgdbLocation;
    	if (oid != null) {
    		hgdbLocation = URI.create("hgdb://" + ontologyIDPanel.getOntologyID().getDefaultDocumentIRI().getFragment()); 
    	} else {
    		hgdbLocation = URI.create("hgdb://default");
    	}
        return hgdbLocation; 
    }

    public OWLOntologyFormat getFormat() {
       return new HGDBOntologyFormat();
    }

    public int showModalDialog() {
        int ret = super.showModalDialog();
//        if(ret == Wizard.FINISH_RETURN_CODE) {
//            physicalLocationPanel.storeRecentLocations();
//        }
        return ret;
    }

}
