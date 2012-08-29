package gov.miamidade.hgowl.plugin.owl;


import javax.swing.JOptionPane;

import gov.miamidade.hgowl.plugin.HGOwlProperties;
import gov.miamidade.hgowl.plugin.owl.model.HGOwlModelManagerImpl;
import gov.miamidade.hgowl.plugin.owlapi.apibinding.PHGDBOntologyManagerImpl;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.PeerViewPanel;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.RemoteRepositoryViewPanel;

import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.CentralDistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.CentralDistributedOntology.CentralDistributionMode;
import org.hypergraphdb.app.owl.versioning.distributed.DistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.PeerDistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowserRepositoryActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.PullActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.PushActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowserRepositoryActivity.BrowseEntry;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.workflow.ActivityResult;
import org.protege.editor.owl.OWLEditorKitFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * VDHGOwlEditorKit.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 26, 2012
 */
public class VDHGOwlEditorKit extends VHGOwlEditorKit {

	public enum OntologyDistributionState {
		ONTO_NOT_SHARED, 
		ONTO_SHARED_DISTRIBUTED,
		ONTO_SHARED_CENTRAL_CLIENT,
		ONTO_SHARED_CENTRAL_SERVER,
	}

	VDHGDBOntologyRepository repository; 
	
	HGPeerIdentity selectedRemotePeer = null;
	
	public VDHGOwlEditorKit(OWLEditorKitFactory editorKitFactory) {
		super(editorKitFactory);
	}

	protected void initialise(){
		super.initialise();
		repository = getVersionedRepository();
	}

