package gov.miamidade.hgowl.plugin.ui.versioning;

import gov.miamidade.hgowl.plugin.owl.VDHGOwlEditorKit;

import gov.miamidade.hgowl.plugin.owl.VHGOwlEditorKit;
import static gov.miamidade.hgowl.plugin.Singles.*;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
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
	public void initialise() throws Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() throws Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub
		System.out.println("Woo hoo actionPerformed.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.protege.editor.core.ui.action.ProtegeDynamicAction#rebuildChildMenuItems
	 * (javax.swing.JMenu)
	 */
	@Override
	public void rebuildChildMenuItems(JMenu menu)
	{
		EditorKit ekit = getEditorKit();
		if (!(ekit instanceof VHGOwlEditorKit))
		{
			System.err.println("Not running with a versioned VHGOwlEditorKit or subclass. Versioning menu will be empty.");
			return;
		}
		VHGOwlEditorKit vKit = (VHGOwlEditorKit) ekit;
		boolean activeVersioned = vKit.isActiveOntologyVersioned();
		boolean activeShared;
		if (vKit instanceof VDHGOwlEditorKit)
		{
			VDHGOwlEditorKit vdKit = (VDHGOwlEditorKit) vKit;
			activeShared = vdKit.isActiveOntologyShared();
		}
		else
		{
			activeShared = false;
		}
		if (!activeVersioned)
		{
			buildNotVersionedMenu(menu, vKit);
		}
		else if (activeVersioned && !activeShared)
		{
			buildVersionedMenu(menu, vKit);
		}
		else if (activeShared)
		{
			buildSharedMenu(menu, vKit);
		}
		else
		{
			System.err.println("DynamicVersioningMenu: Cannot build menu. unknown state detected: actShared: " + activeShared
					+ " actVersioned: " + activeVersioned);
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

	private void buildVersionedMenu(JMenu menu, VHGOwlEditorKit kit)
	{
		// Commit
		// History...
		// Separator
		// Rollback...
		// Revert to...
		// Separator
		// Remove from Local Version Control...
		ProtegeOWLAction cur;
		cur = new VHGCommitActiveAction();
		cur.putValue(Action.NAME, "Commit");
		cur.setEditorKit(kit);
		menu.add(cur);
		cur = new VHGHistoryActiveAction();
		cur.putValue(Action.NAME, "History...");
		cur.setEditorKit(kit);
		menu.add(cur);
		menu.addSeparator();
		cur = new VHGRollbackActiveAction();
		cur.putValue(Action.NAME, bundles().value("undoworking.action.title"));
		cur.setEditorKit(kit);
		menu.add(cur);
		cur = new VHGRevertActiveAction();
		cur.putValue(Action.NAME, "Revert to...");
		cur.setEditorKit(kit);
		menu.add(cur);
		menu.addSeparator();
		cur = new VHGRemoveActiveFromVersionControlAction();
		cur.putValue(Action.NAME, "Remove from Local Version Control...");
		cur.setEditorKit(kit);
		menu.add(cur);
	}

	private void buildSharedMenu(JMenu menu, VHGOwlEditorKit kit)
	{
		menu.add(new AbstractAction("Ontology shared. Use Team menu.")
		{
			private static final long serialVersionUID = -5125693951180514232L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
			}
		});
	}
}