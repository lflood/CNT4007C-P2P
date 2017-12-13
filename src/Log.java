import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
