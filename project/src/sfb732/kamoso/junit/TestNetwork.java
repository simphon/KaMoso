package sfb732.kamoso.junit;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

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
import sfb732.kamoso.pop.Interaction.Type;

public class TestNetwork {


	@Before
	public final void init()
	{
		System.out.println("**** TestNetwork.init ****");

		File outDir = new File(Configuration.DEFAULT_OUPUT_DIR);
		if(! outDir.exists()) {
			System.out.printf("Creating output directory: %s", outDir.getAbsolutePath());
			outDir.mkdir();
		}
	}


	@Test
	public void testReadEdgelistCSV()
	{
		System.out.println("**** TestNetwork.testReadEdgelistCSV ****");

		File csvFile = new File("testdata/edges_par_4x3x2.csv");
		assertTrue(csvFile.isFile());

		int n = 24;

		Configuration conf = Configuration.init();
		Network net = Network.readEdgelistCSV(conf,csvFile);
		assertNotNull(net);
		assertEquals(n, net.size());
		assertTrue(checkConnectedness(net, n));
		assertEquals(NetworkType.undefined, net.getType());

		assertTrue(net.isConnected(0, 1));
		assertTrue(net.isConnected(0, 4));
		assertTrue(net.isConnected(0, 3));
		assertTrue(net.isConnected(0, 8));

		assertTrue(net.isConnected(11, 8));
		assertTrue(net.isConnected(11, 3));
		assertTrue(net.isConnected(11, 10));
		assertTrue(net.isConnected(11, 7));

		assertTrue(net.isConnected(18, 19));
		assertTrue(net.isConnected(18, 22));
		assertTrue(net.isConnected(18, 17));
		assertTrue(net.isConnected(18, 14));

		assertTrue(net.isConnected(7, 16));
		assertTrue(net.isConnected(16, 7));

		assertFalse(net.isConnected(0, 12));
		assertFalse(net.isConnected(11, 12));
		assertFalse(net.isConnected(0, 23));


		Network netRep = Network.readEdgelistCSV(conf,csvFile);
		assertNotNull(netRep);
		assertEquals(n, netRep.size());
		assertTrue(checkConnectedness(netRep, n));
		assertEquals(NetworkType.undefined, netRep.getType());

		assertTrue(net.isEqualTopology(netRep));
	}


	@Test
	public void testNetwork() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsConnected() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDistance() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMeanDistance() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMaximumDistance() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetType() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetAgents() {
		fail("Not yet implemented");
	}

	@Test
	public void testSize() {
		fail("Not yet implemented");
	}

	@Test
	public void testWriteEdgelistCSV()
	{
		System.out.println("**** TestNetwork.testWriteEdgelistCSV ****");

		int ncols = 10;
		int nrows = 10;
		double p = 0.1;

		int n = ncols * nrows;

		Configuration conf = Configuration.init();

		for(int a=0; a<13; a++)
		{
			Network orig = NetworkFactory.makeSmallWorldTorus(conf,ncols, nrows, p);
			assertEquals(n, orig.size());
			checkConnectedness(orig, n);

			File csv = new File(Configuration.DEFAULT_OUPUT_DIR, String.format("test_sw_edgelist_%02d.csv", a));
			csv.deleteOnExit();
			assertTrue(orig.writeEdgelistCSV(csv));
			assertTrue(csv.isFile());

			Network copy = Network.readEdgelistCSV(conf,csv);
			assertEquals(orig.size(), copy.size());
			assertTrue(orig.isEqualTopology(copy));
		}
	}


	/**
	 * Check if there is a path between any pair of distinct nodes in the network
	 * @param net -- the network to be tested
	 * @param n   -- the expected number of nodes in the network
	 * @return
	 */
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



