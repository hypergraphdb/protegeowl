package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;

/**
 * 
 * PeerTableModel.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 26, 2012
 */
public class PeerTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 7543477174882794254L;

    private HyperGraphPeer peer;

    private List<HGPeerIdentity> entries;


    public PeerTableModel(HyperGraphPeer peer) {
        this.peer = peer;
        refresh();
    }
    
    public void refresh() {
        entries = new ArrayList<HGPeerIdentity>();
        for (HGPeerIdentity ore : peer.getConnectedPeers()) {
        	entries.add((HGPeerIdentity) ore);
        }
        fireTableDataChanged();
    }

    public int getColumnCount() {
        return 4;
    }

    public int getRowCount() {
        return entries.size();
    }

    public String getColumnName(int column) {
        if(column == 0) {
            return "Hostname";
        }
        else if(column == 1) {
            return "Graph Location";
        }
        else if(column == 2) {
            return "ID";
        }
        else if(column == 3) {
            return "IP";
        }
        else {
            return "";
        }
    }


    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex == 0) {
            return entries.get(rowIndex).getHostname();
        } else if(columnIndex == 1) {
            return entries.get(rowIndex).getGraphLocation();
        } else if(columnIndex == 2) {
            return entries.get(rowIndex).getId();
        } else if(columnIndex == 3) {
            return entries.get(rowIndex).getIpAddress();
        } else { 
            throw new IllegalArgumentException();
        }
    }

    public HGPeerIdentity getEntryAt(int selectedRow) {
        if(selectedRow == -1) {
            return null;
        }
        return entries.get(selectedRow);
    }
}
