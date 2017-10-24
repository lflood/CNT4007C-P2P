import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Peer {
    //private variables so it can only be accessed within this class
    private byte[] bitfield;
    private int peerid;
    private int numberOfFilePieces;

    //Common.cfg contents
    int numberOfPreferredNeighbors;
    int unchokingInterval;
    int optimisticUnchokingInterval;
    String fileName;
    int fileSize;
    int pieceSize;

    public Peer(int peerid, int port, boolean hasFile) {
        this.peerid = peerid;
        //create peer_ID files
        new File("peer_" + peerid).mkdir();

        //create new server thread

        //read and add file contents to peer variables
        String str;
        String[] config = new String[6];
        try {
            BufferedReader line = new BufferedReader(new FileReader("src/Common.cfg"));
            System.out.println("I am  printing out the common config contents: ");

            //save int values of the config file into our string array Config
            //which is the second value of each line aka token[1]
            int i = 0;
            while ((str = line.readLine()) != null){
                String[] tokens = str.split("\\s+");
                config[i] = tokens[1];
                i++;
            }
            line.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}