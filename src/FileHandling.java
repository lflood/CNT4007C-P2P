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
    private int currentTotalPiecesPeerHas;

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

            byte [] pieceData;

            if((i*peer.pieceSize + peer.pieceSize) > fileSize){

                pieceData = new byte[fileSize - (i * peer.pieceSize)];


            }else{
                pieceData = new byte[peer.pieceSize];
            }

            for(int j = 0; j < pieceData.length; j++){
                int fileIndex = i*peer.pieceSize;
                fileIndex += j;
                pieceData[j] = fileContents[fileIndex];
            }
            pieces.add(new Piece(pieceData));
        }

    }
    public void writeFile(){
        int numPieces = peer.getNumPieces();
        int fileSize = peer.getFileSize();

        byte[] data = pieces.get(304).getPieceData();

        byte [] fileDone = new byte [fileSize];
        int curr = 0;
        for(int i = 0; i < numPieces; i++){
            byte [] temp = pieces.get(i).getPieceData();
            for(int j = 0; j < temp.length; j++){
                fileDone[curr] = temp[j];
                curr++;
            }
        }
        try{
            FileOutputStream fileOutStream = new FileOutputStream("./peer_"+peer.getPeerID()+"/"+peer.fileName);
            fileOutStream.write(fileDone);
            fileOutStream.close();
        }catch(FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
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
    public void setPiece(int index, byte [] piece) {

        if(index == 304) {
            System.out.println("Setting last piece to size: " + piece.length);
        }
        this.pieces.set(index, new Piece(piece));
        currentTotalPiecesPeerHas++;
    }
    public int numPiecesPeerHas(){
        return currentTotalPiecesPeerHas;
    }

}
