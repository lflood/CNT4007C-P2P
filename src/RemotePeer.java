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
        this.bitfield = null;
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

    public void hasPiece(int index){
        bitfield.set(index);
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
    public void isInterestedMe(){
        interestedInMe = true;
    }
    public void isNotInterestedMe(){
        interestedInMe = false;
    }
    public boolean amIInterested(){
        return interestedInMe;
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
