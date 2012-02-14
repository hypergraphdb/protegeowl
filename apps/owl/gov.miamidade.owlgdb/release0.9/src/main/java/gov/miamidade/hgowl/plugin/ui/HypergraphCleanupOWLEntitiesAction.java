package gov.miamidade.hgowl.plugin.ui;

import gov.miamidade.hgowl.plugin.owl.model.HGOwlModelManagerImpl;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;

/**
 * HypergraphCleanupOWLEntitiesAction.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 11, 2011
 */
public class HypergraphCleanupOWLEntitiesAction extends ProtegeOWLAction {


	/**
	 * 
	 */
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
		HGOwlModelManagerImpl mm = (HGOwlModelManagerImpl) this.getOWLModelManager();
		HGDBOntologyManager om =  (HGDBOntologyManager ) mm.getOWLOntologyManager();
		int cleanUpNr = om.getOntologyRepository().cleanUpOwlEntities();
		System.out.println("Cleaned up OwlEntities: " + cleanUpNr);
		JOptionPane.showMessageDialog(getOWLWorkspace(), "Cleaned up OwlEntities: " + cleanUpNr);
	}
}
