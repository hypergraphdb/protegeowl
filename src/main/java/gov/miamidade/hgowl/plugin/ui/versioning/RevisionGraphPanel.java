package gov.miamidade.hgowl.plugin.ui.versioning;

import static org.hypergraphdb.app.owl.test.TU.*;

import org.hypergraphdb.app.owl.test.TU;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;

import javax.swing.JPanel;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.util.CoffmanGraham;
import org.hypergraphdb.app.owl.versioning.ChangeLink;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;

public class RevisionGraphPanel extends JPanel
{
	private static final long serialVersionUID = -6943157589623626498L;

	private int layerSize = 3;
	private int cellWidth = 20, cellHeight = 40;
	int radius = 5;
	private VersionedOntology versionedOntology;
	private List<Revision> revisions;
	
//	SortedMap<Integer, HGHandle[]> layers = null;
//	Map<HGHandle, Pair<Integer, Integer>> coordinates = new HashMap<HGHandle, Pair<Integer, Integer>>();
	
	void initLayers()
	{
		if (layers != null) return;
		CoffmanGraham algo = new CoffmanGraham(versionedOntology.graph(), versionedOntology.getRootRevision());		
		layers = algo.coffmanGrahamLayers(layerSize);
		for (int i = 0; i < layers.size(); i++)
		{
			int ypos = i;			
			HGHandle [] data = layers.get(i + 1);
			for (int xpos = 0; xpos < data.length; xpos++)
				coordinates.put(data[xpos], new Pair<Integer, Integer>(xpos, ypos));
		}		
	}
	
	int cellWidth()
	{
		return cellWidth;
	}
	
	int cellHeight()
	{
		return cellHeight;
	}
	
	void drawCircleByCenter(Graphics g, int x, int y, Color color)
	{
         g.setColor(color);
         int centerX = cellWidth*x + cellWidth / 2;
         int centerY = cellHeight*y + cellHeight / 2;
         g.fillOval(centerX-radius, centerY-radius, 2*radius, 2*radius);
    }
	 
	void drawConnection(Graphics g, int parentx, int parenty, int childx, int childy)
	{
		g.setColor(Color.blue);
        int parentCenterX = cellWidth*parentx + cellWidth / 2;
        int parentCenterY = cellHeight*parenty + cellHeight / 2;
        int childCenterX = cellWidth*childx + cellWidth / 2;
        int childCenterY = cellHeight*childy + cellHeight / 2;		
		g.drawLine(parentCenterX, parentCenterY - radius, childCenterX, childCenterY + radius);
	}

	public RevisionGraphPanel(VersionedOntology versionedOntology, List<Revision> revisions)
	{	
		this.versionedOntology = versionedOntology;
		this.revisions = revisions;
	}
	
	public RevisionGraphPanel build()
	{
//		setSize(400, 600);
		return this;
	}
	
	public void paint(Graphics g)
	{
		initLayers();
		HyperGraph graph = versionedOntology.graph();
		for (int i = 0; i < layers.size(); i++)
		{
			HGHandle [] data = layers.get(i + 1);
			for (HGHandle current : data)
			{
				Revision rev = graph.get(current);
				Pair<Integer, Integer> currentCoord = coordinates.get(current);				
				this.drawCircleByCenter(g, currentCoord.getFirst(), currentCoord.getSecond(), Color.blue);
//				List<HGHandle> parents = graph.findAll(hg.apply(
//						hg.targetAt(graph, 0), 
//						hg.and(hg.type(ChangeLink.class), 
//							   hg.orderedLink(hg.anyHandle(), hg.anyHandle(), current))));
				Set<HGHandle> parents = rev.parents();
				for (HGHandle parent : parents)
				{
					Pair<Integer, Integer> parentCoord = coordinates.get(parent);
					if (parentCoord != null)
					{
						Revision prev = graph.get(parent);
						System.out.println("Drawing line b/w parent " + prev.comment() + " and " + rev.comment());
						drawConnection(g, 
								  	   parentCoord.getFirst(), 
								  	   parentCoord.getSecond(), 
								  	   currentCoord.getFirst(), 
								  	   currentCoord.getSecond());
					}
				}
			}
		}
//		
//		int width = this.getWidth();
//		int height = this.getHeight();
//        // Circular Surface
//        drawCircleByCenter(g, width/2, height/2, width/2, Color.RED);
//        Random r = new Random();
//        Point center = new Point();
//        center.x=r.nextInt(width/2);
//        center.y=r.nextInt(width/2);
//        drawCircleByCenter(g, center.x, center.y, width/15, Color.BLUE);
    }	
	
