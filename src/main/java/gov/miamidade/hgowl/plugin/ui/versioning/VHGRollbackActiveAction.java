package gov.miamidade.hgowl.plugin.ui.versioning;

import gov.miamidade.hgowl.plugin.owl.VHGOwlEditorKit;

import java.awt.event.ActionEvent;

import org.protege.editor.owl.ui.action.ProtegeOWLAction;

/**
 * VHGAddActiveToVersionControlAction.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 11, 2011
 */
public class VHGRollbackActiveAction extends ProtegeOWLAction
{
	private static final long serialVersionUID = -2085444668481360102L;

	@Override
	public void initialise() throws Exception
	{

	}

	@Override
	public void dispose() throws Exception
	{

	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		VHGOwlEditorKit vhgowlKit = (VHGOwlEditorKit) getEditorKit();
		try
		{
			vhgowlKit.undoLocalChanges(); // handleRollbackActiveRequest();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
