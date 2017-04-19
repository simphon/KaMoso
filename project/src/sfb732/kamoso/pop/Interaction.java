package sfb732.kamoso.pop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sfb732.kamoso.mem.Exemplar;
import sfb732.kamoso.net.EpochStatistics;
import sfb732.kamoso.net.Network;

/**
 * 
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public class Interaction {


	private static final Logger LOG = LogManager.getLogger(Interaction.class.getCanonicalName());



	// ===================================================================
	//                                                   INTERACTION TYPES
	// ===================================================================

	/**
	 * Types of pre-defined interactions between individual agents. 
	 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
	 */
	public enum Type {
		/** full pairwise interaction */
		regular,// default or baseline case

		/** interaction based on status of speaker */
		byStatus,

		/** interaction based on status of speaker and distance */
		byStatusAndDistance,

		/** interaction based on distance */
		byDistance
	}



	// ===================================================================
	//                                                          UTTERANCES
	// ===================================================================

	private class Productions {
		public final Agent listener;
		public final ArrayList<Exemplar> exemplars;
		public final ArrayList<Agent> speakers;

		public Productions(Agent listener) {
			this.listener  = listener;
			this.exemplars = new ArrayList<Exemplar>();
			this.speakers  = new ArrayList<Agent>();
		}

		public void addAll(Agent speaker, Exemplar[] utt) {
			for(int i=0; i<utt.length; i++) {
				this.exemplars.add(utt[i]);
				this.speakers.add(speaker);
			}
		}
	}


	// ===================================================================
	// CALLABLES
	// ===================================================================

	private class InputCollector implements Callable<Productions>
	{
		private final Agent listener;

		public InputCollector(Agent listener) {
			this.listener = listener;
		}

		@Override
		public Productions call() throws Exception
		{
			Productions prods= new Productions(listener);
			Iterator<Agent> it = pop.getSpeakerIterator(prods.listener, type);
			while(it.hasNext()){
				Agent speaker = it.next();
				Exemplar[] utt = speaker.speak();
				prods.addAll(speaker, utt);
			}
			return prods;
		}
	}



	private class PerceptionHandler implements Callable<ProductionStats>
	{
		private final Productions prod;

		public PerceptionHandler(Productions p) {
			this.prod = p;
		}

		@Override
		public ProductionStats call() throws Exception
		{
			ProductionStats ps = new ProductionStats();
			Agent listener = this.prod.listener;
			int sz = this.prod.exemplars.size();
			for(int i=0; i<sz; i++) {
				Agent speaker = this.prod.speakers.get(i);
				Exemplar ex   = this.prod.exemplars.get(i);
				double sc = pop.getSocialCloseness(listener, speaker);
				listener.listen(ex, sc);
				if(ex.getType()==Exemplar.Type.A) {
					ps.productionsA++;
				} else {
					ps.productionsB++;
				}
			}
			return ps;
		}
	}


	/**
	 * Container for bookkeeping.
	 */
	private class ProductionStats {
		public int productionsA;
		public int productionsB;

		public double getARatio() {
			return (double)this.productionsA / (double)(this.productionsA + this.productionsB);
		}
	}

	// ===================================================================
	//
	// ===================================================================


	private final ExecutorService executor ;
	private final Network pop;
	private final Type type;

	private int lastFinishedEpoch = -1;

	public Interaction(Interaction.Type type, Network pop)
	{
		this.type = type;
		this.pop = pop;
		this.executor = Executors.newCachedThreadPool();
	}


	/**
	 * 
	 * @return
	 */
	public double runInteractionsMT(EpochStatistics epochStats, int epoch)
	{
		// make sure this is called only once per epoch
		if(this.lastFinishedEpoch == epoch){
			throw new RuntimeException("this method must not be called twice per epoch");
		}

		// collect all productions from speakers:
		ArrayList<Future<Productions>> futuresP = new ArrayList<Future<Productions>>();

		Iterator<Agent> it = this.pop.iterator();
		while(it.hasNext()) {
			Agent listener = it.next();
			InputCollector ic = new InputCollector(listener);
			futuresP.add( this.executor.submit(ic) );
		}

		int sz = futuresP.size();
		ArrayList<Productions> allProductions = new ArrayList<Productions>(sz);

		for(int i=0; i<sz; i++) {
			Future<Productions> f = futuresP.get(i);
			try {
				Productions p = f.get();
				allProductions.add(p);
			} catch (InterruptedException e) {
				LOG.error("Unexpected exception", e);
			} catch (ExecutionException e) {
				LOG.error("Unexpected exception", e);
			}
		}
		futuresP.clear();

		// distribute all productions to listeners:
		ArrayList<Future<ProductionStats>> futuresS = new ArrayList<Future<ProductionStats>>();

		for(int i=0; i<sz; i++) {
			Productions prod = allProductions.get(i);
			PerceptionHandler ph = new PerceptionHandler(prod);
			Future<ProductionStats> f = this.executor.submit(ph);
			futuresS.add(f);
		}

		ProductionStats statsGlobal = new ProductionStats();
		for(int i=0; i<sz; i++) {
			Future<ProductionStats> f = futuresS.get(i);
			try {
				ProductionStats ps = f.get();
				statsGlobal.productionsA += ps.productionsA;
				statsGlobal.productionsB += ps.productionsB;
			} catch (InterruptedException e) {
				LOG.error("Unexpected exception", e);
			} catch (ExecutionException e) {
				LOG.error("Unexpected exception", e);
			}
		}

		// store statistics
		epochStats.addRow(epoch, statsGlobal.productionsA, statsGlobal.productionsB);

		this.lastFinishedEpoch = epoch;

		return statsGlobal.getARatio();
	}





	/**
	 * Single-threadded version
	 * @return
	 */
	public double runInteractions(EpochStatistics epochStats, int epoch)
	{
		// make sure this is called only once per epoch
		if(this.lastFinishedEpoch == epoch){
			throw new RuntimeException("this method must not be called twice per epoch");
		}

		// collect all productions from speakers:
		ArrayList<InputCollector> presentsP = new ArrayList<Interaction.InputCollector>();

		Iterator<Agent> it = this.pop.iterator();
		while(it.hasNext()) {
			Agent listener = it.next();
			InputCollector ic = new InputCollector(listener);
			presentsP.add(ic);
		}

		int sz = presentsP.size();
		ArrayList<Productions> allProductions = new ArrayList<Productions>(sz);

		for(int i=0; i<sz; i++) {
			InputCollector ic = presentsP.get(i);
			try {
				Productions p = ic.call();
				allProductions.add(p);
			} catch (Exception e) {
				LOG.error("Unexpected exception", e);
			}
		}

		// distribute all productions to listeners:
		ArrayList<PerceptionHandler> presentsPH = new ArrayList<Interaction.PerceptionHandler>();

		for(int i=0; i<sz; i++) {
			Productions prod = allProductions.get(i);
			PerceptionHandler ph = new PerceptionHandler(prod);
			presentsPH.add(ph);
		}

		ProductionStats statsGlobal = new ProductionStats();
		for(int i=0; i<sz; i++) {
			PerceptionHandler ph = presentsPH.get(i);
			try {
				ProductionStats ps = ph.call();
				statsGlobal.productionsA += ps.productionsA;
				statsGlobal.productionsB += ps.productionsB;
			} catch (Exception e) {
				LOG.error("Unexpected exception", e);
			}
		}

		// store statistics
		epochStats.addRow(epoch, statsGlobal.productionsA, statsGlobal.productionsB);

		this.lastFinishedEpoch = epoch;

		return statsGlobal.getARatio();
	}





	public void shutdown() {
		this.executor.shutdown();
	}

}
