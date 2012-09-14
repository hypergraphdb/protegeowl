package gov.miamidade.hgowl.plugin.ui.versioning;

import gov.miamidade.hgowl.plugin.ui.versioning.distributed.VDRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ListIterator;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.TransferHandler;
import javax.swing.border.Border;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChangeFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * ChangeSetPanel shows all changes in a changeset. Ctrl-C copy enabled.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 13, 2012
 */
public class ChangeSetPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JList changeSetList; 
	private int maxChangesVisible = Integer.MAX_VALUE;
	private ChangeSet changeSet;
	JScrollPane scrollPane;
	
	public ChangeSetPanel() {
		setLayout(new GridLayout());
		setBackground(Color.RED);
		//JPanel scrollPanel = new JPanel(new GridLayout(1, 1,0,0));
		//scrollPanel.setBackground(Color.blue);
		changeSetList = new JList();
		scrollPane = new JScrollPane(changeSetList);
		//scrollPane.setPreferredSize(new Dimension(2000, 2000));
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		//scrollPanel.add(scrollPane);
		this.add(scrollPane);
		enableClipBoardCopy(changeSetList);
		//this.add(new JLabel("<html><small>Use <i>Ctrl-C</i> to copy changes</small></html>"), BorderLayout.PAGE_END);
	}

	
	public void setChangeSet(ChangeSet cs, HyperGraph graph, HGDBOntology onto) {
		setChangeSet(cs, graph, onto, null);
	}
	
	/**
	 * 
	 * @param cs null shows empty list or firstItemText.
	 * @param firstItemText null allowed.
	 */
	public void setChangeSet(ChangeSet cs, HyperGraph graph, HGDBOntology onto, String firstItemText) {
		changeSet = cs;
		DefaultListModel lm = new DefaultListModel();
		if (firstItemText != null) {
			lm.addElement(firstItemText);
		}
		if (cs != null) {
			scrollPane.setColumnHeaderView(new JLabel("<html>Number of changes: " + cs.getArity() + " <br> Use Ctrl-C to copy changes. </html>"));
			renderChangeset(lm, cs, graph, onto);
		}
		changeSetList.setModel(lm);
		revalidate();
		repaint();
	}
	
	public ChangeSet getChangeSet() {
		return changeSet;
	}

	private void renderChangeset(DefaultListModel lm, ChangeSet cs, HyperGraph graph, OWLOntology onto) {
		// Iterate changeset reverse order
		int nrOfchanges = cs.getChanges().size();
		int i = nrOfchanges;
		ListIterator<VOWLChange> lIt = cs.getChanges().listIterator(nrOfchanges);
		while (lIt.hasPrevious() && (nrOfchanges - i) < maxChangesVisible) {
			VOWLChange vc = lIt.previous();
			i--;
			OWLOntologyChange c = VOWLChangeFactory.create(vc, onto, graph);
			lm.addElement("" + VDRenderer.render(i) + " " + c.toString());
		}
		if (i != 0) {
			lm.add(0, "<html><b>Number of changes omitted from view: " + i + "</b></html>");
		}
		if (nrOfchanges == 0) {
			lm.addElement("There are no changes to show.");
		}
	}

	private void enableClipBoardCopy(JList list) { 
        ActionMap map = list.getActionMap();
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
        InputMap imap = list.getInputMap();
        imap.put(KeyStroke.getKeyStroke("ctrl C"),
            TransferHandler.getCopyAction().getValue(Action.NAME));
        list.setTransferHandler(new ChangesListTransferHandler());
	}

	/**
	 * @return the maxChangesVisible
	 */
	public int getMaxChangesVisible() {
		return maxChangesVisible;
	}


	/**
	 * @param maxChangesVisible the maxChangesVisible to set
	 */
	public void setMaxChangesVisible(int maxChangesVisible) {
		this.maxChangesVisible = maxChangesVisible;
	}

	public class ChangesListTransferHandler extends TransferHandler {

		private static final long serialVersionUID = -1196413717665065379L;

		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#exportToClipboard(javax.swing.JComponent, java.awt.datatransfer.Clipboard, int)
		 */
		@Override
		public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
			if (comp == changeSetList) {
				StringBuffer transfer = new StringBuffer(1000);
					Object[] selectedValues = changeSetList.getSelectedValues();
				for (Object o : selectedValues) {
					transfer.append(o.toString() + "\r\n");
				}
				clip.setContents(new StringSelection(transfer.toString()), null);
			} else {
				//Do nothing
			}
		}
	}
}
