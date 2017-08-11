package sfb732.kamoso.mem;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.mem.Exemplar.Type;
import sfb732.kamoso.pop.Agent;
import sfb732.kamoso.util.MyFileHelper;
import sfb732.kamoso.util.MyMathHelper;


/**
 * Collection of exemplars.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public class Lexicon implements Iterable<Exemplar> {

	private static final Logger LOG = LogManager.getLogger(Lexicon.class.getCanonicalName());




	public enum Similarity {
		/** global lexicon similarity taking into account all exemplars */
		global,

		/** neighbourhood lexicon similarity taking into account only exemplars
		 * within the specified epsilon radius around a stimulus 
		 */
		epsilon
	}





	// ===================================================================
	//                                                    CSV FILE COLUMNS
	// ===================================================================

	/** number of CSV columns (without phonetic dimensions) */
	public static final int CSV_MIN_COLS      = 4;

	public static final int CSV_COL_TYPE      = 0;
	public static final int CSV_COL_S_STATUS  = 1;
	public static final int CSV_COL_S_GENDER  = 2;
	public static final int CSV_COL_CLOSENESS = 3;
	public static final int CSV_COL_PHON_0    = 4;

	public static final String CSV_HEAD_TYPE      = "type";
	public static final String CSV_HEAD_S_STATUS  = "speaker_status";
	public static final String CSV_HEAD_S_GENDER  = "speaker_gender";
	public static final String CSV_HEAD_CLOSENESS = "closeness";
	public static final String CSV_HEAD_PHON_     = "phon_";

	// ===================================================================



	/**
	 * Generate a collection of {@link Exemplar} instances.
	 * The returned array will contain noisy copies of the provided exemplar
	 * prototypes.
	 * @param conf       -- the current configuration
	 * @param capacity   -- length of returned array
	 * @param size       -- number of non-<code>null</code> elements
	 * @param ratioA     -- ratio of type A exemplars (0.0&lt;=r&lt;=1.0)
	 * @param prototypeA -- type A exemplar prototype
	 * @param prototypeB -- type B exemplar prototype
	 * @return array with new {@link Exemplar} instances
	 */
	public static Exemplar[] generateLexicon(Configuration conf, int capacity, int size, double ratioA,
			Exemplar prototypeA, Exemplar prototypeB )
	{
		if(capacity < size) {
			throw new IllegalArgumentException("Capacity must not be smaller than lexicon size");
		}
		if(ratioA < 0.0 || ratioA > 1.0) {
			throw new IllegalArgumentException("Ratio_A must be in range [0.0--1.0]");
		}

		ExemplarTools et = new ExemplarTools(conf);
		Exemplar[] lex = new Exemplar[capacity];

		if(size>0) {
			boolean[] typeA = MyMathHelper.getRandomFlags(size, ratioA);

			for(int i=0; i<size; i++)
			{
				if(typeA[i]){
					lex[i] = et.getNoisyCopy(prototypeA);
				} else {
					lex[i] = et.getNoisyCopy(prototypeB);
				}
			}
		}
		return lex;
	}



	/**
	 * Read lexicon data from a CSV file.
	 * @param inFile
	 * @param capacity
	 * @return
	 */
	public static Exemplar[] readCSV(Configuration conf, File inFile, int capacity)
	{
		int csv_cols = conf.getExemplarPhonDim() + CSV_MIN_COLS;

		Exemplar[] lex = new Exemplar[capacity];

		// read data from file
		String[] lines = MyFileHelper.getLines(inFile, true, true);

		if(lines.length - 1 > capacity){
			LOG.warn(String.format("Lexicon capacity=%d smaller than number of rows=%d in CSV: will ignore last rows in file!", 
					capacity, lines.length));
		}

		// parse contents of input file
		Pattern komma = Pattern.compile(",");

		int phonDim = conf.getExemplarPhonDim();
		int[] phonCols = new int[phonDim];
		for(int i=0; i<phonDim; i++){
			phonCols[i] = CSV_COL_PHON_0 + i;
		}

		// start with second line, since first line contains the column headers
		for(int i=1; i<lines.length; i++)
		{
			String[] fields =  komma.split(lines[i]);
			if(fields.length!=csv_cols) {
				String msg = String.format("Invalid CSV file. Expecting %d columns, found %d", csv_cols, fields.length);
				LOG.error(msg);
				throw new RuntimeException(msg);
			}

			Exemplar.Type type;
			double status;
			Agent.Gender gender;
			double closeness;
			double[] phon;

			try {

				type      = Exemplar.Type.valueOf(fields[CSV_COL_TYPE]);
				status    = Double.parseDouble(fields[CSV_COL_S_STATUS]);
				gender    = Agent.Gender.valueOf(fields[CSV_COL_S_GENDER]);
				closeness = Double.parseDouble(fields[CSV_COL_CLOSENESS]);

				phon = new double[phonDim];
				for(int px=0; px<phonDim; px++){
					phon[px] = Double.parseDouble(fields[phonCols[px]]);
				}

				//TODO check if all required fields are non-empty

				lex[i-1] = new Exemplar(type, status, gender, closeness, phon, conf.getSocialSchores(status, closeness));

				if(i >= lex.length) {
					break;
				}

			} catch (Exception e) {
				String msg = "Could not parse CSV data";
				LOG.error(msg, e);
				throw new RuntimeException(msg, e);
			}
		}


		return lex;
	}



	/**
	 * Read prototype exemplars from CSV file. There must be exactly one
	 * exemplar for each type in {@link Exemplar.Type}
	 * @param inFile
	 * @param numTypes
	 * @return
	 */
	public static Exemplar[] readPrototypes(Configuration conf, File inFile, int numTypes)
	{
		LOG.info(String.format("Reading exemplar prototypes from CSV-file: %s", inFile.getAbsolutePath()));
		Exemplar[] lex = Lexicon.readCSV(conf, inFile, numTypes);
		// check data consistency
		HashSet<Exemplar.Type> foundTypes = new HashSet<Exemplar.Type>();
		for(int i=0; i<numTypes; i++) {
			Exemplar e = lex[i];
			if(foundTypes.contains(e.getType())){
				throw new RuntimeException("The should be only one prototype for each Exemplar.type!");
			} else {
				foundTypes.add(e.getType());
			}
		}
		if( foundTypes.size() != numTypes) {
			throw new RuntimeException("The should be one prototype for each Exemplar.type!");
		}
		return lex;
	}


	// ===================================================================
	//                                                               CLASS
	// ===================================================================

	private final Configuration conf;

	private final Perception perception;
	private final Similarity simType;
	private final double epsilon;

	private final double MIN_SIM;
	private final double thDELTA;
	private final double thACTIVATION;
	private final int DIM;

	private final Exemplar[] exemplars;
	private int size = 0;// current number of contained words;
	private int start;// inclusive
	private long modCountA = Long.MIN_VALUE+1;
	private long modCountB = Long.MIN_VALUE+1;

	// generated:
	private double[] scores;
	private long scoresStampA = Long.MIN_VALUE;
	private long scoresStampB = Long.MIN_VALUE;

	private double[] centroidA;
	private Exemplar centroidAExemplar;
	private long centroidAStamp = Long.MIN_VALUE;

	private double[] centroidB;
	private Exemplar centroidBExemplar;
	private long centroidBStamp = Long.MIN_VALUE;



	/**
	 * Constructor.
	 * The capacity of this lexicon will be equal to the length of the provided
	 * array.
	 * @param conf -- the current configuration
	 * @param exemplars -- the lexicon contents
	 */
	public Lexicon(Configuration conf, Exemplar[] exemplars)
	{
		this.conf         = conf;
		this.MIN_SIM      = this.conf.getExemplarSimilarityMinimum();
		this.thDELTA      = conf.getExemplarDeltaThreshold();
		this.thACTIVATION = Math.exp(-thDELTA);
		this.DIM          = conf.getExemplarPhonDim();

		this.exemplars = exemplars;

		// determine actual lexicon size:
		this.start = 0;
		for(int i=0; i<this.exemplars.length; i++)
		{
			if(null==this.exemplars[i]){
				break;
			} else {
				// current exemplar is not NULL
				this.size++;
			}
		}

		Perception.Type pt = conf.getPerceptionType();
		switch (pt) {
		case magnet:
			this.perception = new PerceptionMagnet(conf, this, this.MIN_SIM);
			break;

		case linear:
			this.perception = new PerceptionLinear(conf, null, 0);
			break;

		default:
			throw new RuntimeException("Unsupported type of perception: " + pt.toString());
		}

		this.simType = conf.getLexiconSimilarityType();
		if(this.simType==Similarity.epsilon){
			this.epsilon = conf.getLexiconSimilarityEpsilon();
		} else {
			this.epsilon = Double.NaN;
		}
	}


	public int getCapacity() {
		return this.exemplars.length;
	}

	public int size() {
		return this.size;
	}



	// ===================================================================
	//                                                          PERCEPTION
	// ===================================================================

	/**
	 * Get percept for a given stimulus.
	 * @param stimulus
	 * @param socialCloseness
	 * @return a new {@link Exemplar} instance
	 */
	public Exemplar getPercept(Exemplar stimulus, double socialCloseness)
	{
		return this.perception.getPercept(stimulus, socialCloseness);
	}


	/**
	 * Get lexicon similarity of the given stimulus to exemplars of the
	 * specified type. This uses the similarity function specified by the
	 * current configuration.
	 * @param stimulus
	 * @param type
	 * @return the similarity between 0 and 1
	 */
	public double getSimilarity(Exemplar stimulus, Exemplar.Type type)
	{
		double sim;

		switch (this.simType) {
		case global:
			sim = this.getSimilarityG(stimulus, type);
			break;

		case epsilon:
			sim = this.getSimilarityN(stimulus, type, epsilon);
			break;

		default:
			sim = Double.NaN;
			break;
		}
		return sim;
	}



	/**
	 * Get similarity of the given stimulus to exemplars of the specified type
	 * in this exemplar collection.
	 * This implements a <i>"global"</i> similarity.
	 * <p>
	 * The total similarity <code>sim_T</code> of a stimulus to a category
	 * <code>T</code> is computed according to the following formula:<br/> 
	 * <code> s_T(x) =  IF d <= th THEN e ^ (-d) ELSE e ^ -th</code><br/>
	 * <code> sim_T = SUM {x in T} s_T(x)</code><br/>
	 * where <code>th</code> is the distance threshold as defined in the current
	 * configuration;
	 * <code>d</code> is the Euclidean distance between the stimulus and
	 * an exemplar <code>x</code>.
	 * 
	 * @param stimulus
	 * @param type
	 * @return the similarity between 0 and 1
	 */
	public double getSimilarityG(Exemplar stimulus, Exemplar.Type type)
	{
		// compute global similarity to exemplar set
		double sim = 0;
		int num=0;
		for(int i=0; i<this.exemplars.length; i++)
		{
			Exemplar e = this.exemplars[i];
			if(null==e || e.getType()!=type){
				continue;
			}
			double d = MyMathHelper.getEuclideanDistance(stimulus.phonFeatures, e.phonFeatures);
			if( d > thDELTA) {
				sim += thACTIVATION;
			} else {
				sim += Math.exp(-d);
			}
			num++;
		}
		if(num>0)
			sim = sim / (double)num;

		return sim;
	}





	/**
	 * Compute similarity based on number of exemplars within the specified
	 * epsilon radius around the given stimulus.
	 * @param stimulus
	 * @param type
	 * @param epsilon
	 * @return
	 */
	public double getSimilarityN(Exemplar stimulus, Exemplar.Type type, double epsilon)
	{
		double sim = 0;

		// pre-compute lower and upper bound in each phonetic dimension
		double[] lower = new double[DIM];
		double[] upper = new double[DIM];
		for(int dx=0; dx<DIM; dx++) {
			double feature = stimulus.getPhoneticFeature(dx);
			lower[dx] = feature - epsilon;
			upper[dx] = feature + epsilon;
		}

		// compute NeighbA and NeighbB, the number of exemplars within the
		// epsilon neighborhood of the stimulus which belong to category A and B
		int nA = 0;
		int nB = 0;

		for(int i=0; i<this.exemplars.length; i++)
		{
			Exemplar e = this.exemplars[i];
			if(null==e){
				continue;
			}
			// check if e is within epsilon-neighborhood
			boolean skip = false;
			for(int dx=0; dx<DIM; dx++) {
				double feature = stimulus.getPhoneticFeature(dx);
				if(feature < lower[dx]){
					skip=true;
					break;
				}
				if(feature > upper[dx]){
					skip=true;
					break;
				}
			}
			if(skip) {
				continue;
			}
			double d = e.getDistance(stimulus);
			if(d <= epsilon) {
				if(e.type==Type.A) {
					nA++;
				} else if(e.type==Type.B) {
					nB++;
				} else {
					LOG.warn("Exemplar with undefinded category found in lexicon!");
				}
			}

		}

		double sum = (double)(nA + nB);

		if(sum>0.0) {
			if(type==Type.A) {
				sim = nA / sum;
			} else if(type==Type.B) {
				sim = nB / sum;
			} else {
				LOG.error("Requested similarity for unknown exemplar type. Returnung 0.");
			}
		}

		return sim;
	}


	// ===================================================================
	// GENERAL ACCESS
	// ===================================================================





	/**
	 * Add exemplar e to this lexicon. If the number of stored exemplars reaches
	 * the lexicon capacity, the new exemplar e will overwrite the oldest
	 * exemplar in this collection.
	 * @param e
	 */
	public void addExemplar(Exemplar e) {
		if(this.size < this.exemplars.length){
			this.exemplars[this.size] = e;
			this.size++;
		} else {
			// replace oldest exemplar
			this.exemplars[this.start] = e;
			this.start = (this.start + 1) % this.exemplars.length;
		}
		if(e.getType()==Type.A){
			this.modCountA++;
		} else {
			this.modCountB++;
		}
	}


	/**
	 * Get a random exemplar according to its score (i.e. higher scores are more
	 * likely to be returned).
	 * @return an exemplar from this lexicon
	 */
	public Exemplar getGoodExemplar()
	{
		Exemplar e = null;
		if(this.size > 0){
			this.computeScores();
			while(null==e){
				int ix = MyMathHelper.getRandomIntForFunction(scores);
				e=this.exemplars[ix];
			}
		} else {
			LOG.debug("there are no exemplars in this lexicon");
		}
		return e;
	}


	private void computeScores() {
		boolean recomputeA = this.scoresStampA != this.modCountA;
		boolean recomputeB = this.scoresStampB != this.modCountB;
		if(null==this.scores) {
			recomputeA=true;
			recomputeB=true;
		}
		if(recomputeA || recomputeB)
		{
			this.computeCentroid();
			this.scores = new double[this.exemplars.length];
			if(0<this.size)
			{
				for(int ix=this.exemplars.length; --ix>=0; ){
					Exemplar e = this.exemplars[ix];
					if(null!=e){
						if(recomputeA && e.getType()==Type.A) {
							this.scores[ix] = conf.getScore(this.exemplars[ix], centroidAExemplar);
						} else if(recomputeB && e.getType()==Type.B) {
							this.scores[ix] = conf.getScore(this.exemplars[ix], centroidBExemplar);
						}
					}
				}
			}
			if(recomputeA)
				this.scoresStampA = this.modCountA;
			if(recomputeB)
				this.scoresStampB = this.modCountB;
		}
	}



	/**
	 * Get phonetic centroid of this lexicon.
	 * All other features are undefined!
	 * @return a virtual Exemplar
	 */
	public Exemplar getCentroid (){
		this.computeCentroid();
		double[] c = MyMathHelper.getWeightedMean(centroidA, centroidB, 1, 1);
		return new Exemplar(null, Double.NaN, null, Double.NaN, c, Double.NaN);
	}

	/**
	 * Get phonetic centroid of this lexicon for variant A.
	 * All other features are undefined!
	 * @return a virtual Exemplar
	 */
	public Exemplar getCentroidA (){
		this.computeCentroid();
		return this.centroidAExemplar;
	}

	/**
	 * Get phonetic centroid of this lexicon for variant B.
	 * All other features are undefined!
	 * @return a virtual Exemplar
	 */
	public Exemplar getCentroidB (){
		this.computeCentroid();
		return this.centroidBExemplar;
	}


	private void computeCentroid()
	{
		boolean recomputeA = null==this.centroidA || this.modCountA != this.centroidAStamp;
		boolean recomputeB = null==this.centroidB || this.modCountB != this.centroidBStamp;
		if(recomputeA || recomputeB)
		{
			int numA = 0;
			int numB = 0;
			if(recomputeA)
				this.centroidA = new double[DIM];
			if(recomputeB)
				this.centroidB = new double[DIM];
			if(0<this.size)
			{
				for(int ix=this.exemplars.length; --ix>=0; ){
					Exemplar e = this.exemplars[ix];
					if(null!=e){
						if(recomputeA && e.getType()==Type.A){
							for(int sx=DIM; --sx>=0; ){
								this.centroidA[sx] += e.getPhoneticFeature(sx);
							}
							numA++;
						} else if(recomputeB && e.getType()==Type.B) {
							for(int sx=DIM; --sx>=0; ){
								this.centroidB[sx] += e.getPhoneticFeature(sx);
							}
							numB++;
						}
					}
				}
				for(int sx=DIM; --sx>=0; ){
					if(recomputeA && numA>0){
						this.centroidA[sx] = this.centroidA[sx] / (double)numA;
					}
					if(recomputeB && numB>0) {
						this.centroidB[sx] = this.centroidB[sx] / (double)numB;
					}
				}
			}
			if(recomputeA){
				this.centroidAExemplar = new Exemplar(null, Double.NaN, null, Double.NaN, centroidA, Double.NaN);
				this.centroidAStamp = this.modCountA;
			}
			if(recomputeB){
				this.centroidBExemplar = new Exemplar(null, Double.NaN, null, Double.NaN, centroidB, Double.NaN);
				this.centroidBStamp = this.modCountB;
			}
		}
	}




	/**
	 * Get the variant type which is represented most in this lexicon
	 * @return
	 */
	public Exemplar.Type getMajorityType () {
		int numA = 0;
		for(int i=0; i<this.exemplars.length; i++) {
			if(null!=this.exemplars[i]) {
				if(this.exemplars[i].getType()==Type.A){
					numA++;
				}
			}
		}
		int numB = this.size -numA;
		if(numB>numA) {
			return Type.B;
		} else {
			return Type.A;
		}
	}


	/**
	 * @return {@link Double#NaN} if the lexicon is empty,
	 *   otherwise the ratio of {@link Type#A} exemplars
	 */
	public double getVariantARatio () {
		if(this.size == 0){
			return Double.NaN;
		}
		int numA = 0;
		for(int i=0; i<this.exemplars.length; i++) {
			if(null!=this.exemplars[i]) {
				if(this.exemplars[i].getType()==Type.A){
					numA++;
				}
			}
		}
		return (double)numA / (double)this.size;
	}




	// ===================================================================
	//                                                           OUTPUT
	// ===================================================================


	public void writeCSV(File outFile)
	{
		OutputStreamWriter writer=null;

		try {
			writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(outFile)), Configuration.DEFAULT_ENCODING);

			boolean writeHead = true;

			if(this.size > 0)
			{
				for(int i=0; i<this.exemplars.length; i++)
				{
					if(null!=this.exemplars[i]) {
						if(writeHead) {
							writer.write(this.exemplars[i].getCSVHead());
							writer.write('\n');
							writeHead=false;
						}
						writer.write(this.exemplars[i].getCSV());
						writer.write('\n');
					}
				}
				writer.flush();
			}
			if(LOG.isInfoEnabled()) {
				LOG.info(String.format("Written lexicon to file %s", outFile.getAbsolutePath()));
			}

		} catch (IOException e) {
			LOG.error("Could not write lexicon to file", e);
		} finally {
			if(null!=writer) {
				try {
					writer.close();
				} catch (IOException e) {
					LOG.error("Could not close lexicon file", e);
				}
				writer=null;
			}
		}
	}



	public void writeToStream(OutputStream out)
	{
		boolean writeHead = true;
		byte[] nl = "\n".getBytes();

		if(this.size > 0)
		{
			try {
				for(int i=0; i<this.exemplars.length; i++)
				{
					if(null!=this.exemplars[i]) {
						if(writeHead) {
							out.write(this.exemplars[i].getCSVHead().getBytes());
							out.write(nl);
							writeHead=false;
						}
						out.write(this.exemplars[i].getCSV().getBytes());
						out.write(nl);
					}
				}
				out.flush();

			} catch (IOException e) {
				LOG.error("Could not write lexicon to output stream", e);
			}
		}
	}



	// ===================================================================
	//                                                           ITERATORS
	// ===================================================================

	@Override
	public Iterator<Exemplar> iterator() {
		return new LexiconIterator(false);
	}

	public Iterator<Exemplar> iterator(boolean random) {
		return new LexiconIterator(random);
	}


	/**
	 * Get indices for the iterator.
	 * @param random
	 * @return
	 */
	private int[] getIteratorIndices(boolean random)
	{
		int[] indices = new int[this.size];
		if(random) {
			// The "inside-out" algorithm of Fisher-Yates shuffle
			for(int i=0; i<this.size; i++) {
				int j = this.conf.randomInt(i+1);
				if(i!=j){
					indices[i] = indices[j];
				}
				indices[j] = i;
			}
		} else {
			int ix = 0;
			for(int ex=start; ix<this.size; ex++, ix++)
			{
				if(ex == exemplars.length) {
					ex = 0;
				}
				indices[ix] = ex;
			}
		}
		return indices;
	}


	/**
	 * The iterator.
	 *
	 */
	private class LexiconIterator implements Iterator<Exemplar> {

		private final long expectedModA;
		private final long expectedModB;
		private int ptr;

		private int[] indices;

		public LexiconIterator(boolean random) {
			this.expectedModA = modCountA;
			this.expectedModB = modCountB;
			this.indices = getIteratorIndices(random);
			this.ptr = 0;
		}

		@Override
		public boolean hasNext() {
			if(this.expectedModA!=modCountA||this.expectedModB!=modCountB){
				throw new ConcurrentModificationException();
			}
			return this.ptr < this.indices.length;
		}

		@Override
		public Exemplar next() {
			if(this.expectedModA!=modCountA||this.expectedModB!=modCountB){
				throw new ConcurrentModificationException();
			}
			Exemplar w ;
			if(this.ptr < this.indices.length){
				w = exemplars[ this.indices[this.ptr]];
				this.ptr++;
			} else {
				throw new NoSuchElementException();
			}
			return w;
		}

		@Override
		public void remove() { }
	}


}
