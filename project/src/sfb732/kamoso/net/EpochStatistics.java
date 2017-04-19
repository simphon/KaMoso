package sfb732.kamoso.net;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sfb732.kamoso.conf.Configuration;


/**
 * Writer for variant productions in each epoch
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public class EpochStatistics {

	private static final Logger LOG = LogManager.getLogger(EpochStatistics.class.getCanonicalName());

	private static final char SEP = ',';
	private static final char END = '\n';


	private final File outFile;
	private final OutputStreamWriter writer;
	private final char colsep;
	private final String timeStamp;


	/**
	 * Constructor.
	 * @param conf    -- the current configuration
	 * @param outFile -- the output file
	 * @throws IOException
	 */
	public EpochStatistics(Configuration conf, File outFile) throws IOException {
		this.outFile = outFile;
		this.colsep = SEP;
		this.writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(outFile)), Configuration.DEFAULT_ENCODING);
		this.timeStamp = conf.getTimestamp();

		// write header:
		this.writer.write(String.format("timestamp%sepoch%sproductionsA%sproductionsB%s", colsep, colsep, colsep, END));
		this.writer.flush();
	}


	public void addRow(int epoch, int productionsA, int productionsB)
	{
		try {
			this.writer.write(this.timeStamp);
			this.writer.write(colsep);
			this.writer.write(String.valueOf(epoch));
			this.writer.write(colsep);
			this.writer.write(String.valueOf(productionsA));
			this.writer.write(colsep);
			this.writer.write(String.valueOf(productionsB));
			this.writer.write(END);
			this.writer.flush();
		} catch (IOException e) {
			LOG.error("Could not write statistics to file", e);
		}
	}




	/**
	 * Flush and close the underlying file stream. After calling this method, no data can
	 * be written anymore using this writer instance.
	 */
	public void close() {
		try {
			this.writer.close();
			if(LOG.isDebugEnabled()){
				LOG.debug(String.format("writer closed for file %s", this.outFile.getAbsolutePath()));
			}
		} catch (IOException e) {
			LOG.error("could not close output file", e);
		}
	}


	/**
	 * @return the corresponding file
	 */
	public File getFile() {
		return this.outFile;
	}


}
