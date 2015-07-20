package gov.miamidade.hgowl.plugin.ui.repository;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;

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
	List<Revision> revisions;

	public static enum Column 
	{
		Master("Master"), 
		Revision("Revision"), 
		TimeStamp("Time Stamp"), 
		User("User"), 
		Comment("Comment"), 
		ChangeCount("#Changes");
		String label;
		Column(String label) { this.label = label; }
		public String toString() { return label; }
	}
	
	public VOntologyTableModel(VersionedOntology vo)
	{
		this.versionedOntology = vo;
		refresh();
	}

	public void refresh()
	{
		revisions = versionedOntology.revisions(); 
	}

	@Override
	public int getColumnCount()
	{
		return 6;
	}

	public String getColumnName(int column)
	{
		if (column >= Column.values().length)
			return "UNKNOWN COL: " + column;
		else
			return Column.values()[column].toString();
	}

	@Override
	public int getRowCount()
	{
		// 1 added for pending head changes
		return revisions.size() + 1;
	}

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
			throw new IllegalArgumentException("Revision index not a committed revision, was: " + 
							revisionIndex);
		}
		Revision rev = revisions.get(revisionIndex);
		switch (columnIndex)
		{
			case 0: return (revisionIndex == revisions.size() - 1) ? "HEAD":"";
			case 1: return "local label?"; // rev.getRevision();
			case 2: return new java.util.Date(rev.timestamp());
			case 3: return rev.user();
			case 4: return rev.comment() == null ? "" : rev.comment();
			case 5: return versionedOntology.changes(rev).size();
			default:return "unknown col index";
		}
	}

	public Object getValueForUncommittedAt(int columnIndex)
	{
		switch (columnIndex)
		{
			case 0:return "UNCOMMITTED";
			case 1:case 2: return "";
			case 3:return "you";
			case 4:return ""; // comments  
			case 5:return versionedOntology.changes().size();
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
			case 2:return new java.util.Date(rev.timestamp());
			case 3:return rev.user();
			case 4:return rev.comment() == null ? "" : rev.comment();
			case 5:return "";
			default:return "unknown col index";
		}
	}
}