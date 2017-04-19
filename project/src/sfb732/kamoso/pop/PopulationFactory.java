package sfb732.kamoso.pop;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.mem.Exemplar;
import sfb732.kamoso.mem.Lexicon;
import sfb732.kamoso.util.MyFileHelper;


/**
 * Helper class to create agent populations.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 *
 */
public class PopulationFactory {


	private static final Logger LOG = LogManager.getLogger(PopulationFactory.class.getCanonicalName());


	// ===================================================================
	//                                                    CSV FILE COLUMNS
	// ===================================================================

	public static final int CSV_NUM_COLS = 10;

	public static final int CSV_COL_NODE_ID   = 0;
	public static final int CSV_COL_AGENT_ID  = 1;
	public static final int CSV_COL_STATUS    = 2;
	public static final int CSV_COL_STAR      = 3;
	public static final int CSV_COL_AGE       = 4;
	public static final int CSV_COL_LEX_LIMIT = 5;
	public static final int CSV_COL_LEX_SIZE  = 6;
	public static final int CSV_COL_LEX_FILE  = 7;
	public static final int CSV_COL_RATIO_A   = 8;
	public static final int CSV_COL_GENDER    = 9;

	public static final String CSV_HEAD_NODE_ID   = "node_id";
	public static final String CSV_HEAD_AGENT_ID  = "agent_id";
	public static final String CSV_HEAD_STATUS    = "status";
	public static final String CSV_HEAD_STAR      = "isStar";
	public static final String CSV_HEAD_AGE       = "age";
	public static final String CSV_HEAD_LEX_LIMIT = "lex_limit";
	public static final String CSV_HEAD_LEX_SIZE  = "lex_size";
	public static final String CSV_HEAD_LEX_FILE  = "lex_file";
	public static final String CSV_HEAD_RATIO_A   = "ratio_A";
	public static final String CSV_HEAD_GENDER    = "gender";



	// ===================================================================
	//
	// ===================================================================


	public static void main(String[] args)
	{

		try{

			for(int ax=0; ax<args.length; ax++)
			{
				if(args[ax].equals(Configuration.ARG_HELP1) || args[ax].equals(Configuration.ARG_HELP2)){
					printMainHelpAndExit(System.out, 0);
				} else {
					System.err.println("Unknown program argument");
					printMainHelpAndExit(System.err, 1);
				}
			}

		} catch (Exception e) {
			System.err.println("Could not parse program arguments.");
			e.printStackTrace();
			printMainHelpAndExit(System.err, 1);
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
		p.print(PopulationFactory.class.getCanonicalName());
		p.println(" [OPTIONS]");

		p.printf("  %-6s          -- show this help messagen and exit\n", Configuration.ARG_HELP1);
		p.printf("  %-6s\n", Configuration.ARG_HELP2);

		//TODO finish
		System.exit(exitCode);
	}





	/**
	 * Read agent specifications from CSV file.
	 * @param agentFile
	 * @param prototypeA
	 * @param prototypeB
	 * @return
	 */
	public static Agent[] readCSV(Configuration conf, File agentFile, Exemplar prototypeA, Exemplar prototypeB)
	{
		LOG.info(String.format("Reading agents from CSV-file: %s", agentFile.getAbsolutePath()));

		ArrayList<Agent> agentList = new ArrayList<Agent>();

		// read agent data from file
		String[] lines = MyFileHelper.getLines(agentFile, true, true);

		// parse contents of agent data input file
		Pattern komma = Pattern.compile(",");

		// start with second line, since first line contains the column headers
		for(int i=1; i<lines.length; i++)
		{
			String[] fields =  komma.split(lines[i]);
			if(fields.length!=CSV_NUM_COLS) {
				String msg = String.format("Invalid CSV file. Expecting %d columns, found %d", CSV_NUM_COLS, fields.length);
				LOG.error(msg);
				throw new RuntimeException(msg);
			}

			int nodeID;
			int agentID;
			double status;
			boolean isStar;
			int age;
			int lexLimit;
			int lexSize;
			String lexFile;
			double ratioA;
			Agent.Gender gender;

			try {

				nodeID   = Integer.parseInt(fields[CSV_COL_NODE_ID]);
				agentID  = Integer.parseInt(fields[CSV_COL_AGENT_ID]);
				status   = Double.parseDouble(fields[CSV_COL_STATUS]);
				isStar   = Boolean.parseBoolean(fields[CSV_COL_STAR]);
				age      = Integer.parseInt(fields[CSV_COL_AGE]);
				lexLimit = Integer.parseInt(fields[CSV_COL_LEX_LIMIT]);
				lexSize  = Integer.parseInt(fields[CSV_COL_LEX_SIZE]);
				lexFile  = fields[CSV_COL_LEX_FILE];
				ratioA   = Double.parseDouble(fields[CSV_COL_RATIO_A]);
				gender   = Agent.Gender.valueOf(fields[CSV_COL_GENDER]);

				//TODO check if all required fields are non-empty

			} catch (Exception e) {
				String msg = "Could not parse CSV data";
				LOG.error(msg, e);
				throw new RuntimeException(msg, e);
			}


			Exemplar[] lex;
			// check if lexicon should be read from file
			if( lexFile.isEmpty() ) {
				// generate lexicon contents
				LOG.trace("Generating lexicon contents...");
				lex = Lexicon.generateLexicon(conf, lexLimit, lexSize, ratioA, prototypeA, prototypeB);
			} else {
				//TODO read lexicon contents from file
				lex=null;
				throw new RuntimeException("call to unimplemented code block");
			}

			Agent a = conf.initAgent(agentID, nodeID, age, gender, status, isStar, lex);
			LOG.debug(String.format("+ Agent %03d: %s", i, a.toString()));
			agentList.add(a);

		}

		return agentList.toArray(new Agent[agentList.size()]);
	}




}
