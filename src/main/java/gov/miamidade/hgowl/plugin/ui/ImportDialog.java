package gov.miamidade.hgowl.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.semanticweb.owlapi.model.OWLOntology;

public class ImportDialog extends DialogBase
{
	private static final long serialVersionUID = 1L;
	private OWLOntology ont;
	private boolean proceed = false;
	private JCheckBox importImports;
	private JCheckBox loadFromDatabase;
	private JCheckBox startVersioning;
	private Component parent;
	
	public ImportDialog(Component parent, OWLOntology ont)
	{
		super(parent, "Import Ontology", true);
		this.ont = ont;
		this.parent = parent;
	}

	public ImportDialog showDialog()
	{
		setVisible(true);
		return this;
	}
	
	public ImportDialog build()
	{
		setLayout(new BorderLayout(5, 5));
		String message = "<html><p>This in-memory ontology will be stored into the Hypergraph Ontology Repository."
				+ "This process is estimated to take one minute per 35000 Axioms.</p><br><p>" + ont.getOntologyID().toString()
				+ " has " + ont.getAxiomCount() + " Axioms. </p><br>"
				+ "<p>Please be patient. A Success Dialog will pop up when the process is finished.</p></html>";
		JPanel northPanel = new JPanel(new BorderLayout(5, 5));
		northPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		northPanel.add(new JLabel(message), BorderLayout.NORTH);
		add(northPanel, BorderLayout.NORTH);
		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
		importImports = new JCheckBox("Store imported ontologies as well.");
		importImports.setSelected(true);
		optionsPanel.add(importImports);
		loadFromDatabase = new JCheckBox("Reload ontology from database immediately after import.");
		loadFromDatabase.setSelected(true);
		loadFromDatabase.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (!loadFromDatabase.isSelected())
					startVersioning.setSelected(false);
			}
		});
		optionsPanel.add(loadFromDatabase);
		startVersioning = new JCheckBox("Put the ontology under version control.");
		startVersioning.setSelected(true);
		optionsPanel.add(startVersioning);		
		add(optionsPanel, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton btn = new JButton("Import");
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { proceed = true; closeDialog(); }
		});
		buttonPanel.add(btn);
		btn = new JButton("Cancel");
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { closeDialog(); }
		});
		buttonPanel.add(btn);		
		add(buttonPanel, BorderLayout.SOUTH);
		setSize(500, 300);
		setLocationRelativeTo(parent);		
		return this;
	}	
	
	public ImportDialog closeDialog()
	{
		this.setVisible(false);
		this.dispose();
		return this;
	}	
	
	public boolean proceed()
	{
		return proceed;
	}
	
	public boolean importImports()
	{
		return importImports.isSelected();
	}
	
	public boolean loadFromDatabase()
	{
		return loadFromDatabase.isSelected();
	}
	
	public boolean startVersioning()
	{
		return startVersioning.isSelected();
	}
}