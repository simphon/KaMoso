package sfb732.kamoso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.net.EpochStatistics;
import sfb732.kamoso.net.Network;
import sfb732.kamoso.pop.Agent;
import sfb732.kamoso.pop.AgentStatisticsRaw;
import sfb732.kamoso.pop.Interaction;



/**
 * Parochial variant competition.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 *
 */
public class ParochialSimulation {

	private static final Logger LOG = LogManager.getLogger(ParochialSimulation.class.getCanonicalName());


	/**
	 * Run Simulation
	 * @param outDir
	 * @param pop
	 * @param maxEpochs
	 */
	public static void runParochialSimulationMT(Configuration conf, File outDir, Network pop, int maxEpochs)
	{
		outDir.mkdirs();

		String pfx = conf.getOutputPrefix();

		int dumpAInterval = conf.getOutputAgentDumpInterval();
		int dumpLInterval = conf.getOutputLexiconDumpInterval();

		Configuration.save(conf, new File(outDir, String.format("%sconfig.prop", pfx)));

		EpochStatistics epochStats = null;
		Interaction interaction = null;

		try {
			epochStats = new EpochStatistics(conf, new File(outDir, String.format("%s%s", pfx, Configuration.FILE_SUFFIX_EPOCHS)));
			interaction = new Interaction(conf.getInteractionType(), pop);

			int wait    = 0;
			int maxWait = conf.getSimulationMaxWait();

			int epoch=0;
			int dumpA = dumpAInterval;
			int dumpL = dumpLInterval;
			boolean doDump = dumpAInterval > 0 || dumpLInterval > 0;

			if( conf.getOutputAgentDumpFirst() ){
				dumpAgentStats(conf, outDir, pfx, epoch, pop, conf.getOutputLexiconDumpFirst());
			} else if (conf.getOutputLexiconDumpFirst()) {
				dumpAgentStats(conf, outDir, pfx, epoch, pop, true);
			}

			boolean dumpLastA = conf.getOutputAgentDumpLast();
			boolean dumpLastL = conf.getOutputLexiconDumpLast();
			int lastDump = Integer.MAX_VALUE;
			if(dumpLastA || dumpLastL){
				lastDump = maxEpochs - 1;
			}

			for(; epoch < maxEpochs; epoch++, dumpA--, dumpL-- )
			{
				LOG.info(String.format("Start epoch %d ...", epoch));

				// collect all productions from speakers (MT) and
				// send all productions to listeners (MT)
				double aRatio = interaction.runInteractionsMT(epochStats, epoch);
				//double aRatio = interaction.runInteractions(epochStats, epoch);

				if( aRatio == 0.0 || aRatio == 1.0 ) {
					wait++;
					if(wait==1) {
						if(doDump){
							dumpAgentStats(conf, outDir, pfx, epoch, pop, true);
						}
					}
					else if(wait==maxWait){
						LOG.info(String.format("Aborting epoch %d: one variant disappeared", epoch));
						break;
					}
				} else {
					wait = 0;
				}

				if(dumpA==0) {
					if(dumpL==0){
						dumpAgentStats(conf, outDir, pfx, epoch, pop, true);
						dumpL = dumpLInterval;
					} else {
						dumpAgentStats(conf, outDir, pfx, epoch, pop, false);
					}
					dumpA = dumpAInterval;
				} else if(dumpL==0) {
					dumpAgentStats(conf, outDir, pfx, epoch, pop, true);
					dumpL = dumpLInterval;
				}

				if(epoch==lastDump) {
					if(dumpLastA) {
						dumpAgentStats(conf, outDir, pfx, epoch, pop, dumpLastL);
					} else if(dumpLastL) {
						dumpAgentStats(conf, outDir, pfx, epoch, pop, true);
					}
				}

				// increment ages
				pop.incrementEpoch();
			}

		} catch (IOException e) {
			LOG.fatal("Could not create I/O stream", e);
		} finally {
			// close open stream and clean up
			if(null!=epochStats) {
				epochStats.close();
			}
			if(null!=interaction) {
				interaction.shutdown();
			}
		}
	}


	/**
	 * Dump agent information to CSV file.
	 * @param outDir
	 * @param pfx
	 * @param epoch
	 * @param pop
	 * @throws IOException
	 */
	private static void dumpAgentStats(Configuration conf, File outDir, String pfx, int epoch, Network pop, boolean dumpLexicon) throws IOException
	{
		LOG.debug("Writing agent information to zipped CSV...");

		File lexZip = new File(outDir, String.format("%s%05d.zip", pfx, epoch));
		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(lexZip));
		AgentStatisticsRaw agentStats = new AgentStatisticsRaw(conf);
		Iterator<Agent> it = pop.iterator();
		while(it.hasNext()) {
			Agent a = it.next();
			agentStats.addRow(epoch, a);
			if(dumpLexicon) {
				ZipEntry e = new ZipEntry( String.format("%09d_lexicon.csv", a.getId()) );
				zipOut.putNextEntry(e);
				a.writeToStream(zipOut);
				zipOut.closeEntry();
			}
		}
		ZipEntry ae = new ZipEntry( "agents.csv" );
		zipOut.putNextEntry(ae);
		agentStats.writeToStream(zipOut);
		zipOut.closeEntry();
		zipOut.close();

	}

}
