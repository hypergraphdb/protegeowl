package gov.miamidade.hgowl.plugin.ui;

import java.awt.event.ActionEvent;

import org.protege.editor.owl.ui.action.ProtegeOWLAction;

public abstract class HGPluginAction extends ProtegeOWLAction
{
	private static final long serialVersionUID = 1L;

	@Override
	public void initialise() throws Exception	{	}
	@Override
	public void dispose() throws Exception { }
	
	protected abstract void act();
	
	public void actionPerformed(ActionEvent ev) 
	{
		try 
		{
			act();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}		
	}	
}