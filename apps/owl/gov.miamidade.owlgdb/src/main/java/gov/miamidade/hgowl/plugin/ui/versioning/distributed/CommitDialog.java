package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import gov.miamidade.hgowl.plugin.ui.versioning.ChangeSetPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.DistributedOntology;
import org.hypergraphdb.peer.HGPeerIdentity;

/**
 * CommitDialog for remote commit of Distributed C/S ontology.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 31, 2012
 */
public class CommitDialog extends JDialog implements ActionListener {
	
	private static final long serialVersionUID = -2849737178569671572L;
	private JTextField tfUserComment;
	private ChangeSetPanel changeSetPanel;
	private JButton btOK;
	private JButton btCancel;

	private boolean userCommitOK;
	private String userCommitComment;
	
	public static CommitDialog showDialog(String title, Component parent, DistributedOntology dOnto, HGPeerIdentity server, String userId) {
		CommitDialog dlg = new CommitDialog(title, SwingUtilities.windowForComponent(parent), dOnto, server, userId);
		dlg.setLocationRelativeTo(parent);
		dlg.setModalityType(ModalityType.APPLICATION_MODAL);
		dlg.setResizable(true);
		dlg.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		dlg.setVisible(true);
		return dlg;
	}
	
	
	public CommitDialog(String title, Window w, DistributedOntology dOnto, HGPeerIdentity server, String userId) {
		super(w);
		setTitle(title);
		VersionedOntology vo = dOnto.getVersionedOntology();
		ChangeSet workingSetChanges  = vo.getWorkingSetChanges();
		String message = "<html> <h2> Commit Ontology Changes to Server " + userId + "</h2> "
		    +"<table width='100%' border='0'>"
		    +"<tr><td align='right'><b>Ontology:</b></td><td>"+ VDRenderer.render(dOnto) + "</td></tr>"
		    +"<tr><td align='right'><b>Server:</b></td><td>"+VDRenderer.render(server) + " " + userId + "</td></tr>"
		    +"<tr><td align='right'><b>Head:</b></td><td>"+VDRenderer.render(dOnto.getVersionedOntology().getHeadRevision()) + "(local)"+ "</td></tr>"
		    +"</table>";
		JPanel northPanel = new JPanel(new BorderLayout(5, 5));
		northPanel.add(new JLabel(message), BorderLayout.NORTH);
		JPanel enterPanel = new JPanel();
		enterPanel.add(new JLabel("Enter Commit Comment: "), BorderLayout.WEST);
		tfUserComment = new JTextField(80);
		enterPanel.add(tfUserComment, BorderLayout.EAST);
		northPanel.add(enterPanel, BorderLayout.CENTER);
		btOK = new JButton("Commit");
		btOK.addActionListener(this);
		btCancel = new JButton("Cancel");	
		btCancel.addActionListener(this);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(btOK);
		buttonPanel.add(btCancel);
		//
		changeSetPanel = new ChangeSetPanel();
		changeSetPanel.setChangeSet(workingSetChanges, vo.getHyperGraph(), vo.getWorkingSetData());
		//renderChangeset((DefaultListModel)changeSetList.getModel(), workingSetChanges, vo.getHyperGraph(), vo.getWorkingSetData());
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.add(northPanel, BorderLayout.NORTH);
		this.add(changeSetPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		setSize(900,450);
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
