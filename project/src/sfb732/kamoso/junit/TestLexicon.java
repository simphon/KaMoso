package sfb732.kamoso.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.mem.Exemplar;
import sfb732.kamoso.mem.Exemplar.Type;
import sfb732.kamoso.mem.Lexicon;
import sfb732.kamoso.mem.LexiconTools;
import sfb732.kamoso.pop.Agent;
import sfb732.kamoso.pop.Agent.Gender;
import sfb732.kamoso.util.MyMathHelper;

/**
 * 
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 *
 */
public class TestLexicon {






	private Lexicon getNumberedLexicon(int capacity, int size) {
		Exemplar[] ex = new Exemplar[capacity];

		for(int i=0; i<size; i++) {
			ex[i] = new Exemplar(Type.A, 0, Gender.f, 0, new double[]{i}, 0);
		}
		Configuration conf = Configuration.init();
		return new Lexicon(conf, ex);
	}



	@Test
	public void testIterator()
	{
		System.out.println("**** TestLexicon.testIterator ****");

		int capacity = 100;
		int sizeMin = 0;
		int sizeMax = 100;

		for(int s=sizeMin; s<=sizeMax; s++)
		{
			Lexicon lex = this.getNumberedLexicon(capacity, s);
			assertEquals(capacity, lex.getCapacity());
			assertEquals(s, lex.size());

			Iterator<Exemplar> it = lex.iterator();
			assertNotNull(it);
			if(s>0){
				assertTrue(it.hasNext());
				int count = 0;
				while(it.hasNext()) {
					Exemplar e = it.next();
					assertNotNull(e);
					assertEquals(count, (int)e.getPhoneticFeature(0));
					count++;
				}
				assertEquals(s, count);
			} else {
				assertFalse(it.hasNext());
			}

			boolean caught = false;
			try {
				Exemplar e = it.next();// this should throw an exception
				fail("should not exist: "+e.toString());
			} catch (NoSuchElementException ex) {
				caught = true;
			} catch (Exception ex) {
				ex.printStackTrace();
				fail("unexpected exception caught");
			}
			assertTrue(caught);
		}

		// check concurrent modification:
		Lexicon lex = this.getNumberedLexicon(100, 50);
		Iterator<Exemplar> it = lex.iterator();
		for(int i = 0; i< 10; i++) {
			Exemplar e = it.next();
			assertNotNull(e);
		}
		lex.addExemplar(new Exemplar(Type.A, 0, Gender.f, 0, new double[]{-7}, 0));
		boolean caught = false;
		try {
			Exemplar e = it.next();// this should throw an exception
			fail("should not exist: "+e.toString());
		} catch (ConcurrentModificationException ex) {
			caught = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("unexpected exception caught");
		}
		assertTrue(caught);


		// test overfull lexicon
		lex = this.getNumberedLexicon(50, 50);
		assertEquals(50, lex.getCapacity());
		assertEquals(50, lex.size());

		// Add more:
		lex.addExemplar(new Exemplar(Type.A, 0, Gender.f, 0, new double[]{50}, 0));
		lex.addExemplar(new Exemplar(Type.A, 0, Gender.f, 0, new double[]{51}, 0));
		lex.addExemplar(new Exemplar(Type.A, 0, Gender.f, 0, new double[]{52}, 0));
		lex.addExemplar(new Exemplar(Type.A, 0, Gender.f, 0, new double[]{53}, 0));
		assertEquals(50, lex.getCapacity());
		assertEquals(50, lex.size());

		it = lex.iterator();
		assertTrue(it.hasNext());
		for(int i = 0; i< 50; i++) {
			Exemplar e = it.next();
			assertEquals(i+4, (int)e.getPhoneticFeature(0));
		}


		lex = this.getNumberedLexicon(50, 50);
		int addMore = 170;
		int expectedLast = lex.size() + addMore;

		for(int i=0; i<addMore; i++) {
			lex.addExemplar(new Exemplar(Type.A, 0, Gender.f, 0, new double[]{i+50}, 0));
		}
		assertEquals(50, lex.getCapacity());
		assertEquals(50, lex.size());

		boolean[] indices = new boolean[expectedLast];
		it = lex.iterator();
		assertTrue(it.hasNext());
		for(int i = 0; i< 50; i++) {
			Exemplar e = it.next();
			int ix = (int)e.getPhoneticFeature(0);
			indices[ix] = true;
		}
		for(int i = 0; i<expectedLast; i++) {
			if(i>169){
				assertTrue(indices[i]);
			} else {
				assertFalse(indices[i]);
			}
		}
	}

