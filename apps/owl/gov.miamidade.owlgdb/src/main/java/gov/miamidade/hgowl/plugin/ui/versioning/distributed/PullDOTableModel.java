package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntologyComparator.RevisionCompareOutcome;
import org.hypergraphdb.app.owl.versioning.VersionedOntologyComparator.RevisionComparisonResult;
import org.hypergraphdb.app.owl.versioning.VersionedOntologyComparator.VersionedOntologyComparisonResult;

import gov.miamidade.hgowl.plugin.ui.versioning.distributed.PeerViewPanel.ViewMode;

import javax.swing.table.AbstractTableModel;

import java.awt.Color;
import java.text.DateFormat;
import java.util.List;

/**
 * CompareVOTableModel.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 11, 2012
 */
public class PullDOTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 7543477174882345254L;

    private VersionedOntologyComparisonResult comparisonResult;
    private int lastSelectedTargetOnlyResultIndex = -1; //Last
    
    private boolean allowTargetRevisionSelection = true;
    
	public PullDOTableModel(VersionedOntologyComparisonResult comparisonResult) {
        refresh(comparisonResult);
    }

    public void refresh(VersionedOntologyComparisonResult comparisonResult) {
    	this.comparisonResult = comparisonResult;
    	lastSelectedTargetOnlyResultIndex = comparisonResult.getRevisionResults().size() - 1;
        fireTableDataChanged();
    }

    public int getColumnCount() {
        return 5;
    }

    /* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (allowTargetRevisionSelection) {
	    	int resultIndex = rowIndexToResultIndex(rowIndex);
	    	return (resultIndex > comparisonResult.getLastMatchingRevisionIndex());
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    	int resultIndex = rowIndexToResultIndex(rowIndex);
		if (Boolean.TRUE.equals(aValue)) {
			lastSelectedTargetOnlyResultIndex = resultIndex;
		} else {
			if (resultIndex > 0 ) {
				lastSelectedTargetOnlyResultIndex = resultIndex - 1;
			} else {
				lastSelectedTargetOnlyResultIndex = -1;
			}
		}
		fireTableDataChanged();
	}

	public int getRowCount() {
        return comparisonResult.getRevisionResults().size();
    }
	
	private int rowIndexToResultIndex(int rowIndex) {
		if (rowIndex < 0) throw new IllegalArgumentException("Rowindex < 0" + rowIndex);
    	return comparisonResult.getRevisionResults().size() - rowIndex - 1;
	}

    public String getColumnName(int column) {
        if(column == 0) {
            return "Revision";
        } else if(column == 1) {
            return "Local";
        } else if(column == 2) {
            return "Remote";
        } else if(column == 3) {
            return "Compare";
        } else if(column == 4) {
            return "Selected";
        }
        else {
            return "";
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
    	List<RevisionComparisonResult> results = comparisonResult.getRevisionResults();
    	int resultIndex = rowIndexToResultIndex(rowIndex);
    	RevisionComparisonResult result = results.get(resultIndex);
        if(columnIndex == 0) {
            return result.getSource() == null? result.getTarget().getRevision() : ( result.getSource().getRevision() );
        } else if(columnIndex == 1) {
            return renderRevision(result.getSource());
        } else if(columnIndex == 2) {
            return renderRevision(result.getTarget());
        } else if(columnIndex == 3) {
            return result.getOutcome();
        } else if(columnIndex == 4) {
            if (result.getOutcome() == RevisionCompareOutcome.TARGETONLY) 
            	if (resultIndex <= lastSelectedTargetOnlyResultIndex) {
            		return Boolean.TRUE;
            	} else {
            		return Boolean.FALSE;
            } else {
            	return Boolean.FALSE;
            }
        } else { 
            throw new IllegalArgumentException();
        }
    }

    public String getTooltipAt(int rowIndex, int columnIndex) {
    	List<RevisionComparisonResult> results = comparisonResult.getRevisionResults();
    	int resultIndex = rowIndexToResultIndex(rowIndex);
    	RevisionComparisonResult result = results.get(resultIndex);
        if(columnIndex == 0) {
        	return null;
        } else if(columnIndex == 1) {
            return result.getSource() == null? null : result.getSource().getRevisionComment();
        } else if(columnIndex == 2) {
            return result.getTarget() == null? "" : result.getTarget().getRevisionComment();
        } else if(columnIndex == 3) {
        	String tooltipString;
        	if (result.getOutcome() == RevisionCompareOutcome.CONFLICT) {
        		tooltipString = "The source revision does not match the target revision. Revert locally.";
        	} else if (result.getOutcome() == RevisionCompareOutcome.MATCH) {
        		tooltipString = "The source revision is equal to the target revision.";
        	} else if (result.getOutcome() == RevisionCompareOutcome.SOURCEONLY) {
        		tooltipString = "The source revision needs to be pushed to the server.";
        	} else if (result.getOutcome() == RevisionCompareOutcome.TARGETONLY) {
        		tooltipString = "The target revision should be pulled.";
        	} else {
        		throw new IllegalArgumentException("Unknown Outcome");
        	}
        	return tooltipString;
        } else if(columnIndex == 4) {
            return null;
        } else {
        	throw new IllegalArgumentException();
        }
    }
	private DateFormat dateF = DateFormat.getDateTimeInstance();

	/**
	 * 
	 * @param r null permitted, returns "".
	 * @return
	 */
    public String renderRevision(Revision r) {
    	if (r == null) return "";
    	return dateF.format(r.getTimeStamp()) + " by " + r.getUser();
    }

    public Color getCellBgColorAt(int rowIndex, int columnIndex) {
    	List<RevisionComparisonResult> results = comparisonResult.getRevisionResults();
    	int resultIndex = rowIndexToResultIndex(rowIndex);
    	RevisionComparisonResult result = results.get(resultIndex);
    	if (result.getOutcome() == RevisionCompareOutcome.CONFLICT) {
    		return  Color.RED;
    	} else if (result.getOutcome() == RevisionCompareOutcome.MATCH) {
    		return  Color.WHITE;
    	} else if (result.getOutcome() == RevisionCompareOutcome.SOURCEONLY) {
    		if (columnIndex == 1) {
    			return  Color.GREEN.brighter().brighter();
    		} else {
    			return  Color.WHITE;
    		}
    	} else if (result.getOutcome() == RevisionCompareOutcome.TARGETONLY) {
   			return  Color.GREEN.brighter().brighter();
    	} else {
    		throw new IllegalArgumentException("Unknown Outcome");
    	}
    }

    public Class getColumnClass(int c) {
    	if (c == 4) return Boolean.class; 
    	else return super.getClass();
    }
    
    public RevisionComparisonResult getEntryAt(int selectedRow) {
    	if (selectedRow < 0) return null;
    	int resultIndex = rowIndexToResultIndex(selectedRow);
        if(resultIndex < 0) {
            return null;
        }
        return comparisonResult.getRevisionResults().get(resultIndex);
    }
    
    public Revision getLastPullTargetRevision() {
    	if (lastSelectedTargetOnlyResultIndex > 0 && lastSelectedTargetOnlyResultIndex > comparisonResult.getLastMatchingRevisionIndex()) {
    		return comparisonResult.getRevisionResults().get(lastSelectedTargetOnlyResultIndex).getTarget();
    	} else {
    		return null;
    	}
    }
}
