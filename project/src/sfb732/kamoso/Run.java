package sfb732.kamoso;

import java.io.File;
import java.io.PrintStream;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.mem.Exemplar;
import sfb732.kamoso.mem.Lexicon;
import sfb732.kamoso.net.Network;
import sfb732.kamoso.net.NetworkFactory;
import sfb732.kamoso.net.NetworkType;
import sfb732.kamoso.pop.Agent;
import sfb732.kamoso.pop.PopulationFactory;



/**
 * This is the main entry point for all KaMoso simulations.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public class Run {


	public static void main(String[] args)
	{
		File outDir        = null;
		File confFile      = null;

		int maxEpochs = 10;

		try{

			for(int ax=0; ax<args.length; ax++)
			{
				if(args[ax].equals(Configuration.ARG_HELP1) || args[ax].equals(Configuration.ARG_HELP2)){
					printMainHelpAndExit(System.out, 0);
				} else if (args[ax].equals(Configuration.ARG_FILE_CONF)) {
					confFile = new File(args[++ax]);
					continue;
				} else if (args[ax].equals(Configuration.ARG_FILE_OUT)) {
					outDir = new File(args[++ax]);
					continue;
				} else if (args[ax].equals(Configuration.ARG_RUN_EPOCHS)) {
					maxEpochs = Integer.parseInt(args[++ax]);
					continue;
				} else {
					System.err.printf("Unknown program argument: %s", args[ax]);
					printMainHelpAndExit(System.err, 1);
				}
			}

		} catch (Exception e) {
			System.err.println("Could not parse program arguments.");
			e.printStackTrace();
			printMainHelpAndExit(System.err, 1);
		}

		Configuration conf = Configuration.init(confFile, outDir);

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

		//TODO implement other simulations
		ParochialSimulation.runParochialSimulationMT(conf,outDir, net, maxEpochs);
	}



	/**
	 * Print help message and exit.
	 * @param p
	 * @param exitCode
	 */
	private static void printMainHelpAndExit(PrintStream p, int exitCode)
	{
		p.println("Run KaMoso simulation. Usage:");
		p.print(Run.class.getCanonicalName());
		p.println(" [OPTIONS]");

		p.println("Optional arguments:");
		p.printf("  %-9s [NUM]    -- Maximum number of epochs\n", Configuration.ARG_RUN_EPOCHS);
		p.println("                        Defaults to 1000 if not specified");

		p.printf("  %-9s          -- show this help messagen and exit\n", Configuration.ARG_HELP1);
		p.printf("  %-9s\n", Configuration.ARG_HELP2);

		//TODO finish
		System.exit(exitCode);
	}

}
