package gov.miamidade.hgowl.plugin.ui;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

public class DialogBase extends JDialog
{
    private static final long serialVersionUID = 1L;

    protected void closeWithEsc()
    {
        ActionListener escListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                System.exit(0);
            }
        };
        getRootPane().registerKeyboardAction(escListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);        
    }
    
    public DialogBase(Window w)
    {
    	super(w);
        closeWithEsc();    	
    }
    
    public DialogBase(Frame owner, String title, boolean modal)
    {
        super(owner, title, modal);
        closeWithEsc();        
    }
}
