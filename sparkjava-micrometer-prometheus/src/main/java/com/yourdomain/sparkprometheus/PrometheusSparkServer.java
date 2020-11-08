package com.yourdomain.sparkprometheus;

import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * Server for exposing Prometheus metrics.
 * With additional functionality for turning the server on/off.
 */
public class PrometheusSparkServer {
    private final PrometheusMeterRegistry prometheusRegistry;
    private final spark.Service server;

    public PrometheusSparkServer(String host, int port, PrometheusMeterRegistry metricRegistry) {
        prometheusRegistry = metricRegistry;
        server = spark.Service.ignite();
        server.ipAddress(host);
        server.port(port);
    }

    public void start() {
        server.get("/metrics", (req, res) -> prometheusRegistry.scrape());
        server.awaitStop();
    }
}