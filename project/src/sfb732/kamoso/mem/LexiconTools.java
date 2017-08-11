package sfb732.kamoso.mem;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.mem.Exemplar.Type;
import sfb732.kamoso.pop.Agent.Gender;
import sfb732.kamoso.util.MyMathHelper;


/**
 * Helper methods for lexicon generation.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public class LexiconTools {



	/**
	 * Generate a lexicon with normally distributed exemplars around the
	 * provided prototype in phonetic space.
	 * Means are taken from the prototype, standard deviations from the sd
	 * argument.
	 * @param conf
	 * @param proto
	 * @param sd
	 * @param lexiconSize
	 * @return
	 */
	public static Lexicon generateNormalLexicon(Configuration conf, Exemplar proto, double[] sd, int lexiconSize)
	{
		return new Lexicon(conf, generateNormalSet(proto, sd, lexiconSize));
	}



	/**
	 * Generate a lexicon with two normally distributed sets of exemplars A and
	 * B. Means are taken from the prototypes, standard deviations from the sd
	 * arguments.
	 * @param conf
	 * @param protoA
	 * @param protoB
	 * @param sdA
	 * @param sdB
	 * @param numA
	 * @param numB
	 * @return
	 */
	public static Lexicon generateNormalLexicon(Configuration conf,
			Exemplar protoA, Exemplar protoB, double[] sdA, double[] sdB,
			int numA, int numB)
	{
		Exemplar[] setA = generateNormalSet(protoA, sdA, numA);
		Exemplar[] setB = generateNormalSet(protoB, sdB, numB);
		Exemplar[] all = new Exemplar[numA+numB];
		System.arraycopy(setA, 0, all, 0, numA);
		System.arraycopy(setB, 0, all, numA, numB);
		return new Lexicon(conf,all);
	}



	/**
	 * Generate a set of exemplars normally distributed around the provided
	 * prototype.
	 * @param proto
	 * @param sd
	 * @param size
	 * @return
	 */
	public static Exemplar[] generateNormalSet(Exemplar proto, double[] sd, int size)
	{
		double[] means = proto.phonFeatures;
		int dim = means.length;

		Type t           = proto.getType();
		double status    = proto.getSpeakerStatus();
		Gender g         = proto.getSpeakerGender();
		double closeness = proto.getSpeakerCloseness();
		double scores    = proto.socialScores;

		Exemplar[] exemplars = new Exemplar[size];
		for(int i=0; i<size; i++)
		{
			double[] phon = new double[dim];
			for(int d=0; d<dim; d++) {
				phon[d] = MyMathHelper.randomGauss(means[d], sd[d]);
			}
			exemplars[i] = new Exemplar(t, status, g, closeness, phon, scores);
		}

		return exemplars;
	}



}
