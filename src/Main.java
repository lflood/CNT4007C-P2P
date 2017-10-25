import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;

public class Main {
    public static void main(String[] args) {
        //accepted peers are added to peerInfo hashTable
        Hashtable<Integer, String> peerInfo = new Hashtable<Integer, String>();
        //String checkID = null;
        /*if(args.length > 0) {
            checkID = args[0];
        } else{
            System.out.println("Argument needed for peerID");
            return;
        }*/

        //Read from peerInfo.cfg file and add the peers into hashtable
        //format:
        //[peer ID] [host name] [listening port] [has file or not]
        String str;
            try{
                BufferedReader line = new BufferedReader(new FileReader("src/PeerInfo.cfg"));
                System.out.println("I am  printing out: ");
                while ((str = line.readLine()) != null) {
                    String[] tokens = str.split("\\s+"); //splits the string contents between any whitespace
                    int peerID = Integer.valueOf(tokens[0]);
                    String hostname = tokens[1];
                    int port = Integer.valueOf(tokens[2]);
                    boolean hasFile = false;
                    if(tokens[3].equals("1")) {
                        hasFile = true;
                    }
                    System.out.println(peerID + " " + hostname + " " + port);

                    peerInfo.put(peerID, hostname + ":" + port);
                    System.out.println("peerInfo is: " + peerInfo);
                    //if(checkID != null && checkID.equals(tokens[0])){
                        //create Peer object - need to create Peer class first
                        Peer peer = new Peer(peerID, port, hasFile);
                        //connect peer by bootsrapping
                    //}
                }
                line.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
    }
}