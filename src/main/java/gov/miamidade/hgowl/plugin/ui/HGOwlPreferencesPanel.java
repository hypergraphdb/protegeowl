package gov.miamidade.hgowl.plugin.ui;

import gov.miamidade.hgowl.plugin.HGOwlProperties;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

/**
 * HGOwlPreferencesPane.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Dec 6, 2011
 */
public class HGOwlPreferencesPanel extends OWLPreferencesPanel {
 
	private static final long serialVersionUID = 2787046958572470215L;

	private java.util.List<OWLPreferencesPanel> optionPages = new ArrayList<OWLPreferencesPanel>();

    private JTabbedPane tabPane;

    public static final String DEFAULT_PAGE = "General Options";

    public void applyChanges() {
        for (OWLPreferencesPanel optionPage : optionPages) {
            optionPage.applyChanges();
        }
    }

    public void initialise() throws Exception {
        setLayout(new BorderLayout());
        tabPane = new JTabbedPane();
        addOptions(new HypergraphDatabaseLocationFolderPanel(), "General Options");
        addOptions(new ShowOtherEditorKitsPanel(), "General Options");
        //addOptions(new ModeOptionsAdapter(), "General Options");
        add(tabPane, BorderLayout.NORTH);
    }

    public void dispose() throws Exception {
        for (OWLPreferencesPanel optionPage : optionPages) {
            optionPage.dispose();
        }
    }

    private void addOptions(OWLPreferencesPanel page, String tabName) throws Exception {
        // If the page does not exist, add it, and add the component
        // to the page.

        Component c = getTab(tabName);
        if(c == null) {
            // Create a new Page
            Box box = new Box(BoxLayout.Y_AXIS);
            box.add(page);
            box.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            tabPane.add(tabName, box);
            optionPages.add(page);
        }
        else {
            Box box = (Box) c;
            box.add(Box.createVerticalStrut(7));
            box.add(page);
            optionPages.add(page);
        }

        page.initialise();
    }

    protected Component getTab(String name) {
        for(int i = 0; i < tabPane.getTabCount(); i++) {
            if(tabPane.getTitleAt(i).equals(name)) {
                return tabPane.getComponentAt(i);
            }
        }
        return null;
    }

    class HypergraphDatabaseLocationFolderPanel extends OWLPreferencesPanel {

	private static final long serialVersionUID = -900161588154332509L;
		private JTextField pathField;

        public void initialise() throws Exception {
            setLayout(new BorderLayout(12, 12));
            setBorder(BorderFactory.createTitledBorder("Hypergraph Database Location Path"));
            add(createUI(), BorderLayout.NORTH);
        }

        public void dispose() throws Exception {
            // do nothing
        }

        protected JComponent createUI() {
            Box panel = new Box(BoxLayout.LINE_AXIS);

            pathField = new JTextField(15);
            pathField.setText(HGOwlProperties.getInstance().getHgLocationFolderPath());

            JButton browseButton = new JButton(new AbstractAction("Browse") {

            	private static final long serialVersionUID = 6762941646097019849L;

                public void actionPerformed(ActionEvent e) {
                    browseForFolder();
                }
            });

            panel.add(new JLabel("Path:"));
            panel.add(pathField);
            panel.add(browseButton);

            return panel;
        }

        protected void browseForFolder() {
            File file = UIUtil.chooseFolder(new JFrame(), "Hypergraph Location Folder");
            if(file != null && file.isDirectory()) {
                pathField.setText(file.getPath());
            }
        }

        public void applyChanges() {
            //DotLayoutEngineProperties.getInstance().setDotProcessPath(pathField.getText());
        	String selectedFolder = pathField.getText();
        	File f = new File(selectedFolder);
        	boolean success = false;
        	if (!pathField.getText().equals(HGOwlProperties.getInstance().getHgLocationFolderPath())) {
	        	if (f.isDirectory()) {
	        		if (f.canWrite()) {
	        			if (f.isAbsolute()) {
	        	        	HGOwlProperties.getInstance().setHgLocationFolderPath(pathField.getText());
	        	        	success = true;
	                		JOptionPane.showMessageDialog(this, "You need to restart Protege for this change to take effect.");        				
	        			} else {
	                		JOptionPane.showMessageDialog(this, "Error: Selected path is relative folder path. Keeping old.");        				
	        			}
	        		} else {
	            		JOptionPane.showMessageDialog(this, "Error: Cannot write to selected path directory. Keeping old.");
	        		}
	        	} else {
	        		JOptionPane.showMessageDialog(this, "Error: Selected path is not a directory. Keeping old.");
	        	}
	        	if (!success) {
	        		JOptionPane.showMessageDialog(this, "Keeping original Hypergraph Location. \r\n" + HGOwlProperties.getInstance().getHgLocationFolderPath());
	                pathField.setText(HGOwlProperties.getInstance().getHgLocationFolderPath());
	        	}
        	}
        }
    }

    class ShowOtherEditorKitsPanel extends OWLPreferencesPanel {

    	private static final long serialVersionUID = -900161588154332509L;
    		private JCheckBox showOthersCB;

            public void initialise() throws Exception {
                setLayout(new BorderLayout(12, 12));
                setBorder(BorderFactory.createTitledBorder("Show other editor kits at startup"));
                add(createUI(), BorderLayout.NORTH);
            }

            public void dispose() throws Exception {
                // do nothing
            }
            
            protected JComponent createUI() {
                Box panel = new Box(BoxLayout.LINE_AXIS);

                showOthersCB = new JCheckBox("Show other Editor Kits at Startup");
                showOthersCB.setSelected(HGOwlProperties.getInstance().isShowLegacyEditorKit());

                panel.add(showOthersCB);
                return panel;
            }

            public void applyChanges() {
            	//System.err.println("IMPLEMENT SET SHOW OTHER KITS, RESTART NECESSARY.");
            	HGOwlProperties.getInstance().setShowLegacyEditorKit(showOthersCB.isSelected());
            }
        }
}