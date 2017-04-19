package sfb732.kamoso.mem;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.pop.Agent;
import sfb732.kamoso.util.MyMathHelper;



/**
 * Exemplar tools: a little helper class.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public class ExemplarTools {


	private final Configuration conf;

	private final double ALPHA;
	private final double BETA;
	private final double GAMMA;
	private final double WEIGHTSUM;

	private final double thDELTA;
	private final double thACTIVATION;


	/**
	 * Constructor.
	 * @param conf
	 */
	public ExemplarTools(Configuration conf) {
		this.conf = conf;
		ALPHA = conf.getExemplarSimilarityWeightAlpha();
		BETA  = conf.getExemplarSimilarityWeightBeta();
		GAMMA = conf.getExemplarSimilarityWeightGamma();
		WEIGHTSUM    = ALPHA + BETA + GAMMA;
		thDELTA      = conf.getExemplarDeltaThreshold();
		thACTIVATION = Math.exp(-thDELTA);
	}




	public Exemplar getNoisyCopy(Exemplar orig)
	{
		double[] phonFeatures = new double[orig.phonFeatures.length];
		for(int i=0; i<phonFeatures.length; i++)
		{
			phonFeatures[i] = orig.phonFeatures[i] + this.conf.randomNoise();
		}
		return new Exemplar(orig.type, orig.status, orig.gender, orig.closeness, phonFeatures, orig.socialScores);
	}




	/**
	 * Get a noisy copy of an original exemplar.
	 * The original speaker closeness and the internal social similarity scores
	 * are deleted by this method.
	 * @param speaker
	 * @param orig
	 * @return
	 */
	public Exemplar getNoisyCopy(Agent speaker, Exemplar orig)
	{
		double[] phonFeatures = new double[orig.phonFeatures.length];
		for(int i=0; i<phonFeatures.length; i++)
		{
			phonFeatures[i] = orig.phonFeatures[i] + this.conf.randomNoise();
		}
		return new Exemplar(orig.type, speaker.getStatus(), speaker.getGender(), Double.NaN, phonFeatures, Double.NaN);
	}


	/**
	 * Compute the score of a candidate exemplar in comparison to a given
	 * reference exemplar (usually the prototype, centroid or medoid of an
	 * exemplar cloud).
	 *
	 * @param candidate
	 * @param reference
	 * @return a score in the range [0.0 ... 1.0]
	 */
	public double getScore(Exemplar candidate, Exemplar reference)
	{
		double d = MyMathHelper.getEuclideanDistance(candidate.phonFeatures, reference.phonFeatures);
		// try speeding things up:
		double scoreA = d < thDELTA ? (Math.exp(-d)*ALPHA) : thACTIVATION;
		double s = ( scoreA + candidate.socialScores  ) / WEIGHTSUM;
		return s;
	}


	public double getSocialSchores(double status, double closeness) {
		return (BETA*status) + (GAMMA*closeness);
	}


}
