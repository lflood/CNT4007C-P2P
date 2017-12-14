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
    private int peerid;
    private static int port;
    private static boolean hasFile;
    private static ArrayList<String> peerInfo;
    private static int targNum;

    private int numberOfFilePieces;
    private int numberOfExpectedPeers;
    private Message messageHandler;
    private FileHandling fileHandler;

    private Hashtable<Integer, RemotePeer> remotePeers;
    private Hashtable<Integer, Socket> connections;

    //Common.cfg contents

    static int numberOfPreferredNeighbors;
    static int unchokingInterval;
    static int optimisticUnchokingInterval;
    static String fileName;
    static int fileSize;
    static int pieceSize;
    static int numPieces;
    static String hShkHeader;

    private BitSet bitfield = new BitSet();

    /*should we have something like this below??

    private static int port;
    private static boolean hasFile;
    private static int
    public static final Peer peer = new peer(peerid, port, hasFile, peerInfo, counter);
    public static Peer getPeer() {return peer}
    */

    public Peer(int peerid, int port, boolean hasFile, ArrayList<String> peerInfo, int peerNum, int totalPeers) throws IOException, InterruptedException {
        this.peerid = peerid;
        this.port = port;
        this.hasFile = hasFile;
        this.peerInfo = peerInfo;
        this.targNum = peerNum;

        hShkHeader = "P2PFILESHARINGPROJ";
        numberOfExpectedPeers = totalPeers - peerNum;


        remotePeers = new Hashtable<>();
        connections = new Hashtable<>();

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
            numPieces = (int)Math.ceil(fileSize / (pieceSize * 1.0));
            messageHandler = new Message(numPieces);
            if(hasFile){
                bitfield.set(0, numPieces);
            } else{
                bitfield.clear(0);
            }
            System.out.println("fileSize is: " + fileSize);
            fileHandler = new FileHandling(this);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //currently hard coded for how many connections it expects. Needs to be changed.
        //assign above to variable first so you can start variable and also join

        //parse peerInfo and then begin for loop for each server to be connected to
        //create new thread and pass in peerID, port, etc

        ConnectionHandler connectionHandler = new ConnectionHandler(numberOfExpectedPeers);
        connectionHandler.start();

        ArrayList<ClientHandler> CH = new ArrayList<ClientHandler>(totalPeers);
        for(int p = 0; p < (peerNum - 1); p++)
		{
            CH.add(new ClientHandler(peerInfo, p));
            CH.get(p).start();
		}

		//TODO MAKE SURE THESE JOIN
        // for each CH CH.join()
        for (int i = 0; i<CH.size(); i++)
        {
        	CH.get(i).join();
        }

        connectionHandler.join();
        connectionHandler.closeConnections();
    }

    /////////////////////////////

    private class ConnectionHandler extends Thread
    {
        ServerSocket listener;
        boolean closed;
        int numConn;
        int expectedConnections;

        public ConnectionHandler(int expectedConnections){

            try {

                listener = new ServerSocket(port);

            } catch (Exception e) {
                e.printStackTrace();
            }

            closed = false;
            numConn = 0;
            this.expectedConnections = expectedConnections;
        }

        public void run(){

            while(numConn < expectedConnections) {

                try {

                    Socket newConnection = listener.accept();
                    numConn++;
                    ServerHandler SH = new ServerHandler(newConnection);
                    SH.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        public void closeConnections(){

            try {

                listener.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

/////////////////////////////
    
    private class ServerHandler extends Thread
    {
        Socket connection;
        int numConn = 0;
        public ServerHandler (Socket connection) throws IOException
        {
                this.connection = connection;
        }
        
        public void run() 
        {
            try
            {
                System.out.println("The server is running.");

                try 
                {
                    ServerRequestHandler RH = new ServerRequestHandler(connection);
                    RH.start();
                    System.out.println("Client is connected!");

                    /*
                   // define RH somewhere and allocate enough memory or use arraylist append!
                    while(numConn < expectedClients) {
                        Socket accepted = listener.accept();

                        RH.add(new ServerRequestHandler(accepted));   //add the flag bit indicating whether they shook hands or not
                        RH.get(numConn).start();
                        numConn++;
                        System.out.println("Client is connected!");
                    }*/
                } 
                finally 
                {

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
                connections.put(clientID, connection);
                System.out.println("Connection stored: " + peerid + " and " + clientID);

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
                        //if we need to send out a message in response spawn a new thread and pass the reply message to it. Let it call send on the outputstream and die!
                        case 2:
                            interestedMe(peerid, clientID);
                            break;
                        case 3:
                            notInterestedMe(peerid, clientID);
                            break;
                        case 5:
                            message_payload = new byte[message_length - 1];
                            dIn.readFully(message_payload);
                            neighbor = remotePeers.get(clientID);
                            BitSet initBitfield = BitSet.valueOf(message_payload);
                            neighbor.initializeBitfield(initBitfield);
                            System.out.println("bitfield message received");
                            break;
                        case 6:
                            //received a request message for a certain piece index
                            index = dIn.readInt();
                            //we have the piece contents below
                            // if client not choked, send piece
                            //TODO add check for choked
                            if(!neighbor.isChoked()){
                                //check if neighbor is preferred
                                //check if optimistically unchoked neighbor changed
                                //because peer might've
                                byte[] piece = getPiece(index);
                                //we parse the data into the pieceMsg format
                                byte[] pieceMsg = messageHandler.getPieceMessage(index, piece);
                                //send it out to our remote peer
                                dOut.write(pieceMsg);
                            }
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
        public void interestedMe(int neighborID, int peerid){
            RemotePeer neighbor = remotePeers.get(neighborID);
            neighbor.isInterestedMe();
            //the neighbor that choked us will now save that we are choked
            try{
                Log.choking(peerid, neighborID);
            }catch(IOException e){
                System.out.println(e.toString());
            }
        }
        public void notInterestedMe(int neighborID, int peerid) {
            RemotePeer neighbor = remotePeers.get(neighborID);
            neighbor.isNotInterestedMe();
            try {
                Log.uninterested(peerid, neighborID);
            } catch (IOException e) {
                System.out.println(e.toString());
            }
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
            return fileHandler.getPiece(index);
        }

        // TODO
        public boolean peerUpdated(){
            return false;
        }

        // TODO
        public void sendUpdateMessages(){

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
                connections.put(serverID, connection);
                System.out.println("Connection stored: " + peerid + " and " + serverID);

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
                            chokeMe(serverID, recepientID);
                            break;
                        case 1:
                            unchokeMe(serverID, recepientID);
                            break;
                        case 4:
                            index = dIn.readInt();
                            neighbor.hasPiece(index);
                            Log.have(serverID, recepientID, index);
                            break;
                        case 5:
                            message_payload = new byte[message_length - 1];
                            dIn.readFully(message_payload);
                            neighbor = remotePeers.get(serverID);
                            BitSet initBitfield = BitSet.valueOf(message_payload);
                            neighbor.initializeBitfield(initBitfield);
                            System.out.println("bitfield message received");
                            break;
                        case 7:
                            index = dIn.readInt();
                            message_payload = new byte[message_length-5]; //this is the piece contents
                            dIn.readFully(message_payload);
                            addPiece(index, message_payload);
                            Log.downloadingPiece(serverID, recepientID, index, fileHandler.numPiecesPeerHas());
                            //we should also send have message to tell the other remote peers an update on what it has
                            byte [] haveMsg = messageHandler.getHaveMessage(index);
                            dOut.write(haveMsg);
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
        public void chokeMe(int neighborID, int peerid){
            RemotePeer neighbor = remotePeers.get(neighborID);
            neighbor.isChokingMe();
            try{
                Log.choking(peerid, neighborID);
            }catch(IOException e){
                System.out.println(e.toString());
            }
        }

        public void unchokeMe(int neighborID, int peerid){
            RemotePeer neighbor = remotePeers.get(neighborID);
            neighbor.isUnchokingMe();
            try{
                Log.unchoking(peerid, neighborID);
            }catch(IOException e){
                System.out.println(e.toString());
            }
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
            return fileHandler.getPiece(index);
        }

        // TODO
        public void addPiece(int index, byte[] piece){
            fileHandler.setPiece(index, piece);
        }

        public int findNewPieceIndex(int peerid){

            RemotePeer neighbor = remotePeers.get(peerid);

            return neighbor.getRandomPieceWanted(bitfield);
        }
    }


    /////////////////////////////


    private class ClientHandler extends Thread
    {
        //ASSUME YOU HAVE THE INFORMATION YOU NEED (IP , ID , PORT , ETC including the socket.)
        Socket requestSocket;
    	int pReq;
        ArrayList<String> peerInfo;
        String address;
        public ClientHandler(ArrayList<String> peerInfo, int pReq) throws IOException
        {
                this.peerInfo = peerInfo;
                this.pReq = pReq;
        }
        
        public void run() 
        {
            try
            {
            	address = peerInfo.get(pReq);
            	int peerID = Integer.parseInt(address.split(":")[0]);
   	 			String hostname = address.split(":")[1];
   	            int port = Integer.parseInt(address.split(":")[2]);
   	    		requestSocket = new Socket(hostname, port);
   	    		//socket connected

                ClientRequestHandler RH = new ClientRequestHandler(requestSocket, peerID);
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

    public int getPeerID(){
        return peerid;
    }
    public boolean getHasFile(){
        return hasFile;
    }
    public int getFileSize(){
        return fileSize;
    }
    public int getPieceSize(){
        return pieceSize;
    }
    public int getNumPieces(){
        return numPieces;
    }

    public void optimisticUnchokingInterval(){

    }
    public void unchokingInterval(){

    }
    public void ALLDONE(){
        fileHandler.writeFile();
    }
}