	public static VersionedOntology createTestData(HyperGraph graph)
	{
		try 
		{
		HGDBOntologyManager ontomanager = TU.ctx().m;
		TU.ctx().o = (HGDBOntology)ontomanager.createOntology(
				IRI.create("hgdb://testrevisiongraphpanel.io/" + UUID.randomUUID().toString()));
		VersionManager vmanager = TU.ctx().vr = new VersionManager(graph, "testuser");
		VersionedOntology vo  = vmanager.versioned(TU.ctx().o.getAtomHandle());
		Revision initialRevision = vo.revision();
		OWLClass classA = owlClass("A"); 
		OWLClass classB = owlClass("B");
		OWLClass classC = owlClass("C");
		a(declare(classA));
		Revision a1 = vo.commit("testuser", "A1");
		aInstanceOf(classA, individual("a2"));
		Revision a2 = vo.commit("testuser", "A2");
		aInstanceOf(classA, individual("a3"));
		Revision a3 = vo.commit("testuser", "A3");
		aInstanceOf(classA, individual("a4"));
		Revision a4 = vo.commit("testuser", "A4");
		aInstanceOf(classA, individual("a5"));
		Revision a5 = vo.commit("testuser", "A5");
		aInstanceOf(classA, individual("a6"));
		Revision a6 = vo.commit("testuser", "A6");
		vo.goTo(a2);
		a(declare(classC));
		Revision c1 = vo.commit("testuser_c", "C1", "C");
		aInstanceOf(classC, individual("c2"));
		Revision c2 = vo.commit("testuser_c", "C2");
		aInstanceOf(classC, individual("c3"));
		Revision c3 = vo.commit("testuser_c", "C3");
		aInstanceOf(classC, individual("c4"));
		Revision c4 = vo.commit("testuser_c", "C4");
		aInstanceOf(classC, individual("c5"));
		Revision c5 = vo.commit("testuser_c", "C5");
		aInstanceOf(classC, individual("c6"));
		Revision c6 = vo.commit("testuser_c", "C6");
		vo.goTo(a3);
		a(declare(classB));
		Revision b1 = vo.commit("testuser_b", "B1", "B");
		aInstanceOf(classB, individual("b2"));
		Revision b2 = vo.commit("testuser_b", "B2");
		aInstanceOf(classB, individual("b3"));
		Revision b3 = vo.commit("testuser_b", "B3");
		aInstanceOf(classB, individual("b4"));
		Revision b4 = vo.commit("testuser_b", "B4");
		aInstanceOf(classB, individual("b5"));
		Revision b5 = vo.commit("testuser_b", "B5");		
		aInstanceOf(classB, individual("b6"));
		Revision b6 = vo.commit("testuser_b", "B6");
		aInstanceOf(classB, individual("b7"));
		Revision b7 = vo.commit("testuser_b", "B7");
		vo.goTo(b5);
		aInstanceOf(classA, individual("b5"));
		Revision ab = vo.commit("testuser_b", "AB", "AB");
		Revision a7 = vo.merge("testuser_a", "A7", "master", a6, ab);
		vo.goTo(a7);
		return vo;
		}
		catch (Exception ex) 
		{
			throw new RuntimeException(ex);
		}
	}
}
