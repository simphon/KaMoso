package sfb732.kamoso.net;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Jama.Matrix;


/**
 * Adjacency matrix of a social network: corresponding to an undirected connected graph.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public class AdjacencyMatrix {


	private static final Logger LOG = LogManager.getLogger(AdjacencyMatrix.class.getCanonicalName());

	/**
	 * The value in a distance matrix indicating that there is no path between a
	 * pair of nodes.
	 */
	public static final int NO_CONNECTION = 0;

	// ===================================================================
	//
	// ===================================================================

	/** adjacency matrix
	 *  <b>index 1:</b> node a ID;
	 *  <b>index 2:</b> node b ID;
	 *  <b>value:</b> 1 if node a is connected to node b
	 */
	protected final int[][] a;

	/** distance matrix (derived from a) */
	protected int[][] d;

	/** inverse distance matrix (derived from d) */
	private double[][] invd;

	private int maxDist;

	private double meanDist;

	private int modCount = Integer.MIN_VALUE;


	public AdjacencyMatrix(int[][] a)
	{
		this.a = a;
	}


	/**
	 * Copy constructor. This creates a new adjacency matrix based on the 
	 * provided other matrix by copying all values.
	 * @param other
	 */
	public AdjacencyMatrix(AdjacencyMatrix other)
	{
		int n = other.a.length;
		this.a = new int[n][n];
		for(int i=0; i<n; i++){
			System.arraycopy(other.a[i], 0, this.a[i], 0, n);
		}
	}


	/**
	 * Get the distance between two nodes. This corresponds the shortest path
	 * length between the two nodes in the network. This does not specify what
	 * the actual paths of the indicated minimum length are,
	 * only that there exists at least one such path.
	 * <p>
	 * Note that off-diagonal cells which contain a value equal to
	 * {@link AdjacencyMatrix#NO_CONNECTION} indicate that there is no path
	 * between the corresponding pair of nodes. If the underlying network is
	 * undirected, this means that there is more than one component of the
	 * network.
	 * </p>
	 * @param i
	 * @param j
	 * @return the minimum number of edges between i and j or
	 *         {@link AdjacencyMatrix#NO_CONNECTION} if there is no connection
	 */
	public int getDistance(int i, int j){
		if(null==this.d){
			getDistanceMatrix();
		}
		return this.d[i][j];
	}



	/**
	 * Get direct neighbors of node i
	 * @param i
	 * @return
	 */
	public int[] getNeighbors(int i) {
		int[] _neighbors = new int[this.a.length];
		int nn = 0;
		for(int j=0; j<this.a.length; j++) {
			if(this.a[i][j] != AdjacencyMatrix.NO_CONNECTION) {
				_neighbors[nn] = j;
				nn++;
			}
		}
		int[] neighbors = new int[nn];
		if(nn> 0) {
			System.arraycopy(_neighbors, 0, neighbors, 0, nn);
		}
		return neighbors;
	}




	/**
	 * Get mean distance between nodes in this network.
	 * @return
	 */
	public double getMeanDistance() {
		if(null==this.d){
			getDistanceMatrix();
		}
		return this.meanDist;
	}


	/**
	 * Get maximum distance between nodes in this network.
	 * @return
	 */
	public int getMaximumDistance() {
		if(null==this.d){
			getDistanceMatrix();
		}
		return this.maxDist;
	}
	//TODO pre-compute in constructor and set public final field?

	/**
	 * Get a distance matrix which gives the shortest distance between any two
	 * nodes in the network according to this adjacency matrix. This matrix does
	 * not specify what the actual paths of the indicated minimum length are, 
	 * only that there exists at least one such path.
	 * <p>
	 * Note that off-diagonal cells which contain a value equal to
	 * {@link AdjacencyMatrix#NO_CONNECTION} indicate that there is no path 
	 * between the corresponding pair of nodes. If the underlying network is
	 * undirected, this means that there is more than one component of the 
	 * network.
	 * @return a quadratic array
	 */
	private int[][] getDistanceMatrix()
	{
		if(null==this.d){
			this.maxDist = 0;
			int n = a.length;
			int missing = (n*n)-n;

			int distSum = 0;
			int numPaths = 0;

			this.d = new int[n][n];
			for(int i=0; i<n; i++)
			{
				for(int j=0; j<n; j++)
				{
					if(0==this.a[i][j]){
						d[i][j] = NO_CONNECTION;
					} else {
						d[i][j] = this.a[i][j];
						distSum++;
						numPaths++;
						missing--;
						if(d[i][j]>this.maxDist){
							this.maxDist = d[i][j];
						}
					}

				}
			}
			// d now contains all direct connections

			Matrix distance0   = getJAMA();
			Matrix distanceKm1 = distance0;

			for(int k=2; k<=n; k++)
			{
				if(missing<=0){// we filled all cells, no longer paths are necessary
					break;
				}
				Matrix distanceK = distanceKm1.times(distance0);

				// merge
				for(int i=0; i<n; i++)
				{
					for(int j=0; j<n; j++)
					{
						double kPaths = distanceK.get(i, j);
						if(i!=j && kPaths>0.0 && d[i][j] == 0){
							d[i][j] = k;
							missing--;
							if(d[i][j]>this.maxDist){
								this.maxDist = k;
							}
							distSum += k;
							numPaths++;
						}
					}
				}
				distanceKm1 = distanceK;
			}
			if(LOG.isDebugEnabled() && missing>0){
				LOG.debug("There are nodes without a path between them in this network!");
			}
			this.meanDist = (double) distSum / (double) numPaths;
		}
		return this.d;
		// this is not safe! Return copy instead?
	}


	/**
	 * Get JAMA {@link Matrix} representation corresponding to this instance
	 * @return a {@link Matrix}
	 */
	private Matrix getJAMA()
	{
		int n = a.length;
		double[][] da = new double[n][n];

		for(int i=0; i<this.a.length; i++)
		{
			for(int j=0; j<this.a[i].length; j++)
			{
				da[i][j] = this.a[i][j];
			}
		}
		return new Matrix(da);
	}


	/**
	 * Check if node i is connected to node j. Note that the node index starts
	 * at 0!
	 * @param i -- a node index
	 * @param j -- a node index
	 * @return <code>true</code> iff node i is connected directly to node j
	 */
	public boolean isConnected(int i, int j){
		return this.a[i][j] != NO_CONNECTION;
	}


	/**
	 * The size of the adjacency matrix. Since it is per definition a quadratic
	 * matrix, the size is defined as the number of rows or columns. This value
	 * does <b>not</b> correspond to the number of edges, but to the total
	 * number of nodes of the underlying network graph.
	 * 
	 * @return an integer &gt;= 0
	 */
	public int size() {
		return this.a.length;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(a);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdjacencyMatrix other = (AdjacencyMatrix) obj;
		if (!Arrays.deepEquals(a, other.a))
			return false;
		return true;
	}


}
