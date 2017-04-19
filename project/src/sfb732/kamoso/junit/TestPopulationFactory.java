package sfb732.kamoso.junit;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.mem.Exemplar;
import sfb732.kamoso.mem.Lexicon;
import sfb732.kamoso.pop.Agent;
import sfb732.kamoso.pop.PopulationFactory;
import sfb732.kamoso.pop.Agent.Gender;

public class TestPopulationFactory {

	@Test
	public void testMain() {
		fail("Not yet implemented");
	}

	@Test
	public void testReadCSV()
	{
		System.out.println("**** TestPopulationFactory.testReadCSV ****");

		File inFile = new File("testdata/agents_0-11_set001.csv");
		File lexFile = new File("testdata/exemplar_prototypes.csv");
		assertTrue(inFile.isFile());
		assertTrue(lexFile.isFile());

		Configuration conf = Configuration.init();

		Exemplar[] lex = Lexicon.readCSV(conf,lexFile, 2);
		Exemplar prototypeA = lex[0];
		Exemplar prototypeB = lex[1];

		Agent[] pop = PopulationFactory.readCSV(conf,inFile, prototypeA, prototypeB);

		assertEquals(12, pop.length);

		double[] expStatus = new double[]{
				0.2, 0.22, 1.0, 1.0, 0.08, 0.18, 0.21, 0.07, 0.12, 0.04, 0.01, 0.22 
		};

		boolean[] expStars = new boolean[]{
				false, false, true, true, false, false,
				false, false, false, false, false, false
		};

		int[] expAge = new int[] {
				1,2,3,4,5,1,  2,3,4,5,1,2
		};

		int[] expLexSize = new int[]{
				0, 100, 200, 300, 400, 0,
				100, 200, 300, 400, 0, 100
		};

		Agent.Gender[] expGender = new Agent.Gender[]{
				Gender.f, Gender.f, Gender.f, Gender.m, Gender.m, Gender.f,
				Gender.f, Gender.f, Gender.m, Gender.m, Gender.m, Gender.f
		};

		for(int i=0; i<12; i++){
			assertEquals(i, pop[i].getId());
			assertEquals(i, pop[i].getNodeId());
			assertEquals(expStatus[i], pop[i].getStatus(), Double.MIN_VALUE);
			assertEquals(expStars[i], pop[i].isStar());
			assertEquals(expAge[i], pop[i].getAge());
			assertEquals(400, pop[i].getLexiconCapacity());
			assertEquals(expLexSize[i], pop[i].getLexiconSize());
			assertEquals(expGender[i], pop[i].getGender());
		}
	}

}
