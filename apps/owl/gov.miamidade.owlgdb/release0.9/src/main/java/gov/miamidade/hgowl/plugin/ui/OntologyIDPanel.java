package gov.miamidade.hgowl.plugin.ui;

import org.protege.editor.owl.OWLEditorKit;

/**
 * Same as superclass, except Finishes after user done.
 * We can do this as long as we use default hgdb location and hgdb Format. 
 * 
 * Not happy about the hardcoding of the next panel in the superclass! 
 * 
 * @author Thomas Hilpold
 */
public class OntologyIDPanel extends
		org.protege.editor.owl.ui.ontology.wizard.create.OntologyIDPanel {

	public OntologyIDPanel(OWLEditorKit editorKit) {
		super(editorKit);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Object getNextPanelDescriptor() {
		return FINISH;
	}
}
