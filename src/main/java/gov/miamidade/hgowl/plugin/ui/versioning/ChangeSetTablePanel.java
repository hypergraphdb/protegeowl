package gov.miamidade.hgowl.plugin.ui.versioning;

import gov.miamidade.hgowl.plugin.ui.versioning.ChangeSetTable.ChangeDisplayMode;

import java.awt.BorderLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.SortedSet;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.TransferHandler;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.newver.ChangeSet;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.protege.editor.owl.OWLEditorKit;

/**
 * ChangeSetPanel shows all changes in a changeset. Ctrl-C copy enabled.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 13, 2012
 */
public class ChangeSetTablePanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private ChangeSetTable changeSetTable;
	private ChangeSet<VersionedOntology> changeSet;
	private JLabel headerLabel;
	private JLabel footerLabel = new JLabel("Select changes and press Ctrl-C to copy");
	private JToggleButton displayModeBt = new JToggleButton("Render Functional");

	JScrollPane scrollPane;

	public ChangeSetTablePanel(HGDBOntology onto, HyperGraph graph, OWLEditorKit kit)
	{
		setLayout(new BorderLayout());
		changeSetTable = new ChangeSetTable(onto, graph, kit);
		scrollPane = new JScrollPane(changeSetTable);
		headerLabel = new JLabel();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		displayModeBt.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (displayModeBt.getModel().isSelected())
				{
					changeSetTable.setMode(ChangeDisplayMode.FUNCTIONAL);
				}
				else
				{
					changeSetTable.setMode(ChangeDisplayMode.OWL);
				}
			}
		});
		JPanel footerPanel = new JPanel(new BorderLayout());
		footerPanel.add(footerLabel, BorderLayout.EAST);
		footerPanel.add(displayModeBt, BorderLayout.WEST);
		this.add(headerLabel, BorderLayout.NORTH);
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(footerPanel, BorderLayout.SOUTH);
		enableClipBoardCopy(changeSetTable);
		// this.add(new
		// JLabel("<html><small>Use <i>Ctrl-C</i> to copy changes</small></html>"),
		// BorderLayout.PAGE_END);
	}

	public void setChangeSet(ChangeSet<VersionedOntology> cs, String headerText)
	{
		setChangeSet(cs, null, headerText);
	}

	public void setChangeSet(ChangeSet<VersionedOntology> cs, SortedSet<Integer> conflictIndices, String headerText)
	{
		headerLabel.setText(headerText);
		changeSet = cs;
		changeSetTable.setChangeSet(cs, conflictIndices);
		if (cs != null)
		{
			// scrollPane.setColumnHeaderView(new
			// JLabel("<html>Number of changes: " + cs.getArity() +
			// " <br> Use Ctrl-C to copy changes. </html>"));
		}
	}

	public ChangeSet<VersionedOntology> getChangeSet()
	{
		return changeSet;
	}

	private void enableClipBoardCopy(JTable table)
	{
		ActionMap map = table.getActionMap();
		map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
		InputMap imap = table.getInputMap();
		imap.put(KeyStroke.getKeyStroke("ctrl C"), TransferHandler.getCopyAction().getValue(Action.NAME));
		table.setTransferHandler(new ChangesListTransferHandler());
	}

	public class ChangesListTransferHandler extends TransferHandler
	{

		private static final long serialVersionUID = -1196413717665065379L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.TransferHandler#exportToClipboard(javax.swing.JComponent,
		 * java.awt.datatransfer.Clipboard, int)
		 */
		@Override
		public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException
		{
			if (comp == changeSetTable)
			{
				StringBuffer transfer = new StringBuffer(1000);
				int[] selectedRows = changeSetTable.getSelectedRows();
				if (selectedRows != null)
				{
					for (int selectedRow : selectedRows)
					{
						Object o = changeSetTable.getModel().getValueAt(selectedRow, ChangeSetTableModel.CHANGE_COLUMN_INDEX);
						transfer.append(o.toString() + "\r\n");
					}
				}
				clip.setContents(new StringSelection(transfer.toString()), null);
			}
			else
			{
				// Do nothing
			}
		}
	}
}
