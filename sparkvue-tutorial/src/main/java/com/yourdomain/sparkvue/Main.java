package com.yourdomain.sparkvue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import spark.Spark;
import org.slf4j.LoggerFactory;

import static spark.Spark.redirect;

public class Main {

    // We use Java's new Record class feature to create an immutable class
    public static record Person(
        String firstname, String lastname, 
        int age, String occupation, 
        String location, String relationship) {}

    public static void main(String... args) {
        final Gson gson = new Gson();

        final List<Person> people = fetchPeople();

        Spark.port(4567);
        // root is 'src/main/resources', so put files in 'src/main/resources/public'
        Spark.staticFiles.location("/public");
        
        addCORS();

        redirect.get("/", "/public/index.html");

        Spark.get("/people", (request, response) -> {
            response.type("application/json;charset=utf-8");
            return gson.toJson(people);
        });

        LoggerFactory.getLogger(Main.class).info("========== API RUNNING =================");
    }


    public static List<Person> fetchPeople() {
        List<Person> m = new ArrayList<>(); 

        m.add(new Person("Bob", "Banda", 13, "Future Accountant", "Blantyre", "Self"));
        m.add(new Person("John", "Banda", 68, "Accountant", "Blantyre", "Father"));
        m.add(new Person("Mary", "Banda", 8, "Accountant", "Blantyre", "Mother"));
        m.add(new Person("James", "Banda", 18, "Accountant", "Blantyre", "Brother"));
        m.add(new Person("Jane", "Banda", 8, "Student", "Blantyre", "Sister"));
        m.add(new Person("John", "Doe", 22, "Developer", "Lilongwe", "Cousin"));
        m.add(new Person("Brian", "Banda", 32, "Student", "Blantyre", "Best Friend"));
        m.add(new Person("Hannibal", "Kaya", 12, "Jerk", "Blantyre", "Arch Enemy"));

        return m;
    }

    private static final HashMap<String, String> corsHeaders = new HashMap<String, String>();

    static {
        corsHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
        corsHeaders.put("Access-Control-Allow-Origin", "*");
        corsHeaders.put("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,");
        corsHeaders.put("Access-Control-Allow-Credentials", "true");
    }
    public final static void addCORS() {
        Spark.after((request, response) -> {
            corsHeaders.forEach((key, value) -> {
                response.header(key, value);
            });
        });
    }
}