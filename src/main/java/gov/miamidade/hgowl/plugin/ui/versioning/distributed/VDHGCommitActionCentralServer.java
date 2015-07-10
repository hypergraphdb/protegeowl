package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import gov.miamidade.hgowl.plugin.owl.VHGOwlEditorKit;

import java.awt.event.ActionEvent;

import org.protege.editor.owl.ui.action.ProtegeOWLAction;

/**
 * VDHGCommitActionCentralServer.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Aug 24, 2012
 */
public class VDHGCommitActionCentralServer extends ProtegeOWLAction
{

	private static final long serialVersionUID = 4954340319870779130L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.protege.editor.core.plugin.ProtegePluginInstance#initialise()
	 */
	@Override
	public void initialise() throws Exception
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.protege.editor.core.Disposable#dispose()
	 */
	@Override
	public void dispose() throws Exception
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		VHGOwlEditorKit vhgowlKit = (VHGOwlEditorKit) getEditorKit();
		try
		{
			vhgowlKit.commitActiveOntology();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}