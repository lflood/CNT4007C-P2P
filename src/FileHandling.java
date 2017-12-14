import java.nio.file.Files;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;


public class FileHandling {
    Peer peer;
    private int fileSize;
    private int pieceSize;
    private int numPieces;

    private ArrayList<Piece> pieces;

    public FileHandling(Peer peer){
        this.peer = peer;
        fileSize = peer.getFileSize();
        pieceSize = peer.getPieceSize();
        numPieces = peer.getNumPieces();

        if(fileSize%pieceSize !=0 ? true:false){
            numPieces = fileSize/pieceSize + 1;
        } else{
            numPieces = fileSize/pieceSize;
        }

        if(peer.getHasFile()){
            File from = new File("./src/"+peer.fileName);
            System.out.println("filename is: " + peer.fileName);
            File to = new File("./peer_"+peer.getPeerID()+"/"+peer.fileName);
            try{
                Files.copy(from.toPath(), to.toPath());
            } catch(IOException e){
                e.printStackTrace();
            }
        }

        if(peer.getHasFile()) {
            pieces = new ArrayList<Piece>();
            splitDataIntoPieces(numPieces);
        }else{
            pieces = new ArrayList<Piece>();
            for (int i = 0; i < numPieces; i++) {
                Piece piece = new Piece();
                pieces.add(piece);
            }
        }

    }
    private void splitDataIntoPieces(int numPieces){
        FileInputStream fileInStream = null;

        byte[] fileContents;
        fileContents = new byte[peer.fileSize];

        try{
            fileInStream = new FileInputStream(new File("./peer_"+peer.getPeerID()+"/"+peer.fileName));
            fileInStream.read(fileContents);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                fileInStream.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        for(int i = 0; i < numPieces; i++){
            byte [] pieceData = new byte[peer.pieceSize];
            for(int j = 0; j < peer.pieceSize; j++){
                int fileIndex = i*pieceData.length;
                fileIndex += j;
                if(fileIndex >= fileContents.length){
                    pieceData[j] = (byte)0;
                }else{
                    pieceData[j] = fileContents[fileIndex];
                }
            }
            pieces.add(new Piece(pieceData));
        }

    }
    private void writingFile(){

    }

    public ArrayList<Piece> getPieces(){
        return pieces;
    }
    public void setPieces(ArrayList<Piece> pieces){
        this.pieces = pieces;
    }
    public byte [] getPiece(int index){
        return pieces.get(index).getPieceData();
    }
    public void setPiece(int index, Piece piece) {
        this.pieces.set(index, piece);
    }

}
