package gov.miamidade.hgowl.plugin.ui.versioning;

import gov.miamidade.hgowl.plugin.ui.repository.VOntologyViewPanel;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.VDRenderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ListIterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
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
public class VHGRevertDialog extends JDialog implements ActionListener, ListSelectionListener {
	
	public static final int MAX_CHANGES_SHOWN = 250;
	public static final String EMPTY_LIST_TEXT = "Please select a revision above.";
	
	private static final long serialVersionUID = -284973463639671572L;
	private Revision selectedRevision;

	private VersionedOntology versionedOntology;
	private VOntologyViewPanel ontologyView;
	private JList changeSetList;
	private JButton btOK;
	private JButton btCancel;

	private DecimalFormat df = new DecimalFormat("####000");
	private DateFormat dateF = DateFormat.getDateTimeInstance();
	private boolean userConfirmedRevert;

	public static VHGRevertDialog showDialog(String title, Component parent, VersionedOntology vo) {
		VHGRevertDialog dlg = new VHGRevertDialog(title, SwingUtilities.windowForComponent(parent), vo);
		dlg.setLocationRelativeTo(parent);
		dlg.setModalityType(ModalityType.APPLICATION_MODAL);
		dlg.setResizable(true);
		dlg.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		dlg.setVisible(true);
		return dlg;
	}
	
	
	public VHGRevertDialog(String title, Window w, VersionedOntology vo) {
		super(w);
		w.addWindowListener(new WindowAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(WindowEvent e) {
				closeDialog();
			}
		});
		versionedOntology = vo;
		setTitle(title);
        String message = "<html> <h2> Revert Ontology to older revision </h2> "
		    +"<table width='100%' border='0'>"
		    +"<tr><td align='right'><b>Ontology:</b></td><td>"+ VDRenderer.render(vo) + "</td></tr>"
		    +"<tr><td align='right'><b>Server:</b></td><td>"+ "local" + "</td></tr>"
		    +"<tr><td align='right'><b>Head:</b></td><td>"+VDRenderer.render(vo.getHeadRevision()) + "(local)"+ "</td></tr>"
		    +"</table>";
		JPanel northPanel = new JPanel(new BorderLayout(5, 5));
		northPanel.add(new JLabel(message), BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
		// TOP SHOWS REVISIONS
		ontologyView = new VOntologyViewPanel(vo);
		ontologyView.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ontologyView.getTable().getSelectionModel().addListSelectionListener(this);
		centerPanel.add(ontologyView);
		
		//BOTTOM SHOWS SELECTED CHANGESET
		changeSetList = new JList(new String[]{EMPTY_LIST_TEXT});
		centerPanel.add(new JScrollPane(changeSetList));
		
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
		//this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setSize(850,500);
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btOK) {
			userConfirmedRevert = true;
		} else if (e.getSource() == btCancel){
		} else {
			throw new IllegalArgumentException("Got an event from an unknown source!" + e);
		}
		closeDialog();
	}

	public void closeDialog() {
		this.setVisible(false);
		this.dispose();
		versionedOntology = null;
	}

	
	/**
	 * 
	 * @param selectedRev
	 */
	public void updateChangeSetList(int selectedRevisionIndex) {
		DefaultListModel lm = new DefaultListModel();
		if (selectedRevisionIndex != -1) {
			java.util.List<Revision> revisions = versionedOntology.getRevisions();
			java.util.List<ChangeSet> changeSets = versionedOntology.getChangeSets();
			if (selectedRevisionIndex == revisions.size()) {
				//Pending changes in local workingset
				lm.addElement("<html>Showing <b>uncommitted</b> Changes that were made by <b>you</b> </html>" );
				ChangeSet selectedCS = changeSets.get(selectedRevisionIndex - 1);
				renderChangeset(lm, selectedCS);
			} else if (selectedRevisionIndex > 0) {
				Revision selectedRev = revisions.get(selectedRevisionIndex);
				ChangeSet selectedCS = changeSets.get(selectedRevisionIndex - 1);
				lm.addElement("<html>Showing Changes that were commited by <b>" 
						+ selectedRev.getUser() + "</b> at " 
						+  dateF.format(selectedRev.getTimeStamp()) + " after revision " 
						+ selectedRev.getRevision() 
						+ " constituting revision " + selectedRev.getRevision() + "</html>");
				lm.addElement("<html>with comment <b>" + selectedRev.getRevisionComment() + "</b> </html>");  
				renderChangeset(lm, selectedCS);
			} else if (selectedRevisionIndex == 0) {
				Revision selectedRev = revisions.get(selectedRevisionIndex);
				lm.addElement("<html> Initial revision that was created by <b>" 
						+ selectedRev.getUser() + "</b> at " 
						+  dateF.format(selectedRev.getTimeStamp()) 
						+ "</html>");
				lm.addElement("<html>with comment <b>" + selectedRev.getRevisionComment() + "</b> </html>");  
				lm.addElement("<html>No changes to show.</html>");  
			} else {
				System.err.println("Cannot render revision: " + selectedRevisionIndex);
				return;
			}
		} else {
			// Empty list, nothing selected
			lm.addElement(EMPTY_LIST_TEXT); 
		}
		changeSetList.setModel(lm);
		//changeSetList.repaint();
	}
		
	private void renderChangeset(DefaultListModel lm, ChangeSet cs) {
		HyperGraph graph = versionedOntology.getHyperGraph();
		OWLOntology onto = versionedOntology.getWorkingSetData();
		// Iterate changeset reverse order
		int nrOfchanges = cs.getChanges().size();
		int i = nrOfchanges;
		ListIterator<VOWLChange> lIt = cs.getChanges().listIterator(nrOfchanges);
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
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;
		else {
			//Row 0 shows pending changes selects latest changeset.
			int selectedRevisionIndex = versionedOntology.getArity() - ontologyView.getTable().getSelectedRow();
			//System.out.println("SELECTED: " + selectedRevisionIndex);
			if (selectedRevisionIndex >= 0 && selectedRevisionIndex <= versionedOntology.getNrOfRevisions()) {
				if (selectedRevisionIndex != versionedOntology.getNrOfRevisions()) {
					selectedRevision = versionedOntology.getRevisions().get(selectedRevisionIndex);
				} else {
					//Pending Changes selected
					selectedRevision = null;
				}
				updateChangeSetList(selectedRevisionIndex);
			} else {
				selectedRevision = null;
				updateChangeSetList(-1);
			}
		}
	}

	/**
	 * @return the revertToRevision
	 */
	public Revision getSelectedRevision() {
		return selectedRevision;
	}

	public boolean isUserConfirmedRevert() {
		return userConfirmedRevert;
	}
}
