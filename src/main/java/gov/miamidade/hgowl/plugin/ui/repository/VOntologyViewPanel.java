package gov.miamidade.hgowl.plugin.ui.repository;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.util.CoffmanGraham;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.protege.editor.owl.model.OWLWorkspace;

import gov.miamidade.hgowl.plugin.ui.uihelp;
import gov.miamidade.hgowl.plugin.ui.versioning.RevisionGraphPanel;

/**
 * VOntologyViewPanel.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 30, 2012
 */
public class VOntologyViewPanel extends JPanel
{
	private static final long serialVersionUID = 159528341514944079L;

	private VersionedOntology versionedOntology;
	private VOntologyTableModel tableModel;
	private Component parent;
	List<Revision> revisions;
	private JTable table;
	RevisionGraphPanel graphpanel; 
	private boolean[][] adjacencyMatrix; // [i][j] == true iff revisions.get(i)
											// is the direct parent of
											// revisions.get(j)

	private List<Revision> orderLayers(HyperGraph graph, Map<Integer, HGHandle[]> layers)
	{
		List<Revision> revisions = new ArrayList<Revision>();
		for (int layerIndex = 1; layers.containsKey(layerIndex); layerIndex++)
		{
			ArrayList<Revision> layerData = new ArrayList<Revision>();
			for (HGHandle revHandle : layers.get(layerIndex))
				layerData.add((Revision) graph.get(revHandle));
			Collections.sort(layerData, new Comparator<Revision>()
			{
				public int compare(Revision left, Revision right)
				{
					return Long.compare(left.timestamp(), right.timestamp());
				}
			});
			revisions.addAll(layerData);
		}
		Collections.reverse(revisions);
		return revisions;
	}

	private boolean[][] makeAdjacencyMatrix(List<Revision> revisions)
	{
		boolean[][] adjacencyMatrix = new boolean[revisions.size()][revisions.size()];
		for (int i = 0; i < revisions.size(); i++)
			for (int j = 0; j < revisions.size(); j++)
				if (revisions.get(i).children().contains(revisions.get(j).getAtomHandle()))
					adjacencyMatrix[i][j] = true;
		return adjacencyMatrix;
	}

	public VOntologyViewPanel(Component parent, VersionedOntology versionedOntology)
	{
		this.parent = parent;
		this.versionedOntology = versionedOntology;
		CoffmanGraham algo = new CoffmanGraham(versionedOntology.graph(), versionedOntology.getRootRevision());
		Map<Integer, HGHandle[]> layers = algo.coffmanGrahamLayers(1);
		revisions = orderLayers(versionedOntology.graph(), layers);
		adjacencyMatrix = makeAdjacencyMatrix(revisions);
		createUI();
	}

	public List<Revision> orderedRevisions()
	{
		return revisions;
	}
	
