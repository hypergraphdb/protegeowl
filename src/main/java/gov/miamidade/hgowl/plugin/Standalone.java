package gov.miamidade.hgowl.plugin;

import gov.miamidade.hgowl.plugin.ui.ImportDialog;
import gov.miamidade.hgowl.plugin.ui.versioning.RevisionDialog;
import gov.miamidade.hgowl.plugin.ui.versioning.VHGCommitDialog;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

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
	
	static void testCommitDialog(String dblocation) throws Exception
	{
		JFrame frame = new JFrame("Test Me");
		HGDBOntologyManager manager = new HGOntologyManagerFactory().getOntologyManager(dblocation);
		HGHandle ontology = ((HGDBOntology)manager.getOntologies().iterator().next()).getAtomHandle();
		VersionedOntology vo = manager.getVersionManager().versioned(ontology);
		VHGCommitDialog dlg = new VHGCommitDialog(frame, vo, false);
		dlg.setVisible(true);
	}
	
	static void testImportDialog(String dblocation) throws Exception
	{
//		HGDBOntologyManager manager = new HGOntologyManagerFactory().getOntologyManager(dblocation);
//		OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("/Users/borislav/granthika/ontology/grantha-discourse.owl"));
		JFrame frame = new JFrame("Test Me");
		
//		SpringLayout slayout = new SpringLayout();
		Box commentBox = new Box(BoxLayout.X_AXIS);
		JLabel commentLabel = new JLabel("Enter Commit Comment:");
		commentLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		commentBox.add(commentLabel);
		commentBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
		JTextArea tfUserComment = new JTextArea(10, 30);
		tfUserComment.setAlignmentY(Component.TOP_ALIGNMENT);		
		commentBox.add(tfUserComment);
		Box branchBox = new Box(BoxLayout.X_AXIS);
		JLabel branchLabel = new JLabel("     Enter Branch Name:");
		branchLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		branchBox.add(branchLabel);
		JTextField tfBranchName = new JTextField(30);
		tfBranchName.setAlignmentY(Component.TOP_ALIGNMENT);
		branchBox.add(tfBranchName);		
		branchBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
//		slayout.putConstraint(SpringLayout.NORTH, commentLabel, 0, SpringLayout.NORTH, tfUserComment);		
//		slayout.putConstraint(SpringLayout.WEST, tfUserComment, 5, SpringLayout.EAST, commentLabel);
//		slayout.putConstraint(SpringLayout.NORTH, branchLabel, 0, SpringLayout.NORTH, tfBranchName);		
//		slayout.putConstraint(SpringLayout.WEST, tfUserComment, 5, SpringLayout.EAST, commentLabel);		
//		slayout.putConstraint(SpringLayout.NORTH, branchLabel, 5, SpringLayout.SOUTH, commentLabel);
		//slayout.putConstraint(SpringLayout.NORTH, tfBranchName, 5, SpringLayout.SOUTH, tfUserComment);
		
		JPanel inputPanel = new JPanel();
		inputPanel.add(commentBox);
		inputPanel.add(branchBox);
		frame.add(inputPanel);
		frame.setSize(800,  500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		
//		ImportDialog dlg = new ImportDialog(frame, ont);
//		dlg.build().showDialog();
	}
	
	public static void main(String [] args)
	{
		try
		{
			String dblocation = HGOwlProperties.getInstance().getHgLocationFolderPath();
			trace("HG Location:" + dblocation);
			//testGraphShow(dblocation);
			//testRevisionDialog(dblocation);
			//testImportDialog(dblocation);
			testCommitDialog(dblocation);
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
		}
	}
}
