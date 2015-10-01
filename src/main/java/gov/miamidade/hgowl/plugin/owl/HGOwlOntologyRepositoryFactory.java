package gov.miamidade.hgowl.plugin.owl;

import gov.miamidade.hgowl.plugin.HGOwlProperties;
import gov.miamidade.hgowl.plugin.Singles;

import java.io.File;

import javax.swing.JOptionPane;

import org.hypergraphdb.app.owl.OntologyDatabase;
import org.hypergraphdb.app.owl.versioning.distributed.OntologyDatabasePeer;
import org.protege.editor.core.OntologyRepository;
import org.protege.editor.core.OntologyRepositoryFactory;

/**
 * HGOwlOntologyRepositoryFactory. A factory for Hypergraph backed Protege
 * OntologyRepositories.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Sep 26, 2011
 */
public class HGOwlOntologyRepositoryFactory extends OntologyRepositoryFactory
{
	OntologyDatabase dbRepository;

	@Override
	public void initialise() throws Exception
	{
		try
		{
			System.out.println("Initialize HGOwlOntologyRepositoryFactory");
			initialiseInternal();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showConfirmDialog(null, "Protege cannot start the repository. Exiting.", "Protege Repository error",
					JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
	}

	public void initialiseInternal() throws Exception
	{
		String hyperGraphLocation = HGOwlProperties.getInstance().getHgLocationFolderPath();
		try
		{
			File f = new File(hyperGraphLocation);
			f.mkdirs();
			// HGDBOntologyRepository.setHypergraphDBLocation(hyperGraphLocation);
		}
		catch (RuntimeException e)
		{
			System.err.println("EXCEPTION setting preferred Hypergraph location " + hyperGraphLocation + " : ");
			e.printStackTrace(System.err);
		}
		dbRepository = Singles.vdRepo();
	}

	@Override
	public void dispose() throws Exception
	{
		dbRepository.dispose();
		dbRepository = null;
	}

	@Override
	public OntologyRepository createRepository()
	{
		OntologyRepository r;
		if (dbRepository == null)
			throw new IllegalStateException("Cannot create HGOwlOntologyRepository. dbRepository was null.");
		if (dbRepository instanceof OntologyDatabasePeer)
		{
			r = new VDHGOwlOntologyRepository("Hypergraph - Team ", (OntologyDatabasePeer) dbRepository);
		}
		else
		{
			r = new HGOwlOntologyRepository("Hypergraph", dbRepository);
		}
		return r;
	}
}