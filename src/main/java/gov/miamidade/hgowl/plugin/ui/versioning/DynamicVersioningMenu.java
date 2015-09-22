package gov.miamidade.hgowl.plugin.ui.versioning;

import gov.miamidade.hgowl.plugin.owl.VDHGOwlEditorKit;
import gov.miamidade.hgowl.plugin.owl.VHGOwlEditorKit;
import gov.miamidade.hgowl.plugin.ui.HGPluginAction;
import static gov.miamidade.hgowl.plugin.Singles.*;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JMenu;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.ui.action.ProtegeDynamicAction;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;

/**
 * DynamicVersioningMenu.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Aug 21, 2012
 */
public class DynamicVersioningMenu extends ProtegeDynamicAction
{
	private static final long serialVersionUID = -2187253639033356483L;

	@Override
	public void initialise() throws Exception { }

	@Override
	public void dispose() throws Exception { }

	@Override
	public void actionPerformed(ActionEvent e) { }

	private JMenu addItem(JMenu menu, ProtegeOWLAction action, String name, boolean enabled)
	{
		action.putValue(Action.NAME, name);
		action.setEnabled(enabled);
		action.setEditorKit(getEditorKit());
		menu.add(action);
		return menu;
	}
	
	@Override
	public void rebuildChildMenuItems(JMenu menu)
	{
		EditorKit ekit = getEditorKit();
		if (!(ekit instanceof VHGOwlEditorKit))
		{
			System.err.println("Not running with a versioned VHGOwlEditorKit or subclass. Versioning menu will be empty.");
			return;
		}
		VDHGOwlEditorKit vKit = (VDHGOwlEditorKit) ekit;
		boolean activeVersioned = vKit.isActiveOntologyVersioned();
		if (!activeVersioned)
		{
			buildNotVersionedMenu(menu, vKit);
		}
		else
		{
			buildVersionedMenu(menu, vKit);
		}
	}

	private void buildNotVersionedMenu(JMenu menu, VHGOwlEditorKit kit)
	{
		ProtegeOWLAction cur;
		cur = new VHGAddActiveToVersionControlAction();
		cur.putValue(Action.NAME, "Add to Local Version Control...");
		cur.setEditorKit(kit);
		menu.add(cur);
	}

	@SuppressWarnings("serial")
	private void buildVersionedMenu(final JMenu menu, final VDHGOwlEditorKit kit)
	{
		boolean userOnline = kit.isNetworking();
		addItem(menu,
				new HGPluginAction() { public void act() { kit.pullActive(); } },
				"Pull",
				userOnline);			
		addItem(menu,
				new HGPluginAction() { public void act() { kit.commitActiveOntology(); } },
				"Commit",
				userOnline);
		addItem(menu,
				new HGPluginAction() { public void act() { kit.pushActive(); } },
				"Push",
				userOnline);			
		addItem(menu,
				new HGPluginAction() { public void act() { kit.showHistoryActive(); } },
				"History",
				true);
		menu.addSeparator();
		addItem(menu,
				new HGPluginAction() { public void act() { kit.undoLocalChanges(); } },
				bundles().value("undoworking.action.title"),
				true);
		addItem(menu,
				new HGPluginAction() { public void act() { kit.revertActive(); } },
				"Revert to...",
				true);
		menu.addSeparator();
		addItem(menu,
				new HGPluginAction() { public void act() { kit.stopVersioningActive(); } },
				"Stop Versioning",
				true);
		menu.addSeparator();
		addItem(menu,
				new HGPluginAction() { public void act() { kit.publishActive(); } },
				"Publish",
				userOnline);				
	}
}