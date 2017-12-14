public class Piece {
    private byte [] data;
    private boolean full;
    private boolean requested = false;

    public Piece(){
        data = new byte[Peer.pieceSize];
        full = false;
    }

    public Piece(byte[] data){
        this.data = data;
        full = true;
    }
    public byte[] getPieceData(){
        if(data != null){
            return data;
        }
        else{
            return null;
        }
    }
    public void setPieceData(byte[] data){
        this.data = data;
        full = true;
    }
    public boolean isRequested(){
        return requested;
    }
    public void request(){
        requested = true;
    }
    public boolean isFull(){
        return full;
    }
}

