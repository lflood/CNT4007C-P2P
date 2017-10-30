package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;

public class Main {
    public static void main(String[] args) {
        Hashtable<Integer, String> peerInfo = new Hashtable<Integer, String>();
        String checkID = null;
        if(args.length > 0) {
            checkID = args[0];
        } else{
            System.out.println("Argument needed for peerID");
            return;
        }

        //Read from peerInfo.cfg file and add the peers into hashtable
        //format:
        //[peer ID] [host name] [listening port] [has file or not]
        String str;
        int counter = 0;
        int portPass = 0;
        int peerIDPass = 0;
        boolean hasFilePass = false;
            try{
                BufferedReader line = new BufferedReader(new FileReader("src/PeerInfo.cfg"));
                while ((str = line.readLine()) != null) {
                    String[] tokens = str.split("\\s+"); //splits the string contents between any whitespace
                    int peerID = Integer.valueOf(tokens[0]);
                    String hostname = tokens[1];
                    int port = Integer.valueOf(tokens[2]);
                    boolean hasFile = false;
                    peerInfo.put(peerID, hostname + ":" + port);
                    if(tokens[3].equals("1")) {
                        hasFile = true;
                    }


                    if(checkID != null && checkID.equals(tokens[0]))
                    {
                    	portPass = port;
                    	peerIDPass = peerID;
                    	hasFilePass = hasFile;
                    }
                    counter++;
                    	
                    	
                    	//peerInfo contains peerID, hostname, and port
                    	//could potentially create validity tests for comparison
                    	//hash map is only as long as the while loop so far. Sorry for the terrible comment. 
                    }
                
                Peer peer = new Peer(peerIDPass, portPass, hasFilePass, peerInfo, counter);
                line.close();  
                           


            }
            catch(Exception e) {
                e.printStackTrace();
            }
    }
}