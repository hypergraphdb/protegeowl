package gov.miamidade.hgowl.plugin.ui.versioning;

import gov.miamidade.hgowl.plugin.ui.DialogBase;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;

/**
 * VHGCommitDialog.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 31, 2012
 */
public class VHGCommitDialog extends DialogBase implements ActionListener
{

	private static final long serialVersionUID = -2849737178569671572L;
	private JTextField tfUserComment;
	private JTextField tfBranchName;
	private JButton btOK;
	private JButton btCancel;

	private boolean userCommitOK;
	private String userCommitComment;
	private String branchName;
	
	public static VHGCommitDialog showDialog(Component parent, VersionedOntology vo, boolean newBranch)
	{
		VHGCommitDialog dlg = new VHGCommitDialog(SwingUtilities.windowForComponent(parent), vo, newBranch);
		dlg.setLocationRelativeTo(parent);
		dlg.setModalityType(ModalityType.APPLICATION_MODAL);
		dlg.setVisible(true);
		dlg.setResizable(true);
		return dlg;
	}

	public VHGCommitDialog(Window w, VersionedOntology vo, boolean newBranch)
	{
		super(w);
		setTitle("Commit Versioned HGDB Ontology - Confirm Commit");

		// Create Message:
		Revision headRevision = vo.revision();		
		int pendingChanges = vo.changes().size();
		String message = 
				"Do you want to commit " + pendingChanges + " change" + ((pendingChanges > 1) ? "s" : "") + ":\n"
				+ "    Last Revision    : " + headRevision + "\n" + "    Created          : "
				+ DateFormat.getDateTimeInstance().format(new Date(headRevision.timestamp())) + "\n" + "    By               : "
				+ headRevision.user() + "\n" + "    Ontology ID : "
				+ headRevision.versioned().getPersistent() + "\n \n";
		JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
		message = "<html><pre><b>" + message + "</b></pre></html>";
		centerPanel.add(new JLabel(message), BorderLayout.NORTH);
		                                                                                                                                
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new GridLayout(2,2));
		inputPanel.add(new JLabel("Enter Commit Comment: "));
		tfUserComment = new JTextField(30);
		inputPanel.add(tfUserComment);
		inputPanel.add(new JLabel("Enter Branch Name: "));
		tfBranchName = new JTextField(30);
		inputPanel.add(tfBranchName);		
		centerPanel.add(inputPanel, BorderLayout.SOUTH);		
		
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
		setSize(400, 200);
	}

	public String getCommitComment()
	{
		return userCommitComment;
	}

	public String getBranchName()
	{
		return branchName;
	}
	
	public boolean isCommitOK()
	{
		return userCommitOK;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == btOK)
		{
			userCommitComment = tfUserComment.getText();
			userCommitOK = true;
			branchName = tfBranchName.getText();
		}
		else if (e.getSource() == btCancel)
		{
			userCommitComment = null;
			userCommitOK = false;
		}
		else
		{
			throw new IllegalArgumentException("Got an event from an unknown source!" + e);
		}
		this.setVisible(false);
		this.dispose();
	}
}