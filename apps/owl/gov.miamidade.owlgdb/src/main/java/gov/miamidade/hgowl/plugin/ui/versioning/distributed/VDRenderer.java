package gov.miamidade.hgowl.plugin.ui.versioning.distributed;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.ClientCentralizedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.DistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.ServerCentralizedOntology;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * VDRenderer.
 * All methods return empty string if called with null.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 12, 2012
 */
public class VDRenderer {

	static DateFormat df = DateFormat.getDateTimeInstance();
	
	public static String render(DistributedOntology dOnto) {
		if (dOnto == null) return "";
		String shareType;
		if (dOnto  instanceof ClientCentralizedOntology) {
			shareType = "Client";
		} else if (dOnto instanceof ServerCentralizedOntology) {
			shareType = "Server";
		} else {
			shareType = "Peer";
		}
		return render(dOnto.getVersionedOntology()) + " " + shareType; 
	}
	
	public static String render(VersionedOntology vo) {
		if (vo == null) return "";
		return render(vo.getWorkingSetData()) + " Head: " + render(vo.getHeadRevision(), !vo.getWorkingSetChanges().isEmpty());
	}

	public static String renderWorkingSetOf(VersionedOntology vo) {
		if (vo == null) return "";
		String s = "" + vo.getWorkingSetChanges().getArity() + " changes ";
		if (!vo.getWorkingSetConflicts().isEmpty()) {
			s+= "(" + vo.getWorkingSetConflicts().size() + " conflicts)";
		}
		return s;
	}

	public static String render(OWLOntology onto) {
		if (onto == null) return "";
		return "" + onto.getOntologyID().getOntologyIRI().getFragment() + " (" + onto.getOntologyID().getOntologyIRI() + ")";
	}
	
	public static String render(Revision rev) {
		if (rev == null) return "";
		return "" + rev.getRevision() + " " + render(rev.getTimeStamp()) + " by " + rev.getUser();
	}

	public static String render(Revision head, boolean isWorkingSetChanges) {
		if (head == null) return "";
		String pending = isWorkingSetChanges? "* " : " "; 
		return "" + head.getRevision() + pending + render(head.getTimeStamp()) + " by " + head.getUser();
	}

	public static String render(HGPeerIdentity pi) {
		return pi != null? pi.getHostname() + "(" +pi.getGraphLocation() + ")" : "";
	}
	
	public static String render(Date date) {
		return date == null? "" : df.format(date);
	}
	
	private static DecimalFormat decf = new DecimalFormat("####000");

	/**
	 * Format: ####000
	 * @param changeNumber
	 * @return
	 */
	public static String render(int changeNumber) {
		return decf.format(changeNumber);
	}


}
