package gov.miamidade.hgowl.plugin.obsolete;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.hypergraphdb.app.owl.versioning.Revision;

/**
 * A VersionedOntologyComparator compares two linear revision histories of a
 * versioned ontology and and determines which is newer or if there is a
 * conflict.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 6, 2012
 */
public class VersionedOntologyComparator
{

	public enum RevisionCompareOutcome
	{
		MATCH, // sourcerevision equal to targetrevision
		CONFLICT, // sourcerevision unequal to existing targetrevision
		SOURCEONLY, // Should push
		TARGETONLY // Should pull
	}

	public VersionedOntologyComparisonResult compare(List<Revision> source, List<Revision> target)
	{
		VersionedOntologyComparisonResult result = new VersionedOntologyComparisonResult();
		ListIterator<Revision> sourceIt = source.listIterator();
		ListIterator<Revision> targetIt = target.listIterator();
		while (sourceIt.hasNext() || targetIt.hasNext())
		{
			Revision curSource = sourceIt.hasNext() ? sourceIt.next() : null;
			Revision curTarget = targetIt.hasNext() ? targetIt.next() : null;
			result.addRevisionComparisonResult(new RevisionComparisonResult(curSource, curTarget));
		}
		return result;
	}

	public static class VersionedOntologyComparisonResult
	{
		private List<RevisionComparisonResult> comparisons = new ArrayList<RevisionComparisonResult>();

		private boolean conflict;
		private boolean targetNewer;
		private boolean sourceNewer;
		private int lastMatchingRevisionIndex = -1;
		private boolean hasMatchingRevisions = false;

		public void addRevisionComparisonResult(RevisionComparisonResult rcr)
		{
			comparisons.add(rcr);
			if (rcr.getOutcome() == RevisionCompareOutcome.CONFLICT)
			{
				conflict = true;
			}
			else if (rcr.getOutcome() == RevisionCompareOutcome.SOURCEONLY)
			{
				sourceNewer = true;
			}
			else if (rcr.getOutcome() == RevisionCompareOutcome.TARGETONLY)
			{
				targetNewer = true;
			}
			else
			{
				hasMatchingRevisions = true;
				lastMatchingRevisionIndex = comparisons.size() - 1;
			}
		}

		public List<RevisionComparisonResult> getRevisionResults()
		{
			return comparisons;
		}

		/**
		 * @return the conflict
		 */
		public boolean isConflict()
		{
			return conflict;
		}

		/**
		 * @return the targetNewer
		 */
		public boolean isTargetNewer()
		{
			return targetNewer;
		}

		/**
		 * @return the sourceNewer
		 */
		public boolean isSourceNewer()
		{
			return sourceNewer;
		}

		public boolean isSourceTargetEqual()
		{
			return !isSourceNewer() && !isTargetNewer() && !isConflict();
		}

		public boolean hasMatchingRevisions()
		{
			return hasMatchingRevisions;
		}

		public int getLastMatchingRevisionIndex()
		{
			return lastMatchingRevisionIndex;
		}
	}

	public static class RevisionComparisonResult
	{
		private Revision source;
		private Revision target;
		private RevisionCompareOutcome outcome;

		public RevisionComparisonResult(Revision source, Revision target)
		{
			this.source = source;
			this.target = target;
			if (source != null)
			{
				if (target != null)
				{
					if (source.equals(target))
					{
						outcome = RevisionCompareOutcome.MATCH;
					}
					else
					{
						outcome = RevisionCompareOutcome.CONFLICT;
					}
				}
				else
				{
					outcome = RevisionCompareOutcome.SOURCEONLY;
				}
			}
			else if (target != null)
			{
				outcome = RevisionCompareOutcome.TARGETONLY;
			}
			else
			{
				throw new IllegalArgumentException("source and target were null. Not allowed.");
			}
		}

		public Revision getSource()
		{
			return source;
		}

		public Revision getTarget()
		{
			return target;
		}

		public RevisionCompareOutcome getOutcome()
		{
			return outcome;
		}
	}
}
