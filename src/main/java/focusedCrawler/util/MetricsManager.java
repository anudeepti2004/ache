package focusedCrawler.util;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class MetricsManager {
    
    private final MetricRegistry metrics;
    private ConsoleReporter reporter;
    
    public MetricsManager() {
        this(true);
    }
    
    public MetricsManager(boolean startConsoleReporter) {
        this(new MetricRegistry(), startConsoleReporter);
    }
    
    public MetricsManager(MetricRegistry metricsRegistry, boolean startConsoleReporter) {
        this.metrics = metricsRegistry;
        if(startConsoleReporter) {
            reporter = ConsoleReporter.forRegistry(metrics)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();
            reporter.start(10, TimeUnit.SECONDS);
        }
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
        if(reporter != null) {
            reporter.report();
            reporter.close();
        }
    }

    public MetricRegistry getMetricsRegistry() {
        return metrics;
    }

}
