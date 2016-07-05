package gov.miamidade.hgowl.plugin.ui;

import java.awt.Component;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * 
 * <p>
 * Use this as base class for plugin dialogs so one can cancel with ESC key. Really annoying that JDialog
 * doesn't work this way by default.
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public class DialogBase extends JDialog
{
    private static final long serialVersionUID = 1L;

    protected void closeWithEsc()
    {
        ActionListener escListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
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

    public DialogBase(Component c, String title, boolean modal)
    {
    	super(SwingUtilities.getWindowAncestor(c), title, modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
    	closeWithEsc();
    }
    
    public DialogBase(Frame owner, String title, boolean modal)
    {
        super(owner, title, modal);
        closeWithEsc();        
    }
}
