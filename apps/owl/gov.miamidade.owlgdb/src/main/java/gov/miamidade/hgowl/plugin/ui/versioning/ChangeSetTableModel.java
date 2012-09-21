package gov.miamidade.hgowl.plugin.ui.versioning;

import java.util.SortedSet;

import gov.miamidade.hgowl.plugin.ui.versioning.ChangeSetTable.ChangeDisplayMode;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.VDRenderer;

import javax.swing.table.AbstractTableModel;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChangeFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * ChangeSetTableModel.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 19, 2012
 */
public class ChangeSetTableModel extends AbstractTableModel {
	//Col Number, Change, Functional
	
	private static final long serialVersionUID = -6611034383139157644L;

	private ChangeSet changeSet;
	private SortedSet<Integer> conflictIndices;
	private OWLOntology onto;
	private HyperGraph graph;
	private ChangeDisplayMode mode; 
	
	public static final int CHANGENR_COLUMN_INDEX = 0;
	public static final int CHANGE_COLUMN_INDEX = 1;

	public ChangeSetTableModel(OWLOntology onto, HyperGraph graph) {
		changeSet = null;
		this.onto = onto;
		this.graph = graph;
	}
	
	/**
	 * @return the mode
	 */
	protected ChangeDisplayMode getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	protected void setMode(ChangeDisplayMode mode) {
		this.mode = mode;
        fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		if (changeSet != null) {
			return changeSet.size();
		} else {
			return 1;
		}
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

    public void refresh(ChangeSet changeSet, SortedSet<Integer> conflictIndices) {
    	this.changeSet = changeSet;
    	this.conflictIndices = conflictIndices;
        fireTableDataChanged();
    }

    public String getColumnName(int column) {
        if(column == 0) {
            return "Nr";
        } else if(column == 1) {
            return "Change";
        } else {
            return "";
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
    	if (changeSet != null) {
	    	int changeIndex = changeSet.getArity() - rowIndex - 1;
	    	VOWLChange voc = changeSet.getChangeAt(changeIndex);
	    	OWLOntologyChange oc = VOWLChangeFactory.create(voc, onto, graph);
	        if(columnIndex == 0) {
	            return VDRenderer.render(changeIndex);
	        } else if(columnIndex == 1) {
	        	if (mode.equals(ChangeDisplayMode.OWL)) {
		            return oc;
	        	} else {
		            return oc.toString();
	        	}
	        } else { 
	            throw new IllegalArgumentException();
	        }
    	} else {
    		return getValueAtNoChangeSet(rowIndex, columnIndex);
    	}
    }

    public Object getValueAtNoChangeSet(int rowIndex, int columnIndex) {
        if(columnIndex == 0) {
            return "";
        } else if(columnIndex == 1) {
            return "No changes to display";
        } else { 
            throw new IllegalArgumentException();
        }
    }
    
    public String getTooltipAt(int rowIndex, int columnIndex) {
    	if (changeSet != null && conflictIndices != null) {
	    	int changeIndex = changeSet.getArity() - rowIndex - 1;
	    	if (conflictIndices.contains(changeIndex)) {
	    		return "This pending change conflicts with an earlier change and will be removed when you commit.";
	    	} else {
	    		return null;
	    	}
    	} else {
    		return null;
    	}
    }

    public boolean isConflict(int rowIndex, int columnIndex) {
    	if (changeSet != null && conflictIndices != null) {
	    	int changeIndex = changeSet.getArity() - rowIndex - 1;
	    	return (conflictIndices.contains(changeIndex));
    	} else {
    		return false;
    	}
    }
}
