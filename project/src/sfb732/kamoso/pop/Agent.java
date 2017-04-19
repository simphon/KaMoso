package sfb732.kamoso.pop;

import java.io.File;
import java.io.OutputStream;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.mem.Exemplar;
import sfb732.kamoso.mem.Exemplar.Type;
import sfb732.kamoso.mem.Lexicon;


/**
 * Agent representing a member of the population.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 *
 */
public class Agent {

	// ===================================================================
	//                                                              GENDER
	// ===================================================================

	public enum Gender {
		m,
		f
	}

	// ===================================================================

	private final Configuration conf;

	private final long id;
	private final int nodeID;
	private final Gender gender;
	private final double status;
	private final boolean isStar;
	private final Lexicon lexicon;

	private int age;

	private int discardedPercepts = 0;
	private int receivedExemplars = 0;
	private int producedExemplars = 0;

	/**
	 * Constructor.
	 * @param conf
	 * @param id
	 * @param nodeID
	 * @param age
	 * @param gender
	 * @param status
	 * @param isStar
	 * @param lexicon
	 */
	protected Agent(Configuration conf, long id, int nodeID, int age, Gender gender, double status, boolean isStar, Exemplar[] lexicon) {
		this.conf    = conf;
		this.id      = id;
		this.nodeID  = nodeID;
		this.gender  = gender;
		this.status  = status;
		this.isStar  = isStar;
		this.lexicon = new Lexicon(conf, lexicon);
		this.age = age;
	}


	public int getAge() {
		return this.age;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Get the network node ID this agent is assigned to
	 * @return an integer &gt;=0
	 */
	public int getNodeId() {
		return this.nodeID;
	}

	public Gender getGender() {
		return this.gender;
	}

	/**
	 * @return the status
	 */
	public double getStatus() {
		return status;
	}

	/**
	 * @return the isStar
	 */
	public boolean isStar() {
		return isStar;
	}

	/**
	 * Increment the age of this Agent by one unit.
	 * @return the new age of the agent
	 */
	public int incrementAge(){
		return ++this.age;
	}

	// ===================================================================
	//                                                             LEXICON
	// ===================================================================


	public int getLexiconSize() {
		return this.lexicon.size();
	}

	public int getLexiconCapacity() {
		return this.lexicon.getCapacity();
	}


	public Exemplar.Type getMajorityType() {
		return this.lexicon.getMajorityType();
	}

	public double getVariantARatio() {
		return this.lexicon.getVariantARatio();
	}

	public void writeLexiconToCSV(File outFile){
		this.lexicon.writeCSV(outFile);
	}

	public void writeToStream(OutputStream out){
		this.lexicon.writeToStream(out);
	}


	/**
	 * Add a given exemplar to the lexicon of this agent:
	 * The stimulus exemplar is converted into a percept and then added to the
	 * exemplar collection of this agent.
	 * <p>
	 * Percepts with a similarity below the specified threshold will be
	 * discarded.
	 * @param stimulus -- an incoming exemplar
	 * @param socialCloseness -- the social closeness to the speaker
	 */
	public void listen(Exemplar stimulus, double socialCloseness)
	{
		Exemplar percept = this.lexicon.getPerceptPM(stimulus, socialCloseness);
		if(percept.getType()==Type.undefined){
			this.discardedPercepts++;
		} else {
			this.lexicon.addExemplar(percept);
		}
		this.receivedExemplars++;
	}


	public int getDiscardedPercepts() {
		return this.discardedPercepts;
	}

	public int getReceivedExemplars() {
		return this.receivedExemplars;
	}

	public int getProducedExemplars() {
		return this.producedExemplars;
	}

	public void resetDiscardedPercepts() {
		this.discardedPercepts = 0;
	}

	public void resetReceivedExemplars() {
		this.receivedExemplars = 0;
	}

	public void resetProducedExemplars() {
		this.producedExemplars = 0;
	}

	/**
	 * Get exemplars spoken by this agent.
	 * @return array of exemplars
	 */
	public Exemplar[] speak() {
		Exemplar[] utt = new Exemplar[this.conf.getUtteranceSize()];
		for(int i=0; i<utt.length; i++){
			Exemplar target = this.lexicon.getGoodExemplar();
			utt[i] = this.conf.getNoisyCopy(this, target);
			this.producedExemplars++;
		}
		return utt;
	}


	// ===================================================================

	@Override
	public String toString() {
		return "Agent [id=" + id + ", nodeID=" + nodeID + ", gender=" + gender
				+ ", status=" + status + ", isStar=" + isStar + ", lexicon="
				+ lexicon.size() + ", age=" + age + "]";
	}


}
