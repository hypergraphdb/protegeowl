package gov.miamidade.hgowl.plugin;

import gov.miamidade.hgowl.plugin.ui.versioning.VHGCommitDialog;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.model.IRI;

public class Standalone
{
	static void trace(String s)
	{
		System.out.println(s);
	}
	
	public static void main(String [] args)
	{
		JFrame frame = new JFrame("Test Me");
		frame.setSize(500,  500);
		trace("HG Location:" + HGOwlProperties.getInstance().getHgLocationFolderPath());
		IRI iri = IRI.create("hgdb://www.semanticweb.org/borislav/ontologies/2015/6/untitled-ontology-224");
		HGDBOntology ontology = Singles.vdRepo().getOntologyByDocumentIRI(iri); 
		trace("ontology: " + ontology);
		VersionedOntology versioned = Singles.vdRepo().getVersionManager().versioned(ontology.getAtomHandle());
		frame.setVisible(true);
		VHGCommitDialog.showDialog(frame, versioned);
		Singles.vdRepo().dispose();		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.dispose();
	}
}
