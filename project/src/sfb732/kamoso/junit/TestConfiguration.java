package sfb732.kamoso.junit;


import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

import sfb732.kamoso.conf.Configuration;


/**
 * JUnit test class
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 *
 */
public class TestConfiguration {


	@Test
	public final void test_publicConstants()
	{
		System.out.println("**** TestConfiguration.test_publicConstants ****");

		assertNotNull(Configuration.DEFAULT_OUPUT_DIR);

		// ---------------------------------------------------------------
		// test program arguments
		HashSet<String> keys = new HashSet<String>();

		assertNotNull(Configuration.ARG_FILE_OUT);
		assertNotNull(Configuration.ARG_HELP1);
		assertNotNull(Configuration.ARG_HELP2);

		int c=0;
		keys.add(Configuration.ARG_FILE_OUT); c++;
		keys.add(Configuration.ARG_HELP1); c++;
		keys.add(Configuration.ARG_HELP2); c++;

		assertEquals(c, keys.size());
	}


	@Test
	public final void testRandomInt()
	{
		System.out.println("**** TestConfiguration.testRandomInt ****");
		int[] starts = new int[]{0,7,100};
		int[] ends   = new int[]{10, 10, 200};
		int iterations = 10000;
		double tolerance = 0.05;

		Configuration conf = Configuration.init();

		for(int l=0; l<starts.length; l++)
		{
			int offset = starts[l];
			int s = starts[l];
			int e = ends[l];
			int n = e - s + 1;
			int[] hist = new int[n];
			for(int i=0; i<iterations; i++)
			{
				int rand = conf.randomInt(s, e);// s+1;//
				hist[ rand-offset ] ++;
			}
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			for(int i=0; i<n; i++)
			{
				if(hist[i] < min)
					min = hist[i];
				if(hist[i]>max)
					max = hist[i];
			}
			double mu = (min+max) / 2.0;
			double ex = iterations / (double)n;
			double q = Math.abs((mu / ex) - 1.0);
			System.out.printf("> randomInt(%04d, %04d) = [min:%04d; max:%04d; mean:%10.6f] - expected: %10.6f (q=%09.6f; tolerance=%06.3f)\n", 
					s, e, min, max, mu, ex, q, tolerance);

			assertTrue("mean exceeds tolerance! This may fail sometimes due to randomness: try again!",q <= tolerance);
		}
	}

}
