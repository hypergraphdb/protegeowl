package gov.miamidade.hgowl.plugin.ui.render;

import java.awt.Component;
import java.net.URL;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.hypergraphdb.app.owl.core.AddPrefixChange;
import org.hypergraphdb.app.owl.core.RemovePrefixChange;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.ImportChange;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;

/**
 * OWLChangeCellRenderer
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 18, 2012
 */
public class OWLChangeCellRenderer extends OWLCellRenderer {

	public static final String ICON_ADD_FILENAME = "gov/miamidade/hgowl/plugin/ui/render/AddChange.png";
	public static final String ICON_REM_FILENAME = "gov/miamidade/hgowl/plugin/ui/render/RemChange.png";

	private Object objectToRender; 
	
	private Icon iconAdd; 
	private Icon iconRemove; 

	private ListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();
	private TableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
	private TreeCellRenderer defaultTreeCellRenderer = new DefaultTreeCellRenderer();
	
	//private Color unselectedBackground;
	
	/**
	 * @param owlEditorKit
	 */
	public OWLChangeCellRenderer(OWLEditorKit owlEditorKit) {
		super(owlEditorKit);
        setHighlightKeywords(false);
        initIcons();
	}

	protected void initIcons() {
        ClassLoader loader = this.getClass().getClassLoader();
        URL urlA = loader.getResource(ICON_ADD_FILENAME);
        URL urlR = loader.getResource(ICON_REM_FILENAME);
        if (urlA == null) System.err.println("NOT FOUND" + ICON_ADD_FILENAME);
        if (urlR == null) System.err.println("NOT FOUND" + ICON_REM_FILENAME);
        iconAdd = new ImageIcon(urlA);
        iconRemove = new ImageIcon(urlR);
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.ui.renderer.OWLCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component comp;
		objectToRender = value;
		if(objectToRender instanceof OWLAxiomChange) {
			value = ((OWLOntologyChange) objectToRender).getAxiom();
		} else if (objectToRender instanceof OWLAxiomChange) {
			value = ((ImportChange)objectToRender).getImportDeclaration();
		} else if (objectToRender instanceof AddOntologyAnnotation) {
			value = ((AddOntologyAnnotation)objectToRender).getAnnotation();
		} else if (objectToRender instanceof RemoveOntologyAnnotation) {
			value = ((RemoveOntologyAnnotation)objectToRender).getAnnotation();
		} else {
			//do nothing
		}
		if (value instanceof String) {
			 comp = defaultTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		} else {
			comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
		if (isSelected) {
            comp.setForeground(table.getSelectionForeground());
            comp.setBackground(table.getSelectionBackground());
		} else {
            comp.setForeground(table.getForeground());
            comp.setBackground(table.getBackground());
		}
		return comp;
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.ui.renderer.OWLCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		objectToRender = value;
		if(objectToRender instanceof OWLAxiomChange) {
			value = ((OWLOntologyChange) objectToRender).getAxiom();
		} else if (objectToRender instanceof OWLAxiomChange) {
			value = ((ImportChange)objectToRender).getImportDeclaration();
		} else if (objectToRender instanceof AddOntologyAnnotation) {
			value = ((AddOntologyAnnotation)objectToRender).getAnnotation();
		} else if (objectToRender instanceof RemoveOntologyAnnotation) {
			value = ((RemoveOntologyAnnotation)objectToRender).getAnnotation();
		} else {
			//do nothing
		}
		if (value instanceof String) {
			return defaultTreeCellRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		} else {
			return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.ui.renderer.OWLCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		objectToRender = value;
		if(objectToRender instanceof OWLAxiomChange) {
			value = ((OWLOntologyChange) objectToRender).getAxiom();
		} else if (objectToRender instanceof OWLAxiomChange) {
			value = ((ImportChange)objectToRender).getImportDeclaration();
		} else if (objectToRender instanceof AddOntologyAnnotation) {
			value = ((AddOntologyAnnotation)objectToRender).getAnnotation();
		} else if (objectToRender instanceof RemoveOntologyAnnotation) {
			value = ((RemoveOntologyAnnotation)objectToRender).getAnnotation();
		} else {
			//do nothing
		}
		if (value instanceof String) {
			return defaultListCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		} else {
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.ui.renderer.OWLCellRenderer#getIcon(java.lang.Object)
	 */
	@Override
	protected Icon getIcon(Object object) {
		if (objectToRender instanceof AddAxiom 
				|| objectToRender instanceof AddImport
				|| objectToRender instanceof AddOntologyAnnotation
				|| objectToRender instanceof AddPrefixChange
				) {
			return iconAdd;
		} else if(objectToRender instanceof RemoveAxiom
			|| objectToRender instanceof RemoveImport
			|| objectToRender instanceof RemoveOntologyAnnotation
			|| objectToRender instanceof RemovePrefixChange
			) {
			return iconRemove;
		} else {
			return super.getIcon(object);
		}
	}	
}
