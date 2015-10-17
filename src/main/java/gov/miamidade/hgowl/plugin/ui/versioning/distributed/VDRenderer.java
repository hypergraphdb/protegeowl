package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import java.text.DateFormat;

import java.text.DecimalFormat;
import java.util.Date;

import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * VDRenderer. All methods return empty string if called with null.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 12, 2012
 */
public class VDRenderer
{
	static DateFormat df = DateFormat.getDateTimeInstance();

	public static String render(VersionedOntology vo)
	{
		if (vo == null)
			return "";
		return render(vo.getRevisionData(vo.getCurrentRevision())) + " Head: " + 
						render(vo.revision(), !vo.changes().isEmpty());
	}

	public static String renderWorkingSetOf(VersionedOntology vo)
	{
		if (vo == null)
			return "";
		String s = "" + vo.changes().getArity() + " changes ";
//		if (!vo.getWorkingSetConflicts().isEmpty())
//		{
//			s += "(" + vo.getWorkingSetConflicts().size() + " conflicts)";
//		}
		return s;
	}

	public static String render(OWLOntology onto)
	{
		if (onto == null)
			return "";
		return "" + onto.getOntologyID().getOntologyIRI().getFragment() + " (" + onto.getOntologyID().getOntologyIRI() + ")";
	}

	public static String render(Revision rev)
	{
		if (rev == null)
			return "";
		return "" + rev.toString() + " " + render(new Date(rev.timestamp())) + " by " + rev.user();
	}

	public static String render(Revision head, boolean isWorkingSetChanges)
	{
		if (head == null)
			return "";
		String pending = isWorkingSetChanges ? "* " : " ";
		return "" + head.toString() + pending + render(new Date(head.timestamp())) + " by " + head.user();
	}

	public static String render(HGPeerIdentity pi)
	{
		return pi != null ? pi.getHostname() + "(" + pi.getGraphLocation() + ")" : "";
	}

	public static String render(Date date)
	{
		return date == null ? "" : df.format(date);
	}

	private static DecimalFormat decf = new DecimalFormat("####000");

	/**
	 * Format: ####000
	 * 
	 * @param changeNumber
	 * @return
	 */
	public static String render(int changeNumber)
	{
		return decf.format(changeNumber);
	}
}