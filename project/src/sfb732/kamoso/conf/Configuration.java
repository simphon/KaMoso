package sfb732.kamoso.conf;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sfb732.kamoso.mem.Exemplar;
import sfb732.kamoso.mem.ExemplarTools;
import sfb732.kamoso.net.NetworkType;
import sfb732.kamoso.pop.Agent;
import sfb732.kamoso.pop.AgentTools;
import sfb732.kamoso.pop.Interaction;
import sfb732.kamoso.pop.Agent.Gender;


/**
 * Access to global configuration
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 *
 */
public final class Configuration {

	private static final Logger LOG = LogManager.getLogger(Configuration.class.getCanonicalName());


	// ===================================================================
	//                                              MAIN PROGRAM ARGUMENTS
	// ===================================================================

	// Input/output files ------------------------------------------------

	public static final String ARG_FILE_CONF   = "-conf";

	/** output file name */
	public static final String ARG_FILE_OUT    = "-out";

	// Help --------------------------------------------------------------

	public static final String ARG_HELP1       = "-h";
	public static final String ARG_HELP2       = "--help";

	// Network specifications --------------------------------------------

	/** number of grid columns */
	public static final String ARG_NET_COLS  = "-cols";

	/** network edges */
	public static final String ARG_NET_EDGES = "-e";

	/** network type */
	public static final String ARG_NET_TYPE = "-net";

	/** probability */
	public static final String ARG_NET_PROB  = "-p";

	/** number of parishes */
	public static final String ARG_NET_PARS  = "-par";

	/** number of grid rows */
	public static final String ARG_NET_ROWS  = "-rows";

	// Simulation run settings -------------------------------------------

	public static final String ARG_RUN_EPOCHS = "-epochs";

	// ===================================================================
	//                                          default configuration file
	// ===================================================================


	public static final String DEFAULT_OUPUT_DIR = "output/";

	public static final String DEFAULT_CONF = "default.prop";

	public static final String DEFAULT_ENCODING = "UTF-8";

	/** The default locale.
	 * Attention: Do not change this to some locale, which defines the comma
	 * character (,) as a decimal separator! This would break all
	 * functionality with CSV-file data import and export.
	 */
	public static final Locale DEFAULT_LOCALE = new Locale("en", "GB");


	public static final String FILE_SUFFIX_EPOCHS = "epochs.csv";



	// ===================================================================
	//                                                  PROPERTY FILE KEYS
	// ===================================================================

	/** an identifier for a given configuration */
	protected static final String KEY_TAG         = "tag";

	protected static final String KEY_LOCALE      = "locale";//output only
	protected static final String KEY_PROP_FILE   = "prop.file";//output only
	protected static final String KEY_TIMESTAMP   = "timestamp";// output only

	protected static final String KEY_OUTPUT_DIR  = "output.dir";
	protected static final String KEY_RANDOM_SEED = "random.seed";

	// Output parameters -------------------------------------------------

	protected static final String KEY_OUT_PREFIX        = "out.prefix";

	protected static final String KEY_OUT_DUMP_AG_EVRY  = "out.dump.agents.interval";
	protected static final String KEY_OUT_DUMP_AG_FIRST = "out.dump.agents.first";
	protected static final String KEY_OUT_DUMP_AG_LAST  = "out.dump.agents.last";

	protected static final String KEY_OUT_DUMP_LEX_EVRY  = "out.dump.lexicon.interval";
	protected static final String KEY_OUT_DUMP_LEX_FIRST = "out.dump.lexicon.first";
	protected static final String KEY_OUT_DUMP_LEX_LAST  = "out.dump.lexicon.last";


	// Simulation parameters ---------------------------------------------

	protected static final String KEY_SIM_MAX_WAIT = "sim.max.wait";


	// Network parameters ------------------------------------------------

	protected static final String KEY_NET_FILE           = "net.file";

	protected static final String KEY_NET_TYPE           = "net.type";

	// parameters for network generation:

	// -net regTorus -cols 40 -rows 10
	protected static final String KEY_NET_COLS           = "net.cols";

	protected static final String KEY_NET_ROWS           = "net.rows";

	protected static final String KEY_NET_PARISHES       = "net.pars";

	protected static final String KEY_NET_PROB           = "net.prob";


	protected static final String KEY_NET_MAX_SW         = "net.sw.maxtry";

	// Exemplar and Memory parameters ------------------------------------

	protected static final String KEY_EX_PROTO_FILE      = "x.proto.file";



	protected static final String KEY_EX_ALPHA           = "x.alpha";

	protected static final String KEY_EX_BETA            = "x.beta";

