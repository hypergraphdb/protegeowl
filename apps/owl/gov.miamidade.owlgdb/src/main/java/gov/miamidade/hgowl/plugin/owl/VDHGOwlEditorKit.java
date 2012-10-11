package gov.miamidade.hgowl.plugin.owl;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import gov.miamidade.hgowl.plugin.HGOwlProperties;
import gov.miamidade.hgowl.plugin.owl.model.HGOwlModelManagerImpl;
import gov.miamidade.hgowl.plugin.owlapi.apibinding.PHGDBOntologyManagerImpl;
import gov.miamidade.hgowl.plugin.ui.render.VDHGOwlIconProviderImpl;
import gov.miamidade.hgowl.plugin.ui.versioning.VHGCommitDialog;
import gov.miamidade.hgowl.plugin.ui.versioning.VHGHistoryDialog;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.CommitDialog;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.CompareVersionedOntologyViewPanel;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.PeerViewPanel;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.PullDistributedOntologyViewPanel;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.RemoteRepositoryViewPanel;
import gov.miamidade.hgowl.plugin.ui.versioning.distributed.VDRenderer;

import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.VersionedOntologyComparator.VersionedOntologyComparisonResult;
import org.hypergraphdb.app.owl.versioning.distributed.ClientCentralizedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.DistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.PeerDistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.ServerCentralizedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowseRepositoryActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.PullActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.PushActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowseRepositoryActivity.BrowseEntry;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.workflow.ActivityResult;
import org.protege.editor.core.OntologyRepository;
import org.protege.editor.core.OntologyRepositoryManager;
import org.protege.editor.owl.OWLEditorKitFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * VDHGOwlEditorKit contains UI functions for editing distributed (shared) versioned ontologies.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 26, 2012
 */
public class VDHGOwlEditorKit extends VHGOwlEditorKit {

	public static int ACTIVITY_TIMEOUT_SECS = 180;
	
	public enum OntologyDistributionState {
		ONTO_NOT_SHARED, ONTO_SHARED_DISTRIBUTED, ONTO_SHARED_CENTRAL_CLIENT, ONTO_SHARED_CENTRAL_SERVER,
	}

	VDHGDBOntologyRepository repository;

	HGPeerIdentity selectedRemotePeer = null;

	public VDHGOwlEditorKit(OWLEditorKitFactory editorKitFactory) {
		super(editorKitFactory);
		System.out.println("ACTIVITY TIMEOUT IS " + ACTIVITY_TIMEOUT_SECS + " secs");
	}

	protected void initialise() {
		super.initialise();
		repository = getDistributedRepository();
	}
	
	protected void initializeIconProvider() {
		getWorkspace().setOWLIconProvider(new VDHGOwlIconProviderImpl(modelManager, this));
	}

	public void handleShowHistoryActiveDistributedRequest() {
		DistributedOntology dOnto = getActiveOntologyAsDistributed();
		VHGHistoryDialog.showDialog("Hypergraph Team - History of " + VDRenderer.render(dOnto), getWorkspace(), dOnto.getVersionedOntology(), this);
	} 

