package sfb732.kamoso.opt;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Optimizer {


	private static final Logger LOG = LogManager.getLogger(Optimizer.class.getCanonicalName());


	private final int maxIterations;
	private final double maxTemp;
	private final double coolingFactor;

	private final double jumpProb;

	private final Solution initial;

	private final Random rand;


	public Optimizer(int maxIterations, double maxTemp, double coolingFactor, double jumpProb,
			Solution initial) {
		super();
		this.maxIterations = maxIterations;
		this.maxTemp = maxTemp;
		this.coolingFactor = coolingFactor;
		this.jumpProb = jumpProb;
		this.initial = initial;
		this.rand = new Random();
	}

	public Solution optimize()
	{
		Solution current = this.initial;
		Solution best    = current;
		double cost      = Double.NaN;
		double temp      = this.maxTemp;

		for(int i=0; i<this.maxIterations; i++)
		{
			LOG.info(String.format("Start optimization iteration %d: Temperature=%.6f", i, temp));


			Solution working;
			if( this.rand.nextDouble() < this.jumpProb ){
				working = current.createRandom();
			} else {
				double noiseFactor = temp / this.maxTemp;
				working = current.createNeighbor(noiseFactor);
			}
			if(null==working) {
				LOG.info("aborting optimization: no new working solution found");
				break;
			}
			working.compute();
			cost = working.cost();

			LOG.info(String.format("*%d* current cost=%.6f", i, cost));

			if( working.cost() <= current.cost() )
			{
				current = working;
				if( current.cost() <= best.cost() ) {
					LOG.info("* found new optimum solution");
					best = current;
				} else {
					LOG.info("* taking suboptimal solution as new state");
				}
			} else if( Math.exp( (current.cost() - working.cost()) / temp  ) > this.rand.nextDouble() ) {
				LOG.info("* taking worse solution as new state");
				current = working;
			}
			temp = temp * this.coolingFactor;
		}

		return best;
	}

}
