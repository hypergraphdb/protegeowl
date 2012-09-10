package gov.miamidade.hgowl.plugin.ui.render;

import java.net.URL;

import gov.miamidade.hgowl.plugin.owl.VDHGOwlEditorKit;
import gov.miamidade.hgowl.plugin.owl.VHGOwlEditorKit;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyImpl;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.OWLIconProviderImpl;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * VHGOwlIconProviderImpl provies an Icon for versioned ontologies.
 * Delegates to superclass as much as possible. 
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 30, 2012
 */
public class VDHGOwlIconProviderImpl extends VHGOwlIconProviderImpl {

	//Icon for Shared ontologies
	public static final String ICON_DBVD_FILENAME = "gov/miamidade/hgowl/plugin/ui/render/ontologyDBVD.png";
	
	private boolean superClassIconMode = true;
	private Icon icon; 
	private Icon ontologyDBVD; 
	private boolean isShared = false;
	
	/**
	 * @param owlModelManager
	 */
	public VDHGOwlIconProviderImpl(OWLModelManager owlModelManager, VDHGOwlEditorKit vdhgEditorKit) {
		super(owlModelManager, vdhgEditorKit);
		//this.vhgEditorKit = vhgEditorKit;
	}
	
	protected void initIcon() {
		super.initIcon();
        ClassLoader loader = this.getClass().getClassLoader();
        URL url = loader.getResource(ICON_DBVD_FILENAME);
        if (url == null) System.err.println("NOT FOUND" + ICON_DBVD_FILENAME);
        ontologyDBVD = new ImageIcon(url);
	}

    public Icon getIcon() {
    	if (superClassIconMode || !isShared) {
    		return super.getIcon();
    	} else {
    		return icon;
    	}
    }

    /**
     * uses superclass, except on ontology. 
     */
    public Icon getIcon(OWLObject owlObject) {
    	//To initialize superClassIconMode in VHG Icon provider.
    	icon = super.getIcon(owlObject);
    	superClassIconMode = !(owlObject instanceof OWLOntology);
    	if (superClassIconMode) {
    		return super.getIcon(owlObject);
    	} else {
            try {
                icon = null;
                owlObject.accept(this);
                return icon;
            }
            catch (Exception e) {
                return null;
            }
    	}
    }

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.ui.renderer.OWLIconProviderImpl#visit(org.semanticweb.owlapi.model.OWLOntology)
	 */
	@Override
	public void visit(OWLOntology owlOntology) {
		if (owlOntology instanceof HGDBOntologyImpl) {
			VDHGOwlEditorKit kit = (VDHGOwlEditorKit) super.vhgEditorKit;
			isShared = kit.getDistributedRepository().isDistributed((HGDBOntology)owlOntology);
			if (isShared) {
				icon = ontologyDBVD;
			} else {
				super.visit(owlOntology);
				icon = super.getIcon();
			}
		} else {
			super.visit(owlOntology);
			icon = super.getIcon();
		}
	}
}