	@Test
	public void testIterator_Random()
	{
		System.out.println("**** TestLexicon.testIterator_Random ****");

		int capacity = 100;
		int sizeMin = 0;
		int sizeMax = 100;
		boolean[] r = new boolean[]{false, true};

		for(int rx=0; rx<r.length; rx++)
		{
			boolean random = r[rx];

			for(int s=sizeMin; s<=sizeMax; s++)
			{
				Lexicon lex = this.getNumberedLexicon(capacity, s);

				StringBuilder sb = new StringBuilder();

				Iterator<Exemplar> it = lex.iterator(random);
				assertNotNull(it);
				if(s>0){
					assertTrue(it.hasNext());
					int count = 0;
					while(it.hasNext()) {
						Exemplar e = it.next();
						assertNotNull(e);
						if(random){
							sb.append((int)e.getPhoneticFeature(0));
							sb.append(';');
						} else {
							assertEquals(count, (int)e.getPhoneticFeature(0));
						}
						count++;
					}
					assertEquals(s, count);
				} else {
					assertFalse(it.hasNext());
				}

				boolean caught = false;
				try {
					Exemplar e = it.next();// this should throw an exception
					fail("should not exist: "+e.toString());
				} catch (NoSuchElementException ex) {
					caught = true;
				} catch (Exception ex) {
					ex.printStackTrace();
					fail("unexpected exception caught");
				}
				assertTrue(caught);

				if(random) {
					System.out.printf("> exemplars: %s\n", sb.toString());
				}
			}

			// check concurrent modification:
			Lexicon lex = this.getNumberedLexicon(100, 50);
			Iterator<Exemplar> it = lex.iterator(random);
			for(int i = 0; i< 10; i++) {
				Exemplar e = it.next();
				assertNotNull(e);
			}
			lex.addExemplar(new Exemplar(Type.A, 0, Gender.f, 0, new double[]{-7}, 0));
			boolean caught = false;
			try {
				Exemplar e = it.next();// this should throw an exception
				fail("should not exist: "+e.toString());
			} catch (ConcurrentModificationException ex) {
				caught = true;
			} catch (Exception ex) {
				ex.printStackTrace();
				fail("unexpected exception caught");
			}
			assertTrue(caught);
		}

	}




	@Test
	public void testGenerateLexicon() {
		fail("Not yet implemented");
	}

	@Test
	public void testReadCSV()
	{
		System.out.println("**** TestLexicon.testReadCSV ****");

		File inFile = new File("testdata/exemplar_prototypes.csv");
		assertTrue(inFile.isFile());

		int capacity = 2;
		Configuration conf = Configuration.init();
		Exemplar[] exemplars = Lexicon.readCSV(conf,inFile, capacity);
		assertNotNull(exemplars);
		assertEquals(capacity, exemplars.length);

		assertEquals(Exemplar.Type.A, exemplars[0].getType());
		assertEquals(0.25, exemplars[0].getSpeakerStatus(), Double.MIN_VALUE);
		assertEquals(Agent.Gender.f, exemplars[0].getSpeakerGender());
		assertEquals(1.0, exemplars[0].getSpeakerCloseness(), Double.MIN_VALUE);
		assertEquals(-39, exemplars[0].getPhoneticFeature(0), Double.MIN_VALUE);
		assertEquals(-39, exemplars[0].getPhoneticFeature(1), Double.MIN_VALUE);
		assertEquals(25, exemplars[0].getPhoneticFeature(2), Double.MIN_VALUE);
		assertEquals(83, exemplars[0].getPhoneticFeature(3), Double.MIN_VALUE);
		assertEquals(38, exemplars[0].getPhoneticFeature(4), Double.MIN_VALUE);

		assertEquals(Exemplar.Type.B, exemplars[1].getType());
		assertEquals(1.0, exemplars[1].getSpeakerStatus(), Double.MIN_VALUE);
		assertEquals(Agent.Gender.f, exemplars[1].getSpeakerGender());
		assertEquals(1.0, exemplars[1].getSpeakerCloseness(), Double.MIN_VALUE);
		assertEquals(36, exemplars[1].getPhoneticFeature(0), Double.MIN_VALUE);
		assertEquals(35, exemplars[1].getPhoneticFeature(1), Double.MIN_VALUE);
		assertEquals(75, exemplars[1].getPhoneticFeature(2), Double.MIN_VALUE);
		assertEquals(-61, exemplars[1].getPhoneticFeature(3), Double.MIN_VALUE);
		assertEquals(59, exemplars[1].getPhoneticFeature(4), Double.MIN_VALUE);
	}