	protected static final String KEY_EX_GAMMA           = "x.gamma";


	protected static final String KEY_EX_DELTA_THRESHOLD = "x.delta.th";

	protected static final String KEY_EX_NOISE_FACTOR    = "x.noise.factor";

	protected static final String KEY_EX_NOISE_MAX       = "x.noise.max";

	protected static final String KEY_EX_PHON_DIM        = "x.phon.dim";

	/** number of exemplars to produce in one utterance */
	protected static final String KEY_EX_UTTERANCE       = "x.utterance";


	protected static final String KEY_EX_MIN_SIM         = "x.min.sim";

	// Social parameters -------------------------------------------------

	protected static final String KEY_SOC_FILE           = "soc.file";

	// if no file specified, generate agents:

	protected static final String KEY_SOC_FEMALE_PROB    = "soc.female.prob";

	protected static final String KEY_SOC_MAX_LIFE       = "soc.max.life";

	protected static final String KEY_SOC_STATUS_MIN     = "soc.status.min";

	protected static final String KEY_SOC_STATUS_MAX     = "soc.status.max";

	protected static final String KEY_SOC_STATUS_HYP     = "soc.status.hyp";

	// social interactions:

	protected static final String KEY_SOC_INTERACTION    = "soc.interaction";

	protected static final String KEY_SOC_TEACHERS       = "soc.teachers";






	// ===================================================================
	//                                           INITIALIZATION AND OUTPUT
	// ===================================================================

	/**
	 * Initialize the default configuration.
	 * @return a new {@link Configuration} instance
	 */
	public static Configuration init() {
		Configuration conf = new Configuration(new File(DEFAULT_OUPUT_DIR), readProperties(null));
		return conf;
	}

	/**
	 * If the provided configuration file is <code>null</code>, the default
	 * configuration will be initialized.
	 * @param confFile -- configuration properties file
	 * @param outDir   -- the current output directory
	 * @return a new {@link Configuration} instance
	 */
	public static Configuration init(File confFile, File outDir)
	{
		Configuration conf = new Configuration(outDir, readProperties(confFile));
		return conf;
	}


	/**
	 * Initialize a configuration for a given set of properties.
	 * @param prop
	 * @param outDir
	 * @return a new {@link Configuration} instance
	 */
	public static Configuration init(Properties prop, File outDir)
	{
		Configuration conf = new Configuration(outDir, prop);
		return conf;
	}

