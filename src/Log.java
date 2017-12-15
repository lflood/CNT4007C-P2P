import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;

public class Log {
    public static void madetcpConnection(int peerid, int remote_peerid) throws IOException{
        Date date = new Date();
        String logger = date.toString() + ": Peer " + peerid + " has made a connection to peer " + remote_peerid + ".\n";
        saveLog(peerid, logger);
    }
    public static void accepttcpConnection(int peerid, int remote_peerid) throws IOException{
        Date date = new Date();
        String logger = date.toString() + ": Peer " + peerid + " is connected from peer " + remote_peerid + ".\n";
        saveLog(peerid, logger);
    }
    public static void choking(int peerid, int remote_peerid) throws IOException{
        Date date = new Date();
        String logger = date.toString() + ": Peer " + peerid + " is choked by peer " + remote_peerid + ".\n";
        saveLog(peerid, logger);
    }
    public static void unchoking(int peerid, int remote_peerid) throws IOException{
        Date date = new Date();
        String logger = date.toString() + ": Peer " + peerid + " is unchoked by peer " + remote_peerid + ".\n";
        saveLog(peerid, logger);
    }
    public static void interested(int peerid, int remote_peerid) throws IOException{
        Date date = new Date();
        String logger = date.toString() + ": Peer " + peerid + " received the 'interested' message from peer " + remote_peerid + ".\n";
        saveLog(peerid, logger);
    }
    public static void uninterested(int peerid, int remote_peerid) throws IOException{
        Date date = new Date();
        String logger = date.toString() + ": Peer " + peerid + " received the 'not interested' message from peer " + remote_peerid + ".\n";
        saveLog(peerid, logger);
    }
    public static void changePreferred(int peerid, int[] preferredNeighbors) throws IOException{
        Date date = new Date();
        String logger = date.toString() + ": Peer " + peerid + " has the preferred neighbors " + Arrays.toString(preferredNeighbors) + ".\n";
        saveLog(peerid, logger);
    }
    public static void changeOptimisticallyUnchokedNeighbor(int peerid, int remote_peerid) throws IOException{
        Date date = new Date();
        String logger = date.toString() + ": Peer " + peerid + " has the optimistically unchoked neighbor " + remote_peerid + ".\n";
        saveLog(peerid, logger);
    }
    public static void have(int peerid, int remote_peerid, int pieceIndex){
        Date date = new Date();
        String logger = date.toString() + ": Peer " + peerid + " received the 'have' message from peer " + remote_peerid + "for the piece " + pieceIndex + ".\n";
        saveLog(peerid, logger);
    }
    public static void downloadingPiece(int peerid, int remote_peerid, int pieceIndex, int numPiecesPeerHas){
        Date date = new Date();
        String logger = date.toString() + ": Peer " + peerid + " has downloaded the piece " + pieceIndex + " from " + remote_peerid + ". Now the number of pieces it has is " + numPiecesPeerHas + ".\n";
        saveLog(peerid, logger);
        //calculate if we have all the pieces, then call completeDownload if we do
        //otherwise call completeDownload in peer when we call ALLDONE()
    }
    public static void completeDownload(int peerid){
        Date date = new Date();
        String logger = date.toString() + ": Peer " + peerid + " has downloaded the complete file." + ".\n";
        saveLog(peerid, logger);
    }
    public static void saveLog(int peerid, String text){
        System.out.println(text);

        try{
            File log = new File("log_peer_" + peerid + ".log");
            if (!log.exists()){
                log.createNewFile();
            }
            PrintWriter out = new PrintWriter(new FileWriter(log, true));
            out.append(text);
            out.close();
        } catch(IOException e){
        }

    }
}
