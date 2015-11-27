package gov.miamidade.hgowl.plugin;

import gov.miamidade.hgowl.plugin.ui.versioning.RevisionDialog;
import javax.swing.JFrame;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.model.IRI;

public class Standalone
{
	static void trace(String s)
	{
		System.out.println(s);
	}
	
//	static void testGraphShow(String dblocation)
//	{
//		TU.ctx.set(TU.newCtx(dblocation));		
//		VersionedOntology vo = RevisionGraphPanel.createTestData(Singles.versionManager().graph());
//		System.out.println(vo.revisions());
//		versioning.printRevisionGraph(vo);
//	}

	static void testRevisionDialog(String dblocation)
	{
//		TU.ctx.set(TU.newCtx(dblocation));		
		JFrame frame = new JFrame("Test Me");
		IRI iri = IRI.create("hgdb://testrevisiongraphpanel.io/670797e5-d2a8-44d8-a07c-47d0cf229d94");
		HGDBOntology ontology = Singles.vdRepo().getOntologyByDocumentIRI(iri); 
		trace("ontology: " + ontology);
		VersionedOntology versioned = Singles.versionManager().versioned(ontology.getAtomHandle());
//		for (Revision rev : versioned.revisions())
//		{
//			System.out.println("Rev " + rev);
//		}
				
//		VersionedOntology versioned = RevisionGraphPanel.createTestData(Singles.versionManager().graph());

		frame.setSize(500,  900);
		frame.setVisible(true);
//		VHGCommitDialog.showDialog(frame, versioned);
		RevisionDialog dlg = new RevisionDialog("Hypergraph Versioning - History of " + versioned, 
								 frame, 
								 versioned, 
								 null).build().showDialog();
		// Just the panel
		
//		JPanel panel = new JPanel();
//		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
//		frame.setContentPane(panel);
//		RevisionGraphPanel revpanel = new RevisionGraphPanel(versioned);
//		revpanel.build();
//		panel.add(revpanel);
//		revpanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		
//		Singles.vdRepo().dispose();		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		frame.dispose();		
	}
	
	public static void main(String [] args)
	{
		String dblocation = HGOwlProperties.getInstance().getHgLocationFolderPath();
		trace("HG Location:" + dblocation);
		//testGraphShow(dblocation);
		testRevisionDialog(dblocation);
	}
}
