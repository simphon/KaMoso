package sfb732.kamoso.opt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Properties;

import sfb732.kamoso.Run;
import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.conf.ConfigurationSearch;

public class StabilityOptimizer {


	public static void main(String[] args) {

		File outDir        = null;
		File confFile      = null;
		File parFile       = null;

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
				} else if (args[ax].equals("-par")) {
					parFile = new File(args[++ax]);
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


		Properties params = new Properties();

		BufferedReader br=null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(parFile), Configuration.DEFAULT_ENCODING));
			params.load(br);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		} finally {
			if(null!=br) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				br = null;
			}
		}

		Configuration conf = Configuration.init(confFile, outDir);

		// read from parameter file:
		double alphaMin = Double.valueOf(params.getProperty("alphaMin"));
		double alphaMax = Double.valueOf(params.getProperty("alphaMax"));

		double betaMin  = Double.valueOf(params.getProperty("betaMin"));
		double betaMax  = Double.valueOf(params.getProperty("betaMax"));

		double gammaMin = Double.valueOf(params.getProperty("gammaMin"));
		double gammaMax = Double.valueOf(params.getProperty("gammaMax"));

		double noiseFacMin = Double.valueOf(params.getProperty("noiseFacMin"));
		double noiseFacMax = Double.valueOf(params.getProperty("noiseFacMax"));

		double noiseLimMin = Double.valueOf(params.getProperty("noiseLimMin"));
		double noiseLimMax = Double.valueOf(params.getProperty("noiseLimMax"));

		ConfigurationSearch cs = new ConfigurationSearch(conf, 
				alphaMin, alphaMax, betaMin, betaMax, gammaMin, gammaMax, noiseFacMin, noiseFacMax, noiseLimMin, noiseLimMax);

		Solution initial = new StabilitySolution(cs, outDir, maxEpochs);

		int maxIterations    = Integer.valueOf(params.getProperty("maxIterations"));
		double maxTemp       = Double.valueOf(params.getProperty("maxTemp"));
		double coolingFactor = Double.valueOf(params.getProperty("coolingFactor"));
		double jumpProb      = Double.valueOf(params.getProperty("jumpProb"));

		Optimizer opt = new Optimizer(maxIterations, maxTemp, coolingFactor, jumpProb, initial);

		StabilitySolution best = (StabilitySolution) opt.optimize();

		Configuration bc = best.getCurrentConfiguration();

		File bestFile = new File(outDir, String.format("%sbest.prop", conf.getOutputPrefix()));
		Configuration.save(bc, bestFile);

		System.out.printf("Optimization done. Best config written to %s\n", bestFile.getAbsolutePath());
	}




	/**
	 * Print help message and exit.
	 * @param p
	 * @param exitCode
	 */
	private static void printMainHelpAndExit(PrintStream p, int exitCode)
	{
		p.println("Run KaMoso simulation parameter optimization. Usage:");
		p.print(Run.class.getCanonicalName());
		p.println(" [OPTIONS]");


		System.exit(exitCode);
	}

}
