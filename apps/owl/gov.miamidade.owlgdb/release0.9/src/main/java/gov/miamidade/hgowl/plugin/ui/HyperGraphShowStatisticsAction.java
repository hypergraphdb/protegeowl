package gov.miamidade.hgowl.plugin.ui;

import gov.miamidade.hgowl.plugin.owl.model.HGOwlModelManagerImpl;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;

import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;

/**
 * HyperGraphShowStatisticsAcion.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Dec 9, 2011
 */
public class HyperGraphShowStatisticsAction extends ProtegeOWLAction {

	private static final long serialVersionUID = 4520686945901107368L;

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
		StringWriter stringWriter = new StringWriter(400);
		PrintWriter s = new PrintWriter(stringWriter);
		om.getOntologyRepository().printStatistics(s);
		s.flush();
		String message = stringWriter.toString();
		JTextPane textPane = new JTextPane();
		//textPane.setSize(500,600);
		textPane.setText(message);
		
		JOptionPaneEx.showConfirmDialog(getWorkspace(),
				"Hypergraph Statistics", 
				textPane, JOptionPane.INFORMATION_MESSAGE, 
				JOptionPane.OK_CANCEL_OPTION, 
				textPane);
	}
}
