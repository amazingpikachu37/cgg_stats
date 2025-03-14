# cgg_stats
A tool to generate a wide array of statistics for users of counting.gg.

Note: The following libraries are needed in order for this project to run:
Jackson version 2.14 https://github.com/FasterXML/jackson (For parsing JSON data)
Apache Fury version 5.1 https://mvnrepository.com/artifact/org.apache.fury/fury-core (For serialization)
Socket IO Client version 2.1 https://mvnrepository.com/artifact/io.socket/socket.io-client (For communicating with websockets on counting.gg. Only necessary if running the bot.)

Additionally, all data from counting.gg is intentionally excluded. The function CombinedStats.updateAllDatabases() can be used to download all the data, or alternatively, CountingThread.updateDatabase() can be called on individual threads to download data for that thread. Note that the initial download may require several GB of memory and could take around a full day. However, when running stats, this program is much lighter on memory usage.
