package sfb732.kamoso.junit;


import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.conf.ConfigurationDefault;


/**
 * JUnit test class
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 *
 */
public class TestConfigurationDefault {


	private static final Logger LOG = LogManager.getLogger(TestConfigurationDefault.class.getCanonicalName());

	@Before
	public final void init()
	{
		System.out.println("**** TestConfigurationDefault.init ****");

		File outDir = new File(Configuration.DEFAULT_OUPUT_DIR);
		if(! outDir.exists()) {
			LOG.info(String.format("Creating output directory: %s", outDir.getAbsolutePath()));
			outDir.mkdir();
		}
	}


	@Test
	public final void test_getDefaultConfiguration()
	{
		System.out.println("**** TestConfigurationDefault.test_getDefaultConfiguration ****");

		Properties p = null;
		p = ConfigurationDefault.getDefaultConfiguration();
		assertNotNull(p);
		assertTrue(p.size() > 0);

		Set<String> keys = null;
		keys = p.stringPropertyNames();
		assertNotNull(keys);
		assertFalse(keys.isEmpty());

		// check if properties are defined
		//XXX Attention: this will fail when the behavior of
		// sfb732.kamoso.conf.ConfigurationDefault.getDefaultConfiguration()
		// is changed!
		assertNotNull(p.getProperty("random.seed"));
		assertNotNull(p.getProperty("net.sw.maxtry"));
		assertNotNull(p.getProperty("x.delta.th"));
		assertNotNull(p.getProperty("x.th.activation"));
		assertNotNull(p.getProperty("x.noise.factor"));
		assertNotNull(p.getProperty("x.phon.dim"));
	}


	@Test
	public final void test_main()
	{
		System.out.println("**** TestConfigurationDefault.test_main ****");

		String outFileName = "test.properties_DELETE_ME";
		File outFile = new File(Configuration.DEFAULT_OUPUT_DIR, outFileName);

		assertFalse("outputfile already exists", outFile.exists());
		outFile.deleteOnExit();

		String[] args = new String[] {
				Configuration.ARG_FILE_OUT, outFile.getAbsolutePath()
		};

		ConfigurationDefault.main(args);

		assertTrue(outFile.isFile());

		// check values!

		Properties dp = ConfigurationDefault.getDefaultConfiguration();
		assertTrue(dp.size() > 0);

		// read from prop file
		Properties pf = new Properties();

		BufferedReader br=null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(outFile), Configuration.DEFAULT_ENCODING));
			pf.load(br);

		} catch (Exception e) {
			fail("Could not read configuration from exported file");
		} finally {
			if(null!=br) {
				try {
					br.close();
				} catch (IOException e) {
					LOG.error("Could not close file stream", e);
				}
				br = null;
			}
		}

		assertTrue(pf.size() > 0);
	}

}
