package sfb732.kamoso.conf;

import java.io.File;
import java.io.PrintStream;
import java.util.Properties;

import sfb732.kamoso.pop.Interaction;


/**
 * Helper for default configuration
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public class ConfigurationDefault {


	private static final String DEFAULT_NAME = "default.prop";

	public static void main(String[] args)
	{
		String f = DEFAULT_NAME;
		try{

			for(int ax=0; ax<args.length; ax++)
			{
				if(args[ax].equals(Configuration.ARG_HELP1) || args[ax].equals(Configuration.ARG_HELP2)){
					printMainHelpAndExit(System.out, 0);
				} else if(args[ax].equals(Configuration.ARG_FILE_OUT)) {
					f= args[++ax];
				} else {
					System.err.println("Unknown program argument");
					printMainHelpAndExit(System.err, 0);
				}
			}
		} catch (Exception e) {
			System.err.println("Could not parse program arguments.");
			e.printStackTrace();
			printMainHelpAndExit(System.err, 1);
		}
		Configuration conf = Configuration.init();
		File cf = new File(f);
		Configuration.save(conf, cf);
	}


	/**
	 * Print help message and exit.
	 * @param p
	 * @param exitCode
	 */
	private static void printMainHelpAndExit(PrintStream p, int exitCode)
	{
		p.println("Usage:");
		p.print(ConfigurationDefault.class.getCanonicalName());
		p.println(" [OPTIONS]");
		p.printf("  %-6s [FILE]   -- output file name for default properties\n", Configuration.ARG_FILE_OUT);
		p.printf("                     Optional: if omitted, \"%s\" will be written to current directory.\n", DEFAULT_NAME);
		p.println("                     Existing files with the same name will be overwritten!");
		p.printf("  %-6s          -- show this help messagen and exit\n", Configuration.ARG_HELP1);
		p.printf("  %-6s\n", Configuration.ARG_HELP2);
		//TODO finish
		System.exit(exitCode);
	}


	/**
	 * Get default configuration
	 * @return a new {@link Properties} instance
	 */
	public static Properties getDefaultConfiguration()
	{
		Properties p = new Properties();

		p.setProperty(Configuration.KEY_PROP_FILE, "<DEFAULT_CONFIGURATION>");

		p.setProperty(Configuration.KEY_OUT_DUMP_AG_EVRY, "-1");
		p.setProperty(Configuration.KEY_OUT_DUMP_AG_FIRST, "false");
		p.setProperty(Configuration.KEY_OUT_DUMP_AG_LAST, "false");

		p.setProperty(Configuration.KEY_OUT_DUMP_LEX_EVRY, "-1");
		p.setProperty(Configuration.KEY_OUT_DUMP_LEX_FIRST, "false");
		p.setProperty(Configuration.KEY_OUT_DUMP_LEX_LAST, "false");

		p.setProperty(Configuration.KEY_SIM_MAX_WAIT, "15");

		p.setProperty(Configuration.KEY_NET_FILE, "");// no default
		p.setProperty(Configuration.KEY_NET_MAX_SW, "3");

		p.setProperty(Configuration.KEY_EX_PERCEPTION, "magnet");
		p.setProperty(Configuration.KEY_EX_SIM, "global");
		p.setProperty(Configuration.KEY_EX_SIM_EPS, "0.0");//not relevant for global similarity

		p.setProperty(Configuration.KEY_EX_ALPHA, "1.0");
		p.setProperty(Configuration.KEY_EX_BETA, "1.0");
		p.setProperty(Configuration.KEY_EX_GAMMA, "1.0");

		p.setProperty(Configuration.KEY_EX_DELTA_THRESHOLD, "13.0");
		p.setProperty(Configuration.KEY_EX_MIN_SIM, "0.001");

		p.setProperty(Configuration.KEY_EX_NOISE_FACTOR, "5.0");
		p.setProperty(Configuration.KEY_EX_NOISE_MAX, "10.0");
		p.setProperty(Configuration.KEY_EX_PHON_DIM, "5");
		p.setProperty(Configuration.KEY_EX_UTTERANCE, "5");
		p.setProperty(Configuration.KEY_SOC_INTERACTION, Interaction.Type.regular.toString());
		p.setProperty(Configuration.KEY_SOC_TEACHERS, "40");
		p.setProperty(Configuration.KEY_SOC_MAX_LIFE, "5");
		p.setProperty(Configuration.KEY_SOC_FEMALE_PROB, "0.5");
		p.setProperty(Configuration.KEY_SOC_STATUS_MIN, "0.01");
		p.setProperty(Configuration.KEY_SOC_STATUS_MAX, "0.25");
		p.setProperty(Configuration.KEY_SOC_STATUS_HYP, "1.0");
		return p;
	}


}
