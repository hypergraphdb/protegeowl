package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.core.ui.workspace.Workspace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * PeerViewPanel shows a list of selected peers as HGPeerIdentities and allows the user to select and entry.
 * The user can also refresh the list (search again for peers), while the dialog is open.
 *  
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 26, 2012
 */
public class PeerViewPanel extends JPanel {

    private static final long serialVersionUID = -328358981641882683L;

    private HyperGraphPeer peer;

    private PeerTable table;
    
    private JButton refreshButton;
    
	public PeerViewPanel(HyperGraphPeer peer) {
        this.peer = peer;
        createUI();
    }

    private void createUI() {
        setLayout(new BorderLayout());
        table = new PeerTable(peer);
        add(new JScrollPane(table));
        refreshButton = new JButton("Refresh Peer table");
        refreshButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				table.refresh();
			}
		});
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }


    public Dimension getPreferredSize() {
        return new Dimension(650, 200);
    }

    /**
	 * @return the table
	 */
	protected PeerTable getTable() {
		return table;
	}
	
    public static HGPeerIdentity showPeerSelectionDialog(Workspace ws, HyperGraphPeer peer) {
        PeerViewPanel panel = new PeerViewPanel(peer);
        int ret = JOptionPaneEx.showConfirmDialog(ws, "Please Select a remote Peer ", panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.table);
        if(ret == JOptionPane.OK_OPTION) {
            return panel.table.getSelectedEntry();
        }
        return null;
    }
}