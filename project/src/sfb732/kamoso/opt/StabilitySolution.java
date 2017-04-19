package sfb732.kamoso.opt;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sfb732.kamoso.ParochialSimulation;
import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.conf.ConfigurationSearch;
import sfb732.kamoso.mem.Exemplar;
import sfb732.kamoso.mem.Lexicon;
import sfb732.kamoso.net.Network;
import sfb732.kamoso.net.NetworkFactory;
import sfb732.kamoso.net.NetworkType;
import sfb732.kamoso.pop.Agent;
import sfb732.kamoso.pop.PopulationFactory;
import sfb732.kamoso.util.StatsFileReader;

public class StabilitySolution implements Solution {

	private static final Logger LOG = LogManager.getLogger(StabilitySolution.class.getCanonicalName());


	private final ConfigurationSearch cs;
	private final File outDir;
	private final int maxEpochs;

	private double cost;


	public StabilitySolution(ConfigurationSearch cs, File outDir, int maxEpochs) {
		this.cs = cs;
		this.outDir = outDir;
		this.maxEpochs = maxEpochs;
		this.cost = Double.POSITIVE_INFINITY;
	}

	@Override
	public void compute() {

		File[] epochFiles = new File[3];

		// run simulations
		LOG.info("Running Simulations with current configuration...");

		for(int i=0; i<3; i++){
			// run simulations
			Date date = new Date();
			String timeStamp  = new SimpleDateFormat("yyyy-MM-dd+HHmmss").format(date);
			File runOut = new File(this.outDir, String.format("%s%s/", this.cs.getOuputPrefix(), timeStamp));
			Configuration conf = this.cs.getCopy(runOut);
			Network net = this.getNet(conf);
			ParochialSimulation.runParochialSimulationMT(conf, runOut, net, maxEpochs);
			epochFiles[i] = new File(runOut, String.format("%s%s", conf.getOutputPrefix(), Configuration.FILE_SUFFIX_EPOCHS));
		}

		LOG.info("Computing cost for current configuration...");
		double[][] aratios = new double[][] {
				StatsFileReader.getARatio(epochFiles[0], maxEpochs),
				StatsFileReader.getARatio(epochFiles[1], maxEpochs),
				StatsFileReader.getARatio(epochFiles[2], maxEpochs),
		};

		double cost = 0.0;
		for(int i=0; i<maxEpochs; i++) {
			double m  = (aratios[0][i] + aratios[1][i] + aratios[2][i]) / 3.0;
			double sd = Math.sqrt(Math.pow((aratios[0][i] - m), 2.0) + Math.pow((aratios[1][i] - m), 2.0) + Math.pow((aratios[2][i] - m), 2.0) / 2.0);
			cost += sd;
		}
		this.cost = cost;
	}

	@Override
	public double cost() {
		return this.cost;
	}

	@Override
	public Solution createNeighbor(double noiseFactor) {
		StabilitySolution s = null;
		Configuration c = this.cs.nextNeighbor(noiseFactor, this.outDir);
		if(null!=c) {
			s = new StabilitySolution(new ConfigurationSearch(this.cs), this.outDir, this.maxEpochs);
		}
		return s;
	}

	@Override
	public Solution createRandom() {
		StabilitySolution s = null;
		Configuration c = this.cs.nextRandom(outDir);
		if(null!=c) {
			s = new StabilitySolution(new ConfigurationSearch(this.cs), this.outDir, this.maxEpochs);
		}
		return s;
	}




	public Configuration getCurrentConfiguration() {
		return this.cs.getCurrent();
	}



	private Network getNet(Configuration conf) {

		File prototypesCSV = conf.getExemplarPrototypeFile();
		if(null==prototypesCSV) {
			throw new RuntimeException("Missing exemplar prototype file");
		}
		Exemplar[] lex = Lexicon.readPrototypes(conf, prototypesCSV, 2);
		Exemplar prototypeA = lex[0];
		Exemplar prototypeB = lex[1];

		File agentsCSV = conf.getAgentsFile();
		Agent[] agents;
		if(null==agentsCSV) {
			throw new RuntimeException("Generation of agents not implemented");
		} else {
			agents = PopulationFactory.readCSV(conf,agentsCSV, prototypeA, prototypeB);
		}

		// initialize network:
		File edgesCSV = conf.getNetworkFile();
		Network net;
		if(null==edgesCSV) {
			NetworkType type = conf.getNetworkType();
			int ncols = conf.getNetworkColumns();
			int nrows = conf.getNetworkRows();
			int pars  = conf.getNetworkParishes();
			double p  = conf.getNetworkProbability();
			net = NetworkFactory.makeNetwork(conf,type, ncols, nrows, pars, p, agents);
		} else {
			// read network edges from file
			net = Network.readEdgelistCSV(conf, edgesCSV);
		}

		net.setAgents(agents);

		return net;
	}



}
