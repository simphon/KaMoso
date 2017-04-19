package sfb732.kamoso.net;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.pop.Agent;
import sfb732.kamoso.pop.Interaction;
import sfb732.kamoso.util.MyFileHelper;



/**
 * Social Network.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 *
 */
public class Network implements Iterable<Agent> {


	private static final Logger LOG = LogManager.getLogger(Network.class.getCanonicalName());



	/**
	 * Expected format:
	 * - comma separated values in plain text file<br/>
	 * - one line per edge<br/>
	 * - first column: starting node ID<br/>
	 * - second column: end node ID<br/>
	 * - other columns are ignored<br/>
	 * - first line is treated as header, thus, the total number of edges is
	 *   equal to the number of non-empty lines in the file minus one<br/>
	 * - node IDs are strings of the form &lt;INTEGER_ID&gt;
	 * @param inFile
	 * @return
	 */
	public static Network readEdgelistCSV(Configuration conf, File inFile)
	{
		LOG.info(String.format("Reading network edges from CSV-file: %s", inFile.getAbsolutePath()));
		AdjacencyMatrix am = null;

		try {
			String[] lines = MyFileHelper.getLines(inFile, true, true);

			HashMap<Integer, HashSet<Integer>> edgeMap = new HashMap<Integer, HashSet<Integer>>();

			Pattern pattern = Pattern.compile(",");

			int maxID = -1;

			for(int lx=1; lx<lines.length; lx++)
			{
				String zeile = lines[lx];
				String[] vals = pattern.split(zeile);
				Integer from = Integer.valueOf(vals[0]);
				Integer to   = Integer.valueOf(vals[1]);

				if(from.intValue() < 0){
					String str = String.format("Node ID must not be <0! Found from-ID=<%s> near line %d", vals[0], lx);
					throw new RuntimeException(str);
				}
				if(to.intValue() < 0){
					String str = String.format("Node ID must not be <0! Found to-ID=<%s> near line %d", vals[1], lx);
					throw new RuntimeException(str);
				}

				if(from.intValue() > maxID) {
					maxID = from.intValue();
				}
				if(to.intValue() > maxID) {
					maxID = to.intValue();
				}

				if( ! edgeMap.containsKey(from)){
					HashSet<Integer> outNodes = new HashSet<Integer>();
					edgeMap.put(from, outNodes);
				}
				edgeMap.get(from).add(to);
			}

			// The following step may blow up your memory:
			//TODO check if matrix may get too large?
			int n = maxID + 1;
			int[][] matrix = new int[n][n];

			Iterator<Integer> it = edgeMap.keySet().iterator();
			while(it.hasNext()) {
				Integer from = it.next();
				for(Integer to : edgeMap.get(from)){
					matrix[from.intValue()][to.intValue()] = 1;
				}
			}

			am = new AdjacencyMatrix(matrix);

		} catch (Exception e) {
			LOG.error("Could not read adjacency matrix from CSV file", e);
			am = null;
		}

		return new Network(conf, am, NetworkType.undefined);

	}




	// ===================================================================
	//                                                                NODE
	// ===================================================================


	private class Node implements Comparable<Node>
	{
		private Agent agent;

		@Override
		public int compareTo(Node o) {
			int c = 0;

			if(this.agent.getStatus() > o.agent.getStatus()) {
				c = -1;
			} else if (this.agent.getStatus() < o.agent.getStatus()) {
				c = 1;
			}
			return c;
		}
	}




	// ===================================================================
	//
	// ===================================================================

	private final Configuration conf;

	private final NetworkType type;

	private final AdjacencyMatrix edges;

	private final Node[] nodes;

	private final int maxAge;

	private final int numTeachers;

	private double[] statusArray;

	private final double[][] closenessMap;

	private long modCount = Long.MIN_VALUE;



	/**
	 * Constructor.
	 * @param a -- adjacency matrix
	 */
	public Network(Configuration conf, AdjacencyMatrix a, NetworkType type) {

		this.conf = conf;
		this.edges = a;
		int n = a.size();

		this.type  = type;

		this.maxAge = conf.getMaxLifespan();

		int _nt = conf.getNumberOfTeachers();

		this.numTeachers = _nt;

		// pre-compute closeness map
		this.closenessMap = new double[n][n];

		double md = this.edges.getMaximumDistance();

		this.nodes = new Node[n];
		for(int i=0; i<n; i++) {
			this.nodes[i] = new Node();
			for(int j=0; j<n; j++) {
				double c = 1.0 - (this.edges.getDistance(i, j) / md);
				this.closenessMap[i][j] = c;
			}
		}

		// initialize status index
		this.statusArray = new double[this.nodes.length];
	}


	/**
	 * This method increments all agents by one epoch.
	 */
	public void incrementEpoch()
	{
		for(int i=0; i<this.nodes.length; i++) {
			Agent smith = this.nodes[i].agent;
			int age = smith.incrementAge();
			if(age >= this.maxAge) {
				// sorry, you're dead
				//TODO dump final lexicon for agent smith to file?
				// generate new agent to replace the old one:
				//TODO set lexicon capacity in config
				Agent k = conf.getNewborn(i, smith.isStar(), smith.getLexiconCapacity());
				this.nodes[i].agent = k;
				this.statusArray[i] = k.getStatus();
				this.modCount++;
			}
		}
	}



