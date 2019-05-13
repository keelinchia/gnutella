Gnutella Implementation
------------------------
Author: Kee Lin Chia

Files: Servent.java Servent3.java

1. A servent joins the network for the first time by "manual bootstrapping",
   wherein the servent establish a TCP/IP connection with the first host in its list
   of known hosts on the network. Once the connection is established, it sends a Ping to
   host for Gnutella connection.

2. The servent program has its main thread listening for connections.
   Once it accepts a connection, it spawns two new threads:
   (1) SendHandler:  Sends a Ping to a connected node within a 10 seconds period.
                     
   (2) ReceiveHandler: Receives ping and pong. If pong is received, spawn a new SendHandler
       thread to send back pong.

3. In this design, Ping has the same structure as Pong.

4. In this package, Servent program acts as a new host who wants to join the network.
   Servent3 program acts as a well-known host with the specified port number 53953.
   (This port number is hard coded in Servent's main method and can be changed to
    to connect to other servents.)

5. Servent's hostCache is initially empty. Servent3 is added to Servent's hostCache in
   Servent's main method.

6. Servent3 automatically adds its neighbors to its hostCache in its main method.

7. Multiple instances of these two programs can be run on different consoles.

8. USAGE:
   (1) Compile with make
   (2) Run Servent3 first with
       	  java Servent3
       Enter 53953 as port number when prompted
       Enter any number of files
   (3) Run Servent on a different console with
       the same command but different port number

ALL TESTS WERE RUN ON PYRITE-N2
