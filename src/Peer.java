import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;



public class Peer {
    //private variables so it can only be accessed within this class
    private byte[] bitfield;
    private static int peerid;
    private int numberOfFilePieces;
    private int numberOfExpectedPeers;

    //Common.cfg contents

    static int numberOfPreferredNeighbors;
    static int unchokingInterval;
    static int optimisticUnchokingInterval;
    static String fileName;
    static int fileSize;
    static int pieceSize;
    static String hShkHeader;

    public Peer(int peerid, int port, boolean hasFile, Hashtable<Integer, String> peerInfo, int targNum) throws IOException, InterruptedException {
        this.peerid = peerid;
        hShkHeader = "P2PFILESHARINGPROJ";
        numberOfExpectedPeers = targNum-1;
        //create peer_ID files
        new File("peer_" + peerid).mkdir();


        //read and add file contents to peer variables
        String str;
        String[] config = new String[6];
        try {
            BufferedReader line = new BufferedReader(new FileReader("src/Common.cfg"));

            //save int values of the config file into our string array Config
            //which is the second value of each line aka token[1]
            int k = 0;
            while ((str = line.readLine()) != null){
                String[] tokens = str.split("\\s+");
                config[k] = tokens[1];
                k++;
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
        
        //currently hard coded for how many connections it expects. Needs to be changed.
        ServerHandler SH = new ServerHandler(port, numberOfExpectedPeers-(peerid-1001));
        SH.start();
        //assign above to variable first so you can start variable and also join

        //parse peerInfo and then begin for loop for each server to be connected to
        //create new thread and pass in peerID, port, etc
        

        ArrayList<ClientHandler> CH = new ArrayList<ClientHandler>(peerid-1001);
        for(int p = 0; p < peerid-1001; p++)
		{
				CH.add(new ClientHandler(peerInfo, p+1001));
				CH.get(p).start();
		}
        
        SH.join();
        // for each CH CH.join()
        for (int i = 0; i<CH.size(); i++)
        {
        	CH.get(i).join();
        }
    }
   
/////////////////////////////
    
    private static class ServerHandler extends Thread 

    {
    	ArrayList<RequestHandler> RH = new ArrayList<RequestHandler>(peerid-1001);
        int port;
        int numConn = 0;
        int expectedClients;
        public ServerHandler (int port, int expectedClients) throws IOException
        {
                this.port = port;
                this.expectedClients = expectedClients;
        }
        
        public void run() 
        {
            try
            {
                System.out.println("The server is running."); 
                ServerSocket listener = new ServerSocket(port);
                
                try 
                {
                	
                   
                   // define RH somewhere and allocate enough memory or use arraylist append!
                    while(numConn < expectedClients) 
                    {
                    	Socket accepted = listener.accept();
                        RH.add(new RequestHandler(accepted));   //add the flag bit indicating whether they shook hands or not
                        RH.get(numConn).start();
                        numConn++;
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
    
    private static class RequestHandler extends Thread //rename to request handler
    {
        private Socket connection;
        private ObjectInputStream in;   //stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket

        public RequestHandler(Socket connection)
        {
                    this.connection = connection;
        }
        
        public void run() 
        {
            try {

                HandShakeMessage hMsg = new HandShakeMessage();
                //Utility.SendMsg(mySocket,Msg)//synchronized on the socket level so that only one can write to the socket at the same time

                // SEND HANDSHAKE
                byte[] handshake = hMsg.getByteMessage(hShkHeader, peerid);

                DataOutputStream dOut = new DataOutputStream(connection.getOutputStream());

                dOut.writeInt(handshake.length); // write length of the message
                dOut.write(handshake);           // write the message

                // RECEIVE HANDSHAKE
                DataInputStream dIn = new DataInputStream(connection.getInputStream());

                int length = dIn.readInt();
                if (length > 0) {

                    byte[] message = new byte[length];
                    dIn.readFully(message, 0, message.length); // read the message

                    hMsg.accept(message);

                }
                //start while loop for receiving messages
                //have a separate thread to send message that dies

                while(true){ //have a better termination condition!
                    int message_length = dIn.readInt();
                    byte message_type = dIn.readByte();
                    switch(message_type){
                        case 0:
                            byte[] message_payload = new byte[message_length-1];
                            dIn.readFully(message_payload);
                            //if we need to send out a message in response spawn a new thread and pass the reply message to it. Let it call send on the outputstream and die!
                            break;
                        case 1:
                            break;

                    }
                }
            }
            catch(Exception ioException)
            {
                
            }
            finally
            {

            }
        }

        //case functions
    }
    
///////////////////////////
    
    private static class ClientHandler extends Thread 
    {
        //ASSUME YOU HAVE THE INFORMATION YOU NEED (IP , ID , PORT , ETC including the socket.)
        Socket requestSocket;
    	int pReq;
        Hashtable<Integer, String> peerInfo;
        String address;
        public ClientHandler(Hashtable<Integer, String> peerInfo, int pReq) throws IOException 
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
   	    		//socket connected

                RequestHandler RH = new RequestHandler(requestSocket);
                RH.run(); // run instead of start so that it it doesn't run on a separate thread!

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