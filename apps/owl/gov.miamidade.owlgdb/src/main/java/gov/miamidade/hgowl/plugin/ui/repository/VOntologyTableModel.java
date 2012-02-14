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
		return versionedOntology.getNrOfRevisions();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		//if (rowIndex > versionedOntology.getNrOfRevisions()) return null;
		Object returnObject;
		Revision rev = revisions.get(revisions.size() - rowIndex - 1);
		ChangeSet cs = changesets.get(changesets.size() - rowIndex - 1);
		switch (columnIndex) {
			case 0: {
				//	Repo Branch info				
				if (rowIndex == 0) {
					returnObject = "HEAD";
				} else if (rowIndex == revisions.size() - 1) {
					returnObject = "BASE";
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
				//#AddAxiom
				returnObject = cs.size();
			}; break;
			default: {
				returnObject = "unknown col index";
			}; break;
			}
		return returnObject;
	}
}