	public boolean isConnected(int i, int j) {
		return this.edges.isConnected(i, j);
	}


	public boolean isEqualTopology(Network other) {
		return this.edges.equals(other.edges);
	}


	public Agent getAgentAtNode(int nodeID) {
		return this.nodes[nodeID].agent;
	}

	/**
	 * Get distance between nodes i and j.
	 * @param i
	 * @param j
	 * @return
	 */
	public int getDistance(int i, int j) {
		return this.edges.getDistance(i, j);
	}

	/**
	 * Get direct neighbors of node i
	 * @param i
	 * @return
	 */
	public int[] getNeighbors(int i) {
		return this.edges.getNeighbors(i);
	}

	/**
	 * Get mean distance between nodes in this network.
	 * @return
	 */
	public double getMeanDistance() {
		return this.edges.getMeanDistance();
	}


	/**
	 * Get maximum distance between nodes in this network.
	 * @return
	 */
	public int getMaximumDistance() {
		return this.edges.getMaximumDistance();
	}


	/**
	 * The closeness of two agents within the network, defined as:
	 * <code> closeness = 1 - ( d / d_max)</code>
	 * where d is the distance between agent a and b and
	 * d_max is the maximum distance between any two agents in the network.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public double getSocialCloseness(Agent a, Agent b) {
		int d = this.edges.getDistance(a.getNodeId(), b.getNodeId());
		return 1.0 - (  d / (double)this.edges.getMaximumDistance() );
	}




	public NetworkType getType() {
		return this.type;
	}


	/**
	 * Set agents of this network.
	 * <p>
	 * The number of agents must be equal to the number of nodes in this network.
	 * Agents will be assigned to the nodes specified by the agent's node ID
	 * (cf. {@link Agent#getNodeId()}), if the ID is <code>&gt;= 0</code>.
	 * @param agents -- array with initialized instances of {@link Agent}
	 */
	public void setAgents(Agent[] agents) {
		int n = agents.length;
		if( n != this.nodes.length ){
			throw new IllegalArgumentException("Number of agents must be equal to number of nodes");
		}
		try {
			for(int i=0; i<n; i++) {
				Agent a = agents[i];
				int node = a.getNodeId();
				this.nodes[node].agent = a;
				// update status index
				this.statusArray[node] = a.getStatus();
			}
			this.modCount++;
		} catch (ArrayIndexOutOfBoundsException e) {
			String msg = "Agent node specification incompatible with network";
			LOG.fatal(msg, e);
			throw new RuntimeException(e);
		}
	}


	/**
	 * Number of nodes in this network
	 * @return integer &gt; 0
	 */
	public int size() {
		return this.edges.size();
	}




	/**
	 * Write edges to CSV file.
	 * <p>
	 * The file structure is as follows:<br/>
	 * - column 1: start node ID<br/>
	 * - column 2: end node ID<br/>
	 * - column 3: edge weight (currently always 1)<br/>
	 * @param outFile
	 * @return
	 */
	public boolean writeEdgelistCSV(File outFile)
	{
		boolean ok = true;

		OutputStreamWriter out = null;

		try {
			out  = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(outFile)), Configuration.DEFAULT_ENCODING);
			out.write("from,to,weight\n");

			int n = this.edges.size();

