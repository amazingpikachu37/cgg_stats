# cgg_stats
A tool to generate a wide array of statistics for users of counting.gg.

# Necessary Libraries  
Jackson version 2.14 https://github.com/FasterXML/jackson (For parsing JSON data)   
Apache Fury version 5.1 https://mvnrepository.com/artifact/org.apache.fury/fury-core (For serialization)  
Socket IO Client version 2.1 https://mvnrepository.com/artifact/io.socket/socket.io-client (For communicating with websockets on counting.gg. Only necessary if running the bot.)  

# Counting Data
All data from counting.gg is intentionally excluded. The function CombinedStats.updateAllDatabases() can be used to download all the data, or alternatively, CountingThread.updateDatabase() can be called on individual threads to download data for that thread. Note that the initial download may require several GB of memory and could take around a full day. However, when running stats, this program is much lighter on memory usage.

# Further Comments
If running locally, almost all stats can be found either in src/Threads/CombinedStats.java or src/Threads/Statable.java. They can then be called directly from src/Threads/Main.java (an example of obtaining the hall of counters for 1/x exists there currently). The file Documentation.md provides documentation on commands when running the bot from src/Bot/BotMain.java. Please contact @pull_fish on discord before running any bots on counting.gg.


