package gov.miamidade.hgowl.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * ProgressDialog.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 7, 2012
 */
public class ProgressDialog extends JDialog {
	
	private static final long serialVersionUID = -260095893581693742L;
	private JProgressBar progressBar;
	private JButton cancelButton;
	private JLabel timeLabel;
	private Date startDate = new Date();
	private volatile boolean isCanceled = false;
	private java.util.List<String> finishedTasks = new LinkedList<String>();

	private NumberFormat numberFormat = new DecimalFormat("###00");

	private Object LOCK = new Integer(5);

	/**
	 * 
	 * @param title
	 * @param modal
	 */
	public ProgressDialog(final Window parent, final String title, final boolean modal, final int width,
			final int height) {
		super(parent);
		createUI(title, modal, width, height);
	}

	protected void createUI(String title, boolean modal, int width, int height) {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		setAlwaysOnTop(true);
		setModal(modal);
		setTitle(title);
		progressBar.setStringPainted(true);
		progressBar.setString("please wait...");
		add(progressBar, BorderLayout.CENTER);
		timeLabel = new JLabel("00:00");
		JPanel buttonPanel = new JPanel();
		// Cancel button
		cancelButton = new JButton("Cancel");			
		buttonPanel.add(cancelButton);
		add(timeLabel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.SOUTH);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isCanceled = true;
			}
		});
		setSize(width, height);
	}

	/**
	 * Thread safe.
	 * 
	 * @return
	 */
	public boolean isCancelled() {
		return isCanceled;
	}

	/**
	 * Thread safe.
	 */
	public void setVisible(final boolean visible) {
		synchronized (LOCK) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						ProgressDialog.super.setVisible(visible);
					}
				});
			}
	}

	/**
	 * Thread safe, even if called from multiple threads.
	 * 
	 * @param size
	 */
	public void setTaskSize(final int size) {
		synchronized (LOCK) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						if (size != progressBar.getMaximum()) {
							if (progressBar.getValue() > 0) {
								// Keep old task time
								finishedTasks.add(getTime());
							}
							progressBar.setMaximum(size);
							setStartTime();
							repaint();
						}
					}
				});
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void setStartTime() {
		startDate = new Date();
	}
	
	public String getTimeLabelString() {
		String returnVal = "";
		int i = 0; 
		for (String s : finishedTasks) {
			i ++;
			returnVal += i + " " + s + ", ";
		}
		returnVal += getTime();
		return returnVal;
	}
	
	public String getTime() {
		Date now = new Date();
		long time = now.getTime() - startDate.getTime();
		int secs = (int) time / 1000;
		int mins = secs / 60;
		int hours = mins / 60;
		mins = mins % 60;
		secs = secs % 60;
		return "" + numberFormat.format(hours) + ":" + numberFormat.format(mins) + ":" + numberFormat.format(secs);
	}

	/**
	 * Thread safe, even if called from multiple threads.
	 * 
	 * @param progress
	 */
	public void setTaskProgress(final int progress) {
		synchronized (LOCK) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						if (progress != progressBar.getValue()) {
							progressBar.setString("" + progress + " / " + progressBar.getMaximum());
							progressBar.setValue(progress);
							timeLabel.setText(getTimeLabelString());
							repaint();
						}
					}
				});
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
