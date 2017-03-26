Bourne.js
===========
Bourne is a distributed JSON-centric processing server, and is optimized for high-speed data/file ingestion.

## Motivation

Systems like [Apache Storm](http://storm.apache.org) or [Spark Streaming](http://spark.apache.org) are powerful and flexible distributed processing engines,
which are usually fed by a message-oriented middleware solution (e.g. [Apache Kafka](http://kafka.apache.org) or [Twitter Kestrel](https://github.com/twitter/kestrel)). 

The challenge that I've identified, is that organizations usually have to build a homegrown solution for the high-speed 
data/file ingestion into Kafka or Kestrel, which distracts them from their core focus. I've built Bourne to help provide 
a solution to that challenge.

## About Bourne

As mentioned above, Bourne is a distributed actor-based processing server, and is optimized for high-speed data/file
ingestion. As such, Bourne is meant to be a complement to systems like Storm, and not necessarily an alternative.

Why the name Bourne? I chose the name Bourne (e.g. Bourne plays or musicals) because it's an actor-based system.
As such you'll encounter terms such as anthology, director, narrative and producer once Bourne's documentation is complete.

## Features

Bourne provides three main functions:

* *Transporting of Files* via a built-in orchestration server, which also has the capability to download files and/or move files from one location (site) to another.
* *Extract, Transform and Loading* and is tailored toward processing flat files (XML, JSON, CSV, delimited, fixed field-length, and hierarchical)
* *File archival system*, which provides the capability for warehousing processed files.

Additionally, since Bourne is a file-centric processing system, it supports features like:
* File-processing dependencies (e.g. File "A" must be processed before Files "B" and "C" can be processed)
* File-processing schedulers and triggers
  * Directories can be watched for specific file names (or match file names via regular expressions) which can then be processed and archived.
  * Files can be limited to being processed at certain times or days of the week.
* An Actor-based I/O system with builtin support for:
  * Binary files
  * Text files (XML, JSON, CSV, delimited, fixed field-length, and hierarchical)
  * Kafka (including Avro)
  * MongoDB
* File archival and retention strategies
* Resource limits (e.g. limit the number of Kafka connections)

Bourne is currently pre-alpha quality software, and although it will currently run simple topologies (anthologies), 
there's still some work to do before it's ready for use by the general public. The current ETA is to have the system 
ready for action by the end of May 2015.

<a name="build-requirements"></a>
## Build Requirements

* [SBT 0.13.13] (http://www.scala-sbt.org/download.html)

<a name="how-it-works"></a>
## How it works

Bourne provides a construct called a narrative (e.g. story), which describes the flow for a single processing event.
The proceeding example is a Bourne narrative that performs the following flow:

* Extracts historical stock quotes from a tabbed-delimited file.
* Encodes the stock quotes as [Avro](avro.apache.org) records.
* Publishes each Avro record to a Kafka topic (eoddata.tradinghistory.avro)

We'll start with the story configuration, which is an XML file (comprised of one or more narratives) that 
describes the flow of the process; in this case, how file feeds are mapped to their respective processing 
endpoints (actors):

```json
{
  "input": {
    "name": "csv-input",
    "type": "file",
    "path": "unknown",
    "format": "csv",
    "columnHeaders": true
  },
  "outputs": [
    {
      "name": "fixed-output",
      "type": "file",
      "path": "./example/incoming/useragents-{{$date:YYYYMMDD}}.txt",
      "format": "fixed",
      "fields": [
        {
          "name": "USER_AGENT_FRAGMENT",
          "length": 80
        },
        {
          "name": "USER_AGENT_SOURCE",
          "length": 12
        },
        {
          "name": "EFFECTIVE_DATETIME",
          "length": 12
        }
      ]
    }
  ],
  "variables": []
}
```

