package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowserRepositoryActivity.BrowseEntry;
import java.awt.Color;
import java.util.List;

import javax.swing.JTable;

/**
 * Author: Matthew Horridge<br> The University Of Manchester<br> Information Management Group<br> Date:
 * 18-Oct-2008<br><br>
 */
public class RemoteRepositoryTable extends JTable {
	
	RemoteRepositoryTableModel  remoteRepoTableModel;
	/**
     * 
     */
    private static final long serialVersionUID = 343836249221539974L;

    public RemoteRepositoryTable(List<BrowseEntry> entries) {
    	remoteRepoTableModel = new RemoteRepositoryTableModel(entries); 
        setModel(remoteRepoTableModel);
        //setRowHeight(getRowHeight() + 4);
        setShowHorizontalLines(true);
        setGridColor(Color.blue);
        //getColumnModel().getColumn(0).setPreferredWidth(100);
//        getTableHeader().addMouseListener(new MouseAdapter() {
//            public void mouseReleased(MouseEvent e) {
//                sort(e);
//            }
//        }
    }
    
    public BrowseEntry getSelectedEntry() {
        return ((RemoteRepositoryTableModel) getModel()).getEntryAt(getSelectedRow());
    }
}
