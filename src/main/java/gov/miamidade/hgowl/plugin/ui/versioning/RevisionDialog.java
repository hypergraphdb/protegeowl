package gov.miamidade.hgowl.plugin.ui.versioning;

import gov.miamidade.hgowl.plugin.ui.DialogBase;
import gov.miamidade.hgowl.plugin.ui.repository.VOntologyViewPanel;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.VDRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.Change;
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
		
		ontologyView = new VOntologyViewPanel(parent, versionedOntology);		
		ontologyView.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		ontologyView.getTable().getSelectionModel().addListSelectionListener(this);
		centerPanel.setLeftComponent(ontologyView);
		revisions = ontologyView.orderedRevisions();

		// BOTTOM SHOWS SELECTED CHANGESET
		changeSetPanel = new ChangeSetTablePanel(versionedOntology.ontology(), 
												 versionedOntology.graph(), 
												 kit); 
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
		List<Change<VersionedOntology>> changes = new ArrayList<Change<VersionedOntology>>();
		String message = null;
		Revision from, to;
		if (selection1 == 0)
		{
			changes.addAll(versionedOntology.changes().changes());
			message = "<html>Showing working changes as well as changes from last commit up to revision ";
			from = revisions.get(revisions.size() - selection2);
			to = revisions.get(revisions.size() - 1);
		}
		else
		{
			from = revisions.get(revisions.size() - selection2);
			to = revisions.get(revisions.size() - selection1);			
		}
		List<Change<VersionedOntology>> revDiffs = versioning.changes(versionedOntology.graph(), from.getAtomHandle(), to.getAtomHandle());
		changes.addAll(revDiffs);			
		if (message == null)
			message = "<html>Showing Changes that were commited by <b>" + to.user() + "</b> at "
					+ VDRenderer.render(new java.util.Date(to.timestamp())) + 
					" for revision ";
		message +=  to + "</html>"; // "<br> with comment <b>" + to.comment() + "</b>";
		changeSetPanel.setChangeSet(changes, 
									null, 
									message);
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		try
		{
			if (e.getValueIsAdjusting())
				return;
			else
			{
				int [] rows = ontologyView.graphPanel().selectedRows(); 
				if (rows != null && rows.length == 2)
				{
					int rev1 = rows[0];
					int rev2 = rows[1];
					updateChangeSetList(rev1, rev2);
				}
				else
				{
					changeSetPanel.setChangeSet(new ArrayList<Change<VersionedOntology>>(), 
												null, 
												"");					
				}
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
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