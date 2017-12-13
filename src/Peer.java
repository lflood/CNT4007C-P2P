import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Hashtable;



public class Peer {
    //private variables so it can only be accessed within this class
    private static int peerid;
    private int numberOfFilePieces;
    private int numberOfExpectedPeers;
    private Message messageHandler;

    private Hashtable<Integer, RemotePeer> remotePeers;

    //Common.cfg contents

    static int numberOfPreferredNeighbors;
    static int unchokingInterval;
    static int optimisticUnchokingInterval;
    static String fileName;
    static int fileSize;
    static int pieceSize;
    static String hShkHeader;

    private BitSet bitfield = new BitSet();

    private HashSet<Integer> chokeMe = new HashSet<>();
    private HashSet<Integer> interested = new HashSet<>();


    public Peer(int peerid, int port, boolean hasFile, Hashtable<Integer, String> peerInfo, int targNum) throws IOException, InterruptedException {
        this.peerid = peerid;
        hShkHeader = "P2PFILESHARINGPROJ";
        numberOfExpectedPeers = targNum-1;


        remotePeers = new Hashtable<>();

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
            int numPieces = (int)Math.ceil(fileSize / (pieceSize * 1.0));
            messageHandler = new Message(numPieces);
            if(hasFile){
                bitfield.set(0, numPieces);
            } else{
                bitfield.clear(0);
            }

            // TODO set to actual bitfield

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
    
    private class ServerHandler extends Thread

    {
    	ArrayList<ServerRequestHandler> RH = new ArrayList<ServerRequestHandler>(peerid-1001);
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
                        RH.add(new ServerRequestHandler(accepted));   //add the flag bit indicating whether they shook hands or not
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
    
    private class ServerRequestHandler extends Thread //rename to request handler
    {
        private Socket connection;
        private ObjectInputStream in;   //stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
        private int clientID;

        public ServerRequestHandler(Socket connection)
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

                    clientID = hMsg.accept(message);

                }

                // add newly connected peer to RemotePeers
                if(!bitfieldIsEmpty()) {
                    System.out.println(bitfieldIsEmpty());
                    byte[] msg = messageHandler.getBitfieldMessage(bitfield);

                    dOut.write(msg);           // write the message
                }

                // create remote peer class to store neighbor info
                remotePeers.put(clientID, new RemotePeer(clientID));

                //start while loop for receiving messages
                //have a separate thread to send message that dies

                RemotePeer neighbor = remotePeers.get(clientID);
                int index;

                while(true){ //have a better termination condition!
                    int message_length = dIn.readInt();
                    System.out.println(message_length);
                    byte message_type = dIn.readByte();
                    System.out.println(message_type);
                    byte[] message_payload;
                    switch(message_type){
                       // case 0:
                         //   byte[] message_payload = new byte[message_length-1];
                           // dIn.readFully(message_payload);
                            //if we need to send out a message in response spawn a new thread and pass the reply message to it. Let it call send on the outputstream and die!

                        case 0:
                            chokeNeighbor(clientID);
                            //if we need to send out a message in response spawn a new thread and pass the reply message to it. Let it call send on the outputstream and die!
                            break;

                        case 1:
                            unchokeNeighbor(clientID);
                            break;

                        case 2:
                            interested(clientID);
                            break;

                        case 3:
                            notInterested(clientID);
                            break;

                        case 4:
                            index = dIn.readInt();

                            neighbor.updateBitfield(index);
                            break;
                        case 5:
                            message_payload = new byte[message_length-1];
                            dIn.readFully(message_payload);

                            neighbor = remotePeers.get(clientID);

                            neighbor.initializeBitfield(message_payload);
                            System.out.println("bitfield message received");
                            break;
                        case 6:
                            index = dIn.readInt();

                            byte[] piece = getPiece(index);

                            byte[] pieceMsg = messageHandler.getPieceMessage(index, piece);

                            dOut.write(pieceMsg);
                            break;

                        case 7:
                            index = dIn.readInt();

                            message_payload = new byte[message_length-5];
                            dIn.readFully(message_payload);

                            addPiece(index, message_payload);

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
        //case functions
        public void chokeNeighbor(int peerid){
            chokeMe.add(peerid);
            /*try{
                log.choking(peerid, );
            }catch(IOException e){
                System.out.println(e.toString());
            }*/
            RemotePeer neighbor = remotePeers.get(peerid);
            neighbor.choke();
        }

        public void unchokeNeighbor(int peerid){
            RemotePeer neighbor = remotePeers.get(peerid);
            neighbor.unchoke();
        }

        public void interested(int peerid){
            RemotePeer neighbor = remotePeers.get(peerid);
            neighbor.setInterested();
        }

        public void notInterested(int peerid){
            RemotePeer neighbor = remotePeers.get(peerid);
            neighbor.setNotInterested();
        }

        public boolean bitfieldIsEmpty(){
            if(bitfield == null){
                return true;
            }else{
                return false;
            }
        }

        // TODO
        public byte[] getPiece(int index){
            return new byte [10];
        }

        // TODO
        public void addPiece(int index, byte[] piece){

        }
    }

///////////////////////////

    private class ClientRequestHandler extends Thread //rename to request handler
    {
        private Socket connection;
        private int serverID;
        private ObjectInputStream in;   //stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket

        public ClientRequestHandler(Socket connection, int serverID)
        {
            this.connection = connection;
            this.serverID = serverID;
        }

        public void run()
        {
            int recepientID = 0;
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

                    handshake = new byte[length];
                    dIn.readFully(handshake, 0, handshake.length); // read the message

                    recepientID = hMsg.accept(handshake);
                    Log.madetcpConnection(peerid, recepientID);
                    Log.accepttcpConnection(recepientID, peerid);

                    if(serverID != recepientID){

                        System.out.println("Error: incorrect peerID");
                        // exit thread, connection incorrect
                    }
                }

                if(!bitfieldIsEmpty()) {

                    byte[] msg = messageHandler.getBitfieldMessage(bitfield);

                    dOut.write(msg);           // write the message
                }

                // create remote peer class to store neighbor info
                remotePeers.put(serverID, new RemotePeer(serverID));

                //start while loop for receiving messages
                //have a separate thread to send message that dies

                RemotePeer neighbor = remotePeers.get(serverID);
                int index;

                while(true){ //have a better termination condition!

                    int message_length = dIn.readInt();
                    System.out.println(message_length);
                    byte message_type = dIn.readByte();
                    System.out.println(message_type);
                    byte[] message_payload;
                    switch(message_type){
                        case 0:
                            chokeNeighbor(serverID, recepientID);
                            //if we need to send out a message in response spawn a new thread and pass the reply message to it. Let it call send on the outputstream and die!
                            break;

                        case 1:
                            unchokeNeighbor(serverID, recepientID);
                            break;

                        case 2:
                            interested(serverID, recepientID);
                            break;

                        case 3:
                            notInterested(serverID, recepientID);
                            break;

                        case 4:
                            index = dIn.readInt();

                            neighbor.updateBitfield(index);
                            break;
                        case 5:
                            message_payload = new byte[message_length-1];
                            dIn.readFully(message_payload);

                            neighbor.initializeBitfield(message_payload);
                            System.out.println("bitfield message received");
                            break;

                        case 6:
                            index = dIn.readInt();

                            byte[] piece = getPiece(index);

                            byte[] pieceMsg = messageHandler.getPieceMessage(index, piece);

                            dOut.write(pieceMsg);
                            break;

                        case 7:
                            index = dIn.readInt();

                            message_payload = new byte[message_length-5];
                            dIn.readFully(message_payload);

                            addPiece(index, message_payload);

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
        public void chokeNeighbor(int peerid, int neighborID){
            //possible implementation using HashSet <Integer> chokeMe
            //maybe add this code into the remote peer class?
            chokeMe.add(peerid);
            try{
                Log.choking(peerid, neighborID);
            }catch(IOException e){
                System.out.println(e.toString());
            }
            //versus this
            RemotePeer neighbor = remotePeers.get(peerid);
            neighbor.choke();
        }

        public void unchokeNeighbor(int peerid, int neighborID){
            chokeMe.remove(peerid);
            try{
                Log.unchoking(peerid, neighborID);
            }catch(IOException e){
                System.out.println(e.toString());
            }
            RemotePeer neighbor = remotePeers.get(peerid);
            neighbor.unchoke();
        }

        public void interested(int peerid, int neighborID){
            interested.add(peerid);
            try{
                Log.interested(peerid, neighborID);
            }catch(IOException e) {
                System.out.println(e.toString());
            }
            RemotePeer neighbor = remotePeers.get(peerid);
            neighbor.setInterested();
        }

        public void notInterested(int peerid, int neighborID){
            interested.remove(peerid);
            try{
                Log.uninterested(peerid, neighborID);
            }catch(IOException e){
                System.out.println(e.toString());
            }
            RemotePeer neighbor = remotePeers.get(peerid);
            neighbor.setNotInterested();
        }

        public boolean bitfieldIsEmpty(){
            if(bitfield == null){
                return true;
            }else{
                return false;
            }
        }

        // TODO
        public byte[] getPiece(int index){
            return new byte [10];
        }

        // TODO
        public void addPiece(int index, byte[] piece){

        }
    }


    /////////////////////////////


    private class ClientHandler extends Thread
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

                ClientRequestHandler RH = new ClientRequestHandler(requestSocket, pReq);
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