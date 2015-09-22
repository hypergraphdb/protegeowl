package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import gov.miamidade.hgowl.plugin.owl.VDHGOwlEditorKit;
import gov.miamidade.hgowl.plugin.ui.HGPluginAction;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JMenu;

import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.ui.action.ProtegeDynamicAction;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;

/**
 * DynamicVersioningMenu.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Aug 21, 2012
 */
public class DynamicTeamMenu extends ProtegeDynamicAction 
{	
	private static final long serialVersionUID = -2187253639033356483L;

	@Override
	public void initialise() throws Exception {	}

	@Override
	public void dispose() throws Exception { }

	@Override
	public void actionPerformed(ActionEvent e)	{ }

	@Override
	public void rebuildChildMenuItems(JMenu menu) 
	{
		EditorKit ekit = getEditorKit();
		if (!(ekit instanceof VDHGOwlEditorKit)) {
			System.err.println("Not running with a versioned distributed VDHGOwlEditorKit. DynamicVersioningMenu will be empty.");
			return;
		}
		VDHGOwlEditorKit vdKit = (VDHGOwlEditorKit) ekit;
		boolean userOnline = vdKit.isNetworking();
		buildGeneralMenuItems(menu, vdKit, false, userOnline);
	}

	private JMenu addItem(JMenu menu, ProtegeOWLAction action, String name, boolean enabled)
	{
		action.putValue(Action.NAME, name);
		action.setEnabled(enabled);
		action.setEditorKit(getEditorKit());
		menu.add(action);
		return menu;
	}
	
	@SuppressWarnings("serial")
	private void buildGeneralMenuItems(final JMenu menu, 
									   final VDHGOwlEditorKit kit, 
									   final boolean shared, 
									   final boolean userOnline) 
	{			
		addItem(menu,
				new HGPluginAction() { public void act() { kit.cloneOntology(); } },
				"Clone",
				userOnline);			
		menu.addSeparator();
		
		if (!shared) 
		{
		}		
//		else 
//		{
//			cur = new VDHGShareActiveCancelAction();
//			cur.putValue(Action.NAME, "Cancel Sharing");
//		}
		else
		{

		}
		ProtegeOWLAction cur;		
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
