package gov.miamidade.hgowl.plugin.ui.versioning;

import java.util.ArrayList;
import java.util.List;

import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.change.VChange;

/**
 * Versioning UI utilities.
 * 
 * @author Borislav Iordanov
 *
 */
public class VU
{
	public static List<VChange<VersionedOntology>> flattenChanges(List<ChangeSet<VersionedOntology>> changeSetList)
	{
		List<VChange<VersionedOntology>> L = new ArrayList<VChange<VersionedOntology>>();	
		if (changeSetList != null) for (ChangeSet<VersionedOntology> cs : changeSetList)
			L.addAll(cs.changes());
		return L;
	}
}
