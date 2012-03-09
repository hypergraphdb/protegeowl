package gov.miamidade.hgowl.plugin.ui.repository;

import gov.miamidade.hgowl.plugin.owl.model.HGOntologyRepositoryEntry;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.hypergraphdb.app.owl.versioning.VersionedOntology;

/**
 * VOntologyViewPanel.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 30, 2012
 */
public class VOntologyViewPanel extends JPanel {
    
	private static final long serialVersionUID = 159528341514944079L;

	private VersionedOntology versionedOntology;

    private JTable table;

	public VOntologyViewPanel(VersionedOntology vOnto) {
        this.versionedOntology = vOnto;
        createUI();
    }

    private void createUI() {
    	setLayout(new BorderLayout());
        table = new JTable(new VOntologyTableModel(versionedOntology));
    	//0.Master 1.Revision 2.TimeStamp 3.User 4.Comment 5.#Changes (after revision)
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

    public Dimension getPreferredSize() {
        return new Dimension(850, 400);
    }

    public static HGOntologyRepositoryEntry showRevisionDialog(Component parent, VersionedOntology vo) {
        VOntologyViewPanel panel = new VOntologyViewPanel(vo);
        String title;
        title = "Versioned Ontology History " 
    		+ vo.getWorkingSetData().getOntologyID().getOntologyIRI()
    		+ " ID: " + vo.getHeadRevision().getOntologyUUID();
        JOptionPane.showMessageDialog(parent, panel, title, JOptionPane.PLAIN_MESSAGE);
//        if(ret == JOptionPane.OK_OPTION) {
//        	//DO NOTHING FOR NOW
//        }
        return null;
    }

//    public static OntologyRepositoryEntry showDeleteDialog(OntologyRepository repository) {
//        repository.refresh();
//        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
//        int ret = JOptionPaneEx.showConfirmDialog(null, "Delete Ontology from " + repository.getName(), panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.table);
//        if(ret == JOptionPane.OK_OPTION) {
//            return panel.table.getSelectedEntry();
//        }
//        return null;
//    }

	
}
