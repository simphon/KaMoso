package sfb732.kamoso.junit;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.mem.Exemplar;
import sfb732.kamoso.mem.Lexicon;
import sfb732.kamoso.net.AdjacencyMatrix;
import sfb732.kamoso.net.Network;
import sfb732.kamoso.net.NetworkFactory;
import sfb732.kamoso.net.NetworkType;
import sfb732.kamoso.pop.Agent;
import sfb732.kamoso.pop.PopulationFactory;

public class TestNetworkFactory {


	private File agents4x3;
	private File agents4x3_reverseIDs;
	private Exemplar prototypeA;
	private Exemplar prototypeB;

	@Before
	public final void init()
	{
		this.agents4x3            = new File("testdata/agents_0-11_set001.csv");
		this.agents4x3_reverseIDs = new File("testdata/agents_0-11_set002.csv");
		File lexFile = new File("testdata/exemplar_prototypes.csv");

		assertTrue(this.agents4x3.isFile());
		assertTrue(this.agents4x3_reverseIDs.isFile());
		assertTrue(lexFile.isFile());

		Configuration conf = Configuration.init();

		Exemplar[] lex = Lexicon.readPrototypes(conf,lexFile, 2);
		this.prototypeA = lex[0];
		this.prototypeB = lex[1];
	}




	@Test
	public void testMain()
	{
		// make regular torus:
		File outFile1 = new File(Configuration.DEFAULT_OUPUT_DIR, "test_regTorus10x40.csv_DELETE_ME");
		assertFalse("outputfile already exists", outFile1.exists());
		outFile1.deleteOnExit();
		String[] args = new String[] {
				Configuration.ARG_FILE_OUT, outFile1.getAbsolutePath(),
				Configuration.ARG_NET_TYPE, NetworkType.regTorus.toString(),
				Configuration.ARG_NET_COLS, "40",
				Configuration.ARG_NET_ROWS, "10"
		};
		NetworkFactory.main(args);
		assertTrue(outFile1.isFile());
		//TODO check exported data

		// make small-world torus:
		File outFile2 = new File(Configuration.DEFAULT_OUPUT_DIR, "test_swTorus10x40.csv_DELETE_ME");
		assertFalse("outputfile already exists", outFile2.exists());
		outFile2.deleteOnExit();
		args = new String[] {
				Configuration.ARG_FILE_OUT, outFile2.getAbsolutePath(),
				Configuration.ARG_NET_TYPE, NetworkType.swTorus.toString(),
				Configuration.ARG_NET_COLS, "40",
				Configuration.ARG_NET_ROWS, "10",
				Configuration.ARG_NET_PROB, "0.01"
		};
		NetworkFactory.main(args);
		assertTrue(outFile2.isFile());
		//TODO check exported data

	}


	@Test
	public void testGetIDForGridCoordinates()
	{
		System.out.println("**** TestNetworkFactory.testGetIDForGridCoordinates ****");
		int ncols = 4;
		int nrows = 3;

		for(int r=0; r<nrows; r++) {
			for(int c=0; c<ncols; c++) {
				int id = NetworkFactory.getIDForGridCoordinates(c, r, ncols);
				System.out.printf("c=%d;\tr=%d\tid=%d\n", c, r, id);
			}
		}
	}


	@Test
	public void testGetGridRowForID ()
	{
		System.out.println("**** TestNetworkFactory.testGetGridRowForID ****");
		int ncols = 4;
		int nrows = 3;
		int n = ncols*nrows;
		for(int i=0; i<n; i++) {
			int r = NetworkFactory.getGridRowForID(i, ncols);
			System.out.printf("i=%d;\tr=%d\n", i, r);
		}
	}


	@Test
	public void testGetGridColumnForID ()
	{
		System.out.println("**** TestNetworkFactory.testGetGridColumnForID ****");
		int ncols = 4;
		int nrows = 3;
		int n = ncols*nrows;
		for(int i=0; i<n; i++) {
			int c = NetworkFactory.getGridColumnForID(i, ncols);
			System.out.printf("i=%d;\tc=%d\n", i, c);
		}
	}


