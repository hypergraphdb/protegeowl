package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowserRepositoryActivity.BrowseEntry;
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
public class RemoteRepositoryViewPanel extends JPanel {

    private static final long serialVersionUID = -328358981641882683L;

    private List<BrowseEntry> entries;

    private RemoteRepositoryTable table;
    
	public RemoteRepositoryViewPanel(List<BrowseEntry> entries) {
        this.entries = entries;
        createUI();
    }

    private void createUI() {
        setLayout(new BorderLayout());
        table = new RemoteRepositoryTable(entries);
        add(new JScrollPane(table));
    }

    public Dimension getPreferredSize() {
        return new Dimension(1000, 400);
    }

    public static BrowseEntry showBrowseEntrySelectionDialog(Workspace ws, HGPeerIdentity remotePeer, java.util.List<BrowseEntry> entries) {
        RemoteRepositoryViewPanel panel = new RemoteRepositoryViewPanel(entries);
        int ret = JOptionPaneEx.showConfirmDialog(ws, "Ontology Selection From Remote Peer " + remotePeer.toString(), panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.table);
        if(ret == JOptionPane.OK_OPTION) {
            return panel.table.getSelectedEntry();
        }
        return null;
    }
}