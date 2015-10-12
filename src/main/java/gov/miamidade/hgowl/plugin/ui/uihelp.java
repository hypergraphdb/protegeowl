package gov.miamidade.hgowl.plugin.ui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;

public class uihelp
{
	public static JTable okOnDoubleClick(final JTable table)
	{
		table.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent me)
			{
//				JTable table = (JTable) me.getSource();
//				Point p = me.getPoint();
//				int row = table.rowAtPoint(p);
				if (me.getClickCount() == 2)
				{
					Component parent = table.getParent();
					while (! (parent instanceof JDialog) )
					{
						if (parent instanceof JOptionPane)
							((JOptionPane)parent).setValue(JOptionPane.OK_OPTION);
						parent = parent.getParent();
					}
					((JDialog)parent).setVisible(false);
					((JDialog)parent).dispose();
				}
			}
		});
		return table;
	}
}
