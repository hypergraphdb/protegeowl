package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import org.hypergraphdb.app.owl.versioning.VersionedOntologyComparator.RevisionComparisonResult;
import org.hypergraphdb.app.owl.versioning.VersionedOntologyComparator.VersionedOntologyComparisonResult;
import org.hypergraphdb.app.owl.versioning.distributed.DistributedOntology;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.core.ui.workspace.Workspace;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * PeerViewPanel shows a list of selected peers as HGPeerIdentities and allows the user to select and entry.
 * The user can also refresh the list (search again for peers), while the dialog is open.
 *  
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 26, 2012
 */
public class CompareVersionedOntologyViewPanel extends JPanel {

    private static final long serialVersionUID = -328358981641882683L;

    private VersionedOntologyComparisonResult result;
    
    private static RevisionComparisonResult selectedResult;

    private CompareVOResultTable table; 
    
	public CompareVersionedOntologyViewPanel(VersionedOntologyComparisonResult result, DistributedOntology dOnto, HGPeerIdentity server, String userId) {
        this.result = result;
        createUI(dOnto, server, userId);
    }

    private void createUI(DistributedOntology dOnto, HGPeerIdentity server, String userId) {
        setLayout(new BorderLayout());
        String message = "<html> <h2> Compare Ontology History to Server " + userId + "</h2> "
		    +"<table width='100%' border='0'>"
		    +"<tr><td align='right'><b>Ontology:</b></td><td>"+ VDRenderer.render(dOnto) + "</td></tr>"
		    +"<tr><td align='right'><b>Server:</b></td><td>"+VDRenderer.render(server) + " " + userId + "</td></tr>"
		    +"<tr><td align='right'><b>Head:</b></td><td>"+VDRenderer.render(dOnto.getVersionedOntology().getHeadRevision()) + "(local)"+ "</td></tr>"
		    +"</table>";
		JPanel northPanel = new JPanel(new BorderLayout(5, 5));
		northPanel.add(new JLabel(message), BorderLayout.NORTH);
        table = new CompareVOResultTable(result);
        add(northPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);    
    }
    
    public void setComparisonResult(VersionedOntologyComparisonResult result) {
    	table.getCompareVOTableModel().refresh(result);
    }

    public Dimension getPreferredSize() {
        return new Dimension(850, 400);
    }

    public static void showCompareVersionedOntologyDialog(String title, Workspace ws, DistributedOntology dOnto, HGPeerIdentity server,  String userId, VersionedOntologyComparisonResult result) {
        CompareVersionedOntologyViewPanel panel = new CompareVersionedOntologyViewPanel(result, dOnto, server, userId);
        JOptionPaneEx.showConfirmDialog(ws, title, panel, JOptionPane.PLAIN_MESSAGE,  JOptionPane.OK_CANCEL_OPTION, panel.table);
        selectedResult = panel.table.getSelectedEntry();
    }
    
    public static RevisionComparisonResult getSelectedResult() {
    	return selectedResult;
    }
}