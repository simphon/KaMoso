package sfb732.kamoso.conf;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import sfb732.kamoso.util.MyFileHelper;


/**
 * Generate a number of configuration files from a CSV table.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 *
 */
public class ConfigurationGenerator {


	public static void main(String[] args)
	{
		String outF=null;
		String inF=null;
		try{
			for(int ax=0; ax<args.length; ax++)
			{
				if(args[ax].equals(Configuration.ARG_HELP1) || args[ax].equals(Configuration.ARG_HELP2)){
					printMainHelpAndExit(System.out, 0);
				} else if(args[ax].equals(Configuration.ARG_FILE_OUT)) {
					outF= args[++ax];
				} else if(args[ax].equals(Configuration.ARG_FILE_CONF)) {
					inF= args[++ax];
				} else {
					System.err.println("Unknown program argument");
					printMainHelpAndExit(System.err, 0);
				}
			}
			if(null==outF || null== inF) {
				System.err.println("Missing required argument(s)");
				printMainHelpAndExit(System.err, 0);
			}

		} catch (Exception e) {
			System.err.println("Could not parse program arguments.");
			e.printStackTrace();
			printMainHelpAndExit(System.err, 1);
		}

		List<Configuration> confs = generateFromCSV(new File(inF), new File(outF), true);

		// write confs to file(s):
		for(int i=0; i<confs.size(); i++) {
			Configuration c = confs.get(i);
			File cf = new File(outF, String.format("%s%s_%s.csv", c.getOutputPrefix(), c.getTag(), c.getTimestamp()));
			Configuration.save(c, cf);
		}

	}


	/**
	 * Print help message and exit.
	 * @param p
	 * @param exitCode
	 */
	private static void printMainHelpAndExit(PrintStream p, int exitCode)
	{
		p.println("Usage:");
		p.print(ConfigurationGenerator.class.getCanonicalName());
		p.println(" [OPTIONS]");
		//TODO finish
	}



	/**
	 * The expected file format is a comma-separated values file (CSV) with the
	 * first column containing all properties keys as specified in
	 * {@link Configuration}. Any subsequent column is expected to contain the
	 * corresponding values for the keys in column 1 of that row. The first
	 * line in the file (the first row) should contain the tags for the
	 * different configurations.
	 * @param inFile
	 */
	public static List<Configuration> generateFromCSV(File inFile, File outDir, boolean verify)
	{
		String[] inLines = MyFileHelper.getLines(inFile, true, true);


		Pattern comma = Pattern.compile(",");

		String[] zeile =  comma.split(inLines[0]);

		if(! zeile[0].trim().equals(Configuration.KEY_TAG)) {
			throw new RuntimeException("Invalid input file: expected "+Configuration.KEY_TAG+" key-value(s) in first rows");
		}
		if(zeile.length <= 1) {
			throw new RuntimeException("Invalid input file: expected at least two columns");
		}

		int nConfs = zeile.length - 1;
		Properties[] props = new Properties[nConfs];
		for(int cx=0; cx<nConfs; cx++) {
			props[cx] = new Properties();
			props[cx].setProperty(Configuration.KEY_TAG, zeile[cx+1].trim());
			props[cx].setProperty(Configuration.KEY_PROP_FILE, 
					String.format("generateFromCSV(%s)", inFile.getAbsolutePath()));
		}

		for(int lx =1; lx < inLines.length; lx++) {
			zeile =  comma.split(inLines[lx], -1);
			String key = zeile[0].trim();
			for(int cx=0; cx<nConfs; cx++) {
				props[cx].setProperty(key, zeile[cx+1]);
			}
		}

		if(verify) {
			String[] expectedKeys = new String[] {
					Configuration.KEY_TAG,
					Configuration.KEY_PROP_FILE,// set by this method
					Configuration.KEY_OUT_PREFIX,
					Configuration.KEY_OUT_DUMP_AG_EVRY,
					Configuration.KEY_OUT_DUMP_AG_FIRST,
					Configuration.KEY_OUT_DUMP_AG_LAST,
					Configuration.KEY_OUT_DUMP_LEX_EVRY,
					Configuration.KEY_OUT_DUMP_LEX_FIRST,
					Configuration.KEY_OUT_DUMP_LEX_LAST,
					Configuration.KEY_NET_FILE,
					Configuration.KEY_NET_TYPE,
					Configuration.KEY_NET_COLS,
					Configuration.KEY_NET_ROWS,
					Configuration.KEY_NET_PARISHES,
					Configuration.KEY_NET_PROB,
					Configuration.KEY_NET_MAX_SW
					//TODO finish...
			};
			for(int cx=0; cx<nConfs; cx++) {
				Properties p = props[cx];

				for(int kx=0; kx<expectedKeys.length; kx++) {
					if(null==p.getProperty(expectedKeys[kx])){
						String msg = String.format("Missing key: %s in configuration %d", expectedKeys[kx], (kx+1));
						throw new RuntimeException(msg);
					}
				}
			}
		}

		ArrayList<Configuration> confs = new ArrayList<Configuration>();
		for(int cx=0; cx<nConfs; cx++) {
			Configuration conf = Configuration.init(props[cx], outDir);
			confs.add(conf);
		}
		return confs;

	}


}
