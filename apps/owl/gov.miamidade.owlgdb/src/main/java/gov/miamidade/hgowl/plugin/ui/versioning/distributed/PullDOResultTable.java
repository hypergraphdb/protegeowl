package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import org.hypergraphdb.app.owl.versioning.VersionedOntologyComparator;
import org.hypergraphdb.app.owl.versioning.VersionedOntologyComparator.VersionedOntologyComparisonResult;
import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowseRepositoryActivity.BrowseEntry;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Author: Matthew Horridge<br> The University Of Manchester<br> Information Management Group<br> Date:
 * 18-Oct-2008<br><br>
 */
public class PullDOResultTable extends JTable {
	
	PullDOTableModel  pullVOTableModel;
	/**
     * 
     */
    private static final long serialVersionUID = 343836249221539974L;

    public PullDOResultTable(VersionedOntologyComparisonResult result) {
    	pullVOTableModel = new PullDOTableModel(result); 
        setModel(pullVOTableModel);
        //setRowHeight(getRowHeight() + 4);
        setShowHorizontalLines(true);
        setGridColor(Color.blue);
        getColumnModel().getColumn(0).setMaxWidth(50);
        getColumnModel().getColumn(3).setMaxWidth(90);
        getColumnModel().getColumn(4).setMaxWidth(50);
//        getTableHeader().addMouseListener(new MouseAdapter() {
//            public void mouseReleased(MouseEvent e) {
//                sort(e);
//            }
//        }
    }
    
    public PullDOTableModel getPullVOTableModel() {
    	return pullVOTableModel;
    }
    
    public VersionedOntologyComparator.RevisionComparisonResult getSelectedEntry() {
    	if (getSelectedRow() >=0) {
    		return ((CompareVOTableModel)getModel()).getEntryAt(getSelectedRow());
    	} else {
    		return null;
    	}
    }
    
    public Component prepareRenderer(TableCellRenderer renderer,int row, int col) {
    	Component comp = super.prepareRenderer(renderer, row, col);
    	JComponent jcomp = (JComponent)comp;
    	if (comp == jcomp) {
    		String tooltipText = pullVOTableModel.getTooltipAt(row,col);
    		jcomp.setToolTipText(tooltipText);
    		jcomp.setBackground(pullVOTableModel.getCellBgColorAt(row, col));
    	}	
    	return comp;
    }
}
