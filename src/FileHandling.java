import java.nio.file.Files;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import static com.sun.deploy.cache.Cache.copyFile;

public class FileHandling {
    Peer peer;
    private ArrayList<Piece> pieces;
    public FileHandling(){
        int fileSize = peer.fileSize;
        int pieceSize = peer.pieceSize;
        int numPieces = peer.numPieces;

        if(fileSize%pieceSize !=0 ? true:false){
            numPieces = fileSize/pieceSize + 1;
        } else{
            numPieces = fileSize/pieceSize;
        }

        if(peer.hasFile){
            File from = new File(peer.fileName);
            File to = new File("./peer_"+peer.peerid+"/"+peer.fileName);
            try{
                Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch(IOException e){
                e.printStackTrace();
            }
        }

        if(peer.hasFile){
            pieces = new ArrayList<Piece>();

            for(int i = 0; i < numPieces; i++){
                pieces.add(new Piece());
            }
        }

    }

}