	@Test
	public final void test_similarity()
	{
		double[] phonA = new double[]{-39,-39,25,83,38};
		double[] phonB = new double[]{36,35,75,-61,59};

		Exemplar protoA = new Exemplar(Type.A, 1.0, Gender.f, 0.9, phonA, 1.0);
		Exemplar protoB = new Exemplar(Type.B, 1.0, Gender.f, 0.9, phonB, 1.0);

		Exemplar[] protos = new Exemplar[] { protoA, protoB };
		Exemplar.Type[] types = new Exemplar.Type[]{Type.A,Type.B};


		int nA = 95;
		int nB = 5;

		double noiseSD = 1.0;

		double deltaTolerance = 3;

		Exemplar[] exA = new Exemplar[nA];
		for(int i=0; i<nA; i++){
			exA[i] = new Exemplar(Type.A, 1, Gender.f, 1, this.addNoise(phonA, 0, noiseSD), 0);
		}
		Exemplar[] exB = new Exemplar[nB];
		for(int i=0; i<nB; i++){
			exB[i] = new Exemplar(Type.B, 1, Gender.f, 1, this.addNoise(phonB, 0, noiseSD), 0);
		}
		Exemplar[] exemplars = new Exemplar [nA+nB];
		System.arraycopy(exA, 0, exemplars, 0, nA);
		System.arraycopy(exB, 0, exemplars, nA, nB);
		Configuration conf = Configuration.init();
		Lexicon lex = new Lexicon(conf,exemplars);

		assertEquals(nA+nB, lex.size());

		Exemplar centroidA = lex.getCentroidA();
		assertNotNull(centroidA);

		System.out.printf(Configuration.DEFAULT_LOCALE,"-    proto A: [%.3f, %.3f, %.3f, %.3f, %.3f]\n", 
				protoA.getPhoneticFeature(0),
				protoA.getPhoneticFeature(1),
				protoA.getPhoneticFeature(2),
				protoA.getPhoneticFeature(3),
				protoA.getPhoneticFeature(4)
				);
		System.out.printf(Configuration.DEFAULT_LOCALE,"- centroid A: [%.3f, %.3f, %.3f, %.3f, %.3f]\n", 
				centroidA.getPhoneticFeature(0),
				centroidA.getPhoneticFeature(1),
				centroidA.getPhoneticFeature(2),
				centroidA.getPhoneticFeature(3),
				centroidA.getPhoneticFeature(4)
				);


		assertEquals(phonA[0], centroidA.getPhoneticFeature(0), deltaTolerance);
		assertEquals(phonA[1], centroidA.getPhoneticFeature(1), deltaTolerance);
		assertEquals(phonA[2], centroidA.getPhoneticFeature(2), deltaTolerance);
		assertEquals(phonA[3], centroidA.getPhoneticFeature(3), deltaTolerance);
		assertEquals(phonA[4], centroidA.getPhoneticFeature(4), deltaTolerance);

		Exemplar centroidB = lex.getCentroidB();
		assertNotNull(centroidB);

		System.out.printf(Configuration.DEFAULT_LOCALE,"-    proto B: [%.3f, %.3f, %.3f, %.3f, %.3f]\n", 
				protoB.getPhoneticFeature(0),
				protoB.getPhoneticFeature(1),
				protoB.getPhoneticFeature(2),
				protoB.getPhoneticFeature(3),
				protoB.getPhoneticFeature(4)
				);
		System.out.printf(Configuration.DEFAULT_LOCALE,"- centroid B: [%.3f, %.3f, %.3f, %.3f, %.3f]\n", 
				centroidB.getPhoneticFeature(0),
				centroidB.getPhoneticFeature(1),
				centroidB.getPhoneticFeature(2),
				centroidB.getPhoneticFeature(3),
				centroidB.getPhoneticFeature(4)
				);

		assertEquals(phonB[0], centroidB.getPhoneticFeature(0), deltaTolerance);
		assertEquals(phonB[1], centroidB.getPhoneticFeature(1), deltaTolerance);
		assertEquals(phonB[2], centroidB.getPhoneticFeature(2), deltaTolerance);
		assertEquals(phonB[3], centroidB.getPhoneticFeature(3), deltaTolerance);
		assertEquals(phonB[4], centroidB.getPhoneticFeature(4), deltaTolerance);


		int nProd = 1000;

		double simMin  =  Double.MAX_VALUE;
		double simMax  = -Double.MAX_VALUE;
		double distMin =  Double.MAX_VALUE;
		double distMax = -Double.MAX_VALUE;

		double[] thresholds = new double[] {
				0.1, 0.01, 0.001, 0.0001, 0.00001, 0.000005, 0.000004, 0.000003, 0.000002, 0.000001
		};
		int[] thHist = new int[thresholds.length];
		int num = 0;
		for(int i=0; i<nProd; i++) {
			for(int px=0; px<protos.length; px++) {

				Exemplar noisy = conf.getNoisyCopy(protos[px]);

				for(int tx=0; tx<types.length; tx++) {

					Exemplar.Type t = types[tx];
					Exemplar c = t == Type.A ? centroidA : centroidB;

					double s = lex.getSimilarity(noisy, t);
					double d = Exemplar.getPhoneticDistance(c, noisy);

					if(s < simMin) {
						simMin = s;
					}
					if(s > simMax) {
						simMax = s;
					}
					if(d < distMin) {
						distMin = d;
					}
					if(d > distMax) {
						distMax = d;
					}

					for(int thx=0; thx<thresholds.length; thx++) {
						if(s < thresholds[thx]) {
							thHist[thx]++;
						}
					}
					num++;
				}
			}
		}
		System.out.printf(Configuration.DEFAULT_LOCALE,"Checked %d noisy copies\n", num);
		System.out.printf(Configuration.DEFAULT_LOCALE,"Similarity min=%.9f; max=%13.9f\n", simMin, simMax); 
		System.out.printf(Configuration.DEFAULT_LOCALE,"  Distance min=%.9f; max=%13.9f\n", distMin, distMax); 
		for(int thx=0; thx<thresholds.length; thx++) {
			System.out.printf(Configuration.DEFAULT_LOCALE,"threshold(%.9f) -> %6d hits\n", thresholds[thx], thHist[thx]); 
		}
	}




