package gov.miamidade.hgowl.plugin;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class HGDBActivator implements BundleActivator {

	@Override
	public void start(BundleContext arg0) throws Exception {
		System.out.println("HGOWL DB Started");		
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		System.out.println("HGOWL DB Stopped");		
	}

}
