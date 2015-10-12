package gov.miamidade.hgowl.plugin;

import java.util.concurrent.Callable;

import javax.swing.JOptionPane;

import org.hypergraphdb.app.owl.util.ImplUtils;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.distributed.OntologyDatabasePeer;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.util.Ref;

/**
 * <p>
 * Singleton objects, statically available in the Protege Plugin environment.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class Singles
{
	static volatile OntologyDatabasePeer ontologyRepository;
	static volatile VersionManager versionManager;
	
	static Ref<HyperGraphPeer> peerFactory = new Ref<HyperGraphPeer>()
	{
		public HyperGraphPeer get()
		{
			HGOwlProperties props = HGOwlProperties.getInstance();
			String connectionString = "hgpeer://" + props.getP2pUser() + ":" + 
					props.getP2pPass() +  "@" + props.getP2pServer() + "#" + props.getP2pRoom();
			HyperGraphPeer peer = ImplUtils.peer(connectionString, 
												 HGOwlProperties.getInstance().getHgLocationFolderPath());
			if (peer.getPeerInterface() == null || !peer.getPeerInterface().isConnected())
			{
				peer.getObjectContext().put(OntologyDatabasePeer.OBJECTCONTEXT_REPOSITORY, 
											ontologyRepository);
			}
			return peer;
		}
	};
	
	static Callable<Boolean> onFailedPeer = new Callable<Boolean>()
	{
		public Boolean call()
		{
			JOptionPane.showMessageDialog(
					null,
					"Please configure P2P server, userName and password in Preferences.",
					"Hypergraph Team - Sign In - Error",
					JOptionPane.ERROR_MESSAGE);			
			return false;
		}
	};
	
	/**
	 * Return the global VDHGDBOntologyRepository instance.
	 */
	public static OntologyDatabasePeer vdRepo()
	{
		if (ontologyRepository == null)
		{
			synchronized (Singles.class)
			{
				ontologyRepository = new OntologyDatabasePeer(
					HGOwlProperties.getInstance().getHgLocationFolderPath(),
					new MaybeRef<HyperGraphPeer>(peerFactory, onFailedPeer)					
				);
			}
		}
		return ontologyRepository;
	}
	
	public static VersionManager versionManager()
	{
		if (versionManager == null)
		{
			synchronized (Singles.class)
			{
				// What is user is not configured here? We need to adjust that
				// check and disable UI action that use the version manager until
				// the user is configured.
				versionManager = new VersionManager(vdRepo().getHyperGraph(),
								HGOwlProperties.getInstance().getP2pUser());
			}
		}
		return versionManager;
	}
	
	/**
	 * Return the resource bundle entry point for localized messages.
	 */
	public static Bundles bundles()
	{
		return Bundles.instance;
	}
}