	@Test
	public final void test_warping()
	{
		System.out.println("**** TestLexicon.test_warping ****");

		int n = 10;
		double deltaTolerance = 0.2;

		// Generate two sets of exemplar
		double[] phonA = new double[]{-1.0, -1.0, -1.0, -1.0, -1.0};
		double[] phonB = new double[]{1.0, 1.0, 1.0, 1.0, 1.0};

		Exemplar[] exA = new Exemplar[n];
		Exemplar[] exB = new Exemplar[n];

		for(int i=0; i<n; i++)
		{
			exA[i] = new Exemplar(Type.A, 1, Gender.f, 1, this.addNoise(phonA, 0, 0.1), 0);
			exB[i] = new Exemplar(Type.B, 1, Gender.f, 1, this.addNoise(phonB, 0, 0.1), 0);
		}

		Exemplar[] exemplars = new Exemplar [2*n];
		System.arraycopy(exA, 0, exemplars, 0, n);
		System.arraycopy(exB, 0, exemplars, n, n);

		Configuration conf = Configuration.init();
		Lexicon lex = new Lexicon(conf,exemplars);

		assertEquals(2*n, lex.size());

		Exemplar centroid = lex.getCentroid();

		assertNotNull(centroid);

		System.out.printf("- centroid  : [%.3f, %.3f, %.3f, %.3f, %.3f]\n", 
				centroid.getPhoneticFeature(0),
				centroid.getPhoneticFeature(1),
				centroid.getPhoneticFeature(2),
				centroid.getPhoneticFeature(3),
				centroid.getPhoneticFeature(4)
				);

		assertTrue(centroid.getPhoneticFeature(0) < deltaTolerance);
		assertTrue(centroid.getPhoneticFeature(1) < deltaTolerance);
		assertTrue(centroid.getPhoneticFeature(2) < deltaTolerance);
		assertTrue(centroid.getPhoneticFeature(3) < deltaTolerance);
		assertTrue(centroid.getPhoneticFeature(4) < deltaTolerance);

		Exemplar centroidA = lex.getCentroidA();
		assertNotNull(centroidA);

		System.out.printf("- centroid A: [%.3f, %.3f, %.3f, %.3f, %.3f]\n", 
				centroidA.getPhoneticFeature(0),
				centroidA.getPhoneticFeature(1),
				centroidA.getPhoneticFeature(2),
				centroidA.getPhoneticFeature(3),
				centroidA.getPhoneticFeature(4)
				);


		assertEquals(phonA[0], centroidA.getPhoneticFeature(0), deltaTolerance);
		assertEquals(phonA[1], centroidA.getPhoneticFeature(1), deltaTolerance);
		assertEquals(phonA[2], centroidA.getPhoneticFeature(2), deltaTolerance);
		assertEquals(phonA[3], centroidA.getPhoneticFeature(3), deltaTolerance);
		assertEquals(phonA[4], centroidA.getPhoneticFeature(4), deltaTolerance);

		Exemplar centroidB = lex.getCentroidB();
		assertNotNull(centroidB);

		System.out.printf("- centroid B: [%.3f, %.3f, %.3f, %.3f, %.3f]\n", 
				centroidB.getPhoneticFeature(0),
				centroidB.getPhoneticFeature(1),
				centroidB.getPhoneticFeature(2),
				centroidB.getPhoneticFeature(3),
				centroidB.getPhoneticFeature(4)
				);

		assertEquals(phonB[0], centroidB.getPhoneticFeature(0), deltaTolerance);
		assertEquals(phonB[1], centroidB.getPhoneticFeature(1), deltaTolerance);
		assertEquals(phonB[2], centroidB.getPhoneticFeature(2), deltaTolerance);
		assertEquals(phonB[3], centroidB.getPhoneticFeature(3), deltaTolerance);
		assertEquals(phonB[4], centroidB.getPhoneticFeature(4), deltaTolerance);


		double start = 2.0;
		double end   = 0.0;
		double step  = -0.005;

		ArrayList<double[]> stimuli = new ArrayList<double[]>();
		double v = start;
		while( v >= end ){
			stimuli.add(new double[]{v, v, v, v, v});
			v += step;
		}

		//Exemplar catB = new Exemplar(Type.B, 1, Gender.f, 1, phonB);
		//String deltaCSV = "";

		System.out.println("stimulus,d_s_p,d_s_A,d_s_B,d_p_A,d_p_B,sim_s_A,sim_s_B");

		for(int sx=0; sx<stimuli.size(); sx++)
		{
			double[] stimPhon = stimuli.get(sx);
			Exemplar stimulus = new Exemplar(null, 1, null, 1, stimPhon, 0);
			Exemplar percept  = lex.getPercept(stimulus, 0);

			double d_s_p = Exemplar.getPhoneticDistance(stimulus, percept);
			double d_s_A = Exemplar.getPhoneticDistance(stimulus, centroidA);
			double d_s_B = Exemplar.getPhoneticDistance(stimulus, centroidB);
			double d_p_A = Exemplar.getPhoneticDistance(percept, centroidA);
			double d_p_B = Exemplar.getPhoneticDistance(percept, centroidB);
			double sim_s_A = lex.getSimilarity(stimulus, Type.A);
			double sim_s_B = lex.getSimilarity(stimulus, Type.B);

			System.out.printf(Configuration.DEFAULT_LOCALE, "%d,%f,%f,%f,%f,%f,%f,%f\n",
					sx,
					d_s_p, d_s_A, d_s_B, d_p_A, d_p_B,
					sim_s_A, sim_s_B
					);

			assertTrue(d_s_p >= 0);
			assertTrue(d_p_A >= 0);
			assertTrue(d_p_B >= 0);
			assertTrue(d_s_A >= 0);
			assertTrue(d_s_B >= 0);
			//assertTrue(d_p_A <= d_s_A);
			//assertTrue(d_p_B <= d_s_B);
		}
	}


