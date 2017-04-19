package sfb732.kamoso.pop;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sfb732.kamoso.conf.Configuration;

/**
 * Helper class for collecting / writing output data.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public class AgentStatisticsRaw {


	private static final Logger LOG = LogManager.getLogger(AgentStatisticsRaw.class.getCanonicalName());

	private static final String SEP = ",";
	private static final String END = "\n";


	private final String timeStamp;
	private final String colsep;

	private ArrayList<byte[]> writer;

	/**
	 * Constructor.
	 * @param outFile
	 * @throws IOException
	 */
	public AgentStatisticsRaw (Configuration conf) throws IOException
	{
		this(conf,  SEP);
	}


	public AgentStatisticsRaw (Configuration conf, String sep) throws IOException
	{
		this.colsep = sep;
		this.writer = new ArrayList<byte[]>();
		this.timeStamp = conf.getTimestamp();

		// write header:
		this.writer.add(String.format("timestamp%sepoch%snodeID%sagentID%sage%sgender%sstatus"
				+ "%sratioA"
				+ "%sexemplars.received%sexemplars.discarded%sexemplars.produced"
				+ "%s",
				colsep, colsep, colsep, colsep, colsep, colsep, colsep, colsep, colsep, colsep, END).getBytes() );
	}


	public void addRow(int epoch, Agent a)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(this.timeStamp);
		sb.append(colsep);

		sb.append(String.valueOf(epoch));
		sb.append(colsep);

		sb.append(String.valueOf(a.getNodeId()));
		sb.append(colsep);

		sb.append(String.valueOf(a.getId()));
		sb.append(colsep);

		sb.append(String.valueOf(a.getAge()));
		sb.append(colsep);

		sb.append(a.getGender().toString());
		sb.append(colsep);

		sb.append(String.format(Configuration.DEFAULT_LOCALE, "%f", a.getStatus()));
		sb.append(colsep);

		sb.append(String.format(Configuration.DEFAULT_LOCALE, "%f", a.getVariantARatio()));
		sb.append(colsep);

		sb.append(String.format(Configuration.DEFAULT_LOCALE, "%d", a.getReceivedExemplars()));
		sb.append(colsep);

		sb.append(String.format(Configuration.DEFAULT_LOCALE, "%d", a.getDiscardedPercepts()));
		sb.append(colsep);

		sb.append(String.format(Configuration.DEFAULT_LOCALE, "%d", a.getProducedExemplars()));	

		sb.append(END);

		this.writer.add( sb.toString().getBytes() );
	}



	public void writeToStream(OutputStream out) {
		try {
			int sz=this.writer.size();
			for(int i=0; i<sz; i++) {
				out.write(this.writer.get(i));
			}
			out.flush();
		} catch (IOException e) {
			LOG.error("Could not write agent statistics to output stream", e);
		}
	}

}
