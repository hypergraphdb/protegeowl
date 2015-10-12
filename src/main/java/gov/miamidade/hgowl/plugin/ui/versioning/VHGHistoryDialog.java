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
import java.util.List;
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

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.versioning;
import org.protege.editor.owl.OWLEditorKit;

/**
 * VHGCommitDialog shows all revisions of a given versioned ontology, allows the
 * user to select a revisions and shows all changes associated with the
 * selection.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Apr 06, 2012
 */
public class VHGHistoryDialog extends JDialog implements ActionListener, ListSelectionListener
{

	public static final int MAX_CHANGES_SHOWN = 250;
	public static final String EMPTY_LIST_TEXT = "Please select a revision above.";

	private static final long serialVersionUID = -284973463639671572L;
//	private VersionedOntology versionedOntology;
	private VOntologyViewPanel ontologyView;
	private ChangeSetTablePanel changeSetPanel;
	private JButton btClose;

	private VersionedOntology versionedOntology;
	
	// TODO...it's not a linear list anymore, maybe do a topological ordering
	List<Revision> revisions = null;
	
	// private DateFormat dateF = DateFormat.getDateTimeInstance();

	public static VHGHistoryDialog showDialog(String title, Component parent, VersionedOntology vo, OWLEditorKit kit)
	{
		VHGHistoryDialog dlg = new VHGHistoryDialog(SwingUtilities.windowForComponent(parent), vo, kit);
		dlg.setTitle(title);
		dlg.setLocationRelativeTo(parent);
		dlg.setModalityType(ModalityType.APPLICATION_MODAL);
		dlg.setVisible(true);
		dlg.setResizable(true);
		dlg.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		return dlg;
	}

	public VHGHistoryDialog(Window w, VersionedOntology vo, OWLEditorKit kit)
	{
		super(w);
		if (w != null)
			w.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent e)
				{
					closeDialog();
				}
			});
		this.versionedOntology = vo;
		setLayout(new BorderLayout());
		String message = "<html> <h2> Local History of Ontology </h2> " + "<table width='100%' border='0'>"
				+ "<tr><td align='right'><b>Ontology:</b></td><td>" + vo + "</td></tr>"
				+ "<tr><td align='right'><b>Server:</b></td><td>" + "local" + "</td></tr>"
				+ "<tr><td align='right'><b>Head:</b></td><td>" + vo.revision() + "(local)"
				+ "</td></tr>" + "</table>";
		JPanel northPanel = new JPanel(new BorderLayout(5, 5));
		northPanel.add(new JLabel(message), BorderLayout.NORTH);
		JSplitPane centerPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		// TOP SHOWS REVISIONS
		revisions = vo.revisions();
		ontologyView = new VOntologyViewPanel(vo, revisions);
		ontologyView.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ontologyView.getTable().getSelectionModel().addListSelectionListener(this);
		centerPanel.setLeftComponent(ontologyView);

		// BOTTOM SHOWS SELECTED CHANGESET
		changeSetPanel = new ChangeSetTablePanel(vo.ontology(), vo.graph(), kit); 
		// centerPanel.add(new JScrollPane(changeSetList));
		centerPanel.setRightComponent(changeSetPanel);
		centerPanel.setDividerLocation(150);
		btClose = new JButton("Close");
		btClose.addActionListener(this);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(btClose);
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
		closeDialog();
	}

	public void closeDialog()
	{
		this.setVisible(false);
		this.dispose();
//		versionedOntology = null;
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
		HyperGraph graph = versionedOntology.graph();
		if (selectedRevisionIndex != -1)
		{
			if (selectedRevisionIndex == revisions.size())
			{
				// Pending changes in local workingset
				firstItemString = "<html>Showing <b>uncommitted</b> Changes that were made by <b>you</b> </html>";
				selectedCS = versionedOntology.changes(); 
				// TODO...
				selectedCSConflicts = new TreeSet<Integer>();//versionedOntology.getWorkingSetConflicts();
				// renderChangeset(lm, selectedCS);
			}
			else if (selectedRevisionIndex > 0)
			{
				Revision selectedRev = revisions.get(selectedRevisionIndex);
				selectedCS = versioning.changes(graph, 
												selectedRev.getAtomHandle(), 
												revisions.get(selectedRevisionIndex-1).getAtomHandle());
				firstItemString = "<html>Showing Changes that were commited by <b>" + selectedRev.user() + "</b> at "
						+ VDRenderer.render(new java.util.Date(selectedRev.timestamp())) + 
						" for revision " + selectedRev + "</html>";
				firstItemString += "<br> with comment <b>" + selectedRev.comment() + "</b>";
				// renderChangeset(lm, selectedCS);
			}
			else if (selectedRevisionIndex == 0)
			{
				Revision selectedRev = revisions.get(selectedRevisionIndex);
				firstItemString = ("<html> Initial revision that was created by <b>" + selectedRev.user() + "</b> at "
						+ VDRenderer.render(new java.util.Date(selectedRev.timestamp())) + "</html>");
				
				selectedCS = graph.get(new VersionManager(graph, selectedRev.user()).emptyChangeSetHandle());
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
		changeSetPanel.setChangeSet(selectedCS.changes(), 
									selectedCSConflicts, 
									firstItemString);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
	 * .ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		try
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
					//
					// Revision selectedRevision =
					// versionedOntology.getRevisions().get(selectedRevisionIndex);
					updateChangeSetList(selectedRevisionIndex);
				}
				else
				{
					updateChangeSetList(-1);
				}
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
		}
	}
}