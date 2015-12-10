The ProtegeOWL module in HyperGraphDB implements a plugin within the [Protege Ontology Editor](http://protege.stanford.edu/). The plugin has the following features:

* Integration with the HyperGraphDB-OWL API implementation. This means ontologies are persisted in a graph database embedded inside the editor environment, instead of files scattered around in your file system. As a consequence, an ontology can be larger than what can be usually fit in RAM memory. 
* Support for distributed version control with full history management, team collaboration, branching, merging of the revision graph etc. Essentially you get something like Mercurial or Git but for ontologies within your Protege environment.


The currently supported version of Protege is 4.3 which can be downloaded from here:

http://protegewiki.stanford.edu/wiki/Protege_Desktop_Old_Versions#Protege_4

To install Protege, follow their installation instructions and make sure you can start it. I tend to use the .zip distribution and then run.sh or run.cmd. Note that it comes with the Fact++ which may fail to work on some older Linux system due to a native dependency. If you get a startup error related to Fact++, simply delete PROTEGE_HOME/plugins/uk.ac.manchester.cs.owl.factplusplus.jar and use the HermitT reasoner instead. 

Note that HyperGraphDB-Protege plugin needs Java 7 to run. The plugin itself can be download from here:

https://github.com/hypergraphdb/protegeowl/releases

Installing the plugin is as simple as unpacking the distribution inside your PROTEGE_HOME directory. It will just copy several .jar files, OSGI modules, inside the PROTEGE_HOME/plugins directory. You then need to restart protege. 

What happens when you restart Protege with the HyperGraphDB plugin:

1. A new HyperGraphDB database instance is automatically created in your home directory under .protegehgdb. This is a default location and you can change it in your preferences after you restart Protege.
2. A few extra menus will be added: `HyperGraphDB`, `Versioning` and `Team`. Those will be pretty self-explanatory, but we've outlined some [use case scenarios](ProtegeUseCaseScenarios) that walk you through importing ontologies, the version control functions, connecting with team members etc.
3. A preferences tab called `Hypergraph` under the main Protege preferences dialog (under the `File->Preferences` menu). This is where you can change the database location (normally there is no need for that) and more importantly this where you configure your P2P (peer-to-peer) network so you can communicate with your team members and whatever standalone ontology repositories you have setup. See the [configuring the peer-to-peer network](ProtegeConfigureP2P) page for more information on that.
