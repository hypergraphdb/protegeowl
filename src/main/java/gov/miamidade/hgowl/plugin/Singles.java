package gov.miamidade.hgowl.plugin;

import java.util.concurrent.Callable;

import javax.swing.JOptionPane;

import org.hypergraphdb.app.owl.util.ImplUtils;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
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
	static volatile VDHGDBOntologyRepository ontologyRepository;
	
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
				peer.getObjectContext().put(VDHGDBOntologyRepository.OBJECTCONTEXT_REPOSITORY, 
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
	public static VDHGDBOntologyRepository vdRepo()
	{
		if (ontologyRepository == null)
		{
			synchronized (Singles.class)
			{
				
				ontologyRepository = new VDHGDBOntologyRepository(
					HGOwlProperties.getInstance().getHgLocationFolderPath(),
					new MaybeRef<HyperGraphPeer>(peerFactory, onFailedPeer)					
				);
			}
		}
		return ontologyRepository;
	}
}