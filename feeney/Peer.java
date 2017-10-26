import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;



public class Peer {
    //private variables so it can only be accessed within this class
    private byte[] bitfield;
    private static int peerid;
    private int numberOfFilePieces;

    //Common.cfg contents
    int numberOfPreferredNeighbors;
    int unchokingInterval;
    int optimisticUnchokingInterval;
    String fileName;
    int fileSize;
    int pieceSize;

    public Peer(int peerid, int port, boolean hasFile, Hashtable<Integer, String> peerInfo) throws IOException {
        this.peerid = peerid;
        //create peer_ID files
        new File("peer_" + peerid).mkdir();

        
        new ServerHandler(port).start();
        new ClientHandler(peerInfo).start();
        

        //read and add file contents to peer variables
        String str;
        String[] config = new String[6];
        try {
            BufferedReader line = new BufferedReader(new FileReader("src/Common.cfg"));

            //save int values of the config file into our string array Config
            //which is the second value of each line aka token[1]
            int i = 0;
            while ((str = line.readLine()) != null){
                String[] tokens = str.split("\\s+");
                config[i] = tokens[1];
                i++;
            }
            line.close();
            
            numberOfPreferredNeighbors = Integer.parseInt(config[0]);
            unchokingInterval = Integer.parseInt(config[1]);
            optimisticUnchokingInterval = Integer.parseInt(config[2]);
            fileName = config[3];
            fileSize = Integer.parseInt(config[4]);
            pieceSize = Integer.parseInt(config[5]);
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
   
/////////////////////////////
    
    private static class ServerHandler extends Thread 
	{

    	int port;
    	public ServerHandler(int port) throws IOException 
        {
    		   	this.port = port;	
        }
    	
    	public void run() 
    	{
	 		try
 			{
	 			System.out.println("The server is running."); 
	 	    	ServerSocket listener = new ServerSocket(port);
	 	    	
	 	    	try 
	 			{
	 	    		while(true) 
	 				{
	 	        		new ConnectionServerHandler(listener.accept()).start();

	 					System.out.println("Client is connected!");
	 					
	 	    		}
	 	    	} 
	 	        finally 
	 	    	{
	 	        	listener.close();
	 	    	} 
 			}
			catch(Exception e)
 			{
				e.printStackTrace();
			}
			finally
			{
			
			}
    	}
	}
    
    
/////////////////////////////
    
	private static class ConnectionServerHandler extends Thread 
	{
		private Socket connection;
    	private ObjectInputStream in;	//stream read from the socket
    	private ObjectOutputStream out;    //stream write to the socket

    	public ConnectionServerHandler(Socket connection) 
        {
            		this.connection = connection;
        }
    	
    	public void run() 
    	{
	 		try
 			{

 			}
			catch(Exception ioException)
 			{
				
			}
			finally
			{

			}
    	}
	}
	
///////////////////////////
	
    private static class ClientHandler extends Thread 
	{

    	
    	Hashtable<Integer, String> peerInfo;
    	public ClientHandler(Hashtable<Integer, String> peerInfo) throws IOException 
        {
    	   		this.peerInfo = peerInfo;
   
        }
    	
    	public void run() 
    	{
	 		try
 			{
	 			for(int p = 1001; p < peerid; p++)
	 			{
	 				new ClientConnectionHandler(peerInfo, p).start();
	 			}

 			}
			catch(Exception e)
 			{
				e.printStackTrace();
			}
			finally
			{
			
			}
    	}
	}
    
   //////////////////////////
    
    private static class ClientConnectionHandler extends Thread 
	{
    	Socket requestSocket;
    	Hashtable<Integer, String> peerInfo;
    	int pReq;
    	String address;

    	public ClientConnectionHandler(Hashtable<Integer, String> peerInfo, int pReq) throws IOException 
        {
    		this.peerInfo = peerInfo;
    		this.pReq = pReq;
        }
    	
    	public void run() 
    	{
	 		try
 			{
	 			
	 			address = peerInfo.get(pReq);
	 			String hostname = address.split(":")[0];
	            int port = Integer.parseInt(address.split(":")[1]);
	    		requestSocket = new Socket(hostname, port);

 			}
			catch(Exception e)
 			{
				e.printStackTrace();
			}
			finally
			{
			
			}
    	}
	}
}