	public void handlePushActiveRequest() {
		if (!ensureRemotePeerAccessible()) return;
		VersionedOntology vo = getActiveAsVersionedOntology();
		if (vo == null) return;
		int confirm = JOptionPane.showConfirmDialog(getWorkspace(),
				"Pushing " + vo.toString() + 
				"\n to " + selectedRemotePeer + 
				"\n Press OK to start Push. Please wait for the completed message and follow progess on console. ",
				"P2P Push In Progress",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (confirm != JOptionPane.OK_OPTION) {
			// user cancelled.
			return;
		}
		PushActivity pa = repository.push(vo, selectedRemotePeer);
		try {
			ActivityResult paa = pa.getFuture().get();
			if (paa.getException() == null) {
				JOptionPane.showMessageDialog(getWorkspace(),
						"Push completed with the following message: " + pa.getCompletedMessage(),
						"P2P Push Complete",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				throw paa.getException();
			}
		} catch (Throwable e) {
	        JOptionPane.showMessageDialog(getWorkspace(),
	                e.toString() +  " - "+ e.getMessage(),
	                "P2P Push Error",
	                JOptionPane.ERROR_MESSAGE);
		}
	}

	public void handlePullActiveRequest() {
		if (!ensureRemotePeerAccessible()) return;
		VersionedOntology vo = getActiveAsVersionedOntology();
		if (vo == null) return;
		int confirm = JOptionPane.showConfirmDialog(getWorkspace(),
				"Pulling for " + vo.toString() + 
				"\n from " + selectedRemotePeer + 
				"\n Press OK to start Pull. Please wait for the completed message and follow progess on console. ",
				"P2P Pull in Progress",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (confirm != JOptionPane.OK_OPTION) {
			// user cancelled.
			return;
		}
		PullActivity pa = repository.pull(vo, selectedRemotePeer);
		try {
			ActivityResult paa = pa.getFuture().get();
			if (paa.getException() == null) {
				JOptionPane.showMessageDialog(getWorkspace(),
						"Pulling " + vo.toString() + 
						"\n from " + selectedRemotePeer + 
						"\n completed with the following message: \n" + pa.getCompletedMessage(),
						"P2P Pull Complete",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				throw paa.getException();
			}
		} catch (Throwable e) {
	        JOptionPane.showMessageDialog(getWorkspace(),
	                e.toString() +  " - "+ e.getMessage(),
	                "P2P Pull Error",
	                JOptionPane.ERROR_MESSAGE);
		}
		//Fire Onto Reloaded
		try {
			this.getOWLModelManager().reload(vo.getWorkingSetData());
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

	public void handlePullAnyRequest() {
		if (!ensureRemotePeerAccessible()) return;
		BrowserRepositoryActivity bra = repository.browseRemote(selectedRemotePeer);
		try {
			ActivityResult braa = bra.getFuture().get();
			BrowseEntry remoteEntry = RemoteRepositoryViewPanel.showBrowseEntrySelectionDialog(getWorkspace(), selectedRemotePeer, bra.getRepositoryBrowseEntries());
			if (remoteEntry != null && braa.getException() == null) {
				//USER CONFIRM
				int confirm = JOptionPane.showConfirmDialog(getWorkspace(),
						"Pulling " + remoteEntry.toString() + 
						"\n from " + selectedRemotePeer + 
						"\n Press ok to start.",
						"P2P Pull Any",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
				if (confirm != JOptionPane.OK_OPTION) {
					// 	user cancelled.
					return;
				}
				// START PULL
				PullActivity pa = repository.pull(remoteEntry.getUuid(), selectedRemotePeer);
				ActivityResult paa = pa.getFuture().get();
				if (paa.getException() == null) {
					JOptionPane.showMessageDialog(getWorkspace(),
							"Pulling " + remoteEntry.toString() + 
							"\n from " + selectedRemotePeer + 
							"\n completed with the following message: " + pa.getCompletedMessage(),
							"P2P Pull Any Complete",
							JOptionPane.INFORMATION_MESSAGE);
					//TODO if active was pulled:
					//refresh all.
					try {
						this.getOWLModelManager().reload(repository.getVersionControlledOntology(remoteEntry.getUuid()).getWorkingSetData());
					} catch (OWLOntologyCreationException e) {
						e.printStackTrace();
					}
				} else {
					throw paa.getException();
				}
			} else {
				JOptionPane.showMessageDialog(getWorkspace(),
						"No remote ontology selected ",
						"P2P Pull Aborted",
						JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (Throwable e) {
	        JOptionPane.showMessageDialog(getWorkspace(),
	                e.toString() +  " - "+ e.getMessage(),
	                "P2P Pull Error",
	                JOptionPane.ERROR_MESSAGE);
		}
	}

	public void handleStartNetworkingRequest() {
		//ask for username and password
		if (isNetworking()) {
	        JOptionPane.showMessageDialog(getWorkspace(),
	                "You are currently connected and need to sign off before you can sign in again.",
	                "P2P Sign In Error",
	                JOptionPane.INFORMATION_MESSAGE);
	        return;
		}
		String hostname = HGOwlProperties.getInstance().getP2pServer();
		String userName = HGOwlProperties.getInstance().getP2pUser();
		String password = HGOwlProperties.getInstance().getP2pPass();
		if (hostname == null || hostname.length() < 5 || userName.length() < 2) {
	        JOptionPane.showMessageDialog(getWorkspace(),
	                "Please configure P2P server, userName and password in Preferences.",
	                "P2P Sign In Error",
	                JOptionPane.ERROR_MESSAGE);
	        return;
		}
		boolean startOK = repository.startNetworking(userName, password, hostname);
		if (startOK) {
	        JOptionPane.showMessageDialog(getWorkspace(),
	                "Networking started as user " + userName + " at server " + hostname + "." ,
	                "P2P Signed In.",
	                JOptionPane.INFORMATION_MESSAGE);
		} else {
	        JOptionPane.showMessageDialog(getWorkspace(),
	                "Could not start networking as user " + userName + " at server " + hostname + "." 
	                + "\n Check the console output for exceptions.",
	                "P2P Sign In Error.",
	                JOptionPane.ERROR_MESSAGE);
		}
	}

	public void handleStopNetworkingRequest() {
		if (!isNetworking()) {
	        JOptionPane.showMessageDialog(getWorkspace(),
	                "You are not currently connected.",
	                "P2P Sign Off",
	                JOptionPane.INFORMATION_MESSAGE);
	        return;
		}
		repository.stopNetworking();
		selectedRemotePeer = null;
        JOptionPane.showMessageDialog(getWorkspace(),
                "You were signed off.",
                "P2P Sign Off",
                JOptionPane.INFORMATION_MESSAGE);
	}

	public void handleSelectRemotePeerRequest() {
		if (isNetworking()) {
			if (HGOwlProperties.getInstance().isP2pAskForRemote()) {
		        JOptionPane.showMessageDialog(getWorkspace(),
		                "Your selection will be ignored, because AskForRemote is set to true in File/Preferences/Hypergraph/P2P.",
		                "P2P Selection Ignored",
		                JOptionPane.WARNING_MESSAGE);
			}
			selectedRemotePeer = PeerViewPanel.showPeerSelectionDialog(getWorkspace(), repository.getPeer());
		} else {
			JOptionPane.showMessageDialog(getWorkspace(),
					"You need to be signed in to select a remote peer.",
					"P2P Not Signed In",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	public void handleStatusRequest() {
	
	}
	
	public boolean isNetworking() {
		return repository.getPeer() != null && repository.getPeer().getPeerInterface().isConnected(); 
	}
	
	/**
	 * Checks if this peer is connected, the selected peer is available and opens a selection dialog. 
	 * @return
	 */
	public boolean ensureRemotePeerAccessible() {
		if (isNetworking()) {
			if (repository.getPeer().getConnectedPeers().contains(selectedRemotePeer)
					&& !HGOwlProperties.getInstance().isP2pAskForRemote()) {
				return true;
			} else {
				selectedRemotePeer = PeerViewPanel.showPeerSelectionDialog(getWorkspace(), repository.getPeer());
				return selectedRemotePeer != null;
			}
		} else {
	        JOptionPane.showMessageDialog(getWorkspace(),
	                "You are not signed in.",
	                "P2P Error.",
	                JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
    public VDHGDBOntologyRepository getVersionedRepository() {
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		PHGDBOntologyManagerImpl  hom = (PHGDBOntologyManagerImpl)hmm.getOWLOntologyManager(); 
		return (VDHGDBOntologyRepository)hom.getOntologyRepository();
    }
    
    /**
     * @return the current active onto as versionedOntology
     */
    public VersionedOntology getActiveAsVersionedOntology() {
    	VersionedOntology vo = null;
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		OWLOntology activeOnto = hmm.getActiveOntology();
		VHGDBOntologyRepository vor = getVersionedRepository();
		if (vor.isVersionControlled(activeOnto)) {
			vo = vor.getVersionControlledOntology(activeOnto);
		} else {
			System.out.println("Active ontology not version controlled.");
			JOptionPane.showMessageDialog(getWorkspace(),
                "Active ontology not version controlled: \r\n" + activeOnto.getOntologyID(),
                "P2P Active not versioned",
                JOptionPane.WARNING_MESSAGE);
		}
		return vo;
    }

    public boolean isActiveOntologyShared() {
    	return getActiveOntologyDistributionState() != OntologyDistributionState.ONTO_NOT_SHARED;
    }
    
    public OntologyDistributionState getActiveOntologyDistributionState() {
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		OWLOntology activeOnto = hmm.getActiveOntology();
    	DistributedOntology donto = getVersionedRepository().getDistributedOntology(activeOnto);
    	if (donto == null) {
    		return OntologyDistributionState.ONTO_NOT_SHARED;
    	} else if (donto instanceof CentralDistributedOntology) {
    		CentralDistributedOntology cdonto = (CentralDistributedOntology)donto;
    		if (cdonto.getCentralDistributionMode() == CentralDistributionMode.CLIENT) {
    			return OntologyDistributionState.ONTO_SHARED_CENTRAL_CLIENT;
    		} else {
    			return OntologyDistributionState.ONTO_SHARED_CENTRAL_SERVER;
    		}
    	} else if (donto instanceof PeerDistributedOntology) {
    		return OntologyDistributionState.ONTO_SHARED_DISTRIBUTED;
    	} else {
    		throw new IllegalStateException("getActiveOntologyDistributionState unknown for: " + donto);
    	}
    }
}