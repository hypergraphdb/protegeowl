package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntologyComparator.RevisionComparisonResult;
import org.hypergraphdb.app.owl.versioning.VersionedOntologyComparator.VersionedOntologyComparisonResult;
import org.hypergraphdb.app.owl.versioning.distributed.DistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowseRepositoryActivity.BrowseEntry;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.core.ui.workspace.Workspace;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

/**
 * PeerViewPanel shows a list of selected peers as HGPeerIdentities and allows the user to select and entry.
 * The user can also refresh the list (search again for peers), while the dialog is open.
 *  
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 26, 2012
 */
public class PullDistributedOntologyViewPanel extends JPanel {

    private static final long serialVersionUID = -328358981641882683L;

    private VersionedOntologyComparisonResult result;
    
    private static Revision lastPullRevision;

    private PullDOResultTable table; 
    
	public PullDistributedOntologyViewPanel(DistributedOntology dOnto, VersionedOntologyComparisonResult result, HGPeerIdentity server, String userName) {
        this.result = result;
        createUI(dOnto, server, userName);
    }

    private void createUI(DistributedOntology dOnto, HGPeerIdentity server, String userName) {
        setLayout(new BorderLayout());
        table = new PullDOResultTable(result);
		String message = "<html> <h2> Update Ontology changes from the server </h2> "
			//+ "Ontology: " + VDRenderer.render(dOnto) + " <br>"
			//+" Server: " + VDRenderer.render(server) + " <br>"
		    +"<table width='100%' border='0'>"
		    +"<tr><td align='right'><b>Ontology:</b></td><td>"+ VDRenderer.render(dOnto) + "</td></tr>"
		    +"<tr><td align='right'><b>Server:</b></td><td>"+VDRenderer.render(server) + " " + userName + "</td></tr>"
		    +"<tr><td align='right'><b>Head:</b></td><td>"+VDRenderer.render(dOnto.getVersionedOntology().getHeadRevision()) + "(local)"+ "</td></tr>"
		    +"</table>";
		
		JPanel northPanel = new JPanel(new BorderLayout(5, 5));
		northPanel.add(new JLabel(message), BorderLayout.NORTH);
		add(northPanel, BorderLayout.NORTH);
        add(new JScrollPane(table));
        add(new JLabel("<html><b>Each green row represents a revision that will be received from the server (remote) to update your local ontology.</b><br\\> "
        		+ "Deselecting revisions will only perform update up to the last revision that remains selected. </html>"), BorderLayout.SOUTH);
    }
    
    public void setComparisonResult(VersionedOntologyComparisonResult result) {
    	table.getPullVOTableModel().refresh(result);
    }

    public Dimension getPreferredSize() {
        return new Dimension(800, 450);
    }

    public static int showUpdateVersionedOntologyDialog(Workspace ws, VersionedOntologyComparisonResult result, DistributedOntology donto, String userId, HGPeerIdentity server) {
        PullDistributedOntologyViewPanel panel = new PullDistributedOntologyViewPanel(donto, result, server, userId);
        int ret = JOptionPaneEx.showConfirmDialog(ws, "Team - Update " + donto.getWorkingSetData().getOntologyID() + " from " + userId, panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.table);
        lastPullRevision = panel.table.getPullVOTableModel().getLastPullTargetRevision();
        return ret;
    }
    
    public static Revision getLastPullRevision() {
    	return lastPullRevision;
    }
}