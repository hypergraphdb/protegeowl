package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import gov.miamidade.hgowl.plugin.owl.VDHGOwlEditorKit;
import gov.miamidade.hgowl.plugin.owl.VHGOwlEditorKit;

import java.awt.event.ActionEvent;

import org.protege.editor.owl.ui.action.ProtegeOWLAction;

/**
 * VHGAddActiveToVersionControlAction.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 11, 2011
 */
public class VDHGPushActiveAction extends ProtegeOWLAction {

	private static final long serialVersionUID = -2085444668481360102L;

	
	/* (non-Javadoc)
	 * @see org.protege.editor.core.plugin.ProtegePluginInstance#initialise()
	 */
	@Override
	public void initialise() throws Exception {
	}


	/* (non-Javadoc)
	 * @see org.protege.editor.core.Disposable#dispose()
	 */
	@Override
	public void dispose() throws Exception {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		VDHGOwlEditorKit vdhgowlKit = (VDHGOwlEditorKit) getEditorKit();
		try {
			vdhgowlKit.handlePushActiveRequest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
