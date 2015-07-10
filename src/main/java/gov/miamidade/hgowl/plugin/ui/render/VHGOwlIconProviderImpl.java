package gov.miamidade.hgowl.plugin.ui.render;

import java.net.URL;

import gov.miamidade.hgowl.plugin.owl.VHGOwlEditorKit;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.hypergraphdb.app.owl.HGDBOntologyImpl;
import org.osgi.framework.FrameworkUtil;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.OWLIconProviderImpl;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * VHGOwlIconProviderImpl provies an Icon for versioned ontologies. Delegates to
 * superclass as much as possible.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 30, 2012
 */
public class VHGOwlIconProviderImpl extends OWLIconProviderImpl
{

	public static final String ICON_DBV_FILENAME = "gov/miamidade/hgowl/plugin/ui/render/ontologyDBV.png";

	private boolean superClassIconMode = true;
	private Icon icon;
	protected VHGOwlEditorKit vhgEditorKit;
	private Icon ontologyDBV;

	/**
	 * @param owlModelManager
	 */
	public VHGOwlIconProviderImpl(OWLModelManager owlModelManager,
			VHGOwlEditorKit vhgEditorKit)
	{
		super(owlModelManager);
		this.vhgEditorKit = vhgEditorKit;
		initIcon();
	}

	protected void initIcon()
	{
		URL url = FrameworkUtil.getBundle(this.getClass()).getBundleContext().getBundle().getEntry(ICON_DBV_FILENAME);
//		ClassLoader loader = .getClassLoader();
//		URL url = loader.getResource(ICON_DBV_FILENAME);
		if (url == null)
			System.err.println("NOT FOUND" + ICON_DBV_FILENAME + " bundle: ");
		else
			ontologyDBV = new ImageIcon(url);
	}

	public Icon getIcon()
	{
		if (superClassIconMode)
		{
			return super.getIcon();
		}
		else
		{
			return icon;
		}
	}

	/**
	 * uses superclass, except on ontology.
	 */
	public Icon getIcon(OWLObject owlObject)
	{
		superClassIconMode = !(owlObject instanceof OWLOntology);
		if (superClassIconMode)
		{
			return super.getIcon(owlObject);
		}
		else
		{
			try
			{
				icon = null;
				owlObject.accept(this);
				return icon;
			}
			catch (Exception e)
			{
				return null;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.protege.editor.owl.ui.renderer.OWLIconProviderImpl#visit(org.semanticweb
	 * .owlapi.model.OWLOntology)
	 */
	@Override
	public void visit(OWLOntology owlOntology)
	{
		if (owlOntology instanceof HGDBOntologyImpl)
		{
//			if (vhgEditorKit.getVersionedRepository().isVersionControlled(
//					owlOntology))
			if (vhgEditorKit.versionManager().isVersioned(
					((HGDBOntologyImpl)owlOntology).getAtomHandle()))
			{
				icon = ontologyDBV;
			}
			else
			{
				super.visit(owlOntology);
				icon = super.getIcon();
			}
		}
		else
		{
			super.visit(owlOntology);
			icon = super.getIcon();
		}
	}
}