package gov.miamidade.hgowl.plugin.owl;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.hypergraphdb.app.owl.HGDBApplication;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.editorkit.EditorKitDescriptor;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.OWLEditorKitFactory;

/**
 * HGOwlEditorKitFactory creates an Editorkit for Hypergraph OWL.
 * 
 * 
 * 
 * @author Thomas Hilpold
 */
public class HGOwlEditorKitFactory extends OWLEditorKitFactory  {

    public static final String ID = "gov.miamidade.owlgdb.HGOwlEditorKitFactory";

    public static final List<String> OWL_EXTENSIONS = Arrays.asList("hgdb", "HyperGraph");


	public EditorKit createEditorKit() throws Exception {
		if (HGDBApplication.VERSIONING) {
			System.out.println("VHG createEditorKit");
			return new VHGOwlEditorKit(this);
		} else {
			System.out.println("HG createEditorKit");
			return new HGOwlEditorKit(this);
		}
    }
    
    /**
     * Gets the identifier for this <code>EditorKitFactory</code>.
     * @return A <code>String</code> representation of the
     *         clsdescriptioneditor kit factory.
     */
    public String getId() {
        return ID;
    }

    public boolean canLoad(URI uri) {
        String s = uri.toString();
        for (String ext : OWL_EXTENSIONS) {
            if (s.startsWith(ext)) {
            	System.out.println("HGOWL canLoad: true " + s);
                return true;
            }
        }
    	System.out.println("HGOWL canLoad: false: " + s);
        return false;
    }


    public boolean isValidDescriptor(EditorKitDescriptor descriptor) {
        URI uri = descriptor.getURI(OWLEditorKit.URI_KEY);
        if(uri == null || uri.getScheme() == null) {
            return false;
        }
        if (uri.getScheme().equals("hgdb")) {
            return true;
        }
        return true;
    }

}
