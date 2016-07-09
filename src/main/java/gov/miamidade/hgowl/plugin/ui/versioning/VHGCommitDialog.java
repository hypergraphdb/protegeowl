package gov.miamidade.hgowl.plugin.ui.versioning;

import gov.miamidade.hgowl.plugin.ui.DialogBase;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

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
	private JTextArea tfUserComment;
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
		
		((JPanel)this.getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
		
		setTitle("Commit Versioned HGDB Ontology - Confirm Commit");

		// Create Message:
		Revision headRevision = vo.revision();		
		int pendingChanges = vo.changes().size();
		String message = 
				"<p><b>Do you want to commit " + pendingChanges + " change" + ((pendingChanges > 1) ? "s" : "") + "?</b></p>" +
			    "<table>" +
				"<tr><td>Last Revision</td><td>" + headRevision + "</td></tr>" +
				"<tr><td>Created</td><td>" + DateFormat.getDateTimeInstance().format(new Date(headRevision.timestamp())) + "</td></tr>" +
				"<tr><td>By User:</td><td>" + headRevision.user() + "</td></tr>" +
				"<tr><td>Ontology Handle:</td><td>" + headRevision.versioned().getPersistent() + "</td></tr>" +
				"</table>";
//				+ "    Last Revision    : " + headRevision + "\n" + "    Created          : "
//				+ DateFormat.getDateTimeInstance().format(new Date(headRevision.timestamp())) + "\n" + "    By               : "
//				+ headRevision.user() + "\n" + "    Ontology ID : "
//				+ headRevision.versioned().getPersistent() + "\n \n";
		JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
		//message = "<html><pre><b>" + message + "</b></pre></html>";
		message= "<html>" + message + "</html>";
		centerPanel.add(new JLabel(message), BorderLayout.NORTH);
		                                 
		Box commentBox = new Box(BoxLayout.X_AXIS);
		JLabel commentLabel = new JLabel("Enter Commit Comment:");
		commentLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		commentBox.add(commentLabel);
		commentBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
		JTextArea tfUserComment = new JTextArea(10, 30);
		tfUserComment.setAlignmentY(Component.TOP_ALIGNMENT);		
		commentBox.add(tfUserComment);
		Box branchBox = new Box(BoxLayout.X_AXIS);
		JLabel branchLabel = new JLabel("       Enter Branch Name:");
		branchLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		branchBox.add(branchLabel);
		JTextField tfBranchName = new JTextField(30);
		tfBranchName.setAlignmentY(Component.TOP_ALIGNMENT);
		branchBox.add(tfBranchName);		
		branchBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
		inputPanel.add(commentBox);
		inputPanel.add(branchBox);
		
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
		setSize(600, 430);
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