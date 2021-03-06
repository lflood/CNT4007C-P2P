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
    private boolean chokingMe;
    private boolean interestedInMe;
    private boolean preferred;


    //might want to pass in port maybe
    public RemotePeer(int ID) {

        this.ID = ID;
        this.choked = true;
        this.interested = false;
        this.interestedInMe = false;
        this.bitfield = null;
        this.chokingMe = true;
        bitfieldInitialized = false;
        this.preferred = false;
    }
    public int getID(){
        return ID;
    }

    public BitSet getBitfield(){
        synchronized (this) {
            return bitfield;
        }
    }

    public void initializeBitfield(BitSet bitfield){

        if(!bitfieldInitialized) {
            bitfieldInitialized = true;

            this.bitfield = bitfield;
        }else{

            System.out.println("Error: bitfield already initialized");
        }
    }

    public void updateBitfield(int index, int numPieces){

        if(!bitfieldInitialized){

            BitSet bitSet = new BitSet();
            bitSet.set(0, numPieces);
            initializeBitfield(bitSet);
            bitfieldInitialized = true;
        }

        bitfield.set(index);
    }

    public int getRandomPieceWanted(BitSet comparisonSet, int numPieces){

        if(!bitfieldInitialized){

            BitSet bitSet = new BitSet();
            bitSet.set(0, numPieces);
            initializeBitfield(bitSet);
            bitfieldInitialized = true;
        }

        ArrayList<Integer> indexList = new ArrayList<>();

        for(int i = 0; i < numPieces; i++){
            if(bitfield.get(i) && !comparisonSet.get(i)){ // if piece is in remote peer and not calling peer, add to index list
                indexList.add(i);
            }
        }

        Random random = new Random();

        int listIndex;
        if(indexList.size() > 0) {
            listIndex = random.nextInt(indexList.size());
        }else{
            return 0;
        }
        return indexList.get(listIndex);
    }

    public boolean hasBitfield(){
        return bitfieldInitialized;
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

    //choke variables for servers of other peers to access
    public void choke(){ choked = true; }

    public void unchoke(){
        choked = false;
    }

    public boolean isChoked(){
        return choked;
    }

    //choke variables for clients of other peers to access
    public void isChokingMe(){
        chokingMe = true;
    }
    public void isUnchokingMe(){
        chokingMe = false;
    }
    public boolean amIChoked(){
        return chokingMe;
    }

    //interested variables for servers of other peers to access
    public boolean isInterestedInMe(){
        return interestedInMe;
    }
    public void notInterestedInMe(){
        interestedInMe = false;
    }

    public void interestedInMe(){
        interestedInMe = true;
    }

    //interested variables for clients of other peers to access
    public void interested(){
        interested = true;
    }

    public void notInterested(){
        interested = false;
    }

    public boolean isInterested(){
        return interested;
    }

    public void preferred(){
        preferred = true;
    }
    public void notPreferred(){
        preferred = false;
    }
    public boolean isPreferred(){
        return preferred;
    }
}
