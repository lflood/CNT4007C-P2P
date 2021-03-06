import javax.xml.crypto.Data;
import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;


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
    private RemotePeer optUnchoked;
    private boolean timingTaken;
    private boolean start;

    private Hashtable<Integer, RemotePeer> remotePeers;
    private Hashtable<Integer, Socket> connections;
    private Hashtable<Integer, Boolean> blocked;
    Hashtable<Integer, Float> peerTimes= new Hashtable<>();

    //Common.cfg contents

    static int numberOfPreferredNeighbors;
    static int unchokingInterval;
    static int optimisticUnchokingInterval;
    static String fileName;
    static int fileSize;
    static int pieceSize;
    static int numPieces;
    static String hShkHeader;

    private BitSet bitfield;
    ClientHandler client;
    ServerHandler server;
    ConnectionHandler connectionHandler;

    public Peer(int peerid, int port, boolean hasFile, ArrayList<String> peerInfo, int peerNum, int totalPeers) throws IOException, InterruptedException {
        this.peerid = peerid;
        this.port = port;
        this.hasFile = hasFile;
        this.peerInfo = peerInfo;
        this.targNum = peerNum;

        timingTaken = false;
        start = false;

        hShkHeader = "P2PFILESHARINGPROJ";
        numberOfExpectedPeers = totalPeers - peerNum;
        optUnchoked = null;
        bitfield = new BitSet();


        remotePeers = new Hashtable<>();
        connections = new Hashtable<>();
        blocked = new Hashtable<>();

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
            messageHandler = new Message();
            if(hasFile){
                bitfield.set(0, numPieces);
            } else{
                bitfield.clear(0, numPieces);
            }
            fileHandler = new FileHandling(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //parse peerInfo and then begin for loop for each server to be connected to
        //create new thread and pass in peerID, port, etc

        //client = new ClientHandler(peerInfo, peerNum);
        //client.start();

        server = new ServerHandler();

        connectionHandler = new ConnectionHandler(numberOfExpectedPeers, totalPeers, peerInfo);
        connectionHandler.start();

        connectionHandler.join();
        server.start();

        connectionHandler.closeConnections();
    }

    /////////////////////////////

    private class ConnectionHandler extends Thread
    {
        ServerSocket listener;
        boolean closed;
        int numConn;
        int expectedConnections;
        int totalConnections;
        ArrayList<String> peerInfo;
        boolean timerStarted;
        TimingHandler TH;

        public ConnectionHandler(int expectedConnections, int totalPeers, ArrayList<String> peerInfo){

            try {

                listener = new ServerSocket(port);

            } catch (Exception e) {
                e.printStackTrace();
            }

            closed = false;
            numConn = 0;
            this.expectedConnections = expectedConnections;
            this.totalConnections = totalPeers - 1;
            this.peerInfo = peerInfo;
            timerStarted = false;
            TH = new TimingHandler();
        }

        public void run(){

            // SENDING CONNECTIONS
            for(int i = 0; i < (totalConnections - expectedConnections); i++){

                try {
                    String address = peerInfo.get(i);
                    int peerID = Integer.parseInt(address.split(":")[0]);
                    String hostname = address.split(":")[1];
                    int port = Integer.parseInt(address.split(":")[2]);
                    Socket requestSocket = new Socket(hostname, port);
                    //socket connected

                    // create remote peer class to store neighbor info
                    remotePeers.put(peerID, new RemotePeer(peerID));
                    connections.put(peerID, requestSocket);
                    blocked.put(peerID, false);
                    System.out.println("Connection created/stored: " + peerid + " and " + peerID);

                    // initiate handshake
                    handshake(requestSocket, peerID);
                    closeHandshake(peerID); // confirm correct

                    InputHandler IH = new InputHandler(peerID);
                    //IH.start();
                    server.addInput(IH);

                    if(!timerStarted){
                        timerStarted = true;
                        TH.start();
                    }

                    if(!bitfieldIsEmpty()){

                        sendBitfield(requestSocket);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // RECEIVING CONNECTIONS
            while(numConn < expectedConnections) {

                try {
                    Socket newConnection = listener.accept();
                    numConn++;

                    int newPeerID = acceptHandshake(newConnection);
                    Log.accepttcpConnection(newPeerID, peerid);
                    // create remote peer class to store neighbor info
                    remotePeers.put(newPeerID, new RemotePeer(newPeerID));
                    connections.put(newPeerID, newConnection);
                    blocked.put(newPeerID, false);
                    System.out.println("Connection created/stored: " + peerid + " and " + newPeerID);

                    // response handshake
                    handshake(newConnection, newPeerID);

                    InputHandler IH = new InputHandler(newPeerID);
                    server.addInput(IH);
                    //IH.start();

                    if(!timerStarted){
                        timerStarted = true;
                        TH.start();
                    }

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

        public void handshake(Socket connection, int pid){

            try {
                HandShakeMessage hMsg = new HandShakeMessage();
                //Utility.SendMsg(mySocket,Msg)//synchronized on the socket level so that only one can write to the socket at the same time

                // SEND HANDSHAKE
                byte[] handshake = hMsg.getByteMessage(hShkHeader, peerid);

                DataOutputStream dOut = new DataOutputStream(connection.getOutputStream());

                dOut.writeInt(handshake.length); // write length of the message
                dOut.write(handshake);           // write the message

                Log.madetcpConnection(peerid, pid);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public int acceptHandshake(Socket connection){

            int newPeerID = -1;

            try {
                HandShakeMessage hMsg = new HandShakeMessage();

                DataInputStream dIn = new DataInputStream(connection.getInputStream());

                int length = dIn.readInt();
                if (length > 0) {

                    byte[] message = new byte[length];
                    dIn.readFully(message, 0, message.length); // read the message

                    newPeerID = hMsg.accept(message);

                    return newPeerID;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return newPeerID;
        }

        public void closeHandshake(int pid){

            HandShakeMessage hMsg = new HandShakeMessage();

            try {
                DataInputStream dIn = new DataInputStream(connections.get(pid).getInputStream());

                int length = dIn.readInt();
                if (length > 0) {

                    byte[] handshake = new byte[length];
                    dIn.readFully(handshake, 0, handshake.length); // read the message

                    int responseID = hMsg.accept(handshake);
                    Log.accepttcpConnection(pid, peerid);

                    if (responseID != pid) {

                        System.out.println("Error: incorrect peerID");
                        // TODO exit thread, connection incorrect
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void sendBitfield(Socket connection){

            try {
                DataOutputStream dOut = new DataOutputStream(connection.getOutputStream());

                byte[] msg = messageHandler.getBitfieldMessage(bitfield);

                dOut.write(msg);           // write the message

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean bitfieldIsEmpty(){
            if(bitfield.toByteArray().length == 0){
                return true;
            }else{
                return false;
            }
        }
    }

    private class TimingHandler extends Thread
    {

        TimingHandler(){

        }

        public void run() {

            Timer optimisticUnchokingIntervalTimer = new Timer();
            optimisticUnchokingIntervalTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    optimisticUnchokingInterval();
                }
            }, 0, getOptimisticUnchokingInterval() * 1000);

            Timer unchokingIntervalTimer = new Timer();
            unchokingIntervalTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(timingTaken) {
                        unchokingInterval();
                    }
                }
            }, 0, getUnchokingInterval() * 1000);
        }
    }

/////////////////////////////

    private class InputHandler extends Thread
    {
        Socket connection;
        DataInputStream dIn;
        DataOutputStream dOut;
        boolean active;
        int remoteID;
        long timestamp1;
        long timestamp2;

        InputHandler(int remoteID){
            this.remoteID = remoteID;
            this.connection = connections.get(remoteID);
            active = true;

        }

        public void run() {
            try {

                if(!bitfieldIsEmpty()){

                    sendBitfield(connection);
                }

                dIn = new DataInputStream(connection.getInputStream());
                dOut = new DataOutputStream(connection.getOutputStream());
                RemotePeer neighbor = remotePeers.get(remoteID);
                int index;

                while (active) { //have a better termination condition!
                    if(!blocked.get(remoteID) && dIn.available() != 0){
                        int message_length = dIn.readInt();
                        System.out.println("incoming message length: " + message_length);
                        byte message_type = dIn.readByte();
                        System.out.println(message_type);
                        byte[] message_payload;

                        int counter = 0;
                            for(RemotePeer friend : remotePeers.values()){
                                if(friend.hasBitfield()){
                                    System.out.println("Peer Cardinality: " + friend.getBitfield().cardinality());
                                    if(friend.getBitfield().cardinality() == numPieces){
                                        counter++;
                                        System.out.println("Count: " + counter);
                                    }
                                }
                            }

                        if(counter == 3 && bitfield.cardinality() == numPieces){
                            ALLDONE();
                            close();
                        }


                        switch (message_type) {
                            //if we need to send out a message in response spawn a new thread and pass the reply message to it. Let it call send on the outputstream and die!
                            case 0:
                                chokeMe(remoteID, peerid);
                                break;
                            case 1:
                                unchokeMe(remoteID, peerid);

                                index = findNewPieceIndex(remoteID);
                                byte[] requestMsg = messageHandler.getRequestMessage(index);
                                writeMessage(dOut, remoteID, requestMsg);
                                break;
                            case 2:
                                interestedMe(remoteID, peerid);
                                break;
                            case 3:
                                notInterestedMe(remoteID, peerid);
                                break;
                            case 4:
                                index = dIn.readInt();
                                neighbor.updateBitfield(index, numPieces);
                                Log.have(peerid, remoteID, index);

                                //determine to send interested message or not
                                if(!neighbor.isInterested() && !hasFile){ // if not interested

                                    if(checkInterest(remoteID)){

                                        System.out.println(peerid + " is now interested in " + remoteID);
                                        byte[] msg = messageHandler.getInterestedMessage();
                                        neighbor.interested();
                                        writeMessage(dOut, remoteID, msg);
                                    }
                                }
                                break;
                            case 5:

                                // receive bitfield, store in neighbor
                                message_payload = new byte[message_length - 1];
                                dIn.readFully(message_payload);
                                neighbor = remotePeers.get(remoteID);
                                BitSet initBitfield = BitSet.valueOf(message_payload);
                                neighbor.initializeBitfield(initBitfield);
                                System.out.println(initBitfield.toString());
                                System.out.println(neighbor.getBitfield().toString());
                                System.out.println("bitfield message received from " + remoteID);

                                // check if interested
                                if(checkInterest(remoteID) && !hasFile){

                                    byte[] msg = messageHandler.getInterestedMessage();

                                    writeMessage(dOut, remoteID, msg);
                                }
                                break;
                            case 6:
                                timestamp1 = System.currentTimeMillis(); // TODO check to make sure not overwritten
                                //received a request message for a certain piece index
                                index = dIn.readInt();
                                //we have the piece contents below
                                // if client not choked, send piece
                                if (!neighbor.isChoked()) {
                                    //check if neighbor is preferred
                                    //check if optimistically unchoked neighbor changed
                                    //because peer might've been choked during that change
                                    byte[] piece = getPiece(index);
                                    //we parse the data into the pieceMsg format
                                    byte[] pieceMsg = messageHandler.getPieceMessage(index, piece);
                                    //send it out to our remote peer
                                    writeMessage(dOut, remoteID, pieceMsg);
                                    System.out.println(peerid + ": Request for piece " + index + " from " + remoteID);
                                }
                                break;
                            case 7:
                                index = dIn.readInt();
                                message_payload = new byte[message_length - 5]; //this is the piece contents
                                dIn.readFully(message_payload);
                                addPiece(index, message_payload);
                                Log.downloadingPiece(peerid, remoteID, index, fileHandler.numPiecesPeerHas());

                                if(index == 304) {
                                    System.out.println("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
                                    System.out.println("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
                                    System.out.println("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
                                    System.out.println("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
                                    System.out.println("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
                                    System.out.println("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
                                    System.out.println("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
                                    System.out.println("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
                                    System.out.println("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
                                    System.out.println("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
                                    System.out.println("Last Piece requested. Piece size = " + message_payload.length);
                                    System.out.println(getPiece(304).length);
                                }


                                //send have message to other remote peers to update their bitfields of what we have
                                for (Integer key : connections.keySet()) {
                                    DataOutputStream out = new DataOutputStream(connections.get(key).getOutputStream());
                                    byte[] haveMsg = messageHandler.getHaveMessage(index);
                                    writeMessage(out, key, haveMsg);
                                }

                                // if no longer interested
                                if(!checkInterest(remoteID)){
                                    neighbor.notInterested();

                                    // send not interested message
                                    byte[] msg = messageHandler.getNotInterestedMessage();
                                    writeMessage(dOut, remoteID, msg);

                                }else { // else, send request message

                                    index = findNewPieceIndex(remoteID);

                                    byte[] msg = messageHandler.getRequestMessage(index);
                                    writeMessage(dOut, remoteID, msg);
                                }
                                timestamp2 = System.currentTimeMillis();
                                calculateTimeDiff(remoteID);
                                break;
                        }
                    }else{
                        //System.out.println("NO INPUT");
                    }
                }
            } catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        public void close(){
            active = false;
        }


        //case functions
        public void interestedMe(int neighborID, int peerid){
            RemotePeer neighbor = remotePeers.get(neighborID);
            neighbor.interestedInMe();
            //the neighbor that choked us will now save that we are choked
            try{
                Log.interested(peerid, neighborID);
            }catch(IOException e){
                System.out.println(e.toString());
            }
        }
        public void notInterestedMe(int neighborID, int peerid) {
            RemotePeer neighbor = remotePeers.get(neighborID);
            neighbor.notInterestedInMe();
            try {
                Log.uninterested(peerid, neighborID);
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }

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

        public void addPiece(int index, byte[] piece){
            fileHandler.setPiece(index, piece);
            bitfield.set(index);
        }

        public boolean checkInterest(int pid){

            RemotePeer neighbor = remotePeers.get(pid);
            BitSet comparison = neighbor.getBitfield();

            for(int i = 0; i < numPieces; i++){
                if(!bitfield.get(i) && comparison.get(i)){ // if piece is in remote peer and not calling peer, interested

                    neighbor.interested();
                    System.out.println(peerid + " interested in " + pid);
                    return true;
                }
            }
            neighbor.notInterested();
            System.out.println(peerid + " not interested in " + pid);
            return false;
        }

        public void sendBitfield(Socket connection){

            try {
                DataOutputStream dOut = new DataOutputStream(connection.getOutputStream());

                byte[] msg = messageHandler.getBitfieldMessage(bitfield);

                dOut.write(msg);           // write the message

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean bitfieldIsEmpty(){
            if(bitfield.toByteArray().length == 0){
                return true;
            }else{
                return false;
            }
        }

        public int findNewPieceIndex(int peerid) {
            RemotePeer neighbor = remotePeers.get(peerid);
            return neighbor.getRandomPieceWanted(bitfield, numPieces);
        }

        public long getTimeStamp1(){
            return timestamp1;
        }
        public long getTimeStamp2(){
            return timestamp2;
        }

        public Hashtable<Integer, Float> calculateTimeDiff(int remoteID){
            ArrayList<Float> times = new ArrayList<Float>();
            long time1 = getTimeStamp1();
            long time2 = getTimeStamp2();
            float difference = Math.abs(time2-time1);
            times.add(difference);
            float sum = 0;
            for(Float time : times){
                sum += time;
            }
            float avg = sum/times.size();
            peerTimes.put(remoteID, avg);

            if(!timingTaken){
                timingTaken = true;
            }

            return peerTimes;
        }
    }


/////////////////////////////
    
    private class ServerHandler extends Thread
    {
        Socket connection;
        boolean initializing;
        ArrayList<InputHandler> inputs;

        public ServerHandler () throws IOException
        {
            inputs = new ArrayList<>();
        }
        
        public void run() 
        {
            try
            {
                System.out.println("The server is running.");

                for(InputHandler IH : inputs){
                    IH.start();
                }

                try 
                {

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

        public void addInput(InputHandler IH){
            inputs.add(IH);
        }

        public void doStuff(){

            try {

                Set<Integer> keys = remotePeers.keySet();
                for(Integer pid : keys) {
                    DataOutputStream dOut = new DataOutputStream(connections.get(pid).getOutputStream());

                    byte[] msg = messageHandler.getChokeMessage();

                    blockPeerInput(pid);
                    dOut.write(msg);
                    unblockPeerInput(pid);


                    msg = messageHandler.getUnchokeMessage();

                    blockPeerInput(pid);
                    dOut.write(msg);
                    unblockPeerInput(pid);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
/////////////////////////////
    
    private class ServerRequestHandler extends Thread //rename to request handler
    {
        private Socket connection;
        private DataInputStream dIn;   //stream read from the socket
        private DataOutputStream dOut;    //stream write to the socket
        private int clientID;
        private boolean initializing;

        public ServerRequestHandler(Socket connection, boolean initializing)
        {
                    this.connection = connection;
                    this.initializing =initializing;
        }

        public void run() {
        }
    }

///////////////////////////

    private class ClientRequestHandler extends Thread //rename to request handler
    {
        private Socket connection;
        private int serverID;
        private boolean initializing;
        private DataInputStream dIn;   //stream read from the socket
        private DataOutputStream dOut;    //stream write to the socket

        public ClientRequestHandler(Socket connection, int serverID, boolean initializing)
        {
            this.connection = connection;
            this.serverID = serverID;
            this.initializing = initializing;
        }

        public void run()
        {

        }
    }


    /////////////////////////////


    private class ClientHandler extends Thread
    {
        //ASSUME YOU HAVE THE INFORMATION YOU NEED (IP , ID , PORT , ETC including the socket.)
        Socket requestSocket;
    	int pNum;
        ArrayList<String> peerInfo;
        String address;
        boolean initializing;

        public ClientHandler(ArrayList<String> peerInfo, int pNum) throws IOException
        {
                this.peerInfo = peerInfo;
                this.pNum = pNum;
        }
        
        public void run() 
        {
            try
            {
            	address = peerInfo.get(pNum);
            	int peerID = Integer.parseInt(address.split(":")[0]);
   	 			String hostname = address.split(":")[1];
   	            int port = Integer.parseInt(address.split(":")[2]);
   	    		requestSocket = new Socket(hostname, port);
   	    		//socket connected

                ClientRequestHandler RH = new ClientRequestHandler(requestSocket, peerID, initializing);
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
    public int getUnchokingInterval(){
        return unchokingInterval;
    }
    public int getOptimisticUnchokingInterval(){
        return optimisticUnchokingInterval;
    }

    public void optimisticUnchokingInterval(){
        System.out.println("finding optimistic peer to unchoke");

        ArrayList<Integer> possibleOptimisticConnections = new ArrayList<Integer>();
        ArrayList<Integer> keys = new ArrayList<>();
        for(Integer key : connections.keySet()){
            RemotePeer neighbor = remotePeers.get(key);
            if(neighbor.isChoked() && neighbor.isInterestedInMe()){
                possibleOptimisticConnections.add(neighbor.getID());
            }
            keys.add(key);
        }
        Random rando = new Random();
        if(possibleOptimisticConnections.size() > 0) {
            int randoChosenOne = rando.nextInt(possibleOptimisticConnections.size());
            int pid = possibleOptimisticConnections.get(randoChosenOne);

            if (optUnchoked == null || pid != optUnchoked.getID()) {

                if(optUnchoked != null){
                    byte[] chokeMsg = messageHandler.getChokeMessage();
                    try {

                        if (remotePeers.get(pid).isChoked()) {
                            System.out.println("rando: " + pid);
                            DataOutputStream output = new DataOutputStream(connections.get(pid).getOutputStream());

                            writeMessage(output, pid, chokeMsg);
                            remotePeers.get(pid).choke();
                            Log.choking(peerid, pid);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //SEND UNCHOKE MESSAGE
                byte[] unchokeMsg = messageHandler.getUnchokeMessage();
                try {

                    if (remotePeers.get(pid).isChoked()) {
                        System.out.println("rando: " + pid);
                        DataOutputStream output = new DataOutputStream(connections.get(pid).getOutputStream());
                        remotePeers.get(pid).unchoke();
                        writeMessage(output, pid, unchokeMsg);
                        Log.changeOptimisticallyUnchokedNeighbor(peerid, pid);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void unchokingInterval(){
        int[] preferredNeighbors = new int[numberOfPreferredNeighbors];
        float min;
        int minPeer;

        // Make list of all neighbors
        for(Integer id : peerTimes.keySet()) {
            remotePeers.get(id).notPreferred();
        }
        for(int i = 0; i < numberOfPreferredNeighbors; i++){

            minPeer = 1001;
            min = Integer.MAX_VALUE;

            for(Integer id : peerTimes.keySet()){

                float time = peerTimes.get(id)/1000000000;

                RemotePeer neighbor = remotePeers.get(id);

                System.out.println("TIME: " + time + " MIN: " + min);

                if (time < min && !neighbor.isPreferred()) {
                    min = time;
                    minPeer = neighbor.getID();
                }
            }
            preferredNeighbors[i] = minPeer;

            remotePeers.get(minPeer).preferred();
        }

        //if peer is interested and choked and preferred
        //send unchoke message to them

        for (RemotePeer neighbor : remotePeers.values()) {
            if (neighbor.isPreferred() && neighbor.isChoked() && neighbor.isInterestedInMe()) {
                byte[] unchokeMsg = messageHandler.getUnchokeMessage();
                try {
                    DataOutputStream output = new DataOutputStream(connections.get(neighbor.getID()).getOutputStream());
                    output.write(unchokeMsg);
                    neighbor.unchoke();
                    Log.unchoking(peerid, neighbor.getID());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if(!neighbor.isPreferred() && !neighbor.isChoked()){
                try {
                    byte[] chokeMsg = messageHandler.getChokeMessage();
                    DataOutputStream output = new DataOutputStream(connections.get(neighbor.getID()).getOutputStream());
                    output.write(chokeMsg);
                    neighbor.choke();
                    Log.choking(peerid, neighbor.getID());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try{
            Log.changePreferred(peerid, preferredNeighbors);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void ALLDONE(){
        fileHandler.writeFile();
    }

    public void blockPeerInput(int pid){
        blocked.put(pid, new Boolean(true));
    }

    public void unblockPeerInput(int pid){
        blocked.put(pid, new Boolean(false));
    }

    public void writeMessage(DataOutputStream dOut, int pid, byte[] msg){

        try {
            blockPeerInput(pid);
            dOut.write(msg);
            unblockPeerInput(pid);

        } catch(IOException e){
            e.printStackTrace();
        }
    }

    // TODO close sockets
}