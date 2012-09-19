package gov.miamidade.hgowl.plugin.ui.versioning;

import gov.miamidade.hgowl.plugin.ui.versioning.ChangeSetPanel;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.VDRenderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.protege.editor.owl.OWLEditorKit;

/**
 * Rollback Dialog for rolling back local uncommitted changes.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 31, 2012
 */
public class RollbackDialog extends JDialog implements ActionListener {
	
	private static final long serialVersionUID = -2849737178569671572L;
	private ChangeSetTablePanel changeSetPanel;
	private JButton btOK;
	private JButton btCancel;

	private boolean userRollbackOK;
	
	public static RollbackDialog showDialog(String title, Component parent, VersionedOntology vo, OWLEditorKit kit) {
		RollbackDialog dlg = new RollbackDialog(title, SwingUtilities.windowForComponent(parent), vo, kit);
		dlg.setLocationRelativeTo(parent);
		dlg.setModalityType(ModalityType.APPLICATION_MODAL);
		dlg.setResizable(true);
		dlg.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		dlg.setVisible(true);
		return dlg;
	}
	
	
	public RollbackDialog(String title, Window w, VersionedOntology vo, OWLEditorKit kit) {
		super(w);
		setTitle(title);
		ChangeSet workingSetChanges  = vo.getWorkingSetChanges();
		String message = "<html> <h2> Rollback Pending Ontology Changes </h2> "
		    +"<table width='100%' border='0'>"
		    +"<tr><td align='right'><b>Ontology:</b></td><td>"+ VDRenderer.render(vo) + "</td></tr>"
		    +"<tr><td align='right'><b>Head:</b></td><td>"+VDRenderer.render(vo.getHeadRevision()) + "(local)"+ "</td></tr>"
		    +"</table>";
		JPanel northPanel = new JPanel(new BorderLayout(5, 5));
		northPanel.add(new JLabel(message), BorderLayout.NORTH);
		btOK = new JButton("Rollback");
		btOK.addActionListener(this);
		btCancel = new JButton("Cancel");	
		btCancel.addActionListener(this);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(btOK);
		buttonPanel.add(btCancel);
		//
		changeSetPanel = new ChangeSetTablePanel(vo.getWorkingSetData(), vo.getHyperGraph(), kit);
		changeSetPanel.setChangeSet(vo.getWorkingSetChanges(), vo.getWorkingSetConflicts(), "Rollback");
		//renderChangeset((DefaultListModel)changeSetList.getModel(), workingSetChanges, vo.getHyperGraph(), vo.getWorkingSetData());
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.add(northPanel, BorderLayout.NORTH);
		this.add(changeSetPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		setSize(900,450);
	}
	
	public boolean isRolbackOK() {
		return userRollbackOK;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btOK) {
			userRollbackOK = true;			
		} else if (e.getSource() == btCancel){
			userRollbackOK = false;			
		} else {
			throw new IllegalArgumentException("Got an event from an unknown source!" + e);
		}
		this.setVisible(false);
		this.dispose();
	}
}