package sfb732.kamoso.util;

import java.util.Random;



/**
 * Collection of little helpers
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public class MyMathHelper {

	private static final Random RAND = new Random();


	/**
	 * A pair of integer values.
	 */
	public static class IntPair {
		public int a;
		public int b;
		public IntPair(int a, int b){
			this.a = a;
			this.b = b;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + a;
			result = prime * result + b;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof IntPair)) {
				return false;
			}
			IntPair other = (IntPair) obj;
			if (a != other.a) {
				return false;
			}
			if (b != other.b) {
				return false;
			}
			return true;
		}
		@Override
		public String toString() {
			return String.format("<%d,%d>", a, b);
		}
	}


	/**
	 * Get the Euclidean distance between two vectors.
	 * <p>
	 * ATTENTION: this method does not check if x and y are of suitable length!
	 * 
	 * @param x
	 * @param y
	 * @return the Euclidean distance between x and y
	 */
	public static double getEuclideanDistance(double[] x, double[] y){
		double d = 0.0;
		// ATTENTION: this assumes that x and y have equal length!
		for(int i=0; i<x.length; i++){
			double diff = x[i] - y[i];
			d += (diff * diff);
		}		
		return Math.sqrt(d);
	}


	/**
	 * Get the <i>squared</i> Euclidean distance between two vectors.
	 * <p>
	 * ATTENTION: this method does not check if x and y are of suitable length!
	 * 
	 * @param x
	 * @param y
	 * @return the squared Euclidean distance between x and y
	 */
	public static double getEuclideanDistanceSq(double[] x, double[] y){
		double d = 0.0;
		// ATTENTION: this assumes that x and y have equal length!
		for(int i=0; i<x.length; i++){
			double diff = x[i] - y[i];
			d += (diff * diff);
		}
		return d;
	}


	/**
	 * Generate a pseudo-random, uniformly distributed double drawn from a
	 * uniform distribution in the range <code>[0.0 1.0)</code>
	 * @return a random integer
	 */
	public static double randomDouble(){
		return RAND.nextDouble();
	}


	/**
	 * Get random number between a minimum and maximum value
	 * (uniformly distributed)
	 * @param min
	 * @param max
	 * @return a random double
	 */
	public static double randomDouble(double min, double max)
	{
		double r = RAND.nextDouble();
		return min + (max-min) * r;
	}



	/**
	 * Get next pseudo-random number from a normal distribution.
	 * @param mean -- desired mean
	 * @param sd   -- desired standard deviation
	 * @return a pseudo-random number.
	 */
	public static double randomGauss(double mean, double sd) {
		return RAND.nextGaussian() * sd + mean;
	}



	/**
	 * 
	 * @param funValues
	 * @return
	 */
	public static int getRandomIntForFunction(double[] funValues)
	{
		double randomMultiplier = 0;
		for (int i = 0; i < funValues.length; i++) {
			randomMultiplier += funValues[i];
		}
		//double randomDouble = RND.nextDouble() * randomMultiplier;
		double randomDouble = RAND.nextDouble() * randomMultiplier;

		int yourFunctionRandomNumber = 0;
		randomDouble = randomDouble - funValues[yourFunctionRandomNumber];
		while (randomDouble > 0) {
			yourFunctionRandomNumber++;
			randomDouble = randomDouble - funValues[yourFunctionRandomNumber];
		}
		return yourFunctionRandomNumber;
	}





	/**
	 * Get a random sequence of booleans with a given probability of
	 * <code>true</code> values
	 * @param n      -- length of sequence
	 * @param tRatio -- probability of <code>true</code> value
	 * @return a boolean array
	 */
	public static boolean[] getRandomFlags(int n, double tRatio)
	{
		boolean[] b = new boolean[n];
		for(int i=0; i<n; i++)
		{
			if(RAND.nextDouble()<tRatio){
				b[i] = true;
			}
		}
		return b;
	}



	/**
	 * Get weighted mean for two vectors.
	 * @param a
	 * @param b
	 * @param wa
	 * @param wb
	 * @return
	 */
	public static double[] getWeightedMean(double[] a, double[] b, double wa, double wb)
	{
		double[] sum = new double[a.length];
		for(int i=0; i<a.length; i++)
		{
			sum[i] = (a[i] * wa + b[i] * wb) / (wa+wb);
		}
		return sum;
	}


}
