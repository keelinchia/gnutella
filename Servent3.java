import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Scanner;
import java.net.InetAddress;


class ReceiveHandler implements Runnable {
    
    Socket clientSocket;
    ArrayList<String> hostCache;
    int PORT;
    int numFiles;
    String messageID;
    
    // Constructor for this thread
    public ReceiveHandler(Socket clientSocket,
		       ArrayList<String> hostCache,
		       int PORT,
		       int numFiles) {
	this.clientSocket = clientSocket;;
	this.hostCache = hostCache;
	this.PORT = PORT;
	this.numFiles = numFiles;
    }
    
    /* Receive ping or pong to Buffer */
    public void run() {
	try {
	    while (true) {
		// create text reader and writer
		DataInputStream inStream  = new DataInputStream(clientSocket.getInputStream());
		DataOutputStream outStream = new DataOutputStream(clientSocket.getOutputStream());
	
		byte[] recheader = new byte[4];
		byte[] pingBuffer = new byte[12];
	
		/* Receive header then receive ping or pong */
		inStream.readFully(recheader, 0, recheader.length);
		inStream.readFully(pingBuffer, 0, pingBuffer.length);
	
		// convert the binary bytes to string
		String MessageID = new String(recheader);
	
		printBinaryArray(pingBuffer, "Received " + MessageID + ": ");
		System.out.println(MessageID +  " content: \n");
	
		byte[] IPbuf2 = new byte[4];
		for(int i = 0; i < 4; i++) {
		    IPbuf2[i] = pingBuffer[i];
		}
	
		byte[] PORTbuf2 = new byte[4];
		for(int i = 4; i < 8; i++) {
		    PORTbuf2[i - 4] = pingBuffer[i];
		}
	
		byte[] numFilesbuf2 = new byte[4];
		for(int i = 8; i < 12; i++) {
		    numFilesbuf2[i - 8] = pingBuffer[i];
		}
	
		System.out.println("~ Received " + MessageID + " ~");
		System.out.println("Peer's IP: " + getIpAddress(IPbuf2));
		System.out.println("Peer's port: " + toInteger(PORTbuf2));
		System.out.println("Peer's number of shared files: " + toInteger(numFilesbuf2));
	
		if (MessageID.equals("ping")) {
		    Thread SendHandler2 = new Thread (new SendHandler(this.clientSocket,
								     this.hostCache,
								     this.PORT,
								     this.numFiles,
								     "pong"));
		    SendHandler2.start();
		}
	    }
	} catch (Exception e) {
            // Throwing an exception
            System.out.println ("Exception is caught");
	    e.printStackTrace(); 
	}
    }

    /* Helper Methods */
    static void printBinaryArray(byte[] b, String comment)
    {
	System.out.println(comment);
	for (int i=0; i<b.length; i++)
	    {
		System.out.print(b[i] + " ");
	    }
	System.out.println();
	System.out.println();
    }
    
    static private byte[] toBytes(int i)
    {
	byte[] result = new byte[4];

	result[0] = (byte) (i >> 24);
	result[1] = (byte) (i >> 16);
	result[2] = (byte) (i >> 8);
	result[3] = (byte) (i /*>> 0*/);

	return result;
    }

    static private int toInteger(byte[] b)
    {
	int result =
	    (b[0] << 24) & 0xff000000|
	    (b[1] << 16) & 0x00ff0000|
	    (b[2] << 8) & 0x0000ff00|
	    (b[3] << 0) & 0x000000ff;
	
	return result;
    }

    private static String getIpAddress(byte[] rawBytes) {
	int i = 4;
        StringBuilder ipAddress = new StringBuilder();
        for (byte raw : rawBytes) {
            ipAddress.append(raw & 0xFF);
            if (--i > 0) {
                ipAddress.append(".");
            }
        }
        return ipAddress.toString();
    }
}

class SendHandler implements Runnable {

    Socket clientSocket;
    ArrayList<String> hostCache;
    int PORT;
    int numFiles;
    String messageID;
    
    // Constructor for this thread
    public SendHandler(Socket clientSocket,
		       ArrayList<String> hostCache,
		       int PORT,
		       int numFiles,
		       String messageID) {
	this.clientSocket = clientSocket;;
	this.hostCache = hostCache;
	this.PORT = PORT;
	this.numFiles = numFiles;
	this.messageID = messageID;
    }

