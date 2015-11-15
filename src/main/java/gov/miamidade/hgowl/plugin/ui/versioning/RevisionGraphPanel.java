package gov.miamidade.hgowl.plugin.ui.versioning;

import static org.hypergraphdb.app.owl.test.TU.*;

import org.hypergraphdb.app.owl.test.TU;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
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
import org.hypergraphdb.app.owl.versioning.versioning;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;

public class RevisionGraphPanel extends JPanel
{
	private static final long serialVersionUID = -6943157589623626498L;

	int yoffset = 0;
	int cellWidth = 10, cellHeight = 40;
	int radius = 5;
	private VersionedOntology versionedOntology;
	private List<Revision> revisions;
	private List<List<Revision>> columns; 
	private boolean [][] adjacencyMatrix;  // [i][j] == true iff revisions.get(i) is the direct parent of revisions.get(j)
	
	void computeColumns()
	{
		columns = new ArrayList<List<Revision>>();
		for (int current = revisions.size() - 1; current >= 0; current--)
		{
			Revision rev = revisions.get(current);
			boolean placed = false;
			for (List<Revision> column : columns)
			{
				Revision last = column.get(column.size() - 1);
				if (last.parents().contains(rev.getAtomHandle()))
				{
					column.add(rev);
					placed = true;
					break;
				}
			}
			if (!placed)
			{
				ArrayList<Revision> column = new ArrayList<Revision>();
				column.add(rev);
				columns.add(column);
			}
		}
	}
	
	void drawCircleByCenter(Graphics g, int x, int y, Color color)
	{
         g.setColor(color);
         int centerX = cellWidth*x + cellWidth / 2;
         int centerY = cellHeight*y + cellHeight / 2;
         g.fillOval(centerX-radius, yoffset + centerY-radius, 2*radius, 2*radius);
    }
	 
	void drawConnection(Graphics g, int parentx, int parenty, int childx, int childy)
	{
		g.setColor(Color.blue);
        int parentCenterX = cellWidth*parentx + cellWidth / 2;
        int parentCenterY = yoffset + cellHeight*parenty + cellHeight / 2;
        int childCenterX = cellWidth*childx + cellWidth / 2;
        int childCenterY = yoffset + cellHeight*childy + cellHeight / 2;		
		g.drawLine(parentCenterX, parentCenterY - radius, childCenterX, childCenterY + radius);
	}

	public RevisionGraphPanel(VersionedOntology versionedOntology, 
							  List<Revision> revisions,
							  boolean [][] adjacencyMatrix)
	{	
		this.versionedOntology = versionedOntology;
		this.revisions = revisions;
		this.adjacencyMatrix = adjacencyMatrix;
		computeColumns();
		versioning.printRevisionGraph(versionedOntology);
		Dimension dims = new Dimension(this.columns.size()*cellWidth(), revisions.size()*cellHeight());
//		this.setMaximumSize(dims);
//		this.setMinimumSize(dims);
		this.setPreferredSize(dims);
	}
	
	public RevisionGraphPanel build()
	{
		return this;
	}
	
	public void paint(Graphics g)
	{
		HashMap<HGHandle, Pair<Integer, Integer>> coordinates = 
				new HashMap<HGHandle, Pair<Integer, Integer>>();
		for (int row = revisions.size() - 1; row >= 0; row--)
		{
			Revision rev = revisions.get(row);
			int col = 0;
			while (!columns.get(col).contains(rev))
				col++;
			int ycoord = revisions.size() - row - 1;
			int xcoord = col;
			drawCircleByCenter(g, xcoord, ycoord, Color.blue);
			coordinates.put(rev.getAtomHandle(), new Pair<Integer, Integer>(xcoord, ycoord));
		}
		for (int row = revisions.size() - 1; row >= 0; row--)
		{
			Revision rev = revisions.get(row);
			Pair<Integer, Integer> currentCoord = coordinates.get(rev.getAtomHandle());
			for (HGHandle parentHandle : rev.parents())
			{
				Pair<Integer, Integer> parentCoord = coordinates.get(parentHandle);		
				drawConnection(g, 
					  	   parentCoord.getFirst(), 
					  	   parentCoord.getSecond(), 
					  	   currentCoord.getFirst(), 
					  	   currentCoord.getSecond());				
			}
		}
    }	
	
	public int cellWidth() 
	{ 
		return cellWidth; 
	}
	public RevisionGraphPanel cellWidth(int cellWidth) 
	{ 
		this.cellWidth = cellWidth; 
		return this; 
	}
	public RevisionGraphPanel cellHeight(int cellHeight) 
	{ 
		this.cellHeight = cellHeight;
		this.yoffset = cellHeight * 1;
		return this; 
	}
	public int cellHeight() 
	{ 
		return cellHeight; 
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
