package gov.miamidade.hgowl.plugin;

import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Provides static access to resource bundles and messages/labels
 * within them.
 * 
 * @author Borislav Iordanov
 *
 */
public class Bundles
{	
	static String [] locations = new String [] {
		"HyperGraphPluginLabelsBundle"	
	};
	
	static Bundles instance = new Bundles();	

	ArrayList<ResourceBundle> bundles = new ArrayList<ResourceBundle>();
	
	Bundles()
	{
		for (String location : locations)
			bundles.add(ResourceBundle.getBundle(location));
	}
	
	public String value(String key)
	{
		return value(key, "");
	}
	
	public String value(String key, String def)
	{
		for (ResourceBundle bundle : bundles)
		{
			String s = bundle.getString(key);
			if (s != null)
				return s;
		}
		return def;
	}
}
