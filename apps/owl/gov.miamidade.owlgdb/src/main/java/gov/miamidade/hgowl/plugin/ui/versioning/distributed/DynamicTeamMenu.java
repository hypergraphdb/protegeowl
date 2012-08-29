package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import gov.miamidade.hgowl.plugin.owl.VDHGOwlEditorKit;
import gov.miamidade.hgowl.plugin.owl.VDHGOwlEditorKit.OntologyDistributionState;
import gov.miamidade.hgowl.plugin.ui.versioning.VHGHistoryActiveAction;
import gov.miamidade.hgowl.plugin.ui.versioning.VHGRevertOneRevisionAction;
import gov.miamidade.hgowl.plugin.ui.versioning.VHGRollbackAction;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.ui.action.ProtegeDynamicAction;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;

/**
 * DynamicVersioningMenu.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Aug 21, 2012
 */
public class DynamicTeamMenu extends ProtegeDynamicAction {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = -2187253639033356483L;

	/* (non-Javadoc)
	 * @see org.protege.editor.core.plugin.ProtegePluginInstance#initialise()
	 */
	@Override
	public void initialise() throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.protege.editor.core.Disposable#dispose()
	 */
	@Override
	public void dispose() throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Woo hoo actionPerformed.");
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.core.ui.action.ProtegeDynamicAction#rebuildChildMenuItems(javax.swing.JMenu)
	 */
	@Override
	public void rebuildChildMenuItems(JMenu menu) {
		EditorKit ekit = getEditorKit();
		if (!(ekit instanceof VDHGOwlEditorKit)) {
			System.err.println("Not running with a versioned distributed VDHGOwlEditorKit. DynamicVersioningMenu will be empty.");
			return;
		}
		VDHGOwlEditorKit vdKit = (VDHGOwlEditorKit) ekit;
		OntologyDistributionState activeOntoDistState = vdKit.getActiveOntologyDistributionState();
		boolean userOnline = vdKit.isNetworking();
		if (activeOntoDistState.equals(OntologyDistributionState.ONTO_NOT_SHARED)) {
			buildGeneralMenuItems(menu, vdKit, false, userOnline);
		} else if (activeOntoDistState.equals(OntologyDistributionState.ONTO_SHARED_CENTRAL_SERVER)) {
			buildCentralServerMenuItems(menu, vdKit);
			buildGeneralMenuItems(menu, vdKit, true, userOnline);
		} else if (activeOntoDistState.equals(OntologyDistributionState.ONTO_SHARED_CENTRAL_CLIENT)) {
			buildCentralClientMenuItems(menu, vdKit, userOnline);
			buildGeneralMenuItems(menu, vdKit, true, userOnline);
		} else if (activeOntoDistState.equals(OntologyDistributionState.ONTO_SHARED_DISTRIBUTED)) {
			menu.add(new JMenuItem("Distributed version control not yet implemented."));
			menu.add(new JMenuItem("Cancel sharing."));
			buildGeneralMenuItems(menu, vdKit, true, userOnline);
		} else {
			System.err.println("DynTeamMenu: Cannot build menu. unknown state detected: " + activeOntoDistState);
		}
	}

	private void buildCentralServerMenuItems(JMenu menu, VDHGOwlEditorKit kit) {
		ProtegeOWLAction cur;
		cur = new VDHGCommitActionCentralServer();
		cur.putValue(Action.NAME, "Commit...");
		cur.setEditorKit(kit);
		menu.add(cur);
		cur = new VHGHistoryActiveAction();
		cur.putValue(Action.NAME, "History...");
		cur.setEditorKit(kit);
		menu.add(cur);
		menu.addSeparator();
		cur = new VHGRollbackAction();
		cur.putValue(Action.NAME, "Rollback...");
		cur.setEditorKit(kit);
		menu.add(cur);
		//TODO just for now the generic revert one. needs to be revert active and show history.
		cur = new VHGRevertOneRevisionAction();
		cur.putValue(Action.NAME, "Revert to...");
		cur.setEditorKit(kit);
		menu.add(cur);
		menu.addSeparator();
	}

	private void buildCentralClientMenuItems(JMenu menu, VDHGOwlEditorKit kit, boolean userOnline) {
		ProtegeOWLAction cur;
		cur = new VDHGCommitActionCentralServer();
		cur.putValue(Action.NAME, "Commit...");
		//Do not allow offline user to commit.
		cur.setEnabled(userOnline);
		cur.setEditorKit(kit);
		menu.add(cur);
		cur = new VDHGCommitActionCentralServer();
		cur.putValue(Action.NAME, "Update...");
		//Do not allow offline user to commit.
		cur.setEnabled(userOnline);
		cur.setEditorKit(kit);
		menu.add(cur);
		cur = new VHGHistoryActiveAction();
		cur.putValue(Action.NAME, "History...");
		cur.setEditorKit(kit);
		menu.add(cur);
		menu.addSeparator();
		cur = new VHGRollbackAction();
		cur.putValue(Action.NAME, "Rollback...");
		cur.setEnabled(userOnline);
		cur.setEditorKit(kit);
		menu.add(cur);
		cur = new VHGRollbackAction();
		cur.putValue(Action.NAME, "Revert to...");
		cur.setEditorKit(kit);
		menu.add(cur);
		//TODO just for now the generic revert one. needs to be revert active and show history.
		cur = new VHGRevertOneRevisionAction();
		cur.putValue(Action.NAME, "Revert to...");
		cur.setEditorKit(kit);
		menu.add(cur);
		menu.addSeparator();
	}

	private void buildGeneralMenuItems(JMenu menu, VDHGOwlEditorKit kit, boolean shared, boolean userOnline) {
		ProtegeOWLAction cur;
		if (!shared) {
			cur = new VDHGShareActiveAction();
			cur.putValue(Action.NAME, "Share...");
			cur.setEnabled(userOnline);
		}  else {
			cur = new VDHGShareActiveCancelAction();
			cur.putValue(Action.NAME, "Cancel Sharing");
		}
		cur.setEditorKit(kit);
		menu.add(cur); 
		menu.addSeparator();
		cur = new VDHGCheckoutNewAction();
		cur.putValue(Action.NAME, "Checkout...");
		cur.setEditorKit(kit);
		cur.setEnabled(userOnline);
		menu.add(cur);
		menu.addSeparator();
		if (!userOnline) {
			cur = new VDHGStartNetworkingAction();
			cur.putValue(Action.NAME, "Sign in");
		}  else {
			cur = new VDHGStopNetworkingAction();
			cur.putValue(Action.NAME, "Log off");
		}
		cur.setEditorKit(kit);
		menu.add(cur);
	}
}
