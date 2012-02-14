package gov.miamidade.hgowl.plugin.ui.repository;

import java.awt.Component;

import gov.miamidade.hgowl.plugin.owl.model.HGOntologyRepositoryEntry;

import javax.swing.JOptionPane;

import org.protege.editor.core.OntologyRepository;
import org.protege.editor.core.ui.util.JOptionPaneEx;

/**
 * VRepositoryViewPanel.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 24, 2012
 */
public class VRepositoryViewPanel extends RepositoryViewPanel {

	private static final long serialVersionUID = 8762858168955963521L;

	/**
	 * @param repository
	 */
	public VRepositoryViewPanel(OntologyRepository repository) {
		super(repository);
	}
	
    public static HGOntologyRepositoryEntry showAddToVersionControlDialog(Component parent, OntologyRepository repository) {
        repository.refresh();
        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
        int ret = JOptionPaneEx.showConfirmDialog(parent, "Add Ontology to Version control (" + repository.getName() + ")", panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.getTable());
        if(ret == JOptionPane.OK_OPTION) {
        	HGOntologyRepositoryEntry ore = panel.getTable().getSelectedEntry(); 
        	if (ore == null) {
        		showNoOntologySelected(parent);
        	}
            return ore; 
        }
        return null;
    }

    public static HGOntologyRepositoryEntry showRemoveFromVersionControlDialog(Component parent, OntologyRepository repository) {
        repository.refresh();
        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
        int ret = JOptionPaneEx.showConfirmDialog(parent, "Remove Ontology from Version control (" + repository.getName() + ")", panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.getTable());
        if(ret == JOptionPane.OK_OPTION) {
        	HGOntologyRepositoryEntry ore = panel.getTable().getSelectedEntry(); 
        	if (ore == null) {
        		showNoOntologySelected(parent);
        	}
            return ore; 
        }
        return null;
    }

    public static HGOntologyRepositoryEntry showCommitDialog(Component parent, OntologyRepository repository) {
        repository.refresh();
        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
        int ret = JOptionPaneEx.showConfirmDialog(parent, "Commit Ontology - Version control (" + repository.getName() + ")", panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.getTable());
        if(ret == JOptionPane.OK_OPTION) {
        	HGOntologyRepositoryEntry ore = panel.getTable().getSelectedEntry(); 
        	if (ore == null) {
        		showNoOntologySelected(parent);
        	}
            return ore; 
        }
        return null;
    }

    public static HGOntologyRepositoryEntry showRollbackDialog(Component parent, OntologyRepository repository) {
        repository.refresh();
        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
        int ret = JOptionPaneEx.showConfirmDialog(parent, "Rollback Ontology - Version control (" + repository.getName() + ")", panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.getTable());
        if(ret == JOptionPane.OK_OPTION) {
        	HGOntologyRepositoryEntry ore = panel.getTable().getSelectedEntry(); 
        	if (ore == null) {
        		showNoOntologySelected(parent);
        	}
            return ore; 
        }
        return null;
    }

    public static HGOntologyRepositoryEntry showRevertOneDialog(Component parent, OntologyRepository repository) {
        repository.refresh();
        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
        int ret = JOptionPaneEx.showConfirmDialog(parent, "Revert Ontology by one revision - Version control (" + repository.getName() + ")", panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.getTable());
        if(ret == JOptionPane.OK_OPTION) {
        	HGOntologyRepositoryEntry ore = panel.getTable().getSelectedEntry(); 
        	if (ore == null) {
        		showNoOntologySelected(parent);
        	}
            return ore; 
        }
        return null;
    }
    
    public static void showNoOntologySelected(Component parent) {
		JOptionPane.showMessageDialog(parent,
    	        "You did not select an ontology.",
                "Hypergraph Versioning - None selected",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
