package sfb732.kamoso.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * Helper class to read CSV file.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 *
 */
public class FileReaderCSV {


	private static final Logger LOG = LogManager.getLogger(FileReaderCSV.class.getCanonicalName());



	/**
	 * Read contents of CSV file
	 * @param csvFile
	 * @return
	 */
	public static ArrayList<String[]> readCSV(File csvFile)
	{
		LOG.debug(String.format("Reading file: %s", csvFile.getAbsolutePath()));

		ArrayList<String[]> lines = new ArrayList<String[]>();

		Pattern pattern = Pattern.compile(",");

		BufferedReader in = null;

		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile)));
			String line = null;
			while( (line = in.readLine()) != null)
			{
				line = line.trim();
				if(line.isEmpty()){
					continue;
				}
				String[] fields = pattern.split(line);
				lines.add(fields);
			}

		} catch (FileNotFoundException e) {
			LOG.error("Could not read file", e.getCause());
		} catch (IOException e) {
			LOG.error("Could not read file", e.getCause());
		} finally {
			if(null!=in){
				try {
					in.close();
				} catch (IOException e) {
					LOG.error("Could not close input file stream", e.getCause());
				}
			}
		}

		LOG.debug(String.format("Found %d lines in file: %s", lines.size(), csvFile.getAbsolutePath()));

		return lines;
	}


}
