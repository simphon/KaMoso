package sfb732.kamoso.util;

import java.io.File;
import java.util.ArrayList;


public class StatsFileReader {




	/**
	 * Get a-ratio from CSV file.
	 * @param epochsFile
	 * @param maxEpochs
	 * @return
	 */
	public static double[] getARatio(File epochsFile, int maxEpochs) {

		ArrayList<String[]> lines = FileReaderCSV.readCSV(epochsFile);
		int sz = lines.size();

		double[] r = new double[maxEpochs];
		double lastR = Double.NaN;
		int x = 0;
		for(int i=1; i<sz; i++) {
			String[] row = lines.get(i);
			double prodA = Double.parseDouble(row[2]);
			double prodB = Double.parseDouble(row[3]);
			r[x] = prodA / (prodA+prodB);
			lastR = r[x];
			x++;
		}
		if(x < maxEpochs) {
			for(int i=x; i<maxEpochs; i++) {
				r[i] = lastR;
			}
		}
		return r;
	}

}
