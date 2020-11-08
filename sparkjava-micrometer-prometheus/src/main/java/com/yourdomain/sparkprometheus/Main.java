package com.yourdomain.sparkprometheus;

import com.creditdatamw.zerocell.Reader;
import com.google.gson.Gson;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import spark.Spark;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Main {
    private static final Map<String, List<Person>> db = new ConcurrentHashMap<>();

    public static void main(String... args) {
        final PrometheusMeterRegistry registry=  new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        final Gson gson = new Gson();
        final int port = 4567;
        Spark.port(port);

        Spark.post("/upload/people", (request, response) -> {
            request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
            Part filePart = request.raw().getPart("file"); //file is name of the upload form
            if (filePart == null)
                return Spark.halt(400);
            if (filePart.getInputStream() == null)
                return Spark.halt(400);
            registry.counter("file.uploads", "endpoint", "/upload/people").increment();
            Path tmpPath = Files.createTempFile("z","xlsx");
            Files.copy(filePart.getInputStream(), tmpPath, StandardCopyOption.REPLACE_EXISTING);

            List<Person> people = Reader.of(Person.class)
                .from(tmpPath.toFile())
                .list();

            registry.counter("people.total").increment(people.size());

            registry.counter("people.fetch", "endpoint", "/upload/people").increment();

            db.put(filePart.getSubmittedFileName(), people);
            response.type("application/json;charset=utf-8");
            return gson.toJson(people);
        });

        Spark.get("/people/all", (request, response) -> {
            registry.counter("people.fetch", "endpoint", "/people/all").increment();
            List<Person> allPeople = db.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

            response.type("application/json;charset=utf-8");
            return gson.toJson(allPeople);
        });

        new PrometheusSparkServer("localhost", 4550, registry).start();
    }
}
