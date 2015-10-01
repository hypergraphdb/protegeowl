package gov.miamidade.hgowl.plugin.owl;

import gov.miamidade.hgowl.plugin.HGOwlProperties;
import gov.miamidade.hgowl.plugin.owl.model.HGOwlModelManagerImpl;
import gov.miamidade.hgowl.plugin.owlapi.apibinding.PHGDBOntologyManagerImpl;
import gov.miamidade.hgowl.plugin.ui.render.VDHGOwlIconProviderImpl;
import gov.miamidade.hgowl.plugin.ui.versioning.VHGCommitDialog;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.PeerViewPanel;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.RemoteRepositoryViewPanel;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.VDRenderer;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.versioning;
import org.hypergraphdb.app.owl.versioning.distributed.ClientCentralizedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.DistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.OntologyDatabasePeer;
import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowseRepositoryActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowseRepositoryActivity.BrowseEntry;
import org.hypergraphdb.app.owl.versioning.distributed.activity.VersionUpdateActivity;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.workflow.ActivityResult;
import org.protege.editor.core.OntologyRepository;
import org.protege.editor.core.OntologyRepositoryManager;
import org.protege.editor.owl.OWLEditorKitFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * VDHGOwlEditorKit contains UI functions for editing distributed (shared)
 * versioned ontologies.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 26, 2012
 */
public class VDHGOwlEditorKit extends VHGOwlEditorKit
{

	public static int ACTIVITY_TIMEOUT_SECS = 180;

	OntologyDatabasePeer repository;

	HGPeerIdentity selectedRemotePeer = null;

	public VDHGOwlEditorKit(OWLEditorKitFactory editorKitFactory)
	{
		super(editorKitFactory);
		System.out.println("ACTIVITY TIMEOUT IS " + ACTIVITY_TIMEOUT_SECS
				+ " secs");
	}

	protected void initialise()
	{
		super.initialise();
		repository = getDistributedRepository();
		if (HGOwlProperties.getInstance().isP2pAutoSignIn())
			try { repository.startNetworking(); }
			catch (Throwable t) { t.printStackTrace(System.err); }
	}

	protected void initializeIconProvider()
	{
		getWorkspace().setOWLIconProvider(
				new VDHGOwlIconProviderImpl(getModelManager(), this));
	}

	private void reportActivityResult(VersionUpdateActivity activity)
	{
		if (activity.getState().isCompleted())
			JOptionPane.showMessageDialog(null, "Operation completed successfully." + 
					(activity.completedMessage() != null ? activity.completedMessage() : ""));
		else if (activity.getState().isFailed())
			JOptionPane.showMessageDialog(null, "Operation failed." + 
					(activity.completedMessage() != null ? activity.completedMessage() : ""));
		else if (activity.getState().isCanceled())
			JOptionPane.showMessageDialog(null, "Operation canceled." + 
					(activity.completedMessage() != null ? activity.completedMessage() : ""));		
		else
			JOptionPane.showMessageDialog(null, "Operation did not finish properly (state " +
					activity.getState() + ", " + 
					(activity.completedMessage() != null ? activity.completedMessage() : ""));		
	}