	public void handleShareActiveRequest() {
		OWLOntology activeOntology = getActiveOntology();
		if (activeOntology instanceof HGDBOntology) {
			if (isNetworking()) {
				if (repository.isVersionControlled(activeOntology)) {
					if (! repository.isDistributed((HGDBOntology)activeOntology)) {
						VersionedOntology activeVo = repository.getVersionControlledOntology(activeOntology);
						int userChoice = PeerViewPanel.showServerSelectionDialog("Hypergraph Team - Share - Select Ontology Server", getWorkspace(), repository);
						HGPeerIdentity serverPeer;
						if (userChoice == JOptionPane.CANCEL_OPTION) {
							return;
						} else {
							serverPeer = PeerViewPanel.getSelectedPeer();
						}
						if (serverPeer == null) {
							int shareLocalOption = JOptionPane.showConfirmDialog(getWorkspace()
									, "You did not select a server. As an experimental option for experienced users it is possible to have Protege serve your ontology locally. \n\r"
									+ "Do you with to share your ontology locally as server? Click NO if not sure.", 
									"Hypergraph Team - Share - Experimental: Local Server", 
									JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE);
							if (shareLocalOption == JOptionPane.YES_OPTION) {
								repository.shareLocalInServerMode(activeVo);
								JOptionPane.showMessageDialog(getWorkspace(),
										"Sharing active ontology locally in server mode completed.", "Hypergraph Team - Share - Sharing completed.",
										JOptionPane.INFORMATION_MESSAGE);
							}
						} else {
							//Check if exists on server, no push if local newer.
							int shareRemoteServerOption = JOptionPane.showConfirmDialog(getWorkspace(),
									"The active ontology " + VDRenderer.render(activeVo) 
									+ "\r\n will be shared on " + VDRenderer.render(serverPeer) + " "
									+ repository.getPeerUserId(serverPeer) + " \r\n"
									+ "All necessary data will be transmitted to the server. Do you want to continue?.", 
									"Hypergraph Team - Share - Share on Server" ,
									JOptionPane.YES_NO_OPTION,
									JOptionPane.INFORMATION_MESSAGE);
							if (shareRemoteServerOption == JOptionPane.YES_OPTION) {
								try {
									repository.shareRemoteInServerMode(activeVo, serverPeer, ACTIVITY_TIMEOUT_SECS);
								} catch (Throwable t) {
									showException(t, "System Error while sharing remote in server mode");
									return;
								}
								//Update icon
								causeViewUpdate();
								JOptionPane.showMessageDialog(getWorkspace(),
										"Sharing active ontology on selected server completed. All necessary data was sucessfullty transmitted.", "Hypergraph Team - Share - Sharing completed.",
										JOptionPane.INFORMATION_MESSAGE);
							} else {
								JOptionPane.showMessageDialog(getWorkspace(),
										"Active ontology was not shared.", "Hypergraph Team - Share - Abort",
										JOptionPane.WARNING_MESSAGE);
								
							}
						}
					} else {
						JOptionPane.showMessageDialog(getWorkspace(),
								"Active ontology is already shared.", "Hypergraph Team - Share - Problem",
								JOptionPane.WARNING_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(getWorkspace(),
							"Active ontology is not version controled, which is a prerequisite to sharing.", "Hypergraph Team - Share - Problem",
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				//not networking
				JOptionPane.showMessageDialog(getWorkspace(),
						"Please sign in before sharing.", "Hypergraph Team - Share - Problem",
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			// file based is active
			JOptionPane.showMessageDialog(getWorkspace(),
					"Ontology is file based. Please import and add version control before sharing.", "Hypergraph Team - Share - Problem",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void handleShareActiveCancelRequest() {
		DistributedOntology activeOntology = getActiveOntologyAsDistributed();
			int confirm = JOptionPane.showConfirmDialog(getWorkspace(), 
					"Press OK to cancel sharing of the active ontology: " +
					"\r\n" + VDRenderer.render(activeOntology) + "\n ",
					"Hypergraph Team - Share - Cancel Sharing Confirm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
			if (confirm != JOptionPane.OK_OPTION) {
				// user cancelled.
				return;
			}
			repository.cancelSharing(activeOntology);
			JOptionPane.showMessageDialog(getWorkspace(),
					"Sharing of the active ontology was cancelled. The full history is still available.", "Hypergraph Team - Share - Cancel Sharing complete",
					JOptionPane.INFORMATION_MESSAGE);
			//Need to update dropdown to change icon
			causeViewUpdate();
	}

	public void handlePushActiveRequest() {
		HGDBOntology activeOntology = (HGDBOntology)getActiveOntology(); 
		DistributedOntology dOnto = repository.getDistributedOntology(activeOntology);
		HGPeerIdentity serverPeer;
		if (dOnto == null) return;
		if (!(dOnto instanceof ClientCentralizedOntology)) {
			if (!ensureRemotePeerAccessible()) return;
			serverPeer = selectedRemotePeer;
		} else {
			serverPeer = ((ClientCentralizedOntology)dOnto).getServerPeer();
		}
		int confirm = JOptionPane.showConfirmDialog(getWorkspace(), "Pushing " + VDRenderer.render(dOnto) + "\n to "
				+ VDRenderer.render(serverPeer) + " " + repository.getPeerUserId(serverPeer)
				+ "\n Press OK to start Push. Please wait for the completed message and follow progess on console. ",
				"Hypergraph Team - Push - Confirm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (confirm != JOptionPane.OK_OPTION) {
			// user cancelled.
			return;
		}
		PushActivity pa = repository.push(dOnto, serverPeer);
		try {
			ActivityResult paa = pa.getFuture().get(ACTIVITY_TIMEOUT_SECS, TimeUnit.SECONDS);
			if (paa.getException() == null) {
				JOptionPane.showMessageDialog(getWorkspace(),
						"Push completed with the following message: " + pa.getCompletedMessage(), "Hypergraph Team - Push Complete",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				throw paa.getException();
			}
		} catch (Throwable t) {
			showException(t, "System Error while pushing ontology");
			return;
		}
	}

	public void handlePullActiveRequest() {
		HGDBOntology activeOntology = (HGDBOntology)getActiveOntology(); 
		DistributedOntology dOnto = repository.getDistributedOntology(activeOntology);
		HGPeerIdentity serverPeer;
		String action = "Pull";
		if (dOnto == null) return;
		if (!(dOnto instanceof ClientCentralizedOntology)) {
			if (!ensureRemotePeerAccessible()) return;
			serverPeer = selectedRemotePeer;
			action = "Update";
		} else {
			serverPeer = ((ClientCentralizedOntology)dOnto).getServerPeer();
		}
		int confirm = JOptionPane.showConfirmDialog(getWorkspace(), action + " for " + dOnto.toString() + "\n from "
				+ serverPeer + " " + repository.getPeer().getNetworkTarget(serverPeer)
				+ "\n Press OK to start " + action + ". Please wait for the completed message and follow progess on console. ",
				"Hypergraph Team - " + action + " - in Progress", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (confirm != JOptionPane.OK_OPTION) {
			// user cancelled.
			return;
		}
		PullActivity pa = repository.pull(dOnto, serverPeer);
		try {
			//TODO BLOCK in NON AWT THREAD, let changes be applied in AWT.
			ActivityResult paa = pa.getFuture().get(ACTIVITY_TIMEOUT_SECS, TimeUnit.SECONDS);
			if (paa.getException() == null) {
				JOptionPane.showMessageDialog(getWorkspace(),
						action + " " + dOnto.toString() + "\n from " 
						+ serverPeer + " " + repository.getPeer().getNetworkTarget(serverPeer)
								+ "\n completed with the following message: \n" + pa.getCompletedMessage(),
						"Hypergraph Team - " + action + " - Complete", JOptionPane.INFORMATION_MESSAGE);
			} else {
				throw paa.getException();
			}
		} catch (Throwable t) {
			showException(t, "System error while pulling ontology");
			return;
//			JOptionPane.showMessageDialog(getWorkspace(), e.toString() + " - " + e.getMessage(), "Team - "+ action + " - Error",			
//					JOptionPane.ERROR_MESSAGE);
		}
		// Fire Onto Reloaded
		try {
			this.getOWLModelManager().reload(dOnto.getWorkingSetData());
		} catch (OWLOntologyCreationException e) {
			showException(e, "OWLOntologyCreation after pulling ontology");
			return;
		}
	}

//	/**
//	 * This will be PULL NEW NOT PULL ANY!!
//	 */
//	public void handlePullAnyRequest() {
//		if (!ensureRemotePeerAccessible()) return;
//		BrowseRepositoryActivity bra = repository.browseRemote(selectedRemotePeer);
//		try {
//			ActivityResult braa = bra.getFuture().get();
//			BrowseEntry remoteEntry = RemoteRepositoryViewPanel.showBrowseEntrySelectionDialog(getWorkspace(),
//					selectedRemotePeer, bra.getRepositoryBrowseEntries());
//			if (remoteEntry != null && braa.getException() == null) {
//				// USER CONFIRM
//				int confirm = JOptionPane.showConfirmDialog(getWorkspace(), "Pulling " + remoteEntry.toString()
//						+ "\n from " + selectedRemotePeer + "\n Press ok to start.", "P2P Pull Any",
//						JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
//				if (confirm != JOptionPane.OK_OPTION) {
//					// user cancelled.
//					return;
//				}
//				// START PULL
//				PullActivity pa = repository.pullNew(remoteEntry.getUuid(), selectedRemotePeer);
//				ActivityResult paa = pa.getFuture().get();
//				if (paa.getException() == null) {
//					JOptionPane.showMessageDialog(getWorkspace(),
//							"Pulling " + remoteEntry.toString() + "\n from " + selectedRemotePeer
//									+ "\n completed with the following message: " + pa.getCompletedMessage(),
//							"P2P Pull Any Complete", JOptionPane.INFORMATION_MESSAGE);
//					// TODO if active was pulled:
//					// refresh all.
//					try {
//						this.getOWLModelManager().reload(
//								repository.getVersionControlledOntology(remoteEntry.getUuid()).getWorkingSetData());
//					} catch (OWLOntologyCreationException e) {
//						e.printStackTrace();
//					}
//				} else {
//					throw paa.getException();
//				}
//			} else {
//				JOptionPane.showMessageDialog(getWorkspace(), "No remote ontology selected ", "P2P Pull Aborted",
//						JOptionPane.INFORMATION_MESSAGE);
//			}
//		} catch (Throwable e) {
//			JOptionPane.showMessageDialog(getWorkspace(), e.toString() + " - " + e.getMessage(), "P2P Pull Error",
//					JOptionPane.ERROR_MESSAGE);
//		}
//	}

	public void handleStartNetworkingRequest() {
		// ask for username and password
		if (isNetworking()) {
			JOptionPane.showMessageDialog(getWorkspace(),
					"You are currently connected and need to sign off before you can sign in again.",
					"Hypergraph Team - Sign In - Error", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		String hostname = HGOwlProperties.getInstance().getP2pServer();
		String userName = HGOwlProperties.getInstance().getP2pUser();
		String password = HGOwlProperties.getInstance().getP2pPass();
		if (hostname == null || hostname.length() < 5 || userName.length() < 2) {
			JOptionPane.showMessageDialog(getWorkspace(),
					"Please configure P2P server, userName and password in Preferences.", "Hypergraph Team - Sign In - Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		boolean startOK = repository.startNetworking(userName, password, hostname);
		if (startOK) {
			JOptionPane.showMessageDialog(getWorkspace(), "Networking started as user " + userName + " at server "
					+ hostname + ".", "Hypergraph Team - Signed In", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(getWorkspace(), "Could not start networking as user " + userName
					+ " at server " + hostname + "." + "\n Check the console output for exceptions.",
					"Hypergraph Team - Sign In - Error.", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void handleStopNetworkingRequest() {
		if (!isNetworking()) {
			JOptionPane.showMessageDialog(getWorkspace(), "You are not currently connected.", "Hypergraph Team - Sign Off",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		repository.stopNetworking();
		selectedRemotePeer = null;
		JOptionPane.showMessageDialog(getWorkspace(), "You were signed off.", "Hypergraph Team - Sign Off",
				JOptionPane.INFORMATION_MESSAGE);
	}

	public void handleSelectRemotePeerRequest() {
		if (isNetworking()) {
			if (HGOwlProperties.getInstance().isP2pAskForRemote()) {
				JOptionPane
						.showMessageDialog(
								getWorkspace(),
								"Your selection will be ignored, because AskForRemote is set to true in File/Preferences/Hypergraph/P2P.",
								"Hypergraph Team - Selection - Ignored", JOptionPane.WARNING_MESSAGE);
			}
			int userChoice = PeerViewPanel.showPeerSelectionDialog(getWorkspace(), repository);
			if (userChoice == JOptionPane.OK_OPTION) {
				selectedRemotePeer = PeerViewPanel.getSelectedPeer();
			}
		} else {
			JOptionPane.showMessageDialog(getWorkspace(), "You need to be signed in to select a remote peer.",
					"P2P Not Signed In", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void handleStatusRequest() {

	}

	public void handleCheckoutRequest() {
		HGPeerIdentity serverPeer =  selectRemotePeer("Hypergraph Team - Checkout - Select Server");
		if (serverPeer == null) return;
		String userId = repository.getPeerUserId(serverPeer);
		BrowseRepositoryActivity bra = repository.browseRemote(serverPeer);
		try {
			ActivityResult braa = bra.getFuture().get();
			if (braa.getException() != null) throw braa.getException();
			BrowseEntry remoteEntry = RemoteRepositoryViewPanel.showBrowseEntrySelectionDialog(getWorkspace(),
					serverPeer, userId, bra.getRepositoryBrowseEntries());
			if (remoteEntry != null && braa.getException() == null) {
				// USER CONFIRM
				if (repository.getHyperGraph().get(remoteEntry.getUuid()) == null) {
					int confirm = JOptionPane.showConfirmDialog(getWorkspace(), "Checking out " + remoteEntry.toString()
							+ "\n from " + VDRenderer.render(serverPeer) + "\n Press ok to start.", "Hypergraph Team - Checkout - Confirm",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
					if (confirm != JOptionPane.OK_OPTION) {
						// user cancelled.
						return;
					}
					// START PULL
					PullActivity pa = repository.pullNew(remoteEntry.getUuid(), serverPeer);
					//
					//TODO BLOCK in NON AWT THREAD, let changes be applied in AWT.
					//
					ActivityResult paa = pa.getFuture().get(ACTIVITY_TIMEOUT_SECS, TimeUnit.SECONDS);
					if (paa.getException() == null) {
						JOptionPane.showMessageDialog(getWorkspace(),
								"Checkout of " + remoteEntry.toString() + "\n from " + VDRenderer.render(serverPeer)
										+ "\n completed with the following message: " + pa.getCompletedMessage() 
										+ "\n The new ontolgy will be loaded.",
								"Hypergraph Team - Checkout - Complete", JOptionPane.INFORMATION_MESSAGE);
						//Load this ontology as active
						OWLOntology loadedOnto = getModelManager().getOWLOntologyManager().loadOntology(IRI.create(remoteEntry.getOwlOntologyDocumentIRI()));
						getModelManager().setActiveOntology(loadedOnto);
					} else {
						throw paa.getException();
					}
				} else {
					//Already Exists!
					JOptionPane.showMessageDialog(getWorkspace(), "The remote ontology already exists locally.", "Hypergraph Team - Checkout - Aborted",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(getWorkspace(), "No remote ontology selected ", "Hypergraph Team - Checkout - Aborted",
						JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (Throwable t) {
			showException(t, "System error while checking out ontology");
			return;
//			JOptionPane.showMessageDialog(getWorkspace(), getRenderedActivityException(t), "Hypergraph Team - Checkout - Error",
//				JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void handleCommitAndPushActiveClientOntologyRequest() {
		ClientCentralizedOntology activeOnto = (ClientCentralizedOntology)getActiveOntologyAsDistributed();
		HGPeerIdentity server = getServerForDistributedOntology(activeOnto);		
		if (server == null) return;
		String userId = repository.getPeerUserId(server);
		VersionedOntology vo = activeOnto.getVersionedOntology();
		ChangeSet pendingChanges = vo.getWorkingSetChanges();
		if (pendingChanges.isEmpty()) {
			// NO PENDING CHANGES OK
			//System.out.println("No pending changes.");
            JOptionPane.showMessageDialog(getWorkspace(),
                    "No need to commit: No pending changes",
                    "Hypergraph Team - Commit - No Changes to commit",
                    JOptionPane.WARNING_MESSAGE);
		} else if (vo.getNrOfCommittableChanges() == 0) {
            JOptionPane.showMessageDialog(getWorkspace(),
                    "Cannot Commit: All pending changes are conflicts \r\n"
            		+ "Check Team/History.",
                    "Hypergraph Team - Commit - All pending changes are conflicts",
                    JOptionPane.WARNING_MESSAGE);
		} else {
			// 	COMMIT WHAT WHO INCREMENT OK CANCEL
			String title = "Hypergraph Team - Commit " + VDRenderer.render(activeOnto);
			CommitDialog dlg = CommitDialog.showDialog(title, getWorkspace(), activeOnto, server, userId, this);
			if (dlg.isCommitOK()) {
				//Check if allowed
				if (checkCommitPushAllowed(activeOnto, server)) {
					//DO IT LOCALLY 
					vo.commit(getSystemUserName(), Revision.REVISION_INCREMENT, dlg.getCommitComment());
					//Push it to server
					boolean needsUndo = false;
					try {
						PushActivity pa = repository.push(activeOnto, server);
						//Should be ok in AWT thread as no view updates are expected:
						ActivityResult ar = pa.getFuture().get(ACTIVITY_TIMEOUT_SECS, TimeUnit.SECONDS);
						if (ar.getException() != null) {
							throw ar.getException();
						}
			            JOptionPane.showMessageDialog(getWorkspace(),
			                    "All changes were committed and uploaded to the server.",
			                    "Hypergraph Team - Commit - Commit completed",
			                    JOptionPane.INFORMATION_MESSAGE);
					} catch (Throwable t) {
						needsUndo = true;
						showException(t, "System error while pushing ontology");
					} finally {
						if (needsUndo) vo.undoCommit();
					}
				} //else error was already shown to user.
			} 
		}
    }
	/**
	 * Checks if local pending changes may be committed and sent to the server based on a comparison.
	 * Will open explanatory dialogs if a reason is found that would prevent a commit.
	 * 
	 * @param dOnto
	 * @param server
	 * @return
	 */
	public boolean checkCommitPushAllowed(DistributedOntology dOnto, HGPeerIdentity server) {
		boolean mayCommit;
		VersionedOntologyComparisonResult result = null;
		try {
			result = repository.compareOntologyToRemote(dOnto, server, ACTIVITY_TIMEOUT_SECS);
		} catch (Throwable t) {
			showException(t, "System error while comparing to remote");
		}
		if (result != null) {
			if (result.isConflict()) {
	            JOptionPane.showMessageDialog(getWorkspace(),
	                    "Cannot commit: A conflict between the local and the server's history exists. "
	            		+ "\r\n Use compare for details. You might need to Revert to an older revision.",
	                    "Hypergraph Team - Commit - Conflict ",
	                    JOptionPane.ERROR_MESSAGE);
	            mayCommit = false;
			} else if (result.isTargetNewer()) {
	            JOptionPane.showMessageDialog(getWorkspace(),
	                    "Cannot commit: The server has newer Revisions that need to be pulled first."
	            		+ "\r\n Use pull to merge your pending changes before you commit. ",
	                    "Hypergraph Team - Commit - Remote is newer",
	                    JOptionPane.WARNING_MESSAGE);
	            mayCommit = false;
			} else {
				mayCommit = true;
			}
		} else {
            JOptionPane.showMessageDialog(getWorkspace(),
                    "Cannot commit: There was a problem comparing the local history to the server's ontology."
            		+ "\r\n This might mean that the server was not available or a timeout occured. ",
                    "Hypergraph Team - Commit - Error on Compare",
                    JOptionPane.ERROR_MESSAGE);
            mayCommit = false;
		}
		return mayCommit;
	}
	
	
	public boolean checkPullAllowed(VersionedOntologyComparisonResult result) {
		boolean mayPull;
		if (result != null) {
			if (result.isConflict()) {
	            JOptionPane.showMessageDialog(getWorkspace(),
	                    "Cannot pull: A conflict between the local and the server's history exists. "
	            		+ "\r\n Use compare for details. You might need to revert to an older revision.",
	                    "Hypergraph Team - Pull - Conflict ",
	                    JOptionPane.ERROR_MESSAGE);
	            mayPull = false;
			} else if (result.isSourceNewer()) {
	            JOptionPane.showMessageDialog(getWorkspace(),
	                    "Cannot pull: The local history has newer revisions that need to be pushed."
	            		+ "\r\n Use push to upload your pending changes to the server. ",
	                    "Hypergraph Team - Pull - Source is newer",
	                    JOptionPane.WARNING_MESSAGE);
	            mayPull = false;
			} else if (result.isSourceTargetEqual()) {
	            JOptionPane.showMessageDialog(getWorkspace(),
	                    "No need to pull: The local and remote revision history "
	            		+ "\r\n of the ontology are equal ",
	                    "Hypergraph Team - Pull - Local and Remote are equal",
	                    JOptionPane.WARNING_MESSAGE);
	            mayPull = false;
			} else {
				//Target is newer
				mayPull = true;
			}
		} else {
            JOptionPane.showMessageDialog(getWorkspace(),
                    "Cannot pull: There was a problem comparing the local history to the server's ontology."
            		+ "\r\n This might mean that the server was not available or a timeout occured. ",
                    "Hypergraph Team - Commit - Error on compare",
                    JOptionPane.ERROR_MESSAGE);
            mayPull = false;
		}
		return mayPull;
	}
	
	public boolean checkPullAllowed(DistributedOntology dOnto, HGPeerIdentity server) {
		VersionedOntologyComparisonResult result = null;
		try {
			result = repository.compareOntologyToRemote(dOnto, server, ACTIVITY_TIMEOUT_SECS);
		} catch (Throwable t) {
			showException(t, "System error while comparing ontologies");
			return false;
		}
		return checkPullAllowed(result);
	}

    public VHGCommitDialog showUserCommitDialog(VersionedOntology vo, OWLOntology onto) {
	    	return VHGCommitDialog.showDialog(getWorkspace(), vo, onto);
	}

	public void handleUpdateActiveClientOntologyRequest() {
		DistributedOntology dOnto = getActiveOntologyAsDistributed();
		HGPeerIdentity serverPeer = getServerForDistributedOntology(dOnto);
		String serverPeerUser = repository.getPeerUserId(serverPeer);
		if (serverPeer == null) return;
		//Show Pull Dialog with incoming changes
		//Offer to Pull until a certain revision
		VersionedOntologyComparisonResult result;
		try {
			result = repository.compareOntologyToRemote(dOnto, serverPeer, ACTIVITY_TIMEOUT_SECS);
		} catch (Exception e) {
			showException(e, "System error while comparing to remote.");
			return;
		}
		if (checkPullAllowed(dOnto, serverPeer)) {
			int confirm = PullDistributedOntologyViewPanel.showUpdateVersionedOntologyDialog(
					getWorkspace(), 
					result, dOnto,
					serverPeerUser, serverPeer);
			Revision lastPullRevision = PullDistributedOntologyViewPanel.getLastPullRevision();
			System.out.println("Last Pull Revision = " + lastPullRevision);
			if (confirm != JOptionPane.OK_OPTION) {
				// user cancelled.
				return;
			}
			if (lastPullRevision == null) {
				JOptionPane.showMessageDialog(getWorkspace(),
						"There was no revision checked for the update. " 
						,"Hypergraph Team - Update - No revision to transmit", JOptionPane.WARNING_MESSAGE);
				return;
			} 
			if (areYouSure("Hyperaph Team - Update", "Are you sure to update?")) {
				PullActivity pa = repository.pullUntilRevision(dOnto, serverPeer, lastPullRevision);
				try {
					//TODO BLOCK in NON AWT THREAD, let changes be applied in AWT.
					ActivityResult paa = pa.getFuture().get(60, TimeUnit.SECONDS);
					if (paa.getException() == null) {
						JOptionPane.showMessageDialog(getWorkspace(),
								"Updating " + VDRenderer.render(dOnto) + "\n from " 
								+ VDRenderer.render(serverPeer) + " " + repository.getPeerUserId(serverPeer)
										+ "\n completed with the following message: \n" + pa.getCompletedMessage(),
								"Hypergraph Team - Update - Complete", JOptionPane.INFORMATION_MESSAGE);
					} else {
						throw paa.getException();
					}
				} catch (Throwable e) {
					showException(e, "System error while pull until revision.");
//					JOptionPane.showMessageDialog(getWorkspace(), e.toString() + " - " + e.getMessage(), "Team Update Error",
//							JOptionPane.ERROR_MESSAGE);
				}
				// Fire Onto Reloaded
				causeViewUpdate();
			} else {
				//user cancelled
			}
		} // else UI already showed reason
	}

	public void handleCompareActiveRequest() {
		DistributedOntology dOnto = getActiveOntologyAsDistributed();
		HGPeerIdentity server = getServerForDistributedOntology(dOnto);
		if (server == null) return;
		String userId = repository.getPeerUserId(server);
		VersionedOntologyComparisonResult result = repository.compareOntologyToRemote(dOnto, server, ACTIVITY_TIMEOUT_SECS);
		if (result != null) {
			String title = "Hypergraph Team - Compare " + VDRenderer.render(dOnto) + " Remote: " + VDRenderer.render(server);
			CompareVersionedOntologyViewPanel.showCompareVersionedOntologyDialog(title, getWorkspace(), dOnto, server, userId, result);
		} else {
			JOptionPane.showMessageDialog(getWorkspace(),
					"Comparison failed. Server not accessible or does not have onto.", 
					"Hypergraph Team - - Error ", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * For ClientCentralOntologies: returns either the clients server, if it is available on the network.
	 * For PeerOntologies: a user selected available server
	 * null otherwise.  
	 * @param dOnto
	 * @return
	 */
	public HGPeerIdentity getServerForDistributedOntology(DistributedOntology dOnto) {
		if  (dOnto == null) throw new IllegalArgumentException("Distributed Ontology null");
		HGPeerIdentity server;
		if (dOnto instanceof ClientCentralizedOntology) {
			ClientCentralizedOntology cDOnto = (ClientCentralizedOntology) dOnto;
			server = cDOnto.getServerPeer();
		} else {
			server = selectRemotePeer("Hypergraph Team - - Select Server for " + dOnto);
		}
		if (server != null) {
			if (!repository.isNetworking()) {
				JOptionPane.showMessageDialog(getWorkspace(),
						"You are not signed in.", 
						"Hypergraph Team - Error ", JOptionPane.ERROR_MESSAGE);
				server = null;
			} else {
				if (!repository.getPeers().contains(server)) {
					JOptionPane.showMessageDialog(getWorkspace(),
							"The server or peer is not accessible on the network. \r\n"
							+ server, 
							"Hypergraph Team - Network Error ", JOptionPane.ERROR_MESSAGE);
					server = null;
				}
			}
		}
		return server;
	}
	
	public DistributedOntology getActiveOntologyAsDistributed() {
		HGDBOntology activeOnto = (HGDBOntology)getActiveOntology();
		DistributedOntology dOnto = repository.getDistributedOntology(activeOnto);
		if (dOnto == null) {
			JOptionPane.showMessageDialog(getWorkspace(),
					"The active ontology is not distributed:" 
					+"\r\n " + activeOnto+ " ",
					"Team - Error ", JOptionPane.ERROR_MESSAGE);
		}
		return dOnto;
	}
		
	public boolean isNetworking() {
		return repository.getPeer() != null && repository.getPeer().getPeerInterface().isConnected();
	}
	
	public HGPeerIdentity selectRemotePeer(String title) {
		if (isNetworking()) {
			PeerViewPanel.showServerSelectionDialog(title, getWorkspace(), repository);
			return PeerViewPanel.getSelectedPeer();
		} else {
			JOptionPane.showMessageDialog(getWorkspace(), "You are not signed in.", "Team Share - Error.",
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
	public boolean ensureRemotePeerAccessible() {
		if (isNetworking()) {
			if (repository.getPeers().contains(selectedRemotePeer)
					&& !HGOwlProperties.getInstance().isP2pAskForRemote()) {
				return true;
			} else {
				int userChoice = PeerViewPanel.showPeerSelectionDialog(getWorkspace(), repository);
				if (userChoice == JOptionPane.CANCEL_OPTION) {
					return repository.getPeers().contains(selectedRemotePeer);
				} else {
					selectedRemotePeer = PeerViewPanel.getSelectedPeer();
				}
				return selectedRemotePeer != null;
			}
		} else {
			JOptionPane.showMessageDialog(getWorkspace(), "You are not signed in.", "Team Share - Error.",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public VDHGDBOntologyRepository getDistributedRepository() {
		HGOwlModelManagerImpl hmm = (HGOwlModelManagerImpl) getOWLModelManager();
		PHGDBOntologyManagerImpl hom = (PHGDBOntologyManagerImpl) hmm.getOWLOntologyManager();
		return (VDHGDBOntologyRepository) hom.getOntologyRepository();
	}

	/**
	 * @return the current active onto as versionedOntology
	 */
	public VersionedOntology getActiveAsVersionedOntology() {
		VersionedOntology vo = null;
		HGOwlModelManagerImpl hmm = (HGOwlModelManagerImpl) getOWLModelManager();
		OWLOntology activeOnto = hmm.getActiveOntology();
		VHGDBOntologyRepository vor = getVersionedRepository();
		if (vor.isVersionControlled(activeOnto)) {
			vo = vor.getVersionControlledOntology(activeOnto);
		} else {
			System.out.println("Active ontology not version controlled.");
			JOptionPane.showMessageDialog(getWorkspace(),
					"Active ontology not version controlled: \r\n" + activeOnto.getOntologyID(),
					"P2P Active not versioned", JOptionPane.WARNING_MESSAGE);
		}
		return vo;
	}

	public boolean isActiveOntologyShared() {
		return getActiveOntologyDistributionState() != OntologyDistributionState.ONTO_NOT_SHARED;
	}

	public OntologyDistributionState getActiveOntologyDistributionState() {
		HGOwlModelManagerImpl hmm = (HGOwlModelManagerImpl) getOWLModelManager();
		OWLOntology activeOnto = hmm.getActiveOntology();
		DistributedOntology donto = repository.getDistributedOntology(activeOnto);
		if (donto == null) {
			return OntologyDistributionState.ONTO_NOT_SHARED;
		} else if (donto instanceof ClientCentralizedOntology) {
				return OntologyDistributionState.ONTO_SHARED_CENTRAL_CLIENT;
		} else if (donto instanceof ServerCentralizedOntology) {
			return OntologyDistributionState.ONTO_SHARED_CENTRAL_SERVER;
		} else if (donto instanceof PeerDistributedOntology) {
			return OntologyDistributionState.ONTO_SHARED_DISTRIBUTED;
		} else {
			throw new IllegalStateException("getActiveOntologyDistributionState unknown for: " + donto);
		}
	}

	/**
     * Returns the VD OntologyRepository implementation of our plugin (Protege Interface).
     * 
     * @return
     */
    public OntologyRepository getProtegeRepository() {
        Collection<OntologyRepository> repositories = OntologyRepositoryManager.getManager().getOntologyRepositories();
        for (OntologyRepository  cur: repositories) {
        	if (cur instanceof VDHGOwlOntologyRepository) {
        		return cur;
        	}
        }
        return null;
    }
    
    public void showException(Throwable t, String title) {
    	Throwable cur = t;
    	while (cur.getCause() != null) {
    		cur = cur.getCause(); 
    	}
    	String excRendered = cur.toString();
		JOptionPane.showMessageDialog(getWorkspace(),
				"System Error: \r\n Caused by: " 
				+ excRendered + " \r\n",
				title, JOptionPane.ERROR_MESSAGE);
		//Print Exception to console
		System.err.println("System Error " + new Date() + " r\n"
				+ excRendered);
		t.printStackTrace(System.err);
    }
}