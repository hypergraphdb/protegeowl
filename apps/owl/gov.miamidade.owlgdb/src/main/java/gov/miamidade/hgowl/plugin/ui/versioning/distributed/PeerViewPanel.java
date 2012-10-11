package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.peer.HGPeerIdentity;
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
	
	public enum ViewMode { ALL_PEERS, ONTOLOGY_SERVERS };

    private static final long serialVersionUID = -328358981641882683L;

    private PeerTable table;
    
    private JButton refreshButton;
    private JCheckBox showOnlyServersCb;
    
	public PeerViewPanel(VDHGDBOntologyRepository repo, ViewMode mode) {
        createUI(repo, mode);
    }

    private void createUI(VDHGDBOntologyRepository repo, ViewMode mode) {
        setLayout(new BorderLayout());
        //String showing = "Ontology servers only";
		String message = "<html> <h2> Select Ontology Server </h2> ";
//	    +"<table width='100%' border='0'>"
//	    +"<tr><td align='right'><b>Showing:</b></td><td>"+ showing + "</td></tr>"
//	    +"</table>";
		JPanel northPanel = new JPanel(new BorderLayout(5, 5));
		northPanel.add(new JLabel(message), BorderLayout.NORTH);
        table = new PeerTable(repo, mode);
        add(northPanel, BorderLayout.NORTH);
        add(new JScrollPane(table));
        refreshButton = new JButton("Refresh Peer table");
        refreshButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				table.refresh();
			}
		});
        showOnlyServersCb = new JCheckBox("Show only Ontology Servers");
        showOnlyServersCb.setSelected(mode == ViewMode.ONTOLOGY_SERVERS);
        showOnlyServersCb.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (showOnlyServersCb.isSelected()) {
					table.peerTableModel.setViewMode(ViewMode.ONTOLOGY_SERVERS);
				} else {
					table.peerTableModel.setViewMode(ViewMode.ALL_PEERS);
				}
			}
		});
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        buttonPanel.add(showOnlyServersCb);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(900, 450);
    }

    /**
	 * @return the table
	 */
	protected PeerTable getTable() {
		return table;
	}

	public HGPeerIdentity getSelectedEntry() {
		return (HGPeerIdentity)table.getSelectedEntry();
	}
	
    private static HGPeerIdentity selectedEntry;
	
    public static int showPeerSelectionDialog(Workspace ws, VDHGDBOntologyRepository repo) {
        PeerViewPanel panel = new PeerViewPanel(repo, ViewMode.ALL_PEERS);
        int ret = JOptionPaneEx.showConfirmDialog(ws, "Please Select a remote Peer ", panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.table);
        selectedEntry = panel.getSelectedEntry();
        return ret;
    }

    /**
     * Shows a list of servers for selection.
     * @param title
     * @param ws
     * @param repo
     * @return a JOptionPane.OK_Option or CANCEL_Option, 
     */
    public static int showServerSelectionDialog(String title, Workspace ws, VDHGDBOntologyRepository repo) {
        PeerViewPanel panel = new PeerViewPanel(repo, ViewMode.ONTOLOGY_SERVERS);
        int ret = JOptionPaneEx.showConfirmDialog(ws, title, panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.table);
        selectedEntry = panel.getSelectedEntry();
        return ret;
    }

    public static int showPeerOrServerSelectionDialog(String title, Workspace ws, VDHGDBOntologyRepository repo, ViewMode mode) {
        PeerViewPanel panel = new PeerViewPanel(repo, mode);
        int ret = JOptionPaneEx.showConfirmDialog(ws, title, panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.table);
        selectedEntry = panel.getSelectedEntry();
        return ret;
    }
    
    public static HGPeerIdentity getSelectedPeer() {
    	return selectedEntry;
    }
}