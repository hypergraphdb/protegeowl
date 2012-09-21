package gov.miamidade.hgowl.plugin.ui.versioning;

import gov.miamidade.hgowl.plugin.ui.render.OWLChangeCellRenderer;

import java.awt.Color;
import java.awt.Component;
import java.util.SortedSet;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
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

	public enum ChangeDisplayMode { OWL, FUNCTIONAL };
	public final static ChangeDisplayMode DEFAULT_MODE = ChangeDisplayMode.OWL;
	
    public static final Color CONFLICT_COLOR = new Color(255,100,100); 

    private static final long serialVersionUID = 8903553111833559044L;
	private ChangeSetTableModel changeSetTableModel;

	private OWLEditorKit kit;
	
	public ChangeSetTable(OWLOntology onto, HyperGraph graph, OWLEditorKit kit) {
		this.kit = kit;
		changeSetTableModel = new ChangeSetTableModel(onto, graph);
		TableCellRendererWithConflict col0Renderer = new TableCellRendererWithConflict();
    	col0Renderer.setHorizontalAlignment(JLabel.RIGHT);
		setModel(changeSetTableModel);
        setMode(DEFAULT_MODE);
        getColumnModel().getColumn(ChangeSetTableModel.CHANGENR_COLUMN_INDEX).setCellRenderer(col0Renderer);
        getColumnModel().getColumn(ChangeSetTableModel.CHANGENR_COLUMN_INDEX).setMaxWidth(60);
	}
	
	public void setChangeSet(ChangeSet changeSet, SortedSet<Integer> conflicts) {
		changeSetTableModel.refresh(changeSet, conflicts);
		repaint();
	}

	public void setChangeSet(ChangeSet changeSet) {
		changeSetTableModel.refresh(changeSet, null);
		repaint();
	}

	public void setMode(ChangeDisplayMode newMode) {
		TableCellRenderer col1Renderer;
		if (!newMode.equals(changeSetTableModel.getMode())) {
			if (newMode.equals(ChangeDisplayMode.OWL)) {
		    	col1Renderer = new ChangeRendererWithConflict(kit);
			} else {
				col1Renderer = new TableCellRendererWithConflict();
			}
			getColumnModel().getColumn(ChangeSetTableModel.CHANGE_COLUMN_INDEX).setCellRenderer(col1Renderer);
			changeSetTableModel.setMode(newMode);
		}
	}

	public ChangeDisplayMode getMode() {
		return changeSetTableModel.getMode();
	}
	
	public class ChangeRendererWithConflict extends OWLChangeCellRenderer {
		private Color normalBackground;
		
		public ChangeRendererWithConflict(OWLEditorKit owlEditorKit) {
			super(owlEditorKit);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			JComponent jcomp = (JComponent)comp;
	   		String tooltipText = changeSetTableModel.getTooltipAt(row,column);
	   		if (tooltipText != null) {
	   			jcomp.setToolTipText(tooltipText);
	   		}
			if (!isSelected) {
		   		if (changeSetTableModel.isConflict(row, column)) {
		   			if (!jcomp.getBackground().equals(CONFLICT_COLOR)) {
		   				normalBackground = jcomp.getBackground();
		   				jcomp.setOpaque(true);
		   				jcomp.setBackground(CONFLICT_COLOR);
		   			}
		   		} else {
		   			if (jcomp.getBackground().equals(CONFLICT_COLOR)) {
		   				jcomp.setBackground(normalBackground);
		   			}
		   		}
			}
			return comp;
		}
	}
	
	public class TableCellRendererWithConflict extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 6470318587686903326L;
		private Color normalBackground;
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			JComponent jcomp = (JComponent)comp;
	   		String tooltipText = changeSetTableModel.getTooltipAt(row,column);
	   		if (tooltipText != null) {
	   			jcomp.setToolTipText(tooltipText);
	   		}
			if (!isSelected) {
		   		if (changeSetTableModel.isConflict(row, column)) {
		   			if (!jcomp.getBackground().equals(CONFLICT_COLOR)) {
		   				normalBackground = jcomp.getBackground();
		   				jcomp.setOpaque(true);
		   				jcomp.setBackground(CONFLICT_COLOR);
		   			}
		   		} else {
		   			if (jcomp.getBackground().equals(CONFLICT_COLOR)) {
		   				jcomp.setBackground(normalBackground);
		   			}
		   		}
			}
			return comp;
		}
	}
}