	private double[] addNoise(double[] orig, double mean, double sd)
	{
		double[] noisy = new double[orig.length];
		System.arraycopy(orig, 0, noisy, 0, orig.length);
		for(int px=0; px<orig.length; px++){
			noisy[px] += MyMathHelper.randomGauss(mean, sd);
		}
		return noisy;
	}





	@Test
	public final void compareSimilarity()
	{
		System.out.println("**** TestLexicon.compareSimilarity ****");
		//String timeStamp  = new SimpleDateFormat("yyyy-MM-dd+HHmmss").format(new Date());

		double[] phonA = new double[]{3.0, 3.0};
		double[] phonB = new double[]{-3.0, -3.0};
		Exemplar protoA = new Exemplar(Type.A, 0.0, Gender.f, 0.0, phonA, 0.0);
		Exemplar protoB = new Exemplar(Type.B, 0.0, Gender.f, 0.0, phonB, 0.0);
		double[] sdA = new double[]{1.0, 1.0};
		double[] sdB = new double[]{1.5, 1.5};
		int numA = 500;
		int numB = 1000;

		Configuration conf = Configuration.init(new File("testdata/pm.prop"), new File(Configuration.DEFAULT_OUPUT_DIR));

		Lexicon lex = LexiconTools.generateNormalLexicon(conf, protoA, protoB, sdA, sdB, numA, numB);
		assertEquals(numA+numB, lex.size());

		double epsilon = 1.5;

		double[] min = new double[]{-7.0, -7.0};
		double[] max = new double[]{7.0, 7.0};

		int trials = 10000;

		long timeGlobal = 0;
		long timeNeighb = 0;

		ArrayList<Integer> cases = new ArrayList<>();
		cases.add(1);
		cases.add(2);
		cases.add(3);
		cases.add(4);

		for(int i=0; i<trials; i++) {
			long start;
			long end;

			double[] stim = new double[]{
					MyMathHelper.randomDouble(min[0], max[0]),
					MyMathHelper.randomDouble(min[1], max[1])
			};

			double distA = MyMathHelper.getEuclideanDistance(phonA, stim);
			double distB = MyMathHelper.getEuclideanDistance(phonB, stim);

			Type type = distA < distB ? Type.A : Type.B;

			Exemplar stimulus = new Exemplar(type, 0, Gender.f, 0, stim, 0);

			Collections.shuffle(cases);

			for(int cx=0; cx<4; cx++) {

				Integer c = cases.get(cx);

				double sim=Double.NaN;
				switch (c) {
				case 1:
					start = System.currentTimeMillis();
					sim = lex.getSimilarity(stimulus, Type.A);
					end = System.currentTimeMillis();
					timeGlobal += (end-start);
					break;

				case 2:
					start = System.currentTimeMillis();
					sim = lex.getSimilarityN(stimulus, Type.A, epsilon);
					end = System.currentTimeMillis();
					timeNeighb += (end-start);
					break;

				case 3:
					start = System.currentTimeMillis();
					sim = lex.getSimilarity(stimulus, Type.B);
					end = System.currentTimeMillis();
					timeGlobal += (end-start);
					break;

				case 4:
					start = System.currentTimeMillis();
					sim = lex.getSimilarityN(stimulus, Type.B, epsilon);
					end = System.currentTimeMillis();
					timeNeighb += (end-start);
					break;
				}

				assertTrue(sim >= 0.0);
				assertTrue(sim <= 1.0);
			}

			lex.addExemplar(stimulus);
		}

		System.out.printf(Configuration.DEFAULT_LOCALE,
				"Trials= %d; global= %,d ms; neighbor= %,d ms\n",
				trials, timeGlobal, timeNeighb);

	}




}
