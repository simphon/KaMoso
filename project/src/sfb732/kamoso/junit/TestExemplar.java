package sfb732.kamoso.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import sfb732.kamoso.mem.Exemplar;
import sfb732.kamoso.util.MyMathHelper;


/**
 * JUnit test case for {@link Exemplar}.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public class TestExemplar {


	private double[] getRandomVector(double min, double max) {
		double[] vec = new double[] {
				MyMathHelper.randomDouble(min, max),
				MyMathHelper.randomDouble(min, max),
				MyMathHelper.randomDouble(min, max),
				MyMathHelper.randomDouble(min, max),
				MyMathHelper.randomDouble(min, max)
		};
		return vec;
	}



	@Test
	public void testGetNoisyCopy() {
		fail("Not yet implemented");
	}

	@Test
	public void testExemplar() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetType() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSpeakerStatus() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSpeakerGender() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSpeakerCloseness() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPhoneticFeature() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPhoneticFeatureDims() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPhoneticString()
	{
		System.out.println("**** TestExemplar.testGetPhoneticString ****");
		// test some random vectors
		double min = -100.0;
		double max = 100.0;
		for(int i=0; i<100; i++){// generate Random vector
			double[] d1 = this.getRandomVector(min, max);
			Exemplar e1 = new Exemplar(null, 0, null, 0, d1, 0);
			System.out.printf("[%03d] e1=%s;\n", i, e1.toString());
			assertTrue(e1.getPhoneticString().length() >= 25);// 5*"," + 5*".000"
		}
	}

	@Test
	public void testToString() {
		System.out.println("**** TestExemplar.testToString ****");
		// test some random vectors
		double min = -100.0;
		double max = 100.0;
		for(int i=0; i<100; i++){// generate Random vector
			double[] d1 = this.getRandomVector(min, max);
			Exemplar e1 = new Exemplar(null, 0, null, 0, d1, 0);
			//System.out.printf("[%03d] e1=%s;\n", i, e1.toString());
			assertTrue(e1.toString().length() >= 40);
		}
	}

	@Test
	public void testGetDistance()
	{
		System.out.println("**** TestExemplar.testGetDistance ****");

		Exemplar e1 = new Exemplar(null, 0, null, 0, new double[]{0,0,0,0,0}, 0 );
		assertEquals(0, e1.getDistance(e1), Double.MIN_VALUE);

		Exemplar e2 = new Exemplar(null, 0, null, 0, new double[]{1,1,1,1,1}, 0 );
		assertEquals(0, e2.getDistance(e2), Double.MIN_VALUE);

		Exemplar e3 = new Exemplar(null, 0, null, 0, new double[]{-1,-1,-1,-1,-1}, 0 );
		assertEquals(0, e3.getDistance(e3), Double.MIN_VALUE);

		assertEquals(2.236, e1.getDistance(e2), 0.001);
		assertEquals(2.236, e2.getDistance(e1), 0.001);
		assertEquals(4.472, e2.getDistance(e3), 0.001);
		assertEquals(4.472, e3.getDistance(e2), 0.001);

		// test some random vectors
		double min = -100.0;
		double max = 100.0;
		for(int i=0; i<100; i++){
			double[] d1 = this.getRandomVector(min, max);
			double[] d2 = this.getRandomVector(min, max);
			e1 = new Exemplar(null, 0, null, 0, d1, 0);
			e2 = new Exemplar(null, 0, null, 0, d2, 0);
			System.out.printf("[%03d] e1=%s;\te2=%s\n", i, e1.toString(), e2.toString());
			assertEquals(0, e1.getDistance(e1), Double.MIN_VALUE);
			assertEquals(0, e2.getDistance(e2), Double.MIN_VALUE);
			double dist12 = e1.getDistance(e2);
			double dist21 = e2.getDistance(e1);
			assertEquals(dist12, dist21, Double.MIN_VALUE);
			assertTrue(dist12 >= 0.0);
			assertTrue(dist21 >= 0.0);
		}
	}

}