	@Test
	public void testMakeRegularTorus()
	{
		System.out.println("**** TestNetworkFactory.testMakeRegularTorus ****");
		int ncols = 4;
		int nrows = 3;
		// expected network structure:
		//
		//   |  |   |   |
		// --0--1---2---3--
		// --4--5---6---7--
		// --8--9--10--11--
		//   |  |   |   |
		//
		int n = ncols*nrows;
		Configuration conf = Configuration.init();
		Network net = NetworkFactory.makeRegularTorus(conf,ncols, nrows);
		assertNotNull(net);
		assertEquals(n, net.size());
		assertEquals(NetworkType.regTorus, net.getType());

		assertTrue(net.isConnected(0, 1));
		assertTrue(net.isConnected(0, 4));
		assertTrue(net.isConnected(0, 3));
		assertTrue(net.isConnected(0, 8));

		assertTrue(net.isConnected(1, 2));
		assertTrue(net.isConnected(1, 5));
		assertTrue(net.isConnected(1, 9));
		assertTrue(net.isConnected(1, 0));

		assertTrue(net.isConnected(3, 0));
		assertTrue(net.isConnected(3, 7));
		assertTrue(net.isConnected(3, 2));
		assertTrue(net.isConnected(3, 11));

		assertTrue(net.isConnected(5, 6));
		assertTrue(net.isConnected(5, 9));
		assertTrue(net.isConnected(5, 4));
		assertTrue(net.isConnected(5, 1));

		assertTrue(net.isConnected(10, 11));
		assertTrue(net.isConnected(10, 2));
		assertTrue(net.isConnected(10, 9));
		assertTrue(net.isConnected(10, 6));

		assertFalse(net.isConnected(0, 2));
		assertFalse(net.isConnected(0, 5));
		assertFalse(net.isConnected(0, 6));
		assertFalse(net.isConnected(0, 7));
		assertFalse(net.isConnected(0, 9));
		assertFalse(net.isConnected(0, 10));
		assertFalse(net.isConnected(0, 11));

		assertEquals(1, net.getDistance(0, 1));
		assertEquals(1, net.getDistance(0, 3));
		assertEquals(1, net.getDistance(0, 4));
		assertEquals(1, net.getDistance(0, 8));

		assertEquals(2, net.getDistance(0, 2));
		assertEquals(2, net.getDistance(0, 5));
		assertEquals(3, net.getDistance(0, 6));
		assertEquals(3, net.getDistance(0, 10));

		assertTrue(checkConnectedness(net, n));
	}


	@Test
	public void testMakeRegularTorus_400()
	{
		System.out.println("**** TestNetworkFactory.testMakeRegularTorus_400 ****");
		int ncols = 40;
		int nrows = 10;

		int n = ncols*nrows;
		Configuration conf = Configuration.init();
		Network net = NetworkFactory.makeRegularTorus(conf,ncols, nrows);
		assertNotNull(net);
		assertEquals(n, net.size());
		assertEquals(NetworkType.regTorus, net.getType());

		assertTrue(checkConnectedness(net, n));

		assertTrue(net.isConnected(0, 1));
		assertTrue(net.isConnected(0, 40));
		assertTrue(net.isConnected(0, 39));
		assertTrue(net.isConnected(0, 360));

		assertTrue(net.isConnected(39, 0));
		assertTrue(net.isConnected(39, 79));
		assertTrue(net.isConnected(39, 38));
		assertTrue(net.isConnected(39, 399));

		assertTrue(net.isConnected(42, 43));
		assertTrue(net.isConnected(42, 82));
		assertTrue(net.isConnected(42, 41));
		assertTrue(net.isConnected(42, 2));

		assertEquals(2, net.getDistance(0, 2));
		assertEquals(3, net.getDistance(0, 42));
		assertEquals(7, net.getDistance(0, 202));
		assertEquals(2, net.getDistance(0, 399));
		assertEquals(25, net.getMaximumDistance());
	}


	@Test
	public void testMakeSmallWorldTorus() {
		System.out.println("**** TestNetworkFactory.testMakeSmallWorldTorus ****");
		int ncols = 4;
		int nrows = 3;
		double p = 0.1;

		Configuration conf = Configuration.init();
		Network netReg = NetworkFactory.makeRegularTorus(conf,ncols, nrows);


		double maxDreg = netReg.getMaximumDistance();
		double mDreg = netReg.getMeanDistance();


		int n = ncols*nrows;

		for(int attempt=0; attempt<7; attempt++)
		{
			System.out.printf(" - Generating small-world net %d\n", attempt);
			Network net = NetworkFactory.makeSmallWorldTorus(conf,ncols, nrows, p);
			assertNotNull(net);
			assertEquals(n, net.size());
			assertEquals(NetworkType.swTorus, net.getType());

			assertTrue(checkConnectedness(net, n));

			// compare with regular network
			assertEquals(net.size(), netReg.size());

			double maxD = net.getMaximumDistance();
			double mD = net.getMeanDistance();

			System.out.printf(" - max distance:  reg=%06.4f; sw=%06.4f\n", maxDreg, maxD);
			System.out.printf(" - mean distance: reg=%06.4f; sw=%06.4f\n", mDreg, mD);
			assertTrue(maxD <= maxDreg);
			assertTrue(mD <= mDreg);
		}
	}




