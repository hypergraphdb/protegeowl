package gov.miamidade.hgowl.plugin.ui.repository;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;

/**
 * VOntologyTableModel.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 30, 2012
 */
public class VOntologyTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -218142221144608713L;
	
	VersionedOntology versionedOntology;
	List<ChangeSet> changesets;
	List<Revision> revisions;
	
	
	public VOntologyTableModel(VersionedOntology vo) {
		this.versionedOntology = vo;
		refresh();
		//Master / Revision / Timestamp / user / #ofTotalChanges
	}
	
	public void refresh() {
		revisions = versionedOntology.getRevisions();
		changesets = versionedOntology.getChangeSets();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return 6;
	}
	
    public String getColumnName(int column) {
    	String returnValue;
    	switch (column) {
    	case 0: {
    		returnValue = "Master";
    	}; break;
    	case 1: {
    		returnValue = "Revision";
    	}; break;
    	case 2: {
    		returnValue = "Time Stamp";
    	}; break;
    	case 3: {
    		returnValue = "User";
    	}; break;
    	case 4: {
    		returnValue = "Comment";
    	}; break;
    	case 5: {
    		returnValue = "#Changes";
    	}; break;
    	default: {
    		returnValue = "UNKNOWN COL: " + column;
    	}; break;
    	}
    	return returnValue;
    }

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		//1 added for pending head changes
		return versionedOntology.getNrOfRevisions() + 1;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		int revisionIndex = revisions.size() - rowIndex; // - 1;
		if (revisionIndex == revisions.size()) {
			return getValueForUncommittedAt(columnIndex);
		} else if (revisionIndex > 0){
			return getValueForCommittedAt(revisionIndex, columnIndex);
		} else if (revisionIndex == 0){
			//Initial
			return getValueForInitialAt(columnIndex);
		} else {
			throw new IllegalArgumentException("Row with rowindex " + rowIndex + " does not exist.");
		}
	}

	public Object getValueForCommittedAt(int revisionIndex, int columnIndex) {
		if (revisionIndex < 1 || revisionIndex >= revisions.size()) {
			throw new IllegalArgumentException("Revision index not a committed revision, was: " + revisionIndex);
		}
		Revision rev = revisions.get(revisionIndex);
		ChangeSet cs = changesets.get(revisionIndex - 1);
		Object returnObject;
		switch (columnIndex) {
			case 0: {
				if (revisionIndex == revisions.size() - 1) {
					returnObject = "HEAD";
				} else {
					returnObject = "";
				}
			}; break;
			case 1: {
				//Revision
				returnObject = rev.getRevision();
			}; break;
			case 2: {
				//Timestamp
				returnObject = rev.getTimeStamp();
			}; break;
			case 3: {
				//User
				returnObject = rev.getUser();
			}; break;
			case 4: {
				//#AddAxiom
				returnObject = rev.getRevisionComment();
				if (returnObject == null) returnObject = "";
			}; break;
			case 5: {
				returnObject = cs.size();
			}; break;
			default: {
				returnObject = "unknown col index";
			}; break;
			}
		return returnObject;
	}
	
	public Object getValueForUncommittedAt(int columnIndex) {
		ChangeSet cs = changesets.get(changesets.size() - 1);
		Object returnObject;
		switch (columnIndex) {
			case 0: {
				returnObject = "UNCOMMITTED";
			}; break;
			case 1: {
				//Revision
				returnObject = "";
			}; break;
			case 2: {
				//Timestamp
				returnObject = "";
			}; break;
			case 3: {
				//User
				returnObject = "you";
			}; break;
			case 4: {
				//Comment
				if (versionedOntology.getWorkingSetConflicts().isEmpty()) {
					returnObject = "";
				} else {
					returnObject = "<html><b>Contains " + versionedOntology.getWorkingSetConflicts().size() 
						 + " conflicts that will be removed on commit.";   
				}
			}; break;
			case 5: {
				returnObject = cs.size();
			}; break;
			default: {
				returnObject = "unknown col index";
			}; break;
			}
		return returnObject;
	}

	public Object getValueForInitialAt(int columnIndex) {
		Revision rev = revisions.get(0);
		Object returnObject;
		switch (columnIndex) {
			case 0: {
				returnObject = "INIT";
			}; break;
			case 1: {
				//Revision
				returnObject = rev.getRevision();
			}; break;
			case 2: {
				//Timestamp
				returnObject = rev.getTimeStamp();
			}; break;
			case 3: {
				//User
				returnObject = rev.getUser();
			}; break;
			case 4: {
				returnObject = rev.getRevisionComment();
				if (returnObject == null) returnObject = "";
			}; break;
			case 5: {
				returnObject = "";
			}; break;
			default: {
				returnObject = "unknown col index";
			}; break;
			}
		return returnObject;
	}
	
}