	@Test
	public void testGetSpeakerIterator()
	{
		System.out.println("**** TestNetwork.testReadEdgelistCSV ****");

		Configuration conf = Configuration.init();

		File csvFile = new File("testdata/edges_par_4x3x2.csv");
		Network net = Network.readEdgelistCSV(conf,csvFile);

		File inFile = new File("testdata/agents_0-23_set001.csv");
		File lexFile = new File("testdata/exemplar_prototypes.csv");

		Exemplar[] lex = Lexicon.readCSV(conf,lexFile, 2);
		Exemplar prototypeA = lex[0];
		Exemplar prototypeB = lex[1];

		Agent[] pop = PopulationFactory.readCSV(conf,inFile, prototypeA, prototypeB);

		assertEquals(24, pop.length);

		net.setAgents(pop);

		assertEquals(24, net.size());
		for(int i=0; i<24; i++) {
			Agent a = net.getAgentAtNode(i);
			assertNotNull(a);
			assertEquals(i, a.getNodeId());
			assertEquals(i, a.getId());
		}

		boolean[] possibleSpeakers = new boolean[] {
				false, true,  true,  true, true,
				false, true,  true,  true, true,
				false, true,  true,  true, true,
				false, true,  true,  true, true,
				false, true,  true,  true
		};

		int numTeachers = conf.getNumberOfTeachers();

		int[] found;
		int foundSum;

		System.out.println("--- regular interaction iterator ---");

		// regular interaction iterator
		for(int i=0; i<24; i++)
		{
			//found = new int[24];
			foundSum = 0;
			Agent listener = net.getAgentAtNode(i);
			Iterator<Agent> it = net.getSpeakerIterator(listener, Type.regular);
			assertTrue(it.hasNext());
			while(it.hasNext()) {
				Agent speaker = it.next();
				//System.out.printf("i=%d; agent=%s\n", i, speaker.toString());
				assertTrue( possibleSpeakers[speaker.getNodeId()] );
				assertNotSame(listener, speaker);
				foundSum++;
			}
			assertEquals(numTeachers, foundSum);
		}

		System.out.println("--- status interaction iterator ---");

		// status interaction iterator
		for(int i=0; i<24; i++)
		{
			found = new int[24];
			foundSum = 0;
			Agent listener = net.getAgentAtNode(i);
			Iterator<Agent> it = net.getSpeakerIterator(listener, Type.byStatus);
			assertTrue(it.hasNext());
			while(it.hasNext()) {
				Agent speaker = it.next();
				//System.out.printf("i=%d; agent=%s\n", i, speaker.toString());
				assertTrue( possibleSpeakers[speaker.getNodeId()] );
				assertNotSame(listener, speaker);
				foundSum++;
				found[speaker.getNodeId()]++;
			}
			assertEquals(numTeachers, foundSum);
			Arrays.toString(found);
		}

		System.out.println("--- closeness interaction iterator ---");

		int maxDist = net.getMaximumDistance();
		// distance interaction iterator
		for(int i=0; i<24; i++)
		{
			int[] distHist = new int[maxDist];
			found = new int[24];
			foundSum = 0;
			Agent listener = net.getAgentAtNode(i);
			Iterator<Agent> it = net.getSpeakerIterator(listener, Type.byDistance);
			assertTrue(it.hasNext());
			while(it.hasNext()) {
				Agent speaker = it.next();
				//System.out.printf("i=%d; agent=%s\n", i, speaker.toString());
				//System.out.printf("listener=%d\t speaker=%d\tdist=%d\n", i, speaker.getNodeId(), net.getDistance(i, speaker.getNodeId()));
				int d = net.getDistance(i, speaker.getNodeId());
				distHist[d-1]++;
				assertTrue( possibleSpeakers[speaker.getNodeId()] );
				assertNotSame(listener, speaker);
				foundSum++;
				found[speaker.getNodeId()]++;
			}
			System.out.printf("listener=%d\tdistances=%s\n", i, Arrays.toString(distHist));
			assertEquals(numTeachers, foundSum);
			Arrays.toString(found);
		}

	}




	@Test
	public void testGetSpeakerIteratorByDistanceDet()
	{
		System.out.println("**** TestNetwork.testReadEdgelistCSV ****");

		Configuration conf = Configuration.init(new File("config/is17-Pr-dis-abg.prop"), new File("output/test/"));

		File edgesFile = new File("data/edges-parSW-5x10x8.csv");
		File agentsFile = new File("data/agents400-par2.csv");
		File prototypesFile = new File("data/prototypes2D.csv");


		Network net = Network.readEdgelistCSV(conf,edgesFile);

		Exemplar[] lex = Lexicon.readCSV(conf,prototypesFile, 2);
		Exemplar prototypeA = lex[0];
		Exemplar prototypeB = lex[1];

		Agent[] pop = PopulationFactory.readCSV(conf, agentsFile, prototypeA, prototypeB);

		net.setAgents(pop);

		assertNotNull(net);
		assertNotNull(pop);
		assertEquals(400, net.size());

		for(int i=0; i<400; i++) {
			Agent a = net.getAgentAtNode(i);
			assertNotNull(a);
			assertEquals(i, a.getNodeId());
			assertEquals(i, a.getId());
		}

		int testEpochs = 7;

		for(int epoch = 0; epoch < testEpochs; epoch++)
		{
			Iterator<Agent> it = net.iterator();
			assertTrue(it.hasNext());

			while(it.hasNext()) {
				Agent a = it.next();

				ArrayList<Agent> speakerList = new ArrayList<Agent>();

				Iterator<Agent> jt = net.getSpeakerIterator(a, Type.byDistanceDet);
				assertTrue(jt.hasNext());

				double previous = 1.0;
				while(jt.hasNext())
				{
					Agent speaker = jt.next();
					assertNotNull(speaker);
					assertNotSame(speaker, a);
					assertTrue(speaker.getAge()>0);
					assertNotEquals(a.getNodeId(), speaker.getNodeId());

					double closeness = net.getSocialCloseness(a, speaker);
					assertTrue(closeness <= previous);
					previous = closeness;

					speakerList.add(speaker);
				}
				assertEquals(conf.getNumberOfTeachers(),  speakerList.size());

				// try again and check if the order is the same:
				jt = net.getSpeakerIterator(a, Type.byDistanceDet);
				assertTrue(jt.hasNext());
				int index = 0;
				while(jt.hasNext())
				{
					Agent speaker = jt.next();
					assertSame(speakerList.get(index), speaker);
					index++;
				}

			}
			System.out.printf("**** TestNetwork.testReadEdgelistCSV: epoch %d: OK\n", epoch);
			net.incrementEpoch();
		}
	}

}
