package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import gov.miamidade.hgowl.plugin.owl.VDHGOwlEditorKit;

import java.awt.event.ActionEvent;

import org.protege.editor.owl.ui.action.ProtegeOWLAction;

/**
 * VDHGCommitActionCentralClient.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Aug 24, 2012
 */
public class VDHGCommitActionCentralClient extends ProtegeOWLAction {

	private static final long serialVersionUID = -40882439570321730L;

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
		VDHGOwlEditorKit vdhgowlKit = (VDHGOwlEditorKit) getEditorKit();
		try {
			vdhgowlKit.handleCommitAndPushActiveClientOntologyRequest();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}