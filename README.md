# Java Program for File Retrieval Engine using ZEROMQ

This program is divided into 2 parts Server-side, Client-side

**Server**

**ProcessingEngine** This class is responsible for the core functionality of indexing and searching files. It uses multithreading to manage indexing tasks and an IndexStore instance to store and search the indexed content.
**WorkerThread** A part of the server application that handles requests from clients. It processes indexing and search requests using the ProcessingEngine and IndexStore, executing the operations concurrently to efficiently manage multiple client requests.
**IndexStore** Manages the storage and retrieval of indexed words across documents. It uses a concurrent map to maintain word counts in documents, offering methods to update the index with new documents and search for words, returning documents where they appear.

**Client**

**AppInterface** This class provides a command-line interface for interacting with a document retrieval engine, allowing users to index files, search for queries, and exit the program. It uses an instance of ProcessingEngine to execute these operations, interpreting commands like "index <path>" for indexing, "search <query>" for searching, "list" for listing active clients and "quit" for exiting.

**ProcessingEngine** This class, part of the client-side application, handles communication with the server for indexing and searching documents. It uses ZeroMQ for message passing, sending requests for indexing directories and searching queries to the server, and handling responses.

# Execution commands


**Server** cd server
**command to compile** mvn compile
**command to execute** mvn exec:java -Dexec.mainClass="org.vishal.server.ServerMain"

**Client** cd client
**command to compile** mvn compile
**command to execute** mvn exec:java -Dexec.mainClass="org.vishal.client.ClientMain"
**commands on client side**
>connect 0.0.0.0:5555
>index <filepath>
>search <query>
>list
>quit
