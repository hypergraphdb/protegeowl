package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import gov.miamidade.hgowl.plugin.owl.VDHGOwlEditorKit;

import java.awt.event.ActionEvent;

import org.protege.editor.owl.ui.action.ProtegeOWLAction;

/**
 * VDHGStartNetworkingAction.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 11, 2012
 */
public class VDHGStartNetworkingAction extends ProtegeOWLAction
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
		VDHGOwlEditorKit vdhgowlKit = (VDHGOwlEditorKit) getEditorKit();
		try
		{
			vdhgowlKit.handleStartNetworkingRequest();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
