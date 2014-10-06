package gov.miamidade.hgowl.plugin.owl;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.editorkit.EditorKitDescriptor;
import org.protege.editor.core.editorkit.EditorKitFactoryPlugin;
import org.protege.editor.core.editorkit.EditorKitManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.OWLEditorKitFactory;

/**
 * HGOwlEditorKitFactory creates an Editorkit for Hypergraph OWL.
 * 
 * 
 * 
 * @author Thomas Hilpold
 */
public class HGOwlEditorKitFactory extends OWLEditorKitFactory
{

	public static final String ID = "gov.miamidade.owlgdb.HGOwlEditorKitFactory";

	public static final List<String> OWL_EXTENSIONS = Arrays.asList("hgdb",
			"HyperGraph");

	public EditorKit createEditorKit()
	{
//		System.out.println("THE HYPERGRAPHDB EDITOR KIIIIIT FACTORY!!!!!!!!!!!!!!!");
		// if (HGDBApplication.DISTRIBUTED) {
		// if (!HGDBApplication.VERSIONING) throw new
		// IllegalStateException("Use Versioning with Distributed.");
		// System.out.println("VDHG createEditorKit");
		// return new VDHGOwlEditorKit(this);
		// } else if (HGDBApplication.VERSIONING) {
		// System.out.println("VHG createEditorKit");
		// return new VHGOwlEditorKit(this);
		// } else {
		// System.out.println("HG createEditorKit");
		// return new HGOwlEditorKit(this);
		// }
		return new VDHGOwlEditorKit(this);
	}

	/**
	 * Gets the identifier for this <code>EditorKitFactory</code>.
	 * 
	 * @return A <code>String</code> representation of the clsdescriptioneditor
	 *         kit factory.
	 */
	public String getId()
	{
		return ID;
	}

	public boolean canLoad(URI uri)
	{
		String s = uri.toString();
		for (String ext : OWL_EXTENSIONS)
		{
			if (s.startsWith(ext))
			{
//				System.out.println("HGOWL canLoad: true " + s);
				return true;
			}
		}
//		System.out.println("HGOWL canLoad: false: " + s);
		return false;
	}

	public boolean isValidDescriptor(EditorKitDescriptor descriptor)
	{
        ProtegeManager pm = ProtegeManager.getInstance();
        
        // This is the worst hack in this whole plugin: we are removing 
        // Protege's own OWLEditorKit plugin from the ProtegeManager singleton
        // The ProtegeManager does a lookup for all editor kits and finds both
        // Protege's and ours, but then picks one at random pretty much, while we
        // actually want to superceed Protege's editor kit. So we need to disable it,
        // which is impossible, so we remove it from this ProtegeManager's internal list.
        // For that we need to use Java reflection to access a private variable.
        // And we chose to do it in this particular location because that's the first
        // execution point within our code base before that editorkit is actually opened. 
        List<EditorKitFactoryPlugin> editorKitFactoryPlugins = pm.getEditorKitFactoryPlugins();
        try
		{
			Field factoriesField = ProtegeManager.class.getDeclaredField("editorKitFactoriesMap");
			factoriesField.setAccessible(true);
	        for (EditorKitFactoryPlugin p : editorKitFactoryPlugins)
	        {
//	        	System.out.println(p.getId());
	        	if (!p.getId().contains("gov.miamidade"))
	        		((Map)factoriesField.get(pm)).remove(p);
	        }			
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EditorKitManager em = ProtegeManager.getInstance().getEditorKitManager();
		URI uri = descriptor.getURI(OWLEditorKit.URI_KEY);
		if (uri == null || uri.getScheme() == null)
		{
			return false;
		}
		if (uri.getScheme().equals("hgdb"))
		{
			return true;
		}
		return true;
	}
}