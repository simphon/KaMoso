package sfb732.kamoso.pop;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.mem.Exemplar;
import sfb732.kamoso.pop.Agent.Gender;
import sfb732.kamoso.util.MyMathHelper;


/**
 * Agent tools: a little helper class.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 *
 */
public class AgentTools {


	private final Configuration conf;

	private final double STATUS_MIN;
	private final double STATUS_MAX;
	private final double STATUS_HYP;
	private final double FEMALE_PROB;

	private long NEXT_ID;


	public AgentTools(Configuration conf) {
		this.conf = conf;
		STATUS_MIN  = conf.getStatusMin();
		STATUS_MAX  = conf.getStatusMax();
		STATUS_HYP  = conf.getStatusHyp();
		FEMALE_PROB = conf.getFemaleProbability();
		NEXT_ID = Long.MIN_VALUE;
	}



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
		Agent.Gender gender;
		if(MyMathHelper.randomDouble() < FEMALE_PROB) {
			gender = Gender.f;
		} else {
			gender = Gender.m;
		}
		double status;
		if(isStar) {
			status = STATUS_HYP;
		} else {
			status = MyMathHelper.randomDouble(STATUS_MIN, STATUS_MAX);
		}
		return new Agent(conf, ++NEXT_ID, nodeID, 0, gender, status, isStar, new Exemplar[lexCapacity]);
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
		if(id>=NEXT_ID){
			NEXT_ID = id+1;
		}
		return new Agent(conf, id, nodeID, age, gender, status, isStar, lexicon);
	}



}
