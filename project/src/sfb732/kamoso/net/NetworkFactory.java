package sfb732.kamoso.net;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.conf.ConfigurationDefault;
import sfb732.kamoso.pop.Agent;
import sfb732.kamoso.util.MyMathHelper;



/**
 * Helper class to generate various types of networks.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public class NetworkFactory {

	private static final Logger LOG = LogManager.getLogger(NetworkFactory.class.getCanonicalName());


	/**
	 * Program interface to generate a network topology and export the edges
	 * to a CSV file.
	 * @param args
	 */
	public static void main(String[] args)
	{
		String outFileName=null;
		NetworkType type = null;

		int nrows = 0;
		int ncols = 0;

		double p = 0.0;

		int parishes = 0;

		try{

			for(int ax=0; ax<args.length; ax++)
			{
				if(args[ax].equals(Configuration.ARG_HELP1) || args[ax].equals(Configuration.ARG_HELP2)){
					printMainHelpAndExit(System.out, 0);
				} else if(args[ax].equals(Configuration.ARG_FILE_OUT)) {
					outFileName= args[++ax];
					continue;
				} else if(args[ax].equals(Configuration.ARG_NET_TYPE)) {
					type = NetworkType.valueOf(args[++ax]);
					continue;
				} else if(args[ax].equals(Configuration.ARG_NET_COLS)) {
					ncols = Integer.parseInt(args[++ax]);
					continue;
				} else if(args[ax].equals(Configuration.ARG_NET_ROWS)) {
					nrows = Integer.parseInt(args[++ax]);
					continue;
				} else if(args[ax].equals(Configuration.ARG_NET_PROB)) {
					p = Double.parseDouble(args[++ax]);
					continue;
				} else if(args[ax].equals(Configuration.ARG_NET_PARS)) {
					parishes = Integer.parseInt(args[++ax]);
					continue;
				} else {
					System.err.printf("Unknown program argument: %s", args[ax]);
					printMainHelpAndExit(System.err, 1);
				}
			}

			if(null==outFileName){
				throw new RuntimeException("Missing required argument: "+Configuration.ARG_FILE_OUT);
			}
			if(null==type){
				throw new RuntimeException("Missing required argument: "+Configuration.ARG_NET_TYPE);
			}

		} catch (Exception e) {
			System.err.println("Could not parse program arguments.");
			e.printStackTrace();
			printMainHelpAndExit(System.err, 1);
		}

		Network net = null;

		Configuration conf = Configuration.init();//TODO use configuration file?

		try {

			net = makeNetwork(conf, type, ncols, nrows, parishes, p, null);

			File outputFile = new File(outFileName);

			net.writeEdgelistCSV(outputFile);

			System.out.printf("Network edges written to file %s\n", outputFile.getAbsolutePath());

		} catch (Exception e) {
			System.err.println("Could not create network.");
			e.printStackTrace();
		}

	}


	/**
	 * Print help message and exit.
	 * @param p
	 * @param exitCode
	 */
	private static void printMainHelpAndExit(PrintStream p, int exitCode)
	{
		p.println("Usage:");
		p.print(ConfigurationDefault.class.getCanonicalName());
		p.println(" [OPTIONS]");

		p.printf("  %-6s [type]   -- network type, with type = {%s}\n", Configuration.ARG_NET_TYPE, NetworkType.getAll());
		p.printf("  %-6s          -- output file name\n", Configuration.ARG_FILE_OUT);
		p.printf("  %-6s          -- show this help messagen and exit\n", Configuration.ARG_HELP1);
		p.printf("  %-6s\n", Configuration.ARG_HELP2);

		p.println("Network specification examples:");
		p.printf("  %-6s %8s -- create a regular grid toroidal network\n", Configuration.ARG_NET_TYPE, NetworkType.regTorus.toString());
		p.printf("  %-6s [N]      -- number of grid columns with N >= 1\n", Configuration.ARG_NET_COLS);
		p.printf("  %-6s [N]      -- number of grid rows, with N >= 1\n", Configuration.ARG_NET_ROWS);
		p.println();
		p.printf("  %-6s %8s -- create a parochial small world network\n", Configuration.ARG_NET_TYPE, NetworkType.parSW.toString());
		p.printf("  %-6s [N]      -- number of grid columns per parish with N >= 1\n", Configuration.ARG_NET_COLS);
		p.printf("  %-6s [N]      -- number of grid rows per parish, with N >= 1\n", Configuration.ARG_NET_ROWS);
		p.printf("  %-6s [N]      -- number of parishes, with N >= 2\n", Configuration.ARG_NET_PARS);
		p.printf("  %-6s [N]      -- re-wiring probability\n", Configuration.ARG_NET_PROB);
		//TODO finish
		System.exit(exitCode);
	}




	/**
	 * Generate a new network according to the provided parameters. Obviously,
	 * not all parameters are relevant for all network types. Optionally, a
	 * set of agents can be assigned to the network nodes.
	 * @param type     -- a predefined network topology
	 * @param ncols    -- number of layout grid columns
	 * @param nrows    -- number of layout grid rows
	 * @param parishes -- number of parishes
	 * @param p        -- probability
	 * @param agents   -- agents to be assigned to network nodes
	 * @return a new {@link Network} instance
	 */
	public static Network makeNetwork(Configuration conf, NetworkType type, int ncols, int nrows, int parishes, double p,
			Agent[] agents)
	{
		Network net = null;

		switch (type) {
		case regTorus:
			net = makeRegularTorus(conf, ncols, nrows);
			break;

		case swTorus:
			net = makeSmallWorldTorus(conf, ncols, nrows, p);
			break;

		case parSW:
			net = makeParochialSmallWorld(conf, ncols, nrows, p, parishes);
			break;

		default:
			throw new RuntimeException("Requested network type (currently) not supported. Sorry.");
		}

		if(null!=agents) {
			LOG.debug("Assigning agents to network");
			net.setAgents(agents);
		}

		return net;
	}




	/**
	 * 
	 * @param numc
	 * @param numr
	 * @return
	 */
	public static Network makeRegularTorus(Configuration conf, int numc, int numr)
	{
		Network net = new Network(conf, makeToroid(numc, numr), NetworkType.regTorus);
		return net;
	}


	/**
	 * 
	 * @param numc
	 * @param numr
	 * @param p
	 * @return
	 */
	public static Network makeSmallWorldTorus(Configuration conf, int numc, int numr, double p)
	{
		AdjacencyMatrix am = makeSmallWordAM(conf.getMaxSWAttempts(), numc, numr, p);
		Network net = new Network(conf, am, NetworkType.swTorus);
		return net;
	}


	/**
	 * Make parochial small-world network.
	 * <p>
	 * The generated parishes are all of equal size <code>n = ncols * nrows</code>.
	 * The size of the entire network is <code>n * parishes</code>
	 * @param ncols     -- number of underlying grid columns per parish
	 * @param nrows     -- number of underlying grid rows per parish
	 * @param p         -- rewiring probability
	 * @param parishes  -- number of parishes
	 * @return a network
	 */
	public static Network makeParochialSmallWorld(Configuration conf, int ncols, int nrows, double p, int parishes)
	{
		if(p<0.0 || p>1.0){
			throw new IllegalArgumentException("Probability parameter p must be in rang 0 <= p <= 1");
		}
		AdjacencyMatrix[] amPars = new AdjacencyMatrix[parishes];

		for(int px=0; px<parishes; px++)
		{
			amPars[px] = makeSmallWordAM(conf.getMaxSWAttempts(), ncols, nrows, p);
		}

		//combine partial adjacency matrices into one big matrix
		AdjacencyMatrix am = merge(amPars);

		// connect parishes
		int parSize = ncols * nrows;
		int startA = 0;
		int endA = parSize -1;

		int startB = parSize;
		int endB = startB + parSize -1;

		for(int px=0; px<parishes; px++)
		{
			if(LOG.isTraceEnabled()) {
				LOG.trace(String.format("px=%d", px));
				LOG.trace(String.format("Parish A = %4d ... %4d", startA, endA));
				LOG.trace(String.format("Parish B = %4d ... %4d", startB, endB));
			}
			// select random node from parish A
			int nodeA = conf.randomInt(startA, endA);
			// select random node from parish B
			int nodeB = conf.randomInt(startB, endB);

			// connect nodeA and nodeB
			am.a[nodeA][nodeB] = 1;
			am.a[nodeB][nodeA] = 1;
			if(LOG.isTraceEnabled())
				LOG.trace(String.format("- connect i=%d and j=%d in parish A and B", nodeA, nodeB));

			// remove two edges from parishes A and B
			for(int i=startA; i<=endA; i++)
			{
				if( am.a[nodeA][i] != AdjacencyMatrix.NO_CONNECTION ){
					if(LOG.isTraceEnabled())
						LOG.trace(String.format("- disconnet i=%d and j=%d in parish A", nodeA, i));
					am.a[nodeA][i] = AdjacencyMatrix.NO_CONNECTION;
					am.a[i][nodeA] = AdjacencyMatrix.NO_CONNECTION;
					break;
				}
			}
			for(int i=startB; i<=endB; i++)
			{
				if( am.a[nodeB][i] != AdjacencyMatrix.NO_CONNECTION ){
					if(LOG.isTraceEnabled())
						LOG.trace(String.format("- disconnet i=%d and j=%d in parish B", nodeB, i));
					am.a[nodeB][i] = AdjacencyMatrix.NO_CONNECTION;
					am.a[i][nodeB] = AdjacencyMatrix.NO_CONNECTION;
					break;
				}
			}

			startA = startB;
			endA   = endB;
			if( px < parishes-2) {
				startB = (px+2) * parSize;
				endB   = startB + parSize -1;
			} else {
				startB = 0;
				endB   = parSize -1;
			}
		}
		am.d = null;

		Network n = new Network(conf, am, NetworkType.parSW);

		LOG.info(String.format("Created network: cols=%d; rows=%d; parishes=%d; nodes=%d\n", ncols, nrows, parishes, n.size()));

		return n;
	}







	/**
	 * Make closed regular network with four neighbors for each node.
	 * The resulting topology is toroidal.
	 * @param ncol
	 * @param nrow
	 * @return an adjacency matrix
	 */
	private static AdjacencyMatrix makeToroid (int ncol, int nrow)
	{
		if(ncol <1 || nrow<1) {
			throw new IllegalArgumentException("Number of columns / rows must be >= 1");
		}

		int n = ncol * nrow;

		int[][] a = new int[n][n];

		int rightEdge  = ncol - 1;
		int bottomEdge = nrow - 1;

		for(int i=0; i<n; i++)
		{
			int r = getGridRowForID(i, ncol);
			int c = getGridColumnForID(i, ncol);
			int j;

			// RIGHT
			if( c == rightEdge ) {
				j = getIDForGridCoordinates(0, r, ncol);
			} else {
				j = i + 1;
			}
			a[i][j] = 1;
			a[j][i] = 1;

			// DOWN
			if( r == bottomEdge ){
				j = getIDForGridCoordinates(c, 0, ncol);
			} else {
				j = getIDForGridCoordinates(c, r+1, ncol);
			}
			a[i][j] = 1;
			a[j][i] = 1;

			// LEFT
			if( c == 0) {
				j = getIDForGridCoordinates(rightEdge, r, ncol);
			} else {
				j = i-1;
			}
			a[i][j] = 1;
			a[j][i] = 1;

			// UP
			if( r == 0) {
				j = getIDForGridCoordinates(c, bottomEdge, ncol);
			} else {
				j = getIDForGridCoordinates(c, r-1, ncol);
			}
			a[i][j] = 1;
			a[j][i] = 1;

		}
		AdjacencyMatrix am = new AdjacencyMatrix(a);
		return am;
	}


	public static int getGridRowForID(int i, int ncols) {
		return (int) Math.floor(i / (double)ncols);
	}

	public static int getGridColumnForID(int i, int ncols) {
		int c = 0;
		if(i < ncols) {
			c = i;
		} else {
			c = (i-ncols) % ncols;
		}
		return c;
	}

	/**
	 * Get node ID for given grid column and row indices in a regular grid.
	 * @param c    -- column index
	 * @param r    -- row index
	 * @param ncol -- number of columns
	 * @return
	 */
	public static int getIDForGridCoordinates(int c, int r, int ncol) {
		return (r * ncol) + c;
	}



	protected static AdjacencyMatrix merge(AdjacencyMatrix[] parts)
	{
		int parishes = parts.length;
		int n = 0;
		for(int px=0; px<parishes; px++)
		{
			n += parts[px].size();
		}

		int[][] am = new int[n][n];

		int offset = 0;

		for(int px=0; px<parishes; px++)
		{
			AdjacencyMatrix par = parts[px];
			int parN = par.size();
			int i = offset;
			for (int pari=0; pari<parN; pari++)
			{
				int j = offset;
				for(int parj=0; parj<parN; parj++)
				{
					am[i][j] = par.a[pari][parj];
					j++;
				}
				i++;
			}
			offset += parN;
		}

		AdjacencyMatrix a = new AdjacencyMatrix(am);
		return a;
	}



	/**
	 * Make small world network. The generated network has only one component
	 * (i.e. there is a path between any given pair of node).
	 * @param maxAttempts -- max. number of attempts to create a network 
	 * @param numc
	 * @param numr
	 * @param p
	 * @return
	 */
	private static AdjacencyMatrix makeSmallWordAM(int maxAttempts, int numc, int numr, double p)
	{
		AdjacencyMatrix am = makeToroid(numc, numr);

		AdjacencyMatrix original = new AdjacencyMatrix(am);
		int n = am.size();

		boolean fehler = true;
		for(int attempt = 0; attempt<maxAttempts; attempt++) {
			if( rewireSmalWorld(am, p) ){
				fehler=false;
				break;
			} else {
				LOG.debug(String.format("attempt %d to rewire small world failed. Trying from scratch!", attempt));
				// reset values of adjacency matrix:
				for(int i=0; i<n; i++){
					System.arraycopy(original.a[i], 0, am.a[i], 0, n);
				}
			}
		}
		if(fehler){
			throw new RuntimeException("Something strange happened: could not create small-world network!");
		}
		return am;
	}




	/**
	 * Re-wire network such that the resulting topology is a
	 * small-world network.
	 * @param am -- adjacency matrix. INSTANCE IS CHANGED BY THIS METHOD
	 * @param p  -- re-wiring probability
	 * @return <code>true</code> if no error was detected
	 */
	private static boolean rewireSmalWorld(AdjacencyMatrix am, double p)
	{

		int n = am.size();
		int cr = 0;

		// key: from node i
		// value: list of replaced to-nodes <original,new>  
		HashMap<Integer, List<MyMathHelper.IntPair>> replacedEdges = new HashMap<Integer, List<MyMathHelper.IntPair>>();

		Random rand = new Random();

		for(int i=0; i<n; i++){
			for(int j=0; j<n; j++){

				if( 0 == am.a[i][j] ){
					continue;
				}

				if(  rand.nextDouble() <= p  ){
					// rewire
					// (1) select new neighbour
					int k = i;
					while(k == i){
						k =  rand.nextInt(n);
						if(k==j){
							k=i;
						}
					}
					if( 0 == am.a[i][k] ){
						Integer iobj = Integer.valueOf(i);
						if( ! replacedEdges.containsKey(iobj)){
							replacedEdges.put(iobj, new ArrayList<MyMathHelper.IntPair>());
						}
						MyMathHelper.IntPair pair = new MyMathHelper.IntPair(j, k);
						replacedEdges.get(iobj).add(pair);

						// (2) disconnect i and j
						am.a[i][j] = 0;
						am.a[j][i] = 0;

						// (3) connect i with k
						am.a[i][k] = 1;
						am.a[k][i] = 1;

						cr++;
					}
				}
			}
		}

		if(LOG.isDebugEnabled()){
			LOG.debug(String.format(Configuration.DEFAULT_LOCALE,"rewired %d connections with p=%.3f", cr, p));
		}

		am.d = null;// re-set distance matrix

		// make sure the network does not fall apart!
		// compute distance matrix
		// check if there is more than one network component:
		// -- find a row with all zeros
		// -- select one node randomly from that row
		// -- select one of its neighbours randomly
		// -- connect the two nodes
		// -- remove one long-distance connection from one of these two nodes

		int maxAttempts = 2*n;

		for(int i=0; i<n; i++){
			for(int j=0; j<n; j++){
				if(i!=j){
					int distance = am.getDistance(i, j);
					if(distance == AdjacencyMatrix.NO_CONNECTION){
						// there is no path between nodes i and j
						// there are at least two network components which are not connected

						if(LOG.isDebugEnabled()){
							LOG.debug(String.format("Re-connecting two unreachable network components between nodes %d and %d", i, j));
						}

						if(maxAttempts>0 && 0==maxAttempts%2){
							// just connect the two unreachable nodes
							// (the problem is usually just a single node which
							// has lost all its edges)
							am.a[i][j] = 1;
							am.a[j][i] = 1;
							if(LOG.isTraceEnabled()){
								LOG.trace(String.format("added connection: %3d - %3d", i, j));
							}
						} else {
							// reconnect i with one of its original neighbours:
							int from = i;

							Integer key = Integer.valueOf(i);
							List<MyMathHelper.IntPair> pairs = replacedEdges.get(key);
							if(pairs==null){
								key = Integer.valueOf(j);
								pairs = replacedEdges.get(key);
								from = j;
							}
							if(pairs==null){
								// still null??
								// select one pair at random:
								Integer[] keys = replacedEdges.keySet().toArray(new Integer[replacedEdges.size()]);
								key = keys[rand.nextInt(keys.length)];
								pairs = replacedEdges.get(key);
								from = key.intValue();
							}
							int sz = pairs.size();
							int pairx = rand.nextInt(sz);
							MyMathHelper.IntPair pair = pairs.get(pairx);

							am.a[from][pair.b] = 0;
							am.a[pair.b][from] = 0;

							am.a[from][pair.a] = 1;
							am.a[pair.a][from] = 1;
							if(LOG.isTraceEnabled()){
								LOG.trace(String.format("disconnected: %3d - %3d\treconnected: %3d - %3d", from, pair.b, from, pair.a));
							}

							pairs.remove(pairx);
							if(pairs.isEmpty()){
								replacedEdges.remove(key);
							}
							if(replacedEdges.isEmpty()){
								return false;
							}
						}

						// start again:
						am.d = null;// force re-calculation of distances

						i=0;
						j=0;
						maxAttempts--;
						if(maxAttempts<0){
							return false;
						}
					}
				}
			}
		}
		replacedEdges.clear();

		return true;
	}



}
