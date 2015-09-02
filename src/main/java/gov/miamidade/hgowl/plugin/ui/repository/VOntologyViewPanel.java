package gov.miamidade.hgowl.plugin.ui.repository;

import gov.miamidade.hgowl.plugin.owl.model.HGOntologyRepositoryEntry;





import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;

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

	private JTable table;

	public VOntologyViewPanel(VersionedOntology vOnto, List<Revision> revisions)
	{
		this.versionedOntology = vOnto;
		createUI(revisions);
	}

	private void createUI(List<Revision> revisions)
	{
		setLayout(new BorderLayout());
		tableModel = new VOntologyTableModel(versionedOntology, revisions);
		table = new JTable(tableModel);
		// 0.Master 1.Revision 2.TimeStamp 3.User 4.Comment 5.#Changes (after
		// revision)
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
		table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
		table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

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
		add(new JScrollPane(table));
	}

	public Dimension getPreferredSize()
	{
		return new Dimension(850, 400);
	}

	public static HGOntologyRepositoryEntry showRevisionDialog(String title, Component parent, VersionedOntology vo)
	{
		VOntologyViewPanel panel = new VOntologyViewPanel(vo, vo.revisions());
		JOptionPane.showMessageDialog(parent, panel, title, JOptionPane.PLAIN_MESSAGE);
		return null;
	}

	public JTable getTable()
	{
		return table;
	}

	public VOntologyTableModel getTableModel()
	{
		return tableModel;
	}
}
