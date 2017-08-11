package sfb732.kamoso.junit;

import static org.junit.Assert.*;

import java.util.Random;

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



	@Test
	public void testRandomGauss()
	{
		Random random = new Random();

		int n = 1000000;

		double[] m = new double[]{0.0, 266.33};
		double[] s = new double[]{1.0,  30.0};

		for(int cx=0; cx<m.length; cx++) {
			double mean = m[cx];
			double sd   = s[cx];
			double sum = 0.0;
			double sumq = 0.0;
			for(int i=0; i<n; i++) {
				double r = MyMathHelper.randomGauss(mean, sd);
				sum += r;

				double q = random.nextGaussian() * sd + mean;
				sumq += q;
			}
			double actualMean = sum / (double)n;
			double actualMeanq = sumq / (double)n;
			System.out.printf(Configuration.DEFAULT_LOCALE,"[testRandomGauss] mean=%9.3f, sd=9.3f, actual mean=%.9f (MyMathHelper)\n", mean, sd, actualMean);
			System.out.printf(Configuration.DEFAULT_LOCALE,"[testRandomGauss] mean=%9.3f, sd=9.3f, actual mean=%.9f\n", mean, sd, actualMeanq);
			System.out.println("[testRandomGauss] mean=" + mean +";\tactual=" + actualMean);
			System.out.println("[testRandomGauss] mean=" + mean +";\tactual=" + actualMean);
			assertEquals(mean, actualMean, 0.05);
		}
	}



	@Test
	public final void testGetWeightedMean()
	{
		System.out.println("**** TestMyMathHelper.testGetWeightedMean ****");
		double delta = 0.0001;

		double[] a = new double[]{1.0, 1.0};
		double[] b = new double[]{-1.0, -1.0};
		double wa = 0.5;
		double wb = 0.5;

		double[] m = MyMathHelper.getWeightedMean(a, b, wa, wb);
		assertEquals(0.0, m[0], delta);
		assertEquals(0.0, m[1], delta);

		m = MyMathHelper.getWeightedMean(a, b, 1.0, 0.0);
		assertEquals(a[0], m[0], delta);
		assertEquals(a[0], m[1], delta);

		m = MyMathHelper.getWeightedMean(a, b, 0.0, 1.0);
		assertEquals(b[0], m[0], delta);
		assertEquals(b[0], m[1], delta);


		Random rand = new Random();
		int repetitions = 1000;

		for(int i=0; i<repetitions; i++) {
			wa = rand.nextDouble();
			wb = 1.0 - wa;
			m = MyMathHelper.getWeightedMean(a, b, wa, wb);

			double da = MyMathHelper.getEuclideanDistance(m, a);
			double db = MyMathHelper.getEuclideanDistance(m, b);

			if(wa>wb) {
				// m should be closer to a
				assertTrue(da < db);
			} else if(wb>wa) {
				// m should be closer to b
				assertTrue(da > db);
			}
		}


		wa = 1.0;
		for(int i=0; i<repetitions; i++) {
			wa = wa / 10.0;
			wb = 1.0 - wa;
			m = MyMathHelper.getWeightedMean(a, b, wa, wb);

			double da = MyMathHelper.getEuclideanDistance(m, a);
			double db = MyMathHelper.getEuclideanDistance(m, b);

			if(wa>wb) {
				// m should be closer to a
				assertTrue(da < db);
			} else if(wb>wa) {
				// m should be closer to b
				assertTrue(da > db);
			}
		}

	}

}
