package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowserRepositoryActivity.BrowseEntry;
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
public class RemoteRepositoryTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 7543477174882794254L;

    private List<BrowseEntry> entries;


    public RemoteRepositoryTableModel(List<BrowseEntry> entries) {
        this.entries = entries;
    }
    
    public int getColumnCount() {
        return 4;
    }

    public int getRowCount() {
        return entries.size();
    }

    public String getColumnName(int column) {
        if(column == 0) {
            return "OntologyIRI";
        }
        else if(column == 1) {
            return "VersionIRI";
        }
        else if(column == 2) {
            return "DocumentIRI";
        }
        else if(column == 3) {
            return "UUID";
        }
        else {
        	 throw new IllegalArgumentException();
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex == 0) {
            return entries.get(rowIndex).getOwlOntologyIRI();
        } else if(columnIndex == 1) {
            return entries.get(rowIndex).getOwlOntologyVersionIRI();
        } else if(columnIndex == 2) {
            return entries.get(rowIndex).getOwlOntologyDocumentIRI();
        } else if(columnIndex == 3) {
            return entries.get(rowIndex).getUuid();
        } else { 
            throw new IllegalArgumentException();
        }
    }

    public BrowseEntry getEntryAt(int selectedRow) {
        if(selectedRow == -1) {
            return null;
        }
        return entries.get(selectedRow);
    }
}
