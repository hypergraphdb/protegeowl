package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import java.awt.Color;
import javax.swing.JTable;

/**
 * Author: Matthew Horridge<br> The University Of Manchester<br> Information Management Group<br> Date:
 * 18-Oct-2008<br><br>
 */
public class PeerTable extends JTable {
	
	PeerTableModel peerTableModel;
	/**
     * 
     */
    private static final long serialVersionUID = 343836249221539974L;

    public PeerTable(HyperGraphPeer peer) {
    	peerTableModel = new PeerTableModel(peer); 
        setModel(peerTableModel);
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

    public void refresh() {
    	peerTableModel.refresh();
    }
    
    public HGPeerIdentity getSelectedEntry() {
        return ((PeerTableModel) getModel()).getEntryAt(getSelectedRow());
    }
}
