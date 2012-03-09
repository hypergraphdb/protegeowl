package gov.miamidade.hgowl.plugin.ui.versioning;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * VHGCommitDialog.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 31, 2012
 */
public class VHGCommitDialog extends JDialog implements ActionListener {
	
	private static final long serialVersionUID = -2849737178569671572L;
	private JTextField tfUserComment;
	private JButton btOK;
	private JButton btCancel;

	private boolean userCommitOK;
	private String userCommitComment;
	
	public static VHGCommitDialog showDialog(Component parent, VersionedOntology vo, OWLOntology onto) {
		VHGCommitDialog dlg = new VHGCommitDialog(SwingUtilities.windowForComponent(parent), vo, onto);
		dlg.setModalityType(ModalityType.APPLICATION_MODAL);
		dlg.setVisible(true);
		dlg.setResizable(true);
		return dlg;
	}
	
	
	public VHGCommitDialog(Window w, VersionedOntology vo, OWLOntology onto) {
		super(w);
		setTitle("Commit Versioned HGDB Ontology - Confirm Commit");

		//Create Message:
		int nrOfRevisions = vo.getNrOfRevisions();
		Revision headRevision = vo.getHeadRevision();
		int pendingChanges = vo.getWorkingSetChanges().size();
		String message = "Do you want to commit " + pendingChanges + " change" 
			+ ((pendingChanges > 1)? "s" : "") + ":\n" 
	      	+ "    Last Revision    : " + headRevision.getRevision() + "\n"
	      	+ "    Created          : " + DateFormat.getDateTimeInstance().format(headRevision.getTimeStamp()) + "\n"
	      	+ "    By               : " + headRevision.getUser() + "\n" 
			+ "    Total Revisions  : " + nrOfRevisions + "\n" 
			+ "    Ontology ID : " + headRevision.getOntologyUUID() + "\n \n"; 
		JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
		message = "<html><pre><b>" + message + "</b></pre></html>";
		centerPanel.add(new JLabel(message), BorderLayout.NORTH);
		JPanel enterPanel = new JPanel();
		enterPanel.add(new JLabel("Enter Commit Comment: "), BorderLayout.WEST);
		tfUserComment = new JTextField(30);
		enterPanel.add(tfUserComment, BorderLayout.EAST);
		centerPanel.add(enterPanel, BorderLayout.SOUTH);
		btOK = new JButton("Commit");
		btOK.addActionListener(this);
		btCancel = new JButton("Cancel");	
		btCancel.addActionListener(this);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(btOK);
		buttonPanel.add(btCancel);
		//
		this.add(buttonPanel, BorderLayout.SOUTH);
		this.add(centerPanel, BorderLayout.CENTER);
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setSize(400,200);
//		int userInput = JOptionPane.showConfirmDialog(getWorkspace()				,
//	                                  message,
//	                                  ,
//	                                  JOptionPane.YES_NO_OPTION);
//	    
//	    return (userInput == JOptionPane.YES_OPTION);
	}

	public String getCommitComment() {
		return userCommitComment;
	}
	
	public boolean isCommitOK() {
		return userCommitOK;
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btOK) {
			userCommitComment = tfUserComment.getText();
			userCommitOK = true;			
		} else if (e.getSource() == btCancel){
			userCommitComment = null;
			userCommitOK = false;			
		} else {
			throw new IllegalArgumentException("Got an event from an unknown source!" + e);
		}
		this.setVisible(false);
		this.dispose();
	}
}