    public void run() {

	try {
            InetAddress address = InetAddress.getLocalHost();

            // Create reader and writer
            DataInputStream inStream  = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream outStream = new DataOutputStream(clientSocket.getOutputStream());

            /* Prepare a PingBuf */
            /* |IP|Port|Number of shared files| */
	    // Convert data to bytes
            byte[] IPbuf = address.getAddress();
            byte[] PORTbuf = toBytes(PORT);
            byte[] numFilesbuf = toBytes(numFiles);

            printBinaryArray(IPbuf, "Local IP Bytes: \n");
            printBinaryArray(PORTbuf, "Local Port Bytes: \n");
            printBinaryArray(numFilesbuf, "Number of shared files: \n");

            // Prepare header
            byte[] header = messageID.getBytes();

            // Store all data into pingBuf
            byte[] pingBuf = new byte[12];

            for(int i = 0; i < 4; i++) {
                pingBuf[i] = IPbuf[i];
            }

            for(int i = 4; i < 8; i++) {
                pingBuf[i] = PORTbuf[i - 4];
            }

            for(int i = 8; i < 12; i++) {
                pingBuf[i] = numFilesbuf[i - 8];
            }

          
            /* Pong only needs to be sent once for each ping  */
            if (messageID.equals("pong")) {
                    outStream.write(header, 0, header.length);
                    outStream.flush();
                    outStream.write(pingBuf, 0, pingBuf.length);
                    outStream.flush();
                } else {
                    /* Send Ping in a 10 seconds period */
                    while (true) {
                        outStream.write(header, 0, header.length);
                        outStream.flush();
                        outStream.write(pingBuf, 0, pingBuf.length);
                        outStream.flush();
                        TimeUnit.SECONDS.sleep(10);
                    }
                }

	    
		} catch (Exception e) {
		    // Throwing an exception
		    System.out.println ("Exception is caught");
		    e.printStackTrace(); 
		}
    }
    
    /* Helper Methods */
    static void printBinaryArray(byte[] b, String comment)
    {
	System.out.println(comment);
	for (int i=0; i<b.length; i++)
	    {
		System.out.print(b[i] + " ");
	    }
	System.out.println();
	System.out.println();
    }
    
    static private byte[] toBytes(int i)
    {
	byte[] result = new byte[4];

	result[0] = (byte) (i >> 24);
	result[1] = (byte) (i >> 16);
	result[2] = (byte) (i >> 8);
	result[3] = (byte) (i /*>> 0*/);

	return result;
    }

    static private int toInteger(byte[] b)
    {
	int result =
	    (b[0] << 24) & 0xff000000|
	    (b[1] << 16) & 0x00ff0000|
	    (b[2] << 8) & 0x0000ff00|
	    (b[3] << 0) & 0x000000ff;
	
	return result;
    }

    private static String getIpAddress(byte[] rawBytes) {
	int i = 4;
        StringBuilder ipAddress = new StringBuilder();
        for (byte raw : rawBytes) {
            ipAddress.append(raw & 0xFF);
            if (--i > 0) {
                ipAddress.append(".");
            }
        }
        return ipAddress.toString();
    }
}

public class Servent3
{
    public static void main(String args[]) throws IOException
    {
	ArrayList<String> hostCache = new ArrayList<String>();
	ServerSocket serverSocket;
	Socket clientSocket;

	Scanner s = new Scanner(System.in);  // Create a Scanner object
	System.out.println("Enter Port to use: ");
	int Port = s.nextInt();  // Read user input
	
	System.out.println("Enter number of files to share: ");
	int numFiles = s.nextInt();  // Read user input

	/* To make sure this is an active servent */
	hostCache.add("10.27.18.245:" + 53954);
	hostCache.add("10.27.18.245:" + 53955);
	
	/* As a CLIENT, if hostCache is empty, start a thread 
	   to connect to a well-known host */
	if (hostCache.isEmpty()) {
	    // Add Well-known host (pyrite-n2) to hostCache 
	    hostCache.add("10.27.18.245:" + 53953);
	    /* Connect to the first host in the list */
	    String serventIpPort = hostCache.get(0);
	    // Separate ip and port
	    StringTokenizer token = new StringTokenizer(serventIpPort, ":");
	    String serventIp = token.nextToken();
	    String serventPort = token.nextToken();
	    int serventPortInt = Integer.parseInt(serventPort);
	    
	    System.out.println("" + hostCache.get(0));
	    
	    // Create client socket
	    Socket cSocket = new Socket(serventIp, serventPortInt);
	
	    Thread SendHandler = new Thread (new SendHandler(cSocket,
							     hostCache,
							     Port,
							     numFiles,
							     "ping"));
	    SendHandler.start();

	       Thread ReceiveHandler = new Thread (new ReceiveHandler(cSocket, hostCache, Port, numFiles));
	     ReceiveHandler.start();
	} 
	 
	// Server socket
	serverSocket = new ServerSocket(Port);
	
	/* As a SERVER, listen for connections */ 
	while (true) {
	    try {
		System.out.println("Listening...\n\n");		
		// Client socket
		clientSocket = serverSocket.accept();
		System.out.println("A new servent has connected. \n\n");
		Thread SendHandler = new Thread (new SendHandler(clientSocket,
								 hostCache,
								 Port,
								 numFiles,
								 "ping"));
		SendHandler.start();
		
		Thread ReceiveHandler = new Thread (new ReceiveHandler(clientSocket, hostCache, Port, numFiles));
		ReceiveHandler.start();
	     
	    } catch (IOException e) {
		System.out.println("I/O error: " + e);
	    }
	}
    }   
}

