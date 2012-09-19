package gov.miamidade.hgowl.plugin.ui.versioning;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.SortedSet;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.TransferHandler;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.protege.editor.owl.OWLEditorKit;

/**
 * ChangeSetPanel shows all changes in a changeset. Ctrl-C copy enabled.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 13, 2012
 */
public class ChangeSetTablePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private ChangeSetTable changeSetTable; 
	private ChangeSet changeSet;
	private JLabel headerLabel;
	JScrollPane scrollPane;
	
	public ChangeSetTablePanel(HGDBOntology onto, HyperGraph graph, OWLEditorKit kit) {
		setLayout(new BorderLayout());
		changeSetTable = new ChangeSetTable(onto, graph, kit);
		scrollPane = new JScrollPane(changeSetTable);
		headerLabel = new JLabel();
		//scrollPane.setPreferredSize(new Dimension(2000, 2000));
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		//scrollPanel.add(scrollPane);
		this.add(headerLabel, BorderLayout.NORTH);
		this.add(scrollPane, BorderLayout.CENTER);
		enableClipBoardCopy(changeSetTable);
		//this.add(new JLabel("<html><small>Use <i>Ctrl-C</i> to copy changes</small></html>"), BorderLayout.PAGE_END);
	}

	
	public void setChangeSet(ChangeSet cs, String headerText) {
		setChangeSet(cs, null, headerText);
	}

	public void setChangeSet(ChangeSet cs, SortedSet<Integer> conflictIndices, String headerText) {
		headerLabel.setText(headerText);
		changeSet = cs;
		changeSetTable.setChangeSet(cs, conflictIndices);
		if (cs != null) {
			//scrollPane.setColumnHeaderView(new JLabel("<html>Number of changes: " + cs.getArity() + " <br> Use Ctrl-C to copy changes. </html>"));
		}
	}
	
	public ChangeSet getChangeSet() {
		return changeSet;
	}

	private void enableClipBoardCopy(JTable table) { 
        ActionMap map = table.getActionMap();
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
        InputMap imap = table.getInputMap();
        imap.put(KeyStroke.getKeyStroke("ctrl C"),
            TransferHandler.getCopyAction().getValue(Action.NAME));
        table.setTransferHandler(new ChangesListTransferHandler());
	}

	public class ChangesListTransferHandler extends TransferHandler {

		private static final long serialVersionUID = -1196413717665065379L;
		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#exportToClipboard(javax.swing.JComponent, java.awt.datatransfer.Clipboard, int)
		 */
		@Override
		public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
			if (comp == changeSetTable) {
				StringBuffer transfer = new StringBuffer(1000);
					int[]selectedRows = changeSetTable.getSelectedRows();
					if (selectedRows != null) {
						for (int selectedRow : selectedRows) {
							Object o = changeSetTable.getModel().getValueAt(selectedRow, ChangeSetTableModel.CHANGE_RENDERED_COLUMN_INDEX);
							transfer.append(o.toString() + "\r\n");
						}
					}
				clip.setContents(new StringSelection(transfer.toString()), null);
			} else {
				//Do nothing
			}
		}
	}
}