	/**
	 * An ontology is published at a remote peer assuming that peer doesn't already
	 * have a copy.
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public void publishActive()	
	{
		if (! (getActiveOntology() instanceof HGDBOntology) )
		{
			JOptionPane.showMessageDialog(null, "The ontology " + 
							getActiveOntology().getOntologyID().getDefaultDocumentIRI() + 
							" is not HyperGraphDB backed and cannot be published for versioning.");
			return;
		}
		HGDBOntology activeOntology = (HGDBOntology) getActiveOntology();
		if (PeerViewPanel.showPeerSelectionDialog(getWorkspace(), repository) != JOptionPane.OK_OPTION)
			return;
		HGPeerIdentity targetPeer = PeerViewPanel.getSelectedPeer();
//		RemoteOntology remoteOnto = graph.getOne(hg.and(hg.type(RemoteOntology.class), 
//				hg.eq("ontologyHandle", ontologyHandle),
//				hg.eq("repository", remoteRepository)));
//		
		VersionUpdateActivity activity = repository.publish(activeOntology.getAtomHandle(), 
															targetPeer);
		try
		{
			activity.getFuture().get();
			reportActivityResult(activity);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			JOptionPane.showMessageDialog(null, "Error, please copy and paste complete stack trace and send to dev team.");			
		}
	}
	
	public void pushActive()
	{
		HGDBOntology activeOntology = (HGDBOntology) getActiveOntology();
		if (PeerViewPanel.showPeerSelectionDialog(getWorkspace(), repository) != JOptionPane.OK_OPTION)
			return;
		HGPeerIdentity serverPeer = PeerViewPanel.getSelectedPeer();
		int confirm = JOptionPane
				.showConfirmDialog(
						getWorkspace(),
						"Pushing "
								+ VDRenderer.render(activeOntology)
								+ "\n to "
								+ VDRenderer.render(serverPeer)
								+ " "
								+ repository.getPeerUserId(serverPeer)
								+ "\n Press OK to start Push. Please wait for the completed message and follow progess on console. ",
						"Hypergraph Team - Push - Confirm",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.INFORMATION_MESSAGE);
		if (confirm != JOptionPane.OK_OPTION)
		{
			// user cancelled.
			return;
		}
		VersionUpdateActivity pa = repository.push(activeOntology.getAtomHandle(), serverPeer);
		try
		{
			ActivityResult paa = pa.getFuture().get(ACTIVITY_TIMEOUT_SECS,
					TimeUnit.SECONDS);
			if (paa.getException() == null)
			{
				JOptionPane.showMessageDialog(
						getWorkspace(),
						"Push completed with the following message: "
								+ pa.completedMessage(),
						"Hypergraph Team - Push Complete",
						JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				throw paa.getException();
			}
		}
		catch (Throwable t)
		{
			showException(t, "System Error while pushing ontology");
			return;
		}
	}

	public void pullActive()
	{
		if (! (getActiveOntology() instanceof HGDBOntology) )
		{
			JOptionPane.showMessageDialog(null, "The ontology " + 
							getActiveOntology().getOntologyID().getDefaultDocumentIRI() + 
							" is not HyperGraphDB backed and cannot be pulled/sychronized with a remote copy.");
			return;
		}
		HGDBOntology activeOntology = (HGDBOntology) getActiveOntology();
		if (!versionManager().isVersioned(activeOntology.getAtomHandle()))
		{
			JOptionPane.showMessageDialog(null, "The ontology " + 
					getActiveOntology().getOntologyID().getDefaultDocumentIRI() + 
					" is not versioned and cannot be pulled/sychronized with a remote copy.");
			return;			
		}
		if (PeerViewPanel.showPeerSelectionDialog(getWorkspace(), repository) != JOptionPane.OK_OPTION)
			return;
		VersionedOntology versioned = versionManager().versioned(activeOntology.getAtomHandle());		
		HGPeerIdentity targetPeer = PeerViewPanel.getSelectedPeer();
		HGHandle currentBranch = versioned.revision().branchHandle(); 
		boolean atbranchhead = currentBranch != null && 
							   versioned.branchHead(currentBranch) == versioned.revision();				
		VersionUpdateActivity activity = repository.pull(versioned, targetPeer);
		try
		{
			activity.getFuture().get();
			// Did we pull changes on the current branch we are working on?
			if (/*atbranchhead && */ activity.getState().isCompleted())
			{
				Revision branchHead = versioned.branchHead(currentBranch);
				if (branchHead != versioned.revision()) // Object ref compare is ok b/w atoms
				{
					if (versioning.isPrior(getDistributedRepository().getHyperGraph(), 
										   versioned.getCurrentRevision(), 
										   branchHead.getAtomHandle()))						
						versioned.goTo(branchHead);
					else
						versioned.merge(versionManager().user(), 
										"auto merge", 
										versioned.revision(), branchHead);
				}
			}
			reportActivityResult(activity);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			JOptionPane.showMessageDialog(null, "Error, please copy and paste complete stack trace and send to dev team.");			
		}
	}

	public void handleStartNetworkingRequest()
	{
		// ask for username and password
		if (isNetworking())
		{
			JOptionPane
					.showMessageDialog(
							getWorkspace(),
							"You are currently connected and need to sign off before you can sign in again.",
							"Hypergraph Team - Sign In - Error",
							JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		String hostname = HGOwlProperties.getInstance().getP2pServer();
		String userName = HGOwlProperties.getInstance().getP2pUser();
//		String password = HGOwlProperties.getInstance().getP2pPass();
//		String room = HGOwlProperties.getInstance().getP2pRoom();
		if (hostname == null || hostname.length() < 5 || userName.length() < 2)
		{
			JOptionPane
					.showMessageDialog(
							getWorkspace(),
							"Please configure P2P server, userName and password in Preferences.",
							"Hypergraph Team - Sign In - Error",
							JOptionPane.ERROR_MESSAGE);
			return;
		}
		boolean startOK = repository.startNetworking();
		
//		startNetworking(userName, 
//													 password,
//													 hostname, 
//													 room);
		if (startOK)
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"Networking started as user " + userName + " at server "
							+ hostname + ".", "Hypergraph Team - Signed In",
					JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"Could not start networking as user " + userName
							+ " at server " + hostname + "."
							+ "\n Check the console output for exceptions.",
					"Hypergraph Team - Sign In - Error.",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void handleStopNetworkingRequest()
	{
		if (!isNetworking())
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"You are not currently connected.",
					"Hypergraph Team - Sign Off",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		repository.stopNetworking();
		selectedRemotePeer = null;
		JOptionPane.showMessageDialog(getWorkspace(), "You were signed off.",
				"Hypergraph Team - Sign Off", JOptionPane.INFORMATION_MESSAGE);
	}

	public void handleSelectRemotePeerRequest()
	{
		if (isNetworking())
		{
			if (HGOwlProperties.getInstance().isP2pAskForRemote())
			{
				JOptionPane
						.showMessageDialog(
								getWorkspace(),
								"Your selection will be ignored, because AskForRemote is set to true in File/Preferences/Hypergraph/P2P.",
								"Hypergraph Team - Selection - Ignored",
								JOptionPane.WARNING_MESSAGE);
			}
			int userChoice = PeerViewPanel.showPeerSelectionDialog(
					getWorkspace(), repository);
			if (userChoice == JOptionPane.OK_OPTION)
			{
				selectedRemotePeer = PeerViewPanel.getSelectedPeer();
			}
		}
		else
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"You need to be signed in to select a remote peer.",
					"P2P Not Signed In", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void handleStatusRequest()
	{
		
	}

	public void cloneOntology()
	{
		HGPeerIdentity serverPeer = selectRemotePeer("Hypergraph Team - Clone - Select Server");
		if (serverPeer == null)
			return;
		String userId = repository.getPeerUserId(serverPeer);
		BrowseRepositoryActivity bra = repository.browseRemote(serverPeer);
		try
		{
			ActivityResult braa = bra.getFuture().get();
			if (braa.getException() != null)
				throw braa.getException();
			BrowseEntry remoteEntry = RemoteRepositoryViewPanel
					.showBrowseEntrySelectionDialog(getWorkspace(), serverPeer,
							userId, bra.getRepositoryBrowseEntries());
			if (remoteEntry != null && braa.getException() == null)
			{
				// USER CONFIRM
				if (repository.getHyperGraph().get(remoteEntry.getUuid()) == null)
				{
					int confirm = JOptionPane.showConfirmDialog(
							getWorkspace(),
							"Checking out " + remoteEntry.toString()
									+ "\n from "
									+ VDRenderer.render(serverPeer)
									+ "\n Press ok to start.",
							"Hypergraph Team - Clone - Confirm",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.INFORMATION_MESSAGE);
					if (confirm != JOptionPane.OK_OPTION)
					{
						// user cancelled.
						return;
					}
					// START PULL
					VersionUpdateActivity pa = repository.clone(remoteEntry.getUuid(), serverPeer);
					//
					// TODO BLOCK in NON AWT THREAD, let changes be applied in
					// AWT.
					//
					ActivityResult paa = pa.getFuture().get(ACTIVITY_TIMEOUT_SECS, TimeUnit.SECONDS);
					if (paa.getException() == null)
					{
						JOptionPane
								.showMessageDialog(
										getWorkspace(),
										"Clone of "
												+ remoteEntry.toString()
												+ "\n from "
												+ VDRenderer.render(serverPeer)
												+ "\n completed with the following message: "
												+ pa.completedMessage()
												+ "\n The new ontolgy will be loaded.",
										"Hypergraph Team - Clone - Complete",
										JOptionPane.INFORMATION_MESSAGE);
						// Load this ontology as active
						OWLOntology loadedOnto = getModelManager()
								.getOWLOntologyManager().loadOntology(
										IRI.create(remoteEntry
												.getOwlOntologyDocumentIRI()));
						getModelManager().setActiveOntology(loadedOnto);
					}
					else
					{
						throw paa.getException();
					}
				}
				else
				{
					// Already Exists!
					JOptionPane.showMessageDialog(getWorkspace(),
							"The remote ontology already exists locally.",
							"Hypergraph Team - Clone - Aborted",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else
			{
				JOptionPane.showMessageDialog(getWorkspace(),
						"No remote ontology selected ",
						"Hypergraph Team - Clone - Aborted",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
		catch (Throwable t)
		{
			showException(t, "System error while checking out ontology");
			return;
			// JOptionPane.showMessageDialog(getWorkspace(),
			// getRenderedActivityException(t),
			// "Hypergraph Team - Clone - Error",
			// JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Checks if local pending changes may be committed and sent to the server
	 * based on a comparison. Will open explanatory dialogs if a reason is found
	 * that would prevent a commit.
	 * 
	 * @param dOnto
	 * @param server
	 * @return
	 */
	public boolean checkCommitPushAllowed(DistributedOntology dOnto, HGPeerIdentity server)
	{
		boolean mayCommit = true;
//		VersionedOntologyComparisonResult result = null;
//		try
//		{
//			result = repository.compareOntologyToRemote(dOnto, server,
//					ACTIVITY_TIMEOUT_SECS);
//		}
//		catch (Throwable t)
//		{
//			showException(t, "System error while comparing to remote");
//		}
//		if (result != null)
//		{
//			if (result.isConflict())
//			{
//				JOptionPane
//						.showMessageDialog(
//								getWorkspace(),
//								"Cannot commit: A conflict between the local and the server's history exists. "
//										+ "\r\n Use compare for details. You might need to Revert to an older revision.",
//								"Hypergraph Team - Commit - Conflict ",
//								JOptionPane.ERROR_MESSAGE);
//				mayCommit = false;
//			}
//			else if (result.isTargetNewer())
//			{
//				JOptionPane
//						.showMessageDialog(
//								getWorkspace(),
//								"Cannot commit: The server has newer Revisions that need to be pulled first."
//										+ "\r\n Use pull to merge your pending changes before you commit. ",
//								"Hypergraph Team - Commit - Remote is newer",
//								JOptionPane.WARNING_MESSAGE);
//				mayCommit = false;
//			}
//			else
//			{
//				mayCommit = true;
//			}
//		}
//		else
//		{
//			JOptionPane
//					.showMessageDialog(
//							getWorkspace(),
//							"Cannot commit: There was a problem comparing the local history to the server's ontology."
//									+ "\r\n This might mean that the server was not available or a timeout occured. ",
//							"Hypergraph Team - Commit - Error on Compare",
//							JOptionPane.ERROR_MESSAGE);
//			mayCommit = false;
//		}
		return mayCommit;
	}

//	public boolean checkPullAllowed(VersionedOntologyComparisonResult result)
//	{
//		boolean mayPull = true;
//		if (result != null)
//		{
//			if (result.isConflict())
//			{
//				JOptionPane
//						.showMessageDialog(
//								getWorkspace(),
//								"Cannot pull: A conflict between the local and the server's history exists. "
//										+ "\r\n Use compare for details. You might need to revert to an older revision.",
//								"Hypergraph Team - Pull - Conflict ",
//								JOptionPane.ERROR_MESSAGE);
//				mayPull = false;
//			}
//			else if (result.isSourceNewer())
//			{
//				JOptionPane
//						.showMessageDialog(
//								getWorkspace(),
//								"Cannot pull: The local history has newer revisions that need to be pushed."
//										+ "\r\n Use push to upload your pending changes to the server. ",
//								"Hypergraph Team - Pull - Source is newer",
//								JOptionPane.WARNING_MESSAGE);
//				mayPull = false;
//			}
//			else if (result.isSourceTargetEqual())
//			{
//				JOptionPane.showMessageDialog(getWorkspace(),
//						"No need to pull: The local and remote revision history "
//								+ "\r\n of the ontology are equal ",
//						"Hypergraph Team - Pull - Local and Remote are equal",
//						JOptionPane.WARNING_MESSAGE);
//				mayPull = false;
//			}
//			else
//			{
//				// Target is newer
//				mayPull = true;
//			}
//		}
//		else
//		{
//			JOptionPane
//					.showMessageDialog(
//							getWorkspace(),
//							"Cannot pull: There was a problem comparing the local history to the server's ontology."
//									+ "\r\n This might mean that the server was not available or a timeout occured. ",
//							"Hypergraph Team - Commit - Error on compare",
//							JOptionPane.ERROR_MESSAGE);
//			mayPull = false;
//		}
//		return mayPull;
//	}

	public boolean checkPullAllowed(DistributedOntology dOnto, HGPeerIdentity server)
	{
//		VersionedOntologyComparisonResult result = null;
//		try
//		{
//			result = repository.compareOntologyToRemote(dOnto, server,
//					ACTIVITY_TIMEOUT_SECS);
//		}
//		catch (Throwable t)
//		{
//			showException(t, "System error while comparing ontologies");
//			return false;
//		}
//		return checkPullAllowed(result);
		return true;
	}

	public VHGCommitDialog showUserCommitDialog(VersionedOntology vo,
			OWLOntology onto)
	{
		// TODO
		return null;//VHGCommitDialog.showDialog(getWorkspace(), vo, onto);
	}

	public void handleUpdateActiveClientOntologyRequest()
	{
//		DistributedOntology dOnto = getActiveOntologyAsDistributed();
//		HGPeerIdentity serverPeer = getServerForDistributedOntology(dOnto);
//		String serverPeerUser = repository.getPeerUserId(serverPeer);
//		if (serverPeer == null)
//			return;
		// Show Pull Dialog with incoming changes
		// Offer to Pull until a certain revision
//		VersionedOntologyComparisonResult result = null;
//		try
//		{
//			result = repository.compareOntologyToRemote(dOnto, serverPeer, ACTIVITY_TIMEOUT_SECS);
//		}
//		catch (Exception e)
//		{
//			showException(e, "System error while comparing to remote.");
//			return;
//		}
//		if (checkPullAllowed(dOnto, serverPeer))
//		{
//			int confirm = PullDistributedOntologyViewPanel
//					.showUpdateVersionedOntologyDialog(getWorkspace(), result,
//							dOnto, serverPeerUser, serverPeer);
//			Revision lastPullRevision = PullDistributedOntologyViewPanel
//					.getLastPullRevision();
//			System.out.println("Last Pull Revision = " + lastPullRevision);
//			if (confirm != JOptionPane.OK_OPTION)
//			{
//				// user cancelled.
//				return;
//			}
//			if (lastPullRevision == null)
//			{
//				JOptionPane.showMessageDialog(getWorkspace(),
//						"There was no revision checked for the update. ",
//						"Hypergraph Team - Update - No revision to transmit",
//						JOptionPane.WARNING_MESSAGE);
//				return;
//			}
//			if (areYouSure("Hyperaph Team - Update", "Are you sure to update?"))
//			{
//				PullActivity pa = repository.pullUntilRevision(dOnto, serverPeer, lastPullRevision);
//				try
//				{
//					// TODO BLOCK in NON AWT THREAD, let changes be applied in
//					// AWT.
//					ActivityResult paa = pa.getFuture().get(60,
//							TimeUnit.SECONDS);
//					if (paa.getException() == null)
//					{
//						JOptionPane
//								.showMessageDialog(
//										getWorkspace(),
//										"Updating "
//												+ VDRenderer.render(dOnto)
//												+ "\n from "
//												+ VDRenderer.render(serverPeer)
//												+ " "
//												+ repository
//														.getPeerUserId(serverPeer)
//												+ "\n completed with the following message: \n"
//												+ pa.getCompletedMessage(),
//										"Hypergraph Team - Update - Complete",
//										JOptionPane.INFORMATION_MESSAGE);
//					}
//					else
//					{
//						throw paa.getException();
//					}
//				}
//				catch (Throwable e)
//				{
//					showException(e, "System error while pull until revision.");
//					// JOptionPane.showMessageDialog(getWorkspace(),
//					// e.toString() + " - " + e.getMessage(),
//					// "Team Update Error",
//					// JOptionPane.ERROR_MESSAGE);
//				}
//				// Fire Onto Reloaded
//				causeViewUpdate();
//			}
//			else
//			{
//				// user cancelled
//			}
//		} // else UI already showed reason
	}

	/**
	 * For ClientCentralOntologies: returns either the clients server, if it is
	 * available on the network. For PeerOntologies: a user selected available
	 * server null otherwise.
	 * 
	 * @param dOnto
	 * @return
	 */
	public HGPeerIdentity getServerForDistributedOntology(DistributedOntology dOnto)
	{
		if (dOnto == null)
			throw new IllegalArgumentException("Distributed Ontology null");
		HGPeerIdentity server;
		if (dOnto instanceof ClientCentralizedOntology)
		{
			ClientCentralizedOntology cDOnto = (ClientCentralizedOntology) dOnto;
			server = cDOnto.getServerPeer();
		}
		else
		{
			server = selectRemotePeer("Hypergraph Team - - Select Server for "
					+ dOnto);
		}
		if (server != null)
		{
			if (!repository.isNetworking())
			{
				JOptionPane.showMessageDialog(getWorkspace(),
						"You are not signed in.", "Hypergraph Team - Error ",
						JOptionPane.ERROR_MESSAGE);
				server = null;
			}
			else
			{
				if (!repository.getPeer().getConnectedPeers().contains(server))
				{
					JOptionPane.showMessageDialog(getWorkspace(),
							"The server or peer is not accessible on the network. \r\n"
									+ server,
							"Hypergraph Team - Network Error ",
							JOptionPane.ERROR_MESSAGE);
					server = null;
				}
			}
		}
		return server;
	}

//	public DistributedOntology getActiveOntologyAsDistributed()
//	{
//		HGDBOntology activeOnto = (HGDBOntology) getActiveOntology();
//		DistributedOntology dOnto = repository.getDistributedOntology(activeOnto);
//		if (dOnto == null)
//		{
//			JOptionPane.showMessageDialog(getWorkspace(),
//					"The active ontology is not distributed:" + "\r\n "
//							+ activeOnto + " ", "Team - Error ",
//					JOptionPane.ERROR_MESSAGE);
//		}
//		return dOnto;
//	}

	public boolean isNetworking()
	{
		return repository.getPeer() != null &&
			   repository.getPeer().getPeerInterface() != null &&  
			   repository.getPeer().getPeerInterface().isConnected();
	}

	public HGPeerIdentity selectRemotePeer(String title)
	{
		if (isNetworking())
		{
			PeerViewPanel.showServerSelectionDialog(title, getWorkspace(), repository);
			return PeerViewPanel.getSelectedPeer();
		}
		else
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"You are not signed in.", "Team Share - Error.",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/**
	 * Checks if this peer is connected, the selected peer is available and
	 * opens a selection dialog.
	 * 
	 * @return
	 */
	public boolean ensureRemotePeerAccessible()
	{
		if (isNetworking())
		{
			if (repository.getPeer().getConnectedPeers().contains(selectedRemotePeer)
					&& !HGOwlProperties.getInstance().isP2pAskForRemote())
			{
				return true;
			}
			else
			{
				int userChoice = PeerViewPanel.showPeerSelectionDialog(
						getWorkspace(), repository);
				if (userChoice == JOptionPane.CANCEL_OPTION)
				{
					return repository.getPeer().getConnectedPeers().contains(selectedRemotePeer);
				}
				else
				{
					selectedRemotePeer = PeerViewPanel.getSelectedPeer();
				}
				return selectedRemotePeer != null;
			}
		}
		else
		{
			JOptionPane.showMessageDialog(getWorkspace(),
					"You are not signed in.", "Team Share - Error.",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public OntologyDatabasePeer getDistributedRepository()
	{
		HGOwlModelManagerImpl hmm = (HGOwlModelManagerImpl) getOWLModelManager();
		PHGDBOntologyManagerImpl hom = (PHGDBOntologyManagerImpl) hmm.getOWLOntologyManager();
		return (OntologyDatabasePeer) hom.getOntologyRepository();
	}

	/**
	 * @return the current active onto as versionedOntology
	 */
	public VersionedOntology getActiveAsVersionedOntology()
	{
		VersionedOntology vo = null;
		// TODO
//		HGOwlModelManagerImpl hmm = (HGOwlModelManagerImpl) getOWLModelManager();
//		OWLOntology activeOnto = hmm.getActiveOntology();
//		VHGDBOntologyRepository vor = getVersionedRepository();
//		if (vor.isVersionControlled(activeOnto))
//		{
//			vo = vor.getVersionControlledOntology(activeOnto);
//		}
//		else
//		{
//			System.out.println("Active ontology not version controlled.");
//			JOptionPane.showMessageDialog(
//					getWorkspace(),
//					"Active ontology not version controlled: \r\n"
//							+ activeOnto.getOntologyID(),
//					"P2P Active not versioned", JOptionPane.WARNING_MESSAGE);
//		}
		return vo;
	}

	/**
	 * Returns the VD OntologyRepository implementation of our plugin (Protege
	 * Interface).
	 * 
	 * @return
	 */
	public OntologyRepository getProtegeRepository()
	{
		Collection<OntologyRepository> repositories = OntologyRepositoryManager
				.getManager().getOntologyRepositories();
		for (OntologyRepository cur : repositories)
		{
			if (cur instanceof VDHGOwlOntologyRepository)
			{
				return cur;
			}
		}
		return null;
	}

	public void showException(Throwable t, String title)
	{
		Throwable cur = t;
		while (cur.getCause() != null)
		{
			cur = cur.getCause();
		}
		String excRendered = cur.toString();
		JOptionPane.showMessageDialog(getWorkspace(),
				"System Error: \r\n Caused by: " + excRendered + " \r\n",
				title, JOptionPane.ERROR_MESSAGE);
		// Print Exception to console
		System.err.println("System Error " + new Date() + " r\n" + excRendered);
		t.printStackTrace(System.err);
	}
}