			for(int from = 0; from < n; from++)
			{
				for(int to = 0; to < n; to++)
				{
					if(this.edges.a[from][to] != 0){
						// this class assumes an unweighted network graph, so we set weight always to 1
						String zeile = String.format("%d,%d,1\n", from, to);
						out.write(zeile);
					}
				}
			}
			LOG.debug(String.format("edges written to: %s", outFile.getAbsolutePath()));

		} catch (UnsupportedEncodingException e) {
			LOG.error("Could not write matrix to file", e.getCause());
			ok = false;
		} catch (FileNotFoundException e) {
			LOG.error("Could not write matrix to file", e.getCause());
			ok = false;
		} catch (IOException e) {
			LOG.error("Could not write matrix to file", e.getCause());
			ok = false;
		} finally {
			if(null!=out){
				try {
					out.close();
				} catch (IOException e) {
					LOG.error("Could not close output stream", e.getCause());
					ok = false;
				}
				out = null;
			}
		}

		return ok;
	}


	// ===================================================================
	//                                                           ITERATORS
	// ===================================================================

	@Override
	public Iterator<Agent> iterator() {
		return new NetworkIterator();
	}


	/**
	 * Get speaker iterator according to specified interaction type. Agents
	 * returned by this iterator are all above age 0.
	 * @param listener
	 * @param it
	 * @return
	 */
	public Iterator<Agent> getSpeakerIterator(Agent listener, Interaction.Type it) {
		Iterator<Agent> iterator;
		int listenerNodeID = listener.getNodeId();
		switch (it) {
		case regular:
			iterator = new NetworkIteratorNonLinear(this.getIndicesOfSpeakers(listenerNodeID));
			break;

		case byDistance:
			iterator = new NetworkIteratorNonLinear(this.getIndicesByDistance(listenerNodeID));
			break;

		case byStatus:
			iterator = new NetworkIteratorNonLinear(this.getIndicesBySatus(listenerNodeID));
			break;

		case byStatusAndDistance:
			//TODO implement
			throw new RuntimeException("interaction byStatusAndDistance not implemented");

		default:
			throw new IllegalArgumentException("unsupported interaction type");
		}
		return iterator;
	}


	/**
	 * 
	 * @param listenerNode
	 * @param numTeachers
	 * @return
	 */
	private int[] getIndicesByDistance(int listenerNode)
	{
		int[] teachers = new int[numTeachers];
		int i= 0;
		while(i < numTeachers)
		{
			// select one node at random
			int nx = this.conf.randomInt(this.nodes.length);
			if(nx==listenerNode){
				continue;
			}
			if(null==this.nodes[nx] || this.nodes[nx].agent.getAge()==0){
				continue;
			}
			// take node with p=closeness
			if( this.conf.randomDouble() < this.closenessMap[listenerNode][nx] ){
				teachers[i] = nx;
				i++;
			}
		}
		return teachers;
	}



	/**
	 * 
	 * @param listenerNode
	 * @param numTeachers
	 * @return
	 */
	private int[] getIndicesBySatus(int listenerNode)
	{
		int[] indices = new int[numTeachers];

		int i=0;
		while(i<numTeachers) {
			// select random node
			int j = this.conf.randomInt(this.statusArray.length);
			if(listenerNode==j){
				continue;
			}
			if(null==this.nodes[j] || this.nodes[j].agent.getAge()==0){
				continue;
			}
			if(this.statusArray[j]<=0.0) {
				// ignore node
				continue;
			} else if (this.statusArray[j]>=1.0) {
				// take node (p=1.0)
				indices[i] = j;
				i++;
			} else {
				// take node with p=s
				if( this.conf.randomDouble() < this.statusArray[j] ) {
					indices[i] = j;
					i++;
				}
			}
		}
		return indices;
	}


	/**
	 * 
	 * @param listenerNode
	 * @return
	 */
	private int[] getIndicesOfSpeakers(int listenerNode)
	{
		int[] speakers = new int[numTeachers];
		if(this.numTeachers>=this.nodes.length){

			// select random start point
			int i = this.conf.randomInt(this.nodes.length);
			// loop through nodes until enough teachers are found
			int numT = 0;
			while(numT<numTeachers) {
				if(i!=listenerNode &&
						this.nodes[i].agent.getAge() > 0){
					speakers[numT] = i;
					numT++;
				}
				i++;
				if(i==this.nodes.length) {
					i = 0;
				}
			}



		} else {
			boolean[] found = new boolean[this.nodes.length];
			int i = 0;
			while(i < numTeachers) {
				int j = this.conf.randomInt(this.nodes.length);
				if( j!=listenerNode && 
						this.nodes[j].agent.getAge() > 0 &&
						!found[j]  ) {
					speakers[i] = j;
					found[j] = true;
					i++;
				}
			}
		}
		return speakers;
	}


	/**
	 * The default iterator which will return all agents in this network in the
	 * sequential order of their corresponding node IDs
	 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
	 */
	private class NetworkIterator implements Iterator<Agent>
	{
		private final long expectedMod;
		private int ptr;

		public NetworkIterator() {
			this.expectedMod = modCount;
			this.ptr = 0;
		}

		@Override
		public boolean hasNext() {
			if(this.expectedMod!=modCount){
				throw new ConcurrentModificationException();
			}
			return this.ptr < nodes.length;
		}

		@Override
		public Agent next() {
			if(this.expectedMod!=modCount){
				throw new ConcurrentModificationException();
			}
			if(this.ptr < nodes.length ) {
				Agent next = nodes[this.ptr].agent;
				this.ptr++;
				return next;
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {} // not needed
	}


	/**
	 * A special iterator for non-linear iteration through the set of agents
	 * in this network.
	 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
	 */
	private class NetworkIteratorNonLinear implements Iterator<Agent>
	{
		private final long expectedMod;
		private int ptr;
		private int[] indices;

		public NetworkIteratorNonLinear(int[] indices) {
			this.expectedMod = modCount;
			this.indices = indices;
			this.ptr = 0;
		}

		@Override
		public boolean hasNext() {
			if(this.expectedMod!=modCount){
				throw new ConcurrentModificationException();
			}
			return this.ptr < this.indices.length;
		}

		@Override
		public Agent next() {
			if(this.expectedMod!=modCount){
				throw new ConcurrentModificationException();
			}
			Agent a;
			if(this.ptr < this.indices.length){
				a = nodes[this.indices[this.ptr]].agent;
				this.ptr++;
			} else {
				throw new NoSuchElementException();
			}
			return a;
		}

		@Override
		public void remove() {}
	}


}
