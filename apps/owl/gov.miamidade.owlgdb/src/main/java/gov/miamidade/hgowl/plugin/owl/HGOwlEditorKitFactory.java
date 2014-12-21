package gov.miamidade.hgowl.plugin.owl;

import gov.miamidade.hgowl.plugin.HGDBActivator;

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

	public static final List<String> OWL_EXTENSIONS = Arrays.asList("hgdb", "HyperGraph");

	public EditorKit createEditorKit()
	{
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
				// System.out.println("HGOWL canLoad: true " + s);
				return true;
			}
		}
		// System.out.println("HGOWL canLoad: false: " + s);
		return false;
	}

	public boolean isValidDescriptor(EditorKitDescriptor descriptor)
	{
		HGDBActivator.hgdbKitOnTop();
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