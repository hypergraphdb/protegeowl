package gov.miamidade.hgowl.plugin.ui.versioning;

import gov.miamidade.hgowl.plugin.ui.DialogBase;
import gov.miamidade.hgowl.plugin.ui.repository.VOntologyViewPanel;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.VDRenderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JButton;
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
import org.hypergraphdb.util.Pair;
import org.protege.editor.owl.OWLEditorKit;

public class RevisionDialog extends DialogBase implements ListSelectionListener
{

	public static final int MAX_CHANGES_SHOWN = 250;
	public static final String EMPTY_LIST_TEXT = "Please select a revision above.";

	private static final long serialVersionUID = -284973463639671572L;
	private Revision selectedRevision;	
	private VOntologyViewPanel ontologyView;
	private ChangeSetTablePanel changeSetPanel;
	private List<Pair<String, Runnable>> actions = new ArrayList<Pair<String, Runnable>>();
	private VersionedOntology versionedOntology;
	private OWLEditorKit kit;
	private String title;
	private Component parent;
	
	// TODO...it's not a linear list anymore, maybe do a topological ordering
	List<Revision> revisions = null;
	
	// private DateFormat dateF = DateFormat.getDateTimeInstance();
 

	public RevisionDialog(String title, Component parent, VersionedOntology vo, OWLEditorKit kit)
	{
		super(SwingUtilities.windowForComponent(parent));
		this.versionedOntology = vo;
		this.kit = kit;
		this.title = title;
		this.parent = parent;
	}

	public RevisionDialog showDialog()
	{
		setVisible(true);
		return this;
	}

	public RevisionDialog build()
	{
		setLayout(new BorderLayout());
		String message = "<html> <table width='100%' border='0'>"
				+ "<tr><td align='right'><b>Ontology:</b></td><td>" + 
						versionedOntology.ontology().getOntologyID() + "</td></tr>"
				+ "<tr><td align='right'><b>Head:</b></td><td>" + versionedOntology.revision() + "(local)"
				+ "</td></tr>" + "</table>";
		JPanel northPanel = new JPanel(new BorderLayout(5, 5));
		northPanel.add(new JLabel(message), BorderLayout.NORTH);
		JSplitPane centerPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		// TOP SHOWS REVISIONS
		revisions = versionedOntology.revisions();
		
		ontologyView = new VOntologyViewPanel(versionedOntology);
		ontologyView.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		ontologyView.getTable().getSelectionModel().addListSelectionListener(this);
		centerPanel.setLeftComponent(ontologyView);

		// BOTTOM SHOWS SELECTED CHANGESET
		changeSetPanel = new ChangeSetTablePanel(versionedOntology.ontology(), 
												 versionedOntology.graph(), 
												 kit); 
		// centerPanel.add(new JScrollPane(changeSetList));
		centerPanel.setRightComponent(changeSetPanel);
		centerPanel.setDividerLocation(150);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		for (final Pair<String, Runnable> action : actions)
		{
			JButton btn = new JButton(action.getFirst());
			btn.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) { action.getSecond().run(); }
			});
			buttonPanel.add(btn);			
		}
		JButton btn = new JButton("Close");
		btn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) { closeDialog(); }
		});
		buttonPanel.add(btn);
		
		add(northPanel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.SOUTH);
		add(centerPanel, BorderLayout.CENTER);
		setSize(1000, 600);		
		setTitle(title);
		setLocationRelativeTo(parent);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);				
		return this;
	}
	
	public void closeDialog()
	{
		this.setVisible(false);
		this.dispose();
	}

	public RevisionDialog action(String caption, Runnable runnable)
	{
		actions.add(new Pair<String, Runnable>(caption, runnable));
		return this;
	}
	/**
	 * 
	 * @param selectedRev
	 */
	public void updateChangeSetList(int selection1, int selection2)
	{
		String firstItemString = null;
		ChangeSet<VersionedOntology> selectedCS = null;
		SortedSet<Integer> selectedCSConflicts = null;
		HyperGraph graph = versionedOntology.graph();
		Revision selectedRev = revisions.get(selection2);
		selectedCS = versioning.changes(graph, 
										selectedRev.getAtomHandle(), 
										revisions.get(selection1).getAtomHandle());
		firstItemString = "<html>Showing Changes that were commited by <b>" + selectedRev.user() + "</b> at "
				+ VDRenderer.render(new java.util.Date(selectedRev.timestamp())) + 
				" for revision " + selectedRev + "</html>";
		firstItemString += "<br> with comment <b>" + selectedRev.comment() + "</b>";
		
//		if (selectedRevisionIndex != -1)
//		{
//			if (selectedRevisionIndex == revisions.size())
//			{
//				// Pending changes in local workingset
//				firstItemString = "<html>Showing <b>uncommitted</b> Changes that were made by <b>you</b> </html>";
//				selectedCS = versionedOntology.changes(); 
//				// TODO...
//				selectedCSConflicts = new TreeSet<Integer>();//versionedOntology.getWorkingSetConflicts();
//				// renderChangeset(lm, selectedCS);
//			}
//			else if (selectedRevisionIndex > 0)
//			{
//				Revision selectedRev = revisions.get(selectedRevisionIndex);
//				selectedCS = versioning.changes(graph, 
//												selectedRev.getAtomHandle(), 
//												revisions.get(selectedRevisionIndex-1).getAtomHandle());
//				firstItemString = "<html>Showing Changes that were commited by <b>" + selectedRev.user() + "</b> at "
//						+ VDRenderer.render(new java.util.Date(selectedRev.timestamp())) + 
//						" for revision " + selectedRev + "</html>";
//				firstItemString += "<br> with comment <b>" + selectedRev.comment() + "</b>";
//				// renderChangeset(lm, selectedCS);
//			}
//			else if (selectedRevisionIndex == 0)
//			{
//				Revision selectedRev = revisions.get(selectedRevisionIndex);
//				firstItemString = ("<html> Initial revision that was created by <b>" + selectedRev.user() + "</b> at "
//						+ VDRenderer.render(new java.util.Date(selectedRev.timestamp())) + "</html>");
//				
//				selectedCS = graph.get(new VersionManager(graph, selectedRev.user()).emptyChangeSetHandle());
//			}
//			else
//			{
//				System.err.println("Cannot render revision: " + selectedRevisionIndex);
//				return;
//			}
//		}
//		else
//		{
//			// Empty list, nothing selected
//			firstItemString = EMPTY_LIST_TEXT;
//		}
		changeSetPanel.setChangeSet(selectedCS.changes(), 
									selectedCSConflicts, 
									firstItemString);
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getValueIsAdjusting())
			return;
		else
		{
			if (ontologyView.getTable().getSelectedRowCount() == 2)
			{
				int rev1 = ontologyView.getTable().getSelectedRows()[0];
				int rev2 = ontologyView.getTable().getSelectedRows()[1];
				updateChangeSetList(rev1, rev2);
			}
		}
	}
	
	public Revision getSelectedRevision()
	{
		return selectedRevision;
	}	
	
	public VOntologyViewPanel ontologyView()
	{
		return ontologyView;
	}
}