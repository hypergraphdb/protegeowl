package gov.miamidade.hgowl.plugin.ui.versioning;

import java.awt.Color;
import java.awt.Component;
import java.util.SortedSet;
import java.util.TreeSet;

import gov.miamidade.hgowl.plugin.ui.render.OWLChangeCellRenderer;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * ChangeSetTable. //Columns: Number, Change, Functional
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 19, 2012
 */
public class ChangeSetTable extends JTable {

	private static final long serialVersionUID = 8903553111833559044L;
	private OWLChangeCellRenderer changeCellRenderer;
	private ChangeSetTableModel changeSetTableModel;

	public ChangeSetTable(OWLOntology onto, HyperGraph graph, OWLEditorKit kit) {
		changeCellRenderer = new OWLChangeCellRenderer(kit);
		changeSetTableModel = new ChangeSetTableModel(onto, graph);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setRowSelectionAllowed(true);
		setCellSelectionEnabled(false);
		setModel(changeSetTableModel);
        setGridColor(Color.blue);
        setSelectionBackground(Color.blue);
        getColumnModel().getColumn(ChangeSetTableModel.CHANGENR_COLUMN_INDEX).setMaxWidth(60);
        getColumnModel().getColumn(ChangeSetTableModel.CHANGE_RENDERED_COLUMN_INDEX).setPreferredWidth(400);
        getColumnModel().getColumn(ChangeSetTableModel.CHANGE_FUCNTIONAL_COLUMN_INDEX).setPreferredWidth(300);
	}
	
	public void setChangeSet(ChangeSet changeSet, SortedSet<Integer> conflicts) {
		changeSetTableModel.refresh(changeSet, conflicts);
		repaint();
	}

	public void setChangeSet(ChangeSet changeSet) {
		changeSetTableModel.refresh(changeSet, null);
		repaint();
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		if (column == ChangeSetTableModel.CHANGE_RENDERED_COLUMN_INDEX) {
			return changeCellRenderer;
		} else {
			return super.getCellRenderer(row, column);
		}
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
		//System.out.print("prep rend: " + row + " col " + col);
    	Component comp = super.prepareRenderer(renderer, row, col);
    	JComponent jcomp = (JComponent)comp;
   		String tooltipText = changeSetTableModel.getTooltipAt(row,col);
   		jcomp.setToolTipText(tooltipText);
   		Color c = changeSetTableModel.getCellBgColorAt(row, col);
   		if (!c.equals(Color.white)) {
   			jcomp.setBackground(c);
   		} else {
   			jcomp.setBackground(getBackground());
   		}
		//System.out.println("prep rend: " + row + " col " + col + " Col: " + jcomp.getBackground());
   		return comp;
    }
}