package gov.miamidade.hgowl.plugin.ui;

import gov.miamidade.hgowl.plugin.owl.model.HGOwlModelManagerImpl;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyImpl;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.gc.GarbageCollector;
import org.hypergraphdb.app.owl.gc.GarbageCollectorStatistics;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * HGRunGCAction.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 11, 2011
 */
public class HGRunGCAction extends ProtegeOWLAction
{

	private static final long serialVersionUID = -2085444668481360102L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.protege.editor.core.plugin.ProtegePluginInstance#initialise()
	 */
	@Override
	public void initialise() throws Exception
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.protege.editor.core.Disposable#dispose()
	 */
	@Override
	public void dispose() throws Exception
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		HGOwlModelManagerImpl mm = (HGOwlModelManagerImpl) this.getOWLModelManager();
		HGDBOntologyManager om = (HGDBOntologyManager) mm.getOWLOntologyManager();
		if (mayRun())
		{
			if (showRunGCConfirmation())
			{
				mm.getHistoryManager().getLoggedChanges().clear();
				int mode = HGRunGCModeSelectionPanel.showDialog(getOWLEditorKit(), "Run Garbage Collector Mode");
				// this can take long:
				if (mode >= 0)
				{
					GarbageCollector gc = new GarbageCollector(om.getOntologyRepository());
					// GarbageCollectorStatistics stats =
					// gc.runGarbageCollection(mode);
					runGCThread(gc, mode);
				}
				else
				{
					System.out.println("GC aborted by user.");
				}
				for (OWLOntology onto : om.getOntologies())
				{
					if (!(onto instanceof HGDBOntology) && mm.isDirty(onto) && onto.getAxiomCount() > 0)
						try
						{
							mm.reload(onto);
						}
						catch (OWLOntologyCreationException e)
						{
							e.printStackTrace();
						}
				}
			}
		}
		else
		{
			showRunGCNotAllowed();
		}
	}

	private boolean mayRun()
	{
		HGOwlModelManagerImpl mm = (HGOwlModelManagerImpl) this.getOWLModelManager();
		HGDBOntologyManager om = (HGDBOntologyManager) mm.getOWLOntologyManager();
		// if we have an in memory ontology that has been modified and was unsaved, we
		// can't do garbage collection because some of the entities might get deleted?
		// not sure why this was done....perhaps we should make the plugin deal 
		// completely independently with inmemory ontologies and HGDB-backed ones so
		// that loading in memory doesn't have any impact on the DB at all. 
		for (OWLOntology o : mm.getDirtyOntologies())
			if ( ! (o instanceof HGDBOntologyImpl) && 
					(o.getAxiomCount() > 0 || !o.getImports().isEmpty()))
				return false;
		return true;
	}
	
	/**
	 * Shows a
	 */
	public void showRunGCNotAllowed()
	{
		String message = "You have modified in memory Ontologies loaded: \n"
				+ "    Running Garbage Collection interfere with unsaved changes. \n"
				+ "    Please save any unsaved ontologies first... and try again.  \n";
		JOptionPane.showMessageDialog(getWorkspace(), message, "Garbage Collection - Not Allowed", JOptionPane.WARNING_MESSAGE);

	}

	public boolean showRunGCConfirmation()
	{
		String message = "Please confirm that you want to run garbage collection: \n"
				+ "    Be advised that Undo/Redo changes will be reset \n"
				+ "    The duration of this operation can be predicted by running analyze first. \n";
		int userInput = JOptionPane.showConfirmDialog(getWorkspace(), message, "Confirm Garbage Collection - Full run",
				JOptionPane.YES_NO_OPTION);
		return (userInput == JOptionPane.YES_OPTION);
	}

	public void showResult(GarbageCollectorStatistics stats)
	{
		StringWriter stringWriter = new StringWriter(400);
		PrintWriter s = new PrintWriter(stringWriter);
		s.println();
		s.println("----------------------------");
		s.println("- GARBAGE COLLECTOR STATS  -");
		s.println("- Ontologies    : " + stats.getOntologies());
		s.println("- Axioms        : " + stats.getAxioms());
		s.println("- Entities      : " + stats.getEntities());
		s.println("- IRIs          : " + stats.getIris());
		s.println("- Annotations   : " + stats.getAnnotations());
		s.println("- Other Objects : " + stats.getOtherObjects());
		s.println("- Total Atoms   : " + stats.getTotalAtoms());
		s.println("----------------------------");
		s.println("- Times we met an axiom contained in more than one ontology");
		s.println("-   and therefore could not be removed: " + stats.getAxiomNotRemovableCases());
		s.flush();
		String message = stringWriter.toString();
		JTextPane textPane = new JTextPane();
		// textPane.setSize(500,600);
		textPane.setText(message);

		JOptionPaneEx.showConfirmDialog(getWorkspace(), "Hypergraph Garbage Collection Statistics", textPane,
				JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, textPane);
	}

	private void runGCThread(final GarbageCollector gc, final int mode)
	{
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		ExecutorService monitor = Executors.newSingleThreadExecutor();
		// Garbage Collector Callable
		Callable<GarbageCollectorStatistics> c = new Callable<GarbageCollectorStatistics>()
		{
			@Override
			public GarbageCollectorStatistics call() throws Exception
			{
				GarbageCollectorStatistics results = gc.runGarbageCollection(mode);
				return results;
			}

		};
		final Future<GarbageCollectorStatistics> fStats = executor.submit(c);
		//
		// Open Progress Monitor
		//
		monitor.execute(new Runnable()
		{
			@Override
			public void run()
			{
				gc.resetTask();
				int lastMax = -1;
				Window parent = SwingUtilities.getWindowAncestor(getWorkspace());
				ProgressDialog pd = new ProgressDialog(parent, "Garbage Collection Progress", true, 400, 120);
				pd.setVisible(true);
				while (!fStats.isDone())
				{
					try
					{
						Thread.sleep(100);
						int curMax = gc.getTaskSize();
						int curProgress = gc.getTaskProgess();
						if (lastMax != curMax)
						{
							pd.setTaskSize(curMax);
							lastMax = curMax;
						}
						// System.out.print("" + curProgress);
						pd.setTaskProgress(curProgress);
						if (pd.isCancelled())
						{
							gc.cancelTask();
						}
					}
					catch (InterruptedException e)
					{
					}
					;
				}
				pd.setVisible(false);
				try
				{
					final GarbageCollectorStatistics stats = fStats.get();
					gc.resetTask();
					System.out.println("Total GCd atoms: " + stats.getTotalAtoms());
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							Toolkit.getDefaultToolkit().beep();
							System.out.println("Total GCd atoms: " + stats.getTotalAtoms());
							showResult(stats);
						}
					});
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				catch (ExecutionException e)
				{
					e.printStackTrace();
				}

			}
		});
		executor.shutdown();
		monitor.shutdown();
	}
}
