package sfb732.kamoso.junit;

import static org.junit.Assert.*;

import org.junit.Test;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.util.MyMathHelper;

public class TestMyMathHelper {

	@Test
	public void testGetRandomFlags()
	{
		System.out.println("**** TestMyMathHelper.testGetRandomFlags ****");
		int iterations = 10000;
		double tolerance = 0.05;
		int n = 100;

		double[] tRatios = new double[]{0.1, 0.33, 0.5, 0.66, 0.9};

		for(int rx=0; rx<tRatios.length; rx++)
		{
			double r = tRatios[rx];
			int expected = (int) Math.round(n * r * iterations);
			int nt = 0;

			for(int i = 0; i<iterations; i++) {
				boolean[] flags = MyMathHelper.getRandomFlags(n, r);
				assertEquals(n, flags.length);
				for(int j=0; j<n; j++){
					if(flags[j]){
						nt++;
					}
				}
			}
			double q = Math.abs(1.0 - Math.abs(nt / (double)expected));
			System.out.printf(Configuration.DEFAULT_LOCALE,
					"> getRandomFlags(%04d, %7.4f) * %d: #TRUE=%d, expected=%d (q=%09.6f; tolerance=%06.3f)\n", 
					n, r, iterations, nt, expected, q, tolerance);

			assertTrue("This may fail sometimes due to randomness: try again!",q <= tolerance);

		}
	}

}