	/**
	 * Save configuration to file.
	 * @param conf
	 * @param outFile
	 * @return <code>false</code> if something went wrong
	 */
	public static boolean save(Configuration conf, File outFile) {
		boolean ok = true;

		if(outFile.isFile()) {
			LOG.warn(String.format("Overwriting existing file at: %s", outFile.getAbsolutePath()));
		} else {
			// create directories
			outFile.getParentFile().mkdirs();
		}
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(outFile)), Configuration.DEFAULT_ENCODING);
			conf.prop.store(writer, " Configuration exported from "+ Configuration.class.getCanonicalName());
			writer.close();
			LOG.info(String.format("Configuration written to: %s", outFile.getAbsolutePath()));

		} catch (IOException e) {
			LOG.error("Could not write configuration file.",e);
			ok = false;
		} finally {
			if(null!=writer) {
				try {
					writer.close();
				} catch (IOException e) {
					LOG.error("Could not close writer for configuration file.",e);
					ok = false;
				}
			}
		}
		return ok;
	}


	// ===================================================================
	//                                           INITIALIZATION AND OUTPUT
	// ===================================================================


	public String getTag() {
		return this.prop.getProperty(KEY_TAG);
	}

	/**
	 * Get time stamp for this configuration instance
	 * @return a string
	 */
	public String getTimestamp() {
		return this.prop.getProperty(KEY_TIMESTAMP);
	}


	protected Properties getPropertiesCopy() {
		Properties p = new Properties();
		Iterator<String> it = this.prop.stringPropertyNames().iterator();
		while(it.hasNext()) {
			String key = it.next();
			String val = this.prop.getProperty(key);
			p.setProperty(key, val);
		}
		return p;
	}


	// ===================================================================
	//                                                      RANDOM NUMBERS
	// ===================================================================

	/**
	 * Generate a pseudo-random, uniformly distributed double drawn from a
	 * uniform distribution in the range <code>[0.0 1.0)</code>
	 * @return a random integer
	 */
	public double randomDouble(){
		return this.rand.nextDouble();
	}


	/**
	 * Generate a pseudo-random, uniformly distributed integer.
	 * @return a random integer
	 */
	public int randomInt(){
		return this.rand.nextInt();
	}


	/**
	 * Generate a pseudo-random integer drawn from a uniform distribution in the
	 * range <code>[0 n)</code>
	 * @param n
	 * @return a random integer between 0 (inclusive) and n (exclusive)
	 */
	public int randomInt(int n){
		return this.rand.nextInt(n);
	}


	/**
	 * Get next pseudo-random integer between specified minimum and maximum values
	 * @param min
	 * @param max
	 * @return a random integer
	 */
	public int randomInt(int min, int max) {
		return this.rand.nextInt((max - min) + 1) + min;
	}

	/**
	 * Get next pseudo-random number, drawn from a normal distribution with mean
	 * 0.0 and standard deviation 1.0. The returned value is scaled by the noise
	 * factor and limited by the maximum noise specified in the current
	 * configuration.
	 * <p>
	 * Note: random numbers exceeding the specified maximum are discarded and
	 * 0.0 is returned instead. This distorts the distribution of the returned
	 * noise values.
	 * @return a random double
	 */
	public double randomNoise() {
		double n = this.rand.nextGaussian() * this.noiseFactor;
		if(n > this.noiseMax || n < this.noiseMin){
			n = 0.0;
		}
		return n;
	}


	protected double getNoiseFactor() {
		return this.noiseFactor;
	}


	protected double getNoiseMaximum() {
		return this.noiseMax;
	}


	// ===================================================================
	//                                                        OUTPUT SETUP
	// ===================================================================

	public String getOutputPrefix() {
		return this.prop.getProperty(KEY_OUT_PREFIX);
	}


	public int getOutputAgentDumpInterval() {
		return Integer.parseInt(this.prop.getProperty(KEY_OUT_DUMP_AG_EVRY, "-1"));
	}

	public boolean getOutputAgentDumpFirst() {
		return Boolean.parseBoolean(this.prop.getProperty(KEY_OUT_DUMP_AG_FIRST, "false"));
	}

	public boolean getOutputAgentDumpLast() {
		return Boolean.parseBoolean(this.prop.getProperty(KEY_OUT_DUMP_AG_LAST, "false"));
	}

	public int getOutputLexiconDumpInterval() {
		return Integer.parseInt(this.prop.getProperty(KEY_OUT_DUMP_LEX_EVRY, "-1"));
	}

	public boolean getOutputLexiconDumpFirst() {
		return Boolean.parseBoolean(this.prop.getProperty(KEY_OUT_DUMP_LEX_FIRST, "false"));
	}

	public boolean getOutputLexiconDumpLast() {
		return Boolean.parseBoolean(this.prop.getProperty(KEY_OUT_DUMP_LEX_LAST, "false"));
	}




	// ===================================================================
	//                                            GENERAL SIMULATION SETUP
	// ===================================================================

	public int getSimulationMaxWait() {
		return Integer.parseInt(this.prop.getProperty(KEY_SIM_MAX_WAIT, "15"));
	}


	// ===================================================================
	//                                                       NETWORK SETUP
	// ===================================================================


	/**
	 * Get CSV-file with network topology specification (edges)
	 * @return <code>null</code> if no file is specified by configuration
	 */
	public File getNetworkFile() {
		File f = null;
		String csv = this.prop.getProperty(KEY_NET_FILE);
		if(null!= csv && !csv.isEmpty()) {
			f = new File(csv);
		}
		return f;
	}


	/**
	 * Get network type
	 * @return instance of {@link NetworkType}
	 */
	public NetworkType getNetworkType() {
		return NetworkType.valueOf(this.prop.getProperty(KEY_NET_TYPE));
	}


	public int getNetworkColumns() {
		return Integer.valueOf(this.prop.getProperty(KEY_NET_COLS));
	}

	public int getNetworkRows() {
		return Integer.valueOf(this.prop.getProperty(KEY_NET_ROWS));
	}

	public int getNetworkParishes() {
		return Integer.valueOf(this.prop.getProperty(KEY_NET_PARISHES));
	}

	public double getNetworkProbability() {
		return Double.valueOf(this.prop.getProperty(KEY_NET_PROB));
	}


	/**
	 * @return maximum number of attempts to re-wire small-world network
	 */
	public int getMaxSWAttempts() {
		return Integer.parseInt(this.prop.getProperty(KEY_NET_MAX_SW));
	}


	// ===================================================================
	//                                                   SOCIAL PARAMETERS
	// ===================================================================


	public File getAgentsFile() {
		File f = null;
		String csv = this.prop.getProperty(KEY_SOC_FILE);
		if(null!= csv && !csv.isEmpty()) {
			f = new File(csv);
		}
		return f;
	}




	public Interaction.Type getInteractionType() {
		return Interaction.Type.valueOf(this.prop.getProperty(KEY_SOC_INTERACTION));
	}

	public int getMaxLifespan() {
		return Integer.parseInt(this.prop.getProperty(KEY_SOC_MAX_LIFE));
	}


	public int getNumberOfTeachers() {
		return Integer.parseInt(this.prop.getProperty(KEY_SOC_TEACHERS));
	}

	/**
	 * Get probability of new born agents being female.
	 * @return a double
	 */
	public double getFemaleProbability(){
		return Double.parseDouble(this.prop.getProperty(KEY_SOC_FEMALE_PROB));
	}



	/**
	 * Get minimum social status of ordinary agents.
	 * @return a double
	 */
	public double getStatusMin() {
		return Double.parseDouble(this.prop.getProperty(KEY_SOC_STATUS_MIN));
	}


	/**
	 * Get maximum social status of ordinary agents.
	 * @return a double
	 */
	public double getStatusMax() {
		return Double.parseDouble(this.prop.getProperty(KEY_SOC_STATUS_MAX));
	}


	/**
	 * Get social status of hyper-influential agents.
	 * @return a double
	 */
	public double getStatusHyp() {
		return Double.parseDouble(this.prop.getProperty(KEY_SOC_STATUS_HYP));
	}


	// ===================================================================
	//                                                           EXEMPLARS
	// ===================================================================


	public File getExemplarPrototypeFile() {
		File f = null;
		String csv = this.prop.getProperty(KEY_EX_PROTO_FILE);
		if(null!= csv && !csv.isEmpty()) {
			f = new File(csv);
		}
		return f;
	}


	/**
	 * Get number of phonetic dimensions of exemplars
	 * @return an integer &gt; 0
	 */
	public int getExemplarPhonDim(){
		return Integer.parseInt(this.prop.getProperty(KEY_EX_PHON_DIM));
	}


	/**
	 * Get exemplar distance threshold above which a default activation should
	 * be applied
	 * @return
	 */
	public double getExemplarDeltaThreshold() {
		return Double.parseDouble(this.prop.getProperty(KEY_EX_DELTA_THRESHOLD));
	}


	public double getExemplarSimilarityWeightAlpha() {
		return Double.parseDouble(this.prop.getProperty(KEY_EX_ALPHA));
	}


	public double getExemplarSimilarityWeightBeta() {
		return Double.parseDouble(this.prop.getProperty(KEY_EX_BETA));
	}


	public double getExemplarSimilarityWeightGamma() {
		return Double.parseDouble(this.prop.getProperty(KEY_EX_GAMMA));
	}




	public double getExemplarSimilarityMinimum() {
		return this.minSim;
	}

	/**
	 * Get number of exemplars produced in one utterance.
	 * @return
	 */
	public int getUtteranceSize(){
		return this.utteranceSize;
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
		return this.et.getNoisyCopy(speaker, orig);
	}

	public Exemplar getNoisyCopy(Exemplar orig)
	{
		return this.et.getNoisyCopy(orig);
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
		return this.et.getScore(candidate, reference);
	}


	public double getSocialSchores(double status, double closeness) {
		return this.et.getSocialSchores(status, closeness);
	}

	// ===================================================================
	//                                                              AGENTS
	// ===================================================================	

	/**
	 * Initialize a new agent with empty lexicon and age 0.
	 * Gender will be assigned probabilistically according to configuration.
	 * Status will be hyper-influential if <code>isStar==true</code>, otherwise
	 * it will be assigned probabilistically according to configuration.
	 * @param nodeID
	 * @param isStar
	 * @param lexCapacity
	 * @return a new {@link Agent} instance
	 */
	public Agent getNewborn(int nodeID, boolean isStar, int lexCapacity)
	{
		return this.at.getNewborn(nodeID, isStar, lexCapacity);
	}


	/**
	 * Initialize a new agent with a given ID.
	 * @param id
	 * @param nodeID
	 * @param age
	 * @param gender
	 * @param status
	 * @param isStar
	 * @param lexicon
	 * @return a new {@link Agent} instance
	 */
	public Agent initAgent(long id, int nodeID, int age, Gender gender, double status, boolean isStar, Exemplar[] lexicon)
	{
		return this.at.initAgent(id, nodeID, age, gender, status, isStar, lexicon);
	}



	// ===================================================================
	//                                                               CLASS
	// ===================================================================


	private final Properties prop;

	private final AgentTools at;
	private final ExemplarTools et;

	private final Random rand;

	private final double noiseFactor;
	private final double noiseMax;
	private final double noiseMin;

	private final File outDir;

	private final double  minSim;
	private final int utteranceSize;

	private final int hash;


	/**
	 * Constructor using configuration from specified file or from a given set
	 * properties.
	 * @param confFile
	 * @param outDir
	 * @param props
	 */
	private Configuration(File outDir, Properties props)
	{
		this.prop = props;

		// generate a time stamp for the output directory
		Date date = new Date();
		String timeStamp  = new SimpleDateFormat("yyyy-MM-dd+HHmmss").format(date);

		// get seed for random number generator
		String seed = this.prop.getProperty(KEY_RANDOM_SEED);
		long seedL;
		if(null!=seed && !seed.isEmpty()) {
			seedL = Long.parseLong(seed);
			LOG.debug(String.format("Setting random seed from configuration file: %d", seedL));
		} else {
			seedL = System.nanoTime();
			LOG.debug(String.format("Setting random seed from system time: %d", seedL));
		}
		this.rand = new Random(seedL);

		// set properties:
		this.prop.setProperty(KEY_LOCALE, DEFAULT_LOCALE.toString());
		this.prop.setProperty(KEY_TIMESTAMP, timeStamp);

		this.noiseFactor = Double.parseDouble(this.prop.getProperty(KEY_EX_NOISE_FACTOR));

		this.noiseMax    = Double.parseDouble(this.prop.getProperty(KEY_EX_NOISE_MAX));
		this.noiseMin    = - this.noiseMax;

		if(null!=outDir) {
			this.outDir = outDir;
			this.prop.setProperty(KEY_OUTPUT_DIR, outDir.getAbsolutePath());
		} else {
			this.outDir = new File(new File(DEFAULT_OUPUT_DIR), timeStamp+File.separator);
			this.prop.setProperty(KEY_OUTPUT_DIR, this.outDir.getAbsolutePath());
		}

		this.at = new AgentTools(this);
		this.et = new ExemplarTools(this);
		this.minSim        = Double.parseDouble(this.prop.getProperty(KEY_EX_MIN_SIM));
		this.utteranceSize = Integer.parseInt(this.prop.getProperty(KEY_EX_UTTERANCE));

		this.hash = computeHash();
	}


	/**
	 * Read properties from file.
	 * @param confFile
	 * @return
	 */
	private static Properties readProperties(File confFile)
	{
		Properties prop;

		if(null==confFile) {
			LOG.info("Loading default configuration");
			prop = ConfigurationDefault.getDefaultConfiguration();
		} else {
			// read from CSV file
			if( ! confFile.isFile() ){
				String err = String.format("Specified configuration file does not seem to be a valid file name", confFile.getAbsolutePath());
				LOG.fatal(err);
				throw new RuntimeException(err);
			}
			LOG.info("Loading configuration from file: "+confFile.getAbsolutePath());
			prop = new Properties();
			BufferedReader br=null;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(confFile), DEFAULT_ENCODING));
				prop.load(br);

				prop.setProperty(KEY_PROP_FILE, confFile.getAbsolutePath());

			} catch (Exception e) {
				LOG.fatal(String.format("Could not read configuration from file: %s", confFile.getAbsolutePath()));
				throw new RuntimeException(e);
			} finally {
				if(null!=br) {
					try {
						br.close();
					} catch (IOException e) {
						LOG.error("Could not close file stream", e);
					}
					br = null;
				}
			}
		}
		return prop;
	}



	private int computeHash() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(minSim);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(noiseFactor);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(noiseMax);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(noiseMin);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((prop == null) ? 0 : prop.hashCode());
		result = prime * result + utteranceSize;
		return result;
	}

	@Override
	public int hashCode() {
		return this.hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Configuration other = (Configuration) obj;
		if (Double.doubleToLongBits(minSim) != Double
				.doubleToLongBits(other.minSim))
			return false;
		if (Double.doubleToLongBits(noiseFactor) != Double
				.doubleToLongBits(other.noiseFactor))
			return false;
		if (Double.doubleToLongBits(noiseMax) != Double
				.doubleToLongBits(other.noiseMax))
			return false;
		if (Double.doubleToLongBits(noiseMin) != Double
				.doubleToLongBits(other.noiseMin))
			return false;
		if (prop == null) {
			if (other.prop != null)
				return false;
		} else if (!prop.equals(other.prop))
			return false;
		if (utteranceSize != other.utteranceSize)
			return false;
		return true;
	}


}
