package sfb732.kamoso.conf;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;

import sfb732.kamoso.util.MyMathHelper;


/**
 * Generator of conigurations for optimization.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public class ConfigurationSearch {


	private final static int MAX_TRY = 7;

	private static final int X_ALPHA  = 0;
	private static final int X_BETA   = 1;
	private static final int X_GAMMA  = 2;
	private static final int X_NOISEF = 3;
	private static final int X_NOISEL = 4;


	private final Configuration initial;

	private final double alphaMin;
	private final double alphaMax;

	private final double betaMin;
	private final double betaMax;

	private final double gammaMin;
	private final double gammaMax;

	private final double noiseFacMin;
	private final double noiseFacMax;

	private final double noiseLimMin;
	private final double noiseLimMax;


	private HashSet<double[]> previous;

	private Configuration currentConf;


	/**
	 * Constructor.
	 * @param initial     -- initial configuration
	 * @param alphaMin    -- minimum alpha value
	 * @param alphaMax    -- maximum alpha value
	 * @param betaMin
	 * @param betaMax
	 * @param gammaMin
	 * @param gammaMax
	 * @param noiseFacMin
	 * @param noiseFacMax
	 * @param noiseLimMin
	 * @param noiseLimMax
	 */
	public ConfigurationSearch(Configuration initial, double alphaMin,
			double alphaMax, double betaMin, double betaMax, double gammaMin,
			double gammaMax, double noiseFacMin, double noiseFacMax,
			double noiseLimMin, double noiseLimMax) {
		super();
		this.initial = initial;
		this.alphaMin = alphaMin;
		this.alphaMax = alphaMax;
		this.betaMin = betaMin;
		this.betaMax = betaMax;
		this.gammaMin = gammaMin;
		this.gammaMax = gammaMax;
		this.noiseFacMin = noiseFacMin;
		this.noiseFacMax = noiseFacMax;
		this.noiseLimMin = noiseLimMin;
		this.noiseLimMax = noiseLimMax;

		this.previous = new HashSet<double[]>();
		this.currentConf = initial;
	}



	public ConfigurationSearch(ConfigurationSearch cs) {
		super();
		this.initial = cs.initial;
		this.alphaMin = cs.alphaMin;
		this.alphaMax = cs.alphaMax;
		this.betaMin = cs.betaMin;
		this.betaMax = cs.betaMax;
		this.gammaMin = cs.gammaMin;
		this.gammaMax = cs.gammaMax;
		this.noiseFacMin = cs.noiseFacMin;
		this.noiseFacMax = cs.noiseFacMax;
		this.noiseLimMin = cs.noiseLimMin;
		this.noiseLimMax = cs.noiseLimMax;
		this.previous = cs.previous;
		this.currentConf = cs.currentConf;
	}


	public Configuration getCopy(File outDir) {
		return Configuration.init(this.currentConf.getPropertiesCopy(), outDir);
	}


	public Configuration getCurrent() {
		return this.currentConf;
	}

	public String getOuputPrefix() {
		return this.initial.getOutputPrefix();
	}


	public Configuration nextNeighbor(double noiseFactor, File outDir)
	{
		Configuration c = null;

		double[] t = new double[5];
		t[X_ALPHA]  = currentConf.getExemplarSimilarityWeightAlpha();
		t[X_BETA]   = currentConf.getExemplarSimilarityWeightBeta();
		t[X_GAMMA]  = currentConf.getExemplarSimilarityWeightGamma();
		t[X_NOISEF] = currentConf.getNoiseFactor();
		t[X_NOISEL] = currentConf.getNoiseMaximum();

		double[] p = new double[5];

		for(int i=0; i<MAX_TRY; i++) {
			p[X_ALPHA]  = this.getRandomValue(t[X_ALPHA], this.alphaMin, this.alphaMax, noiseFactor);
			p[X_BETA]   = this.getRandomValue(t[X_BETA], this.betaMin, this.betaMax, noiseFactor);
			p[X_GAMMA]  = this.getRandomValue(t[X_GAMMA], this.gammaMin, this.gammaMax, noiseFactor);
			p[X_NOISEF] = this.getRandomValue(t[X_NOISEF], this.noiseFacMin, this.noiseFacMax, noiseFactor);
			p[X_NOISEL] = this.getRandomValue(t[X_NOISEL], this.noiseLimMin, this.noiseLimMax, noiseFactor);

			if(  ! this.previous.contains(p) ){
				this.previous.add(p);
				c = make(p, outDir);
				break;
			}
		}
		if(null!=c) {
			this.currentConf = c;
		}
		return c;
	}


	private double getRandomValue(double orig, double min, double max, double noise) {
		double r = orig + ( MyMathHelper.randomGauss(0.0, 1.0) * noise);
		if(r < min) r = min;
		if(r > max) r = max;
		return r;
	}



	public Configuration nextRandom(File outDir)
	{
		Configuration c = null;
		double[] p = new double[5];

		for(int i=0; i<MAX_TRY; i++) {

			p[X_ALPHA]  = MyMathHelper.randomDouble(alphaMin, alphaMax);
			p[X_BETA]   = MyMathHelper.randomDouble(betaMin, betaMax);
			p[X_GAMMA]  = MyMathHelper.randomDouble(gammaMin, gammaMax);
			p[X_NOISEF] = MyMathHelper.randomDouble(noiseFacMin, noiseFacMax);
			p[X_NOISEL] = MyMathHelper.randomDouble(noiseLimMin, noiseLimMax);

			if(  ! this.previous.contains(p) ){
				this.previous.add(p);
				c = make(p, outDir);
				break;
			}
		}
		if(null!=c) {
			this.currentConf = c;
		}
		return c;
	}



	private Configuration make(double[] p, File outDir)
	{
		Properties prop = this.initial.getPropertiesCopy();
		prop.setProperty(Configuration.KEY_EX_ALPHA, Double.toString(p[X_ALPHA]));
		prop.setProperty(Configuration.KEY_EX_BETA, Double.toString(p[X_BETA]));
		prop.setProperty(Configuration.KEY_EX_GAMMA, Double.toString(p[X_GAMMA]));
		prop.setProperty(Configuration.KEY_EX_NOISE_FACTOR, Double.toString(p[X_NOISEF]));
		prop.setProperty(Configuration.KEY_EX_NOISE_MAX, Double.toString(p[X_NOISEL]));
		return Configuration.init(prop, outDir);
	}



}
