package focusedCrawler.util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


public class MetricsManager {

	private final MetricRegistry metrics;
	private ConsoleReporter reporter;
	private String storageDirectory;
	private final ObjectMapper mapper = new ObjectMapper()
			.registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, false, MetricFilter.ALL));
	
	private static final Logger logger = LoggerFactory.getLogger(MetricsManager.class);

	public MetricsManager() {
		this(true, null);
	}
	
	public MetricsManager(String directoryPath) {
		this(true, directoryPath);
	}

	public MetricsManager(boolean startConsoleReporter) {
		this(startConsoleReporter, null);
	}
	
	public MetricsManager(boolean startConsoleReporter, String directoryPath) {
		this(new MetricRegistry(), startConsoleReporter, directoryPath);
	}
	

	public MetricsManager(MetricRegistry metricsRegistry, boolean startConsoleReporter, String directoryPath){
		if(directoryPath != null) {
			this.metrics = loadMetrics(metricsRegistry, directoryPath);
		}else {
			this.metrics = metricsRegistry;
		}
		this.storageDirectory = directoryPath;
		//this.metrics = metricsRegistry;
		if (startConsoleReporter) {
			reporter = ConsoleReporter.forRegistry(metrics).convertRatesTo(TimeUnit.SECONDS)
					.convertDurationsTo(TimeUnit.MILLISECONDS).build();
			reporter.start(10, TimeUnit.SECONDS);
		}

		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
		mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		
	}

	/**
	 * Saves the metrics to a file for it to be reloaded when the crawler restarts
	 * @param metricsRegistry
	 * @param directoryPath
	 * @throws IOException
	 */
	void saveMetrics(MetricRegistry metricsRegistry, String directoryPath) {
		String directoryName = directoryPath + "/metrics/";
		File file = new File(directoryName);
		if(file.exists()) {
			file.delete();
		}
		file.mkdir();
		try {
			mapper.writeValue(new File(directoryName + "metrics.json"), metricsRegistry);
		}catch(IOException e) {
			logger.error("Unable to save metrics to a file."+ e.getMessage());
		}
		
	}
	
	void saveMetrics(String directoryPath) {
		this.saveMetrics(metrics, directoryPath);		
	}
	
	void saveMetrics() {
		this.saveMetrics(metrics, storageDirectory);
	}
	
	/**
	 * Loads metrics from the file at the directry path
	 * @param metricsRegistry
	 * @param directoryPath
	 * @return
	 * @throws IOException
	 */
	MetricRegistry loadMetrics(MetricRegistry metricsRegistry, String directoryPath){
		File metricsFile = new File(directoryPath+"/metrics/metrics.json");
		if(metricsFile.exists()) {
			try {
				metricsRegistry = mapper.readValue(metricsFile,  MetricRegistry.class);
			}catch(IOException e) {
				logger.error("Unable to deserialize metrics registry: "+ e.getMessage());
			}
		}
		return metricsRegistry;
	}

	public Timer getTimer(String name) {
		return metrics.timer(name);
	}

	public Counter getCounter(String name) {
		return metrics.counter(name);
	}

	public void register(String name, Gauge<?> gauge) {
		metrics.register(name, gauge);
	}

	public void close() {
		if (reporter != null) {
			reporter.report();
			reporter.close();
			saveMetrics();
		}
	}

	public MetricRegistry getMetricsRegistry() {
		return metrics;
	}

}