	@Test
	public void testMakeSmallWorldTorus_400() {
		System.out.println("**** TestNetworkFactory.testMakeSmallWorldTorus_400 ****");
		int ncols = 40;
		int nrows = 10;
		double p = 0.01;

		Configuration conf = Configuration.init();

		Network netReg = NetworkFactory.makeRegularTorus(conf,ncols, nrows);
		double maxDreg = netReg.getMaximumDistance();
		double mDreg = netReg.getMeanDistance();

		int n = ncols*nrows;

		for(int attempt=0; attempt<3; attempt++)
		{
			System.out.printf("> Generating small-world net %d: %d x %d\n", attempt, ncols, nrows);
			Network net = NetworkFactory.makeSmallWorldTorus(conf,ncols, nrows, p);
			assertNotNull(net);
			assertEquals(n, net.size());
			assertEquals(NetworkType.swTorus, net.getType());

			assertTrue(checkConnectedness(net, n));

			// compare with regular network
			assertEquals(net.size(), netReg.size());

			double maxD = net.getMaximumDistance();
			double mD = net.getMeanDistance();

			System.out.printf(" - max distance:  reg=%06.4f; sw=%06.4f\n", maxDreg, maxD);
			System.out.printf(" - mean distance: reg=%06.4f; sw=%06.4f\n", mDreg, mD);
			assertTrue(maxD <= maxDreg);
			assertTrue(mD <= mDreg);
		}
	}



	@Test
	public void testMakeParochialSmallWorld()
	{
		System.out.println("**** TestNetworkFactory.testMakeParochialSmallWorld ****");
		int ncols = 4;
		int nrows = 3;
		int pars  = 3;
		double p = 0.1;

		int n = ncols * nrows * pars;
		Configuration conf = Configuration.init();
		Network net = NetworkFactory.makeParochialSmallWorld(conf,ncols, nrows, p, pars);
		assertNotNull(net);
		assertEquals(n, net.size());
		assertEquals(NetworkType.parSW, net.getType());

		assertTrue(checkConnectedness(net, n));
	}


	@Test
	public void testMakeParochialSmallWorld_400()
	{
		System.out.println("**** TestNetworkFactory.testMakeParochialSmallWorld_400 ****");
		int ncols = 10;
		int nrows = 10;
		int pars  = 4;
		double p = 0.1;

		int n = ncols * nrows * pars;
		Configuration conf = Configuration.init();
		Network net = NetworkFactory.makeParochialSmallWorld(conf,ncols, nrows, p, pars);
		assertNotNull(net);
		assertEquals(n, net.size());
		assertEquals(NetworkType.parSW, net.getType());

		assertTrue(checkConnectedness(net, n));
	}


	@Test
	public void testMakeNetwork()
	{
		NetworkType type = null;
		int ncols = 0;
		int nrows = 0;
		int parishes = 0;
		double p = 0.0;
		Agent[] agents = null;

		Network net = null;

		// generate 4*3 network
		type  = NetworkType.regTorus;
		ncols = 4;
		nrows = 3;
		Configuration conf = Configuration.init();
		agents = PopulationFactory.readCSV(conf,agents4x3, prototypeA, prototypeB);
		net = NetworkFactory.makeNetwork(conf, type, ncols, nrows, parishes, p, agents);
		assertEquals(12, net.size());
		for(int i=0; i<12; i++)
		{
			Agent a = net.getAgentAtNode(i);
			assertNotNull(a);
			assertEquals(i, a.getNodeId());
			assertSame(agents[i], a);
		}

		// generate 4*3 network: agents with IDs which don't match node IDs
		type  = NetworkType.regTorus;
		ncols = 4;
		nrows = 3;
		agents = PopulationFactory.readCSV(conf,agents4x3_reverseIDs, prototypeA, prototypeB);
		net = NetworkFactory.makeNetwork(conf,type, ncols, nrows, parishes, p, agents);
		assertEquals(12, net.size());

		for(int i=0, j=11; i<12; i++, j--)
		{
			Agent a = net.getAgentAtNode(i);
			assertNotNull(a);
			assertEquals(i, a.getNodeId());
			assertSame(agents[j], a);
		}
	}





	@Test
	public void testMerge() {
		fail("Not yet implemented");
	}




	private boolean checkConnectedness(Network net, int n)
	{
		boolean ok = true;
		// check if there is a path between any two distinct nodes:
		for(int i=0; i<n; i++) {
			for(int j=0; j<n; j++) {
				int d = net.getDistance(i, j);
				//System.out.printf("i=%02d; j=%02d; dist=%d\n",i,j,d);
				if(i==j) {
					//XXX ATTENTION: network contains no self-edges,
					// however, paths like 1-2-6-5-1 are theoretically possible,
					// still the function getDistance(1,1) returns NO_CONNECTION.
					// This is no problem, since distances for agents to themselves
					// are not relevant for the simulations!
					//assertEquals(AdjacencyMatrix.NO_CONNECTION, d);
					if(AdjacencyMatrix.NO_CONNECTION != d) {
						ok = false;
						break;
					}
				} else {
					//assertNotEquals(AdjacencyMatrix.NO_CONNECTION, d);
					if(AdjacencyMatrix.NO_CONNECTION == d) {
						ok = false;
						break;
					}
				}
			}
			if(!ok) break;
		}
		return ok;
	}



}
