package gov.miamidade.hgowl.plugin;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.editorkit.EditorKitFactory;
import org.protege.editor.core.editorkit.EditorKitFactoryPlugin;

public class HGDBActivator implements BundleActivator
{
	public static void hgdbKitOnTop()
	{
		ProtegeManager pm = ProtegeManager.getInstance();

		try
		{
			Field f = ProtegeManager.class.getDeclaredField("editorKitFactoriesMap");
			f.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<EditorKitFactoryPlugin, EditorKitFactory> M = (Map<EditorKitFactoryPlugin, EditorKitFactory>)f.get(pm);
			EditorKitFactoryPlugin hgplugin = null;
			for (EditorKitFactoryPlugin p : M.keySet())
			{
				System.out.println(p.getId() + " " + p.getClass().getName());
				if (p.getId().contains("hgdbprotege"))
				{
					hgplugin = p;
				}
			}
			LinkedHashMap<EditorKitFactoryPlugin, EditorKitFactory> orderedM = new LinkedHashMap<EditorKitFactoryPlugin, EditorKitFactory>();
			orderedM.put(hgplugin, M.get(hgplugin));
			for (EditorKitFactoryPlugin p : M.keySet())
				orderedM.put(p, M.get(p));
			f.set(pm, orderedM);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}		
	}
	
	@Override
	public void start(final BundleContext context) throws Exception
	{
		System.out.println("HGOWL DB Started");
	}

	@Override
	public void stop(BundleContext arg0) throws Exception
	{
		System.out.println("HGOWL DB Stopped");
	}

}