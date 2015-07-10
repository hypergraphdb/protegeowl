package gov.miamidade.hgowl.plugin.ui;

import java.awt.BorderLayout;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.hypergraphdb.app.owl.gc.GarbageCollector;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.owl.OWLEditorKit;


public class HGRunGCModeSelectionPanel extends JPanel {
	
	/**
	 * Indices have to match to GarbageCollector MODEs.
	 */
	public static final String[] MODE_STRINGS = new String[]{ 
		"Full Mode", 
		"Deleted Ontologies", 
		"Disconnected Axioms", 
		"Disconnected Other",
		"Disconnected Entities"		
	};
    /**
     * 
     */
    private static final long serialVersionUID = 1207904671457290130L;

    private JComboBox comboBox;

    private JLabel messageLabel;


    public HGRunGCModeSelectionPanel() {
        //List<Object> options = Arrays.<Object>asList((Object[])MODE_STRINGS);
        comboBox = new JComboBox(MODE_STRINGS);
        setLayout(new BorderLayout(12, 12));
        add(comboBox, BorderLayout.SOUTH);
        comboBox.setSelectedItem(MODE_STRINGS[GarbageCollector.MODE_FULL]);
    }

    public void setMessage(String message) {
        if (messageLabel == null){
            messageLabel = new JLabel(message);
            add(messageLabel, BorderLayout.NORTH);
        }
        else{
            messageLabel.setText(message);
        }
        revalidate();
    }

    public int getSelectedMode(){
        return comboBox.getSelectedIndex(); 
    }

    public void addItemListener(ItemListener l){
        comboBox.addItemListener(l);
    }

    public void removeItemListener(ItemListener l){
        comboBox.removeItemListener(l);
    }

    /**
     * 
     * @param editorKit
     * @param message
     * @return an int matching GarbageCollector mode constants or -1 if cancelled.
     */
    public static int showDialog(OWLEditorKit editorKit, String message) {
    	HGRunGCModeSelectionPanel panel = new HGRunGCModeSelectionPanel();
    	if (message != null){
    		panel.setMessage(message);
    	}
		int ret = JOptionPaneEx.showConfirmDialog(editorKit.getWorkspace(),
				"Select a garbage collection mode ",
				panel,
				JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION,
				panel.comboBox);
		if (ret != JOptionPane.OK_OPTION) {
			return -1;
		} else {
			return panel.getSelectedMode();
    	}
    }
}
