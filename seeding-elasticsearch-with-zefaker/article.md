In this short article, we will see how you can use zefaker to generate 5 million records of random data which can easily be indexed in Elasticsearch using `esbulk`.

**Prerequisites**:

* [zefaker](https://github.com/creditdatamw/zefaker/releases/) (version 0.6 at the time of writing)
* [JDK 15+](https://www.azul.com/downloads/zulu-community/?architecture=x86-64-bit&package=jdk)
* [esbulk](https://github.com/miku/esbulk)
* [Elasticsearch / OpenSearch](https://www.elastic.co/downloads/elasticsearch)
* [curl](https://curl.se/download.html) (optional, but really you should have this already)


## zefaker script file

Firstly, we need to create a Groovy script to use with the zefaker to specify the form of our random data.
You can copy the code snippet below and place it in a file named `data.groovy` which we will pass to zefaker
to generate our data.

```groovy
// in data.groovy
import com.google.gson.JsonObject

firstName = column(index= 0, name= "firstName")
lastName  = column(index= 1, name= "lastName")
age       = column(index= 2, name= "age")

accountStatus = column(index=3, name="accountStatus")
accountMeta   = column(index=4, name="accountMeta")

generateFrom([
    (firstName): { faker -> faker.name().firstName() },
    (lastName): { faker -> faker.name().lastName() },
    (age): { faker -> faker.number().numberBetween(18, 70) },
    (accountStatus): { faker -> faker.options().option("Open", "Closed") },
    // You can nest objects like this
    (accountMeta): { faker -> 
        def meta = new JsonObject()
        meta.addProperty("totalTokens", faker.number().numberBetween(5000, 10000))
        meta.addProperty("activityStatus", faker.options().option("Active", "Dormant"))
        return meta
    }
])
```

## Generating the data 

zefaker requires Java to be installed to run. I'm assuming you have the `java` command in your PATH.
With that we can run the following to generate 5 million rows of random data exported into a JSON Lines format (basically a plain text file where each line is a JSON Object).

```sh
$ java -jar zefaker-all.jar -f data.groovy -jsonl -output elasticdata.jsonl -rows 5000000
```

## Indexing the data in Elasticsearch

Again, it is assumed that you have installed Elasticsearch and have it running. We will use esbulk, a nifty small command-line program written in Go, to perform the indexing.

```sh
$ esbulk -index "people-2021.07.07" -optype create -server http://localhost:9200 < elasticdata.jsonl
```

After esbulk completes (silently) you can check that the operation was successful by visiting http://localhost:9200/people-2021.07.07/_search in your browser or using curl like so:

```sh
$ curl -G http://localhost:9200/people-2021.07.07/_search
```

Date: 2021-07-08