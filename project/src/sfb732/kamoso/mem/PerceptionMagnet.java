package sfb732.kamoso.mem;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.util.MyMathHelper;


/**
 * Perceptual magnet.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 * @see Perception
 */
public class PerceptionMagnet implements Perception {

	private static final Logger LOG = LogManager.getLogger(PerceptionMagnet.class.getCanonicalName());

	private final Configuration conf;
	private final Lexicon lex;

	private final double MIN_SIM;

	public PerceptionMagnet(Configuration conf, Lexicon lex, double minSimilarity) {
		this.conf = conf;
		this.lex = lex;
		this.MIN_SIM = minSimilarity;
	}



	// [Wedel&VanVolkinburg:10] ``[...] the perceptual magnet effect is
	// modeled here. To model this effect, each sound in an incoming
	// percept is biased slightly toward previously stored sounds in
	// relation to their distance and frequency [...]''
	//
	// [Wedel2006:265] ``Mimicking the perceptual magnet effect as
	// modeled by Guenther and Gjaja (1996), cross-category blending
	// proceeds by warping of percepts towards distributional maxima in
	// the entire distribution of previously perceived segments. This is
	// done by calculating a ‘population vector’ [...] over the current
	// activations of all segment exemplars relative to the current
	// percept, and averaging the percept with that vector.''
	//
	/**
	 * Get percept for a given stimulus with an applied perceptual magnet effect.
	 * The phonetic features of the stimulus are changed according to the contents
	 * of this lexicon and the implemented auditory principles.
	 * The social closeness features of the percept is updated according to the
	 * specified value.
	 * @param stimulus
	 * @param socialCloseness
	 * @return a new {@link Exemplar} instance
	 */
	// Note: this implementation is insensitive to the total number of exemplars
	// within one category. I.e. the boundaries between categories are not
	// shifted towards the smaller one (as in Lacerda's [1995] model).
	// This is due to the employed global similarity function.
	// See: sfb732.kamoso.test.PerceptualMagnetKuhl.checkLacerda()
	@Override
	public Exemplar getPercept(Exemplar stimulus, double socialCloseness) {
		double[] phon;

		Exemplar.Type t = stimulus.getType();

		if(this.lex.size() > 0)
		{
			//this.lex.computeCentroid();

			Exemplar centroidA = this.lex.getCentroidA();
			Exemplar centroidB = this.lex.getCentroidB();

			double simA = this.lex.getSimilarity(stimulus, Exemplar.Type.A);
			double simB = this.lex.getSimilarity(stimulus, Exemplar.Type.B);


			if(simA>simB) {
				if(simA<MIN_SIM){
					if(LOG.isTraceEnabled())
						LOG.trace(String.format("Similarity A too low: %.6f < %.6f. T=%s", simA, MIN_SIM, t.toString()));
					t = Exemplar.Type.undefined;
					phon = new double[stimulus.getPhoneticFeatureDims()];
					System.arraycopy(stimulus.phonFeatures, 0, phon, 0, phon.length);
				} else {
					phon = MyMathHelper.getWeightedMean(centroidA.phonFeatures, stimulus.phonFeatures, simA, (1.0-simA));
				}
			} else {
				if(simB<MIN_SIM) {
					if(LOG.isTraceEnabled())
						LOG.trace(String.format("Similarity B too low: %.6f < %.6f. T=%s", simA, MIN_SIM, t.toString()));
					t = Exemplar.Type.undefined;
					phon = new double[stimulus.getPhoneticFeatureDims()];
					System.arraycopy(stimulus.phonFeatures, 0, phon, 0, phon.length);
				} else {
					phon = MyMathHelper.getWeightedMean(centroidB.phonFeatures, stimulus.phonFeatures, simB, (1.0-simB));
				}
			}

			if(LOG.isTraceEnabled()){
				LOG.trace(String.format(">   stimulus=%s", Arrays.toString(stimulus.phonFeatures)));
				LOG.trace(String.format("- centroid A=%s", Arrays.toString(centroidA.phonFeatures)));
				LOG.trace(String.format("- centroid B=%s", Arrays.toString(centroidB.phonFeatures)));
				LOG.trace(String.format("-    percept=%s", Arrays.toString(phon)));
			}
		} else {
			// this is a special case for the very first perceived exemplar
			phon = new double[stimulus.getPhoneticFeatureDims()];
			System.arraycopy(stimulus.phonFeatures, 0, phon, 0, phon.length);
		}

		//TODO assign type according to similarities

		Exemplar percept = new Exemplar(t, stimulus.getSpeakerStatus(),
				stimulus.getSpeakerGender(), socialCloseness, phon,
				conf.getSocialSchores(stimulus.getSpeakerStatus(), socialCloseness) );

		return percept;
	}

	@Override
	public Type getType() {
		return Perception.Type.magnet;
	}



	@Override
	public boolean isCategorizing() {
		// FIXME implement categorization?
		return false;
	}

}
