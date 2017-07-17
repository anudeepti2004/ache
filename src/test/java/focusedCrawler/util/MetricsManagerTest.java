package focusedCrawler.util;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


import static org.junit.Assert.assertTrue;

import com.codahale.metrics.Counter;

public class MetricsManagerTest {
	MetricsManager metricsManager;
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Before
	public void setUp() throws IOException {
//		metricsManager = new MetricsManager("/tmp/testing");
		metricsManager = new MetricsManager(tempFolder.newFolder().getAbsolutePath());
	}
	
	@Test
	public void testSavingOfMetrics() throws IOException {
		Counter counter = metricsManager.getCounter("sample_counter");
		counter.inc();
		
		String directoryPath = tempFolder.newFolder().getAbsolutePath(); //"/tmp/testing"; 
		metricsManager.saveMetrics(directoryPath);
		assertTrue(new File(directoryPath+"/metrics/metrics.json").exists());
	}
	
	
	@Test
	public void testLoadingOfMetrics() throws IOException {
		Counter counter = metricsManager.getCounter("sample_counter");
		counter.inc();
		counter.inc();
		
		String directoryPath = tempFolder.newFolder().getAbsolutePath(); //"/tmp/testing"; 
		metricsManager.saveMetrics(directoryPath);
		
		
		MetricsManager metricsManager2 = new MetricsManager(directoryPath);
		Counter testCounter = metricsManager2.getCounter("sample_counter");
		assertTrue(testCounter.getCount() == 2);
		
	}

}
