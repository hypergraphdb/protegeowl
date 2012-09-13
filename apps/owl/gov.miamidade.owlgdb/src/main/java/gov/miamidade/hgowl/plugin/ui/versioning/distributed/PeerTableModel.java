package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.peer.HGPeerIdentity;

import gov.miamidade.hgowl.plugin.ui.versioning.distributed.PeerViewPanel.ViewMode;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * 
 * PeerTableModel.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 26, 2012
 */
public class PeerTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 7543477174882794254L;

    private VDHGDBOntologyRepository repository;

    private List<HGPeerIdentity> entries;

    private ViewMode viewMode;

	public PeerTableModel(VDHGDBOntologyRepository repository, ViewMode viewMode) {
        this.repository = repository;
        this.viewMode = viewMode;
        refresh();
    }

    /**
	 * @return the viewMode
	 */
	public ViewMode getViewMode() {
		return viewMode;
	}

	/**
	 * Sets the viewmode and refreshes the table.
	 * @param viewMode the viewMode to set
	 */
	public void setViewMode(ViewMode viewMode) {
		if (this.viewMode != viewMode) {
			this.viewMode = viewMode;
			refresh();
		}
	}

    public void refresh() {
		Set<HGPeerIdentity> visiblePeers;
    	if (viewMode.equals(ViewMode.ALL_PEERS)) {
    		visiblePeers = repository.getPeers();
    	} else if (viewMode.equals(ViewMode.ONTOLOGY_SERVERS)) {
    		visiblePeers = repository.getOntologyServers();
    	} else {
    		throw new IllegalStateException("The viewMode was not recognized: " + viewMode);
    	}
    	entries = new ArrayList<HGPeerIdentity>();
        for (HGPeerIdentity ore : visiblePeers) {
        	entries.add((HGPeerIdentity) ore);
        }
        fireTableDataChanged();
    }

    public int getColumnCount() {
        return 5;
    }

    public int getRowCount() {
        return entries.size();
    }

    public String getColumnName(int column) {
        if(column == 0) {
            return "Peername";
        }
        if(column == 1) {
            return "Hostname";
        }
        else if(column == 2) {
            return "Graph Location";
        }
        else if(column == 3) {
            return "ID";
        }
        else if(column == 4) {
            return "IP";
        }
        else {
            return "";
        }
    }


    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex == 0) {
            return repository.getPeer().getNetworkTarget(entries.get(rowIndex));
        } else if(columnIndex == 1) {
                return entries.get(rowIndex).getHostname();
        } else if(columnIndex == 2) {
            return entries.get(rowIndex).getGraphLocation();
        } else if(columnIndex == 3) {
            return entries.get(rowIndex).getId();
        } else if(columnIndex == 4) {
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
