package gov.miamidade.hgowl.plugin;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;

/**
 * HGOwlProperties.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Dec 6, 2011
 */
public class HGOwlProperties {

	    public static final String PREFERENCES_SET_KEY = "gov.miamidade.hgowl";

	    public static final String PREFERENCES_KEY = "HypergraphDB";

	    public static final String HYPERGRAPH_LOCATION_KEY = "HypergraphLocationFolderJE-17";

	    public static final String SHOW_LEGACY_KEY = "ShowLegacyEditorKit";

	    private static HGOwlProperties instance;

	    public static final String DEFAULT_HYPERGRAPH_LOCATION_FOLDER_PATH = System.getProperty("user.home") + System.getProperty("file.separator") + ".protegedb-je-17";

	    private static final boolean DEFAULT_SHOW_LEGACY_EDITOR_KIT = false;

	    private static final boolean DEFAULT_P2P_ASK_FOR_REMOTE = true;
	    
	    // DISTRIBUTED START
	    public static final String P2P_ROOM = "P2PRoomName";
	    public static final String P2P_USER = "P2PUserName";
	    public static final String P2P_PASSWORD = "P2PPassword";
	    public static final String P2P_SERVER= "P2PServer";
	    public static final String P2P_ASK_FOR_REMOTE = "P2PAskForRemote";
	    // DISTRIBUTED END
	    
	    private String hgLocationFolderPath;
	    private boolean showLegacyEditorKit;
	    // DISTRIBUTED START
	    private String p2pUser;
	    private String p2pPass;
	    private String p2pRoom;
	    private String p2pServer;
	    private boolean p2pAskForRemote; //ask for the remote target before each operation.
	    // DISTRIBUTED END

	    protected HGOwlProperties() {
	        loadFromPrefs();
	    }

	    public static synchronized HGOwlProperties getInstance() {
	        if(instance == null) {
	            instance = new HGOwlProperties();
	        }

	        return instance;
	    }

	    private static Preferences getPreferences() {
	        return PreferencesManager.getInstance().getPreferencesForSet(PREFERENCES_SET_KEY, PREFERENCES_KEY);
	    }

	    private void loadFromPrefs() {
	        hgLocationFolderPath = getPreferences().getString(HYPERGRAPH_LOCATION_KEY, DEFAULT_HYPERGRAPH_LOCATION_FOLDER_PATH);
	        showLegacyEditorKit= getPreferences().getBoolean(SHOW_LEGACY_KEY, DEFAULT_SHOW_LEGACY_EDITOR_KIT);
	        //DISTRIBUTED
	        p2pRoom = getPreferences().getString(P2P_ROOM, "");
	        p2pUser = getPreferences().getString(P2P_USER, "");
	        p2pPass = getPreferences().getString(P2P_PASSWORD, "");
	        p2pServer = getPreferences().getString(P2P_SERVER, "");
	        p2pAskForRemote = getPreferences().getBoolean(P2P_ASK_FOR_REMOTE, DEFAULT_P2P_ASK_FOR_REMOTE);
	    }

	    private void savePrefs() {
	        getPreferences().putString(HYPERGRAPH_LOCATION_KEY, hgLocationFolderPath);
	        getPreferences().putBoolean(SHOW_LEGACY_KEY, showLegacyEditorKit);
	        //DISTRIBUTED
	        getPreferences().putString(P2P_ROOM, p2pRoom);
	        getPreferences().putString(P2P_USER, p2pUser);
	        getPreferences().putString(P2P_PASSWORD, p2pPass);
	        getPreferences().putString(P2P_SERVER, p2pServer);
	        getPreferences().putBoolean(P2P_ASK_FOR_REMOTE, p2pAskForRemote);
	    }

		/**
		 * @return the hgLocationFolderPath
		 */
		public String getHgLocationFolderPath() {
			return hgLocationFolderPath;
		}

		/**
		 * @param hgLocationFolderPath the hgLocationFolderPath to set
		 */
		public void setHgLocationFolderPath(String hgLocationFolderPath) {
			this.hgLocationFolderPath = hgLocationFolderPath;
			savePrefs();
		}

		/**
		 * @return the showLegacyEditorKit
		 */
		public boolean isShowLegacyEditorKit() {
			return showLegacyEditorKit;
		}

		/**
		 * @param showLegacyEditorKit the showLegacyEditorKit to set
		 */
		public void setShowLegacyEditorKit(boolean showLegacyEditorKit) {
			this.showLegacyEditorKit = showLegacyEditorKit;
			savePrefs();
		}

        /**
         * @return the p2pUser
         */
        public String getP2pRoom() {
            return p2pRoom;
        }

        /**
         * @param p2pUser the p2pUser to set
         */
        public void setP2pRoom(String p2pRoom) {
            this.p2pRoom = p2pRoom;
            savePrefs();
        }
		
		/**
		 * @return the p2pUser
		 */
		public String getP2pUser() {
			return p2pUser;
		}

		/**
		 * @param p2pUser the p2pUser to set
		 */
		public void setP2pUser(String p2pUser) {
			this.p2pUser = p2pUser;
			savePrefs();
		}

		/**
		 * @return the p2pPass
		 */
		public String getP2pPass() {
			return p2pPass;
		}

		/**
		 * @param p2pPass the p2pPass to set
		 */
		public void setP2pPass(String p2pPass) {
			this.p2pPass = p2pPass;
			savePrefs();
		}

		/**
		 * @return the p2pServer HostName
		 */
		public String getP2pServer() {
			return p2pServer;
		}

		/**
		 * @param p2pHostName the setP2pServer hostname to set
		 */
		public void setP2pServer(String p2pHostName) {
			this.p2pServer = p2pHostName;
			savePrefs();
		}

		/**
		 * @return the p2pAskForRemote
		 */
		public boolean isP2pAskForRemote() {
			return p2pAskForRemote;
		}

		/**
		 * @param p2pAskForRemote the p2pAskForRemote to set
		 */
		public void setP2pAskForRemote(boolean p2pAskForRemote) {
			this.p2pAskForRemote = p2pAskForRemote;
			savePrefs();
		}
}