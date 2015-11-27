package gov.miamidade.hgowl.plugin.ui;

import java.awt.Color;
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
				// JTable table = (JTable) me.getSource();
				// Point p = me.getPoint();
				// int row = table.rowAtPoint(p);
				if (me.getClickCount() == 2)
				{
					Component parent = table.getParent();
					while (!(parent instanceof JDialog))
					{
						if (parent instanceof JOptionPane)
							((JOptionPane) parent).setValue(JOptionPane.OK_OPTION);
						parent = parent.getParent();
					}
					((JDialog) parent).setVisible(false);
					((JDialog) parent).dispose();
				}
			}
		});
		return table;
	}

	public static Color blend(Color c0, Color c1)
	{
		double totalAlpha = c0.getAlpha() + c1.getAlpha();
		double weight0 = c0.getAlpha() / totalAlpha;
		double weight1 = c1.getAlpha() / totalAlpha;

		double r = weight0 * c0.getRed() + weight1 * c1.getRed();
		double g = weight0 * c0.getGreen() + weight1 * c1.getGreen();
		double b = weight0 * c0.getBlue() + weight1 * c1.getBlue();
		double a = Math.max(c0.getAlpha(), c1.getAlpha());

		return new Color((int) r, (int) g, (int) b, (int) a);
	}
}
