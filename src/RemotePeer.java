import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

public class RemotePeer {

    private BitSet bitfield;
    private int ID;
    private boolean choked;
    private boolean interested;
    private boolean bitfieldInitialized;

    public RemotePeer(int ID) {

        this.ID = ID;
        this.choked = true;
        this.interested = false;
        this.bitfield = null;
        bitfieldInitialized = false;
    }

    public void initializeBitfield(BitSet bitfield){

        if(!bitfieldInitialized) {
            bitfieldInitialized = true;

            this.bitfield = bitfield;
        }else{

            System.out.println("Error: bitfield already initialized");
        }
    }

    public int getRandomPieceWanted(BitSet comparisonSet){

        ArrayList<Integer> indexList = new ArrayList<>();

        int numPieces = bitfield.size();

        for(int i = 0; i < numPieces; i++){

            if(bitfield.get(i) && !comparisonSet.get(i)){ // if piece is in remote peer and not calling peer, add to index list

                indexList.add(i);
            }
        }

        Random random = new Random();

        int index = random.nextInt(indexList.size());

        return index;
    }

    public boolean hasBitfield(){
        return bitfieldInitialized;
    }

    public boolean isChoked(){
        return choked;
    }

    public boolean isInterested() {
        return interested;
    }

    public byte[] getByteMessage(String header, int id){

        byte[] headerBytes = header.getBytes();
        byte[] zeroBytes = new byte [10];
        byte[] idBytes = ByteBuffer.allocate(4).putInt(id).array();


        byte[] result = null;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(headerBytes);
            outputStream.write(zeroBytes);
            outputStream.write(idBytes);

            result = outputStream.toByteArray();
        } catch (IOException ie){
            ie.printStackTrace();
        }

        return result;
    }

    public void choke(){
        choked = true;
    }

    public void unchoke(){
        choked = false;
    }

    public void setInterested(){
        interested = true;
    }

    public void setNotInterested(){
        interested = false;
    }

    public void updateBitfield(int index){

        // set index location to having a piece
    }
}
