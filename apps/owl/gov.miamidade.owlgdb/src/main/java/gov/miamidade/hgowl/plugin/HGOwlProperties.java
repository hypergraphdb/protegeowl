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

	    public static final String HYPERGRAPH_LOCATION_KEY = "HypergraphLocationFolder52";

	    public static final String SHOW_LEGACY_KEY = "ShowLegacyEditorKit";

	    private static HGOwlProperties instance;

	    public static final String DEFAULT_HYPERGRAPH_LOCATION_FOLDER_PATH = System.getProperty("user.home") + System.getProperty("file.separator") + ".protegedb52";

	    private static final boolean DEFAULT_SHOW_LEGACY_EDITOR_KIT = false;
	   
	    private String hgLocationFolderPath;
	    private boolean showLegacyEditorKit;


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
	    }

	    private void savePrefs() {
	        getPreferences().putString(HYPERGRAPH_LOCATION_KEY, hgLocationFolderPath);
	        getPreferences().putBoolean(SHOW_LEGACY_KEY, showLegacyEditorKit);
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

}
