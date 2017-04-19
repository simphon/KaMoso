package sfb732.kamoso.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sfb732.kamoso.conf.Configuration;




/**
 * Helper class for file input.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart; SFB732(A2)
 *
 */
public class MyFileHelper {

	private static final Logger LOG = LogManager.getLogger(MyFileHelper.class.getCanonicalName());


	/**
	 * 
	 * @param inFile
	 * @param ignoreEmptyLines
	 * @param trim
	 * @return
	 */
	public static String[] getLines(
			File inFile,
			boolean ignoreEmptyLines,
			boolean trim)
	{

		List<String> lines = new ArrayList<String>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), Configuration.DEFAULT_ENCODING));
			String line = null;
			while( (line = br.readLine()) != null) {

				if(ignoreEmptyLines && line.trim().isEmpty()){
					continue;
				}
				if(trim){
					lines.add(line.trim());
				} else {
					lines.add(line);
				}
			}
		} catch (Exception e) {
			LOG.error("Could not read data from file", e);
			lines.clear();
		} finally {
			// make sure the stream is closed:
			if(null!=br){
				try {
					br.close();
				} catch (IOException e) {
					LOG.error("Could not close file input stream", e);
				}
			}
		}
		return lines.toArray(new String[lines.size()]);


	}

}
