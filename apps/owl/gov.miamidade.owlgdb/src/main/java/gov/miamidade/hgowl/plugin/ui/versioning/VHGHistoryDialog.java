package gov.miamidade.hgowl.plugin.ui.versioning;

import gov.miamidade.hgowl.plugin.ui.repository.VOntologyViewPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ListIterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChangeFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * VHGCommitDialog shows all revisions of a given versioned ontology, allows the user to select a revisions 
 *  and shows all changes associated with the selection.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Apr 06, 2012
 */
public class VHGHistoryDialog extends JDialog implements ActionListener, ListSelectionListener {
	
	public static final int MAX_CHANGES_SHOWN = 250;
	public static final String EMPTY_LIST_TEXT = "Please select a revision above.";
	
	private static final long serialVersionUID = -284973463639671572L;
	private VersionedOntology versionedOntology;
	private VOntologyViewPanel ontologyView;
	private JList changeSetList;
	private JButton btOK;
	private JButton btCancel;

	private DecimalFormat df = new DecimalFormat("####000");
	private DateFormat dateF = DateFormat.getDateTimeInstance();

	public static VHGHistoryDialog showDialog(Component parent, VersionedOntology vo) {
		VHGHistoryDialog dlg = new VHGHistoryDialog(SwingUtilities.windowForComponent(parent), vo);
		dlg.setModalityType(ModalityType.APPLICATION_MODAL);
		dlg.setVisible(true);
		dlg.setResizable(true);
		dlg.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		return dlg;
	}
	
	
	public VHGHistoryDialog(Window w, VersionedOntology vo) {
		super(w);
		versionedOntology = vo;
		setTitle("Versioned HGDB Ontology - History of active Ontology " + vo.getWorkingSetData().getOntologyID());
		JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
		// TOP SHOWS REVISIONS
		ontologyView = new VOntologyViewPanel(vo);
		ontologyView.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ontologyView.getTable().getSelectionModel().addListSelectionListener(this);
		centerPanel.add(ontologyView);
		
		//BOTTOM SHOWS SELECTED CHANGESET
		changeSetList = new JList(new String[]{EMPTY_LIST_TEXT});
		centerPanel.add(new JScrollPane(changeSetList));
		
		btOK = new JButton("OK");
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
		setSize(800,600);
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btOK) {
		} else if (e.getSource() == btCancel){
		} else {
			throw new IllegalArgumentException("Got an event from an unknown source!" + e);
		}
		this.setVisible(false);
		this.dispose();
		versionedOntology = null;
	}
	
	/**
	 * 
	 * @param selectedRev
	 */
	public void updateChangeSetList(int selectedIndex) {
		HyperGraph graph = versionedOntology.getHyperGraph();
		OWLOntology onto = versionedOntology.getWorkingSetData();
		DefaultListModel lm = new DefaultListModel();
		if (selectedIndex != -1) {
			java.util.List<Revision> revisions = versionedOntology.getRevisions(); 
			Revision selectedRev = revisions.get(selectedIndex);
			Revision nextRev = null; 
			if (selectedIndex + 1 < revisions.size()) {
				nextRev = revisions.get(selectedIndex + 1);
				lm.addElement("<html>Showing Changes that were commited by <b>" 
						+ nextRev.getUser() + "</b> at " 
						+  dateF.format(nextRev.getTimeStamp()) + " after revision " 
						+ selectedRev.getRevision() 
						+ " constituting revision " + nextRev.getRevision() + "</html>");
				lm.addElement("<html>with comment <b>" + nextRev.getRevisionComment() + "</b> </html>");  
			} else {
				//Pending changes in local workingset
				lm.addElement("<html>Showing <b>uncommitted</b> Changes that were made by <b>you</b> after revision </html>" 
						+ selectedRev.getRevision()); 
			}
			// Iterate changeset reverse order
			ChangeSet selectedCS = versionedOntology.getChangeSet(selectedRev);
			int nrOfchanges = selectedCS.getChanges().size();
			int i = nrOfchanges;
			ListIterator<VOWLChange> lIt = selectedCS.getChanges().listIterator(nrOfchanges);
			while (lIt.hasPrevious() && (nrOfchanges - i) < MAX_CHANGES_SHOWN) {
				VOWLChange vc = lIt.previous();
				i--;
				OWLOntologyChange c = VOWLChangeFactory.create(vc, onto, graph);
				lm.addElement("" + df.format(i) + " " + c.toString());
			}
			if (i != 0) {
				lm.add(0, "<html><b>Number of changes omitted from view: " + i + "</b></html>");
			}
			if (nrOfchanges == 0) {
				lm.addElement("There are no changes to show.");
			}
		} else {
			// Empty list
			lm.addElement(EMPTY_LIST_TEXT); 
		}
		changeSetList.setModel(lm);
		//changeSetList.repaint();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;
		else {
			
			int selectedRevisionIndex = versionedOntology.getArity() - ontologyView.getTable().getSelectedRow() - 1;
			//System.out.println("SELECTED: " + selectedRevisionIndex);
			if (selectedRevisionIndex >= 0 && selectedRevisionIndex < versionedOntology.getArity()) {
			//
				//Revision selectedRevision = versionedOntology.getRevisions().get(selectedRevisionIndex);
				updateChangeSetList(selectedRevisionIndex);
				} else {
				updateChangeSetList(-1);
			}
		}
	}
}
