package sfb732.kamoso.mem;

import sfb732.kamoso.conf.Configuration;


/**
 * Linear perceptual system: this does not change the phonetic values of a given
 * stimulus exemplar.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public class PerceptionLinear implements Perception {

	//private static final Logger LOG = LogManager.getLogger(PerceptionLinear.class.getCanonicalName());

	private final Configuration conf;

	/**
	 * Constructor.
	 * @param conf -- the current configuration
	 * @param lex  -- ignored
	 * @param minSimilarity -- ignored
	 */
	public PerceptionLinear(Configuration conf, Lexicon lex, double minSimilarity) {
		this.conf = conf;
	}


	@Override
	public Exemplar getPercept(Exemplar stimulus, double socialCloseness) {

		double[] phon = new double[stimulus.getPhoneticFeatureDims()];
		System.arraycopy(stimulus.phonFeatures, 0, phon, 0, phon.length);

		Exemplar percept = new Exemplar(stimulus.getType(), stimulus.getSpeakerStatus(),
				stimulus.getSpeakerGender(), socialCloseness, phon,
				conf.getSocialSchores(stimulus.getSpeakerStatus(), socialCloseness) );

		return percept;
	}

	@Override
	public Type getType() {
		return Perception.Type.linear;
	}




	@Override
	public boolean isCategorizing() {
		// FIXME implement categorization?
		return false;
	}

}