	private JPopupMenu revisionPopupMenu(final Revision revision)
	{
		JPopupMenu menu = new JPopupMenu();
		JMenuItem item = new JMenuItem("Goto Revision");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev)
			{
				try 
				{
					versionedOntology.goTo(revision);
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(parent, ex.getMessage(), "GoTo Revision Failed", JOptionPane.ERROR_MESSAGE);
				}
				table.repaint();
				if (parent instanceof OWLWorkspace)
					((OWLWorkspace)parent).refreshComponents();
			}
		});
		menu.add(item);
		return menu;
	}
	
	private void createUI()
	{
		tableModel = new VOntologyTableModel(versionedOntology, revisions);
		table = new JTable(tableModel);
	    //table.getTableHeader().setResizingAllowed(true);
	    table.getTableHeader().setVisible(true);
		TableCellRenderer cellRenderer = new TableCellRenderer()
		{
			public final DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
			
		    public Component getTableCellRendererComponent(JTable table, Object value,
		    											   boolean isSelected, boolean hasFocus,
		    											   int row, int column)
		    {
		    	Component c = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		    	if (row > 0 && revisions.get(revisions.size() - row) == versionedOntology.revision())
		    		if (isSelected)
		    			c.setBackground(uihelp.blend(Color.green.darker(), table.getSelectionBackground()));
		    		else
		    			c.setBackground(uihelp.blend(Color.green, table.getBackground()));
		    	else if (!isSelected)
		    		c.setBackground(Color.white);
		    	else
		    		c.setBackground(table.getSelectionBackground());
		    	return c;
		    }
		};
		table.setDefaultRenderer(Object.class, cellRenderer);

		table.getColumnModel().getColumn(0).setMinWidth(20);
		table.getColumnModel().getColumn(1).setMinWidth(30);
		table.getColumnModel().getColumn(2).setMinWidth(140);

		table.getColumnModel().getColumn(0).setPreferredWidth(60);
		table.getColumnModel().getColumn(1).setPreferredWidth(30);
		table.getColumnModel().getColumn(2).setPreferredWidth(140);
		table.getColumnModel().getColumn(3).setPreferredWidth(100);
		table.getColumnModel().getColumn(4).setPreferredWidth(300);
		table.getColumnModel().getColumn(5).setPreferredWidth(60);
		table.getColumnModel().getColumn(5).setMaxWidth(60);

		graphpanel = new RevisionGraphPanel(this.versionedOntology, revisions, adjacencyMatrix);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent event)
			{
				// System.out.println(table.getValueAt(table.getSelectedRow(),
				// 0).toString());
				if (table.getSelectedRowCount() > 2)
				{
					int min = Integer.MAX_VALUE, max = 0; 
					for (int i = 0; i < table.getSelectedRows().length; i++)
					{
						if (min > table.getSelectedRows()[i])
							min = table.getSelectedRows()[i];
						if (max < table.getSelectedRows()[i])
							max = table.getSelectedRows()[i];
					}	
//					table.getSelectionModel().removeSelectionInterval(
//							table.getSelectionModel().getLeadSelectionIndex(),
//							table.getSelectionModel().getLeadSelectionIndex());
					graphpanel.selectedRows(new int[] { min, max });
				}
				else
					graphpanel.selectedRows(table.getSelectedRows());
				graphpanel.selectionBackground(table.getSelectionBackground());
				graphpanel.repaint();
			}
		});

		table.addMouseListener(new MouseAdapter()
		{
			@Override public void mousePressed(MouseEvent e)
			{
				mousePopup(e);
			}
			@Override public void mouseReleased(MouseEvent e)
			{
				mousePopup(e);
			}
			void mousePopup(MouseEvent e)
			{
				int rowindex = table.getSelectedRow();
				if (rowindex < 0)
					return;
				if (e.isPopupTrigger() && e.getComponent() instanceof JTable)
				{
					System.out.println("Going to " + revisions.get(revisions.size() - rowindex));
					JPopupMenu popup = revisionPopupMenu(revisions.get(revisions.size() - rowindex));
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		Rectangle cellSize = table.getCellRect(0, 0, true);
		graphpanel.cellHeight((int) cellSize.getHeight());

		// scroll bar won't show up if we don't set preferred size
		JPanel internalPanel = new JPanel()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize()
			{
				return new Dimension(850, 400);
			}
		};
		internalPanel.setBackground(Color.white);
		// BoxLayout layout = new BoxLayout(internalPanel, BoxLayout.X_AXIS);
		SpringLayout layout = new SpringLayout();
		internalPanel.setLayout(layout);
		internalPanel.add(graphpanel);
		internalPanel.add(table.getTableHeader());
		internalPanel.add(table);
		layout.putConstraint(SpringLayout.NORTH, graphpanel, 0, SpringLayout.NORTH, internalPanel);
		layout.putConstraint(SpringLayout.WEST, graphpanel, 0, SpringLayout.WEST, internalPanel);
		layout.putConstraint(SpringLayout.WEST, table, 0, SpringLayout.EAST, graphpanel);
		layout.putConstraint(SpringLayout.WEST, table.getTableHeader(), 0, SpringLayout.EAST, graphpanel);
		layout.putConstraint(SpringLayout.NORTH, table.getTableHeader(), 0, SpringLayout.NORTH, internalPanel);
		layout.putConstraint(SpringLayout.NORTH, table, 0, SpringLayout.SOUTH, table.getTableHeader());
		layout.putConstraint(SpringLayout.EAST, table, 0, SpringLayout.EAST, internalPanel);
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));		
		add(new JScrollPane(internalPanel));
	}

	public RevisionGraphPanel graphPanel()
	{
		return graphpanel;
	}
	
//	public Dimension getPreferredSize()
//	{
//		return new Dimension(850, 400);
//	}

//	public static HGOntologyRepositoryEntry showRevisionDialog(String title, Component parent, VersionedOntology vo)
//	{
//		VOntologyViewPanel panel = new VOntologyViewPanel(vo);
//		JOptionPane.showMessageDialog(parent, panel, title, JOptionPane.PLAIN_MESSAGE);
//		return null;
//	}

	public JTable getTable()
	{
		return table;
	}

	public VOntologyTableModel getTableModel()
	{
		return tableModel;
	}
}
