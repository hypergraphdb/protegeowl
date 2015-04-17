package gov.miamidade.hgowl.plugin.ui.repository;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.hypergraphdb.app.owl.newver.ChangeSet;
import org.hypergraphdb.app.owl.newver.Revision;
import org.hypergraphdb.app.owl.newver.VersionedOntology;

/**
 * VOntologyTableModel.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 30, 2012
 */
public class VOntologyTableModel extends AbstractTableModel
{

	private static final long serialVersionUID = -218142221144608713L;

	VersionedOntology versionedOntology;
	List<ChangeSet<VersionedOntology>> changesets;
	List<Revision> revisions;

	public VOntologyTableModel(VersionedOntology vo)
	{
		this.versionedOntology = vo;
		refresh();
		// Master / Revision / Timestamp / user / #ofTotalChanges
	}

	public void refresh()
	{
		// TODO - we should try to avoid loading all revisions and change sets all the time.
		revisions = new ArrayList<Revision>(); 
		changesets = new ArrayList<ChangeSet<VersionedOntology>>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount()
	{
		return 6;
	}

	public String getColumnName(int column)
	{
		switch (column)
		{
			case 0: return "Master";
			case 1: return "Revision";
			case 2: return "Time Stamp";
			case 3: return "User";
			case 4: return "Comment";
			case 5: return "#Changes";
			default: return "UNKNOWN COL: " + column;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount()
	{
		// 1 added for pending head changes
		return revisions.size() + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		int revisionIndex = revisions.size() - rowIndex; // - 1;
		if (revisionIndex == revisions.size())
		{
			return getValueForUncommittedAt(columnIndex);
		}
		else if (revisionIndex > 0)
		{
			return getValueForCommittedAt(revisionIndex, columnIndex);
		}
		else if (revisionIndex == 0)
		{
			// Initial
			return getValueForInitialAt(columnIndex);
		}
		else
		{
			throw new IllegalArgumentException("Row with rowindex " + rowIndex + " does not exist.");
		}
	}

	public Object getValueForCommittedAt(int revisionIndex, int columnIndex)
	{
		if (revisionIndex < 1 || revisionIndex >= revisions.size())
		{
			throw new IllegalArgumentException("Revision index not a committed revision, was: " + revisionIndex);
		}
		Revision rev = revisions.get(revisionIndex);
		ChangeSet<VersionedOntology> cs = changesets.get(revisionIndex - 1);
		switch (columnIndex)
		{
			case 0: return (revisionIndex == revisions.size() - 1) ? "HEAD":"";
			case 1: return "local label?"; // rev.getRevision();
			case 2: return new java.util.Date(rev.getTimestamp());
			case 3: return rev.getUser();
			case 4: return rev.getComment() == null ? "" : rev.getComment();
			case 5: return cs.size();
			default:return "unknown col index";
		}
	}

	public Object getValueForUncommittedAt(int columnIndex)
	{
		ChangeSet<VersionedOntology> cs = changesets.get(changesets.size() - 1);
		switch (columnIndex)
		{
			case 0:return "UNCOMMITTED";
			case 1:case 2: return "";
			case 3:return "you";
			case 4:return "conflicts?";  
					/*
					(versionedOntology.getWorkingSetConflicts().isEmpty())
			{
				returnObject = "";
			}
			else
			{
				returnObject = "<html><b>Contains " + versionedOntology.getWorkingSetConflicts().size()
						+ " conflicts that will be removed on commit.";
			}
		}
			;
			break; */
			case 5:return cs.size();
			default:return "unknown col index";
		}
	}

	public Object getValueForInitialAt(int columnIndex)
	{
		Revision rev = revisions.get(0);
		switch (columnIndex)
		{
			case 0:return "INIT";
			case 1:return "local label?";//rev.getRevision();
			case 2:return new java.util.Date(rev.getTimestamp());
			case 3:return rev.getUser();
			case 4:return rev.getComment() == null ? "" : rev.getComment();
			case 5:return "";
			default:return "unknown col index";
		}
	}
}