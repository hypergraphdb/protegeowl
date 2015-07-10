package gov.miamidade.hgowl.plugin.ui.versioning;

import gov.miamidade.hgowl.plugin.ui.repository.VOntologyViewPanel;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.VDRenderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.hypergraphdb.app.owl.newver.ChangeSet;
import org.hypergraphdb.app.owl.newver.Revision;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.protege.editor.owl.OWLEditorKit;

/**
 * VHGCommitDialog shows all revisions of a given versioned ontology, allows the
 * user to select a revisions and shows all changes associated with the
 * selection.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Apr 06, 2012
 */
public class VHGRevertDialog extends JDialog implements ActionListener, ListSelectionListener
{

	public static final int MAX_CHANGES_SHOWN = 250;
	public static final String EMPTY_LIST_TEXT = "Please select a revision above.";

	private static final long serialVersionUID = -284973463639671572L;
	private Revision selectedRevision;

	private VersionedOntology versionedOntology;
	private ChangeSetTablePanel changeSetPanel;
	private VOntologyViewPanel ontologyView;
	private JButton btOK;
	private JButton btCancel;

	private boolean userConfirmedRevert;

	// TODO...it's not a linear list anymore, maybe do a topological ordering
	java.util.List<Revision> revisions = new ArrayList<Revision>(); //versionedOntology.getRevisions();
	java.util.List<ChangeSet<VersionedOntology>> changeSets = new ArrayList<ChangeSet<VersionedOntology>>();// versionedOntology.getChangeSets();
	
	public static VHGRevertDialog showDialog(String title, Component parent, VersionedOntology vo, OWLEditorKit kit)
	{
		VHGRevertDialog dlg = new VHGRevertDialog(title, SwingUtilities.windowForComponent(parent), vo, kit);
		dlg.setLocationRelativeTo(parent);
		dlg.setModalityType(ModalityType.APPLICATION_MODAL);
		dlg.setResizable(true);
		dlg.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		dlg.setVisible(true);
		return dlg;
	}

	public VHGRevertDialog(String title, Window w, VersionedOntology vo, OWLEditorKit kit)
	{
		super(w);
		w.addWindowListener(new WindowAdapter()
		{
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent
			 * )
			 */
			@Override
			public void windowClosing(WindowEvent e)
			{
				closeDialog();
			}
		});
		versionedOntology = vo;
		setLayout(new BorderLayout());
		String message = "<html> <h2> Revert Ontology to older revision </h2> " + "<table width='100%' border='0'>"
				+ "<tr><td align='right'><b>Ontology:</b></td><td>" + vo + "</td></tr>"
				+ "<tr><td align='right'><b>Server:</b></td><td>" + "local" + "</td></tr>"
				+ "<tr><td align='right'><b>Head:</b></td><td>" + vo.revision() + "(local)"
				+ "</td></tr>" + "</table>";
		JPanel northPanel = new JPanel(new BorderLayout(5, 5));
		northPanel.add(new JLabel(message), BorderLayout.NORTH);
		JSplitPane centerPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		// TOP SHOWS REVISIONS
		ontologyView = new VOntologyViewPanel(vo);
		ontologyView.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ontologyView.getTable().getSelectionModel().addListSelectionListener(this);
		centerPanel.setLeftComponent(ontologyView);

		// BOTTOM SHOWS SELECTED CHANGESET
		changeSetPanel = new ChangeSetTablePanel(vo.ontology(), vo.graph(), kit);
		// centerPanel.add(new JScrollPane(changeSetList));
		centerPanel.setRightComponent(changeSetPanel);
		centerPanel.setDividerLocation(150);

		btOK = new JButton("Revert ontology");
		btOK.addActionListener(this);
		btCancel = new JButton("Cancel");
		btCancel.addActionListener(this);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(btOK);
		buttonPanel.add(btCancel);
		//
		add(northPanel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.SOUTH);
		add(centerPanel, BorderLayout.CENTER);
		// this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setSize(1000, 600);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == btOK)
		{
			userConfirmedRevert = true;
		}
		else if (e.getSource() == btCancel)
		{
		}
		else
		{
			throw new IllegalArgumentException("Got an event from an unknown source!" + e);
		}
		closeDialog();
	}

	public void closeDialog()
	{
		this.setVisible(false);
		this.dispose();
		versionedOntology = null;
	}

	/**
	 * 
	 * @param selectedRev
	 */
	public void updateChangeSetList(int selectedRevisionIndex)
	{
		String firstItemString = null;
		ChangeSet<VersionedOntology> selectedCS = null;
		SortedSet<Integer> selectedCSConflicts = null;

		if (selectedRevisionIndex != -1)
		{
			if (selectedRevisionIndex == revisions.size())
			{
				// Pending changes in local workingset
				firstItemString = "<html>Showing <b>uncommitted</b> Changes that were made by <b>you</b> </html>";
				selectedCS = changeSets.get(selectedRevisionIndex - 1);
				selectedCSConflicts = new TreeSet<Integer>();//versionedOntology.getWorkingSetConflicts();
				// renderChangeset(lm, selectedCS);
			}
			else if (selectedRevisionIndex > 0)
			{
				Revision selectedRev = revisions.get(selectedRevisionIndex);
				selectedCS = changeSets.get(selectedRevisionIndex - 1);
				firstItemString = "<html>Showing Changes that were commited by <b>" + selectedRev.getUser() + "</b> at "
						+ VDRenderer.render(new java.util.Date(selectedRev.getTimestamp())) + 
						" for revision " + selectedRev + "</html>";
				firstItemString += "<br> with comment <b>" + selectedRev.getComment() + "</b>";
				// renderChangeset(lm, selectedCS);
			}
			else if (selectedRevisionIndex == 0)
			{
				Revision selectedRev = revisions.get(selectedRevisionIndex);
				firstItemString = ("<html> Initial revision that was created by <b>" + selectedRev.getUser() + "</b> at "
						+ VDRenderer.render(new java.util.Date(selectedRev.getTimestamp())) + "</html>");
				// lm.addElement("<html>with comment <b>" +
				// selectedRev.getRevisionComment() + "</b> </html>");
				// lm.addElement("<html>No changes to show.</html>");
			}
			else
			{
				System.err.println("Cannot render revision: " + selectedRevisionIndex);
				return;
			}
		}
		else
		{
			// Empty list, nothing selected
			firstItemString = EMPTY_LIST_TEXT;
		}
		changeSetPanel.setChangeSet(selectedCS, selectedCSConflicts, firstItemString);
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getValueIsAdjusting())
			return;
		else
		{
			// Row 0 shows pending changes selects latest changeset.
			int selectedRevisionIndex = revisions.size() - ontologyView.getTable().getSelectedRow();
			// System.out.println("SELECTED: " + selectedRevisionIndex);
			if (selectedRevisionIndex >= 0 && selectedRevisionIndex <= revisions.size())
			{
				if (selectedRevisionIndex != revisions.size())
				{
					selectedRevision = revisions.get(selectedRevisionIndex);
				}
				else
				{
					// Pending Changes selected
					selectedRevision = null;
				}
				updateChangeSetList(selectedRevisionIndex);
			}
			else
			{
				selectedRevision = null;
				updateChangeSetList(-1);
			}
		}
	}

	/**
	 * @return the revertToRevision
	 */
	public Revision getSelectedRevision()
	{
		return selectedRevision;
	}

	public boolean isUserConfirmedRevert()
	{
		return userConfirmedRevert;
	}
}
