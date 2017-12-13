import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RemotePeer {

    private byte[] bitfield;
    private int ID;
    private boolean choked;
    private boolean interested;
    private boolean bitfieldInitialized;

    //might want to pass in port maybe
    public RemotePeer(int ID) {

        this.ID = ID;
        this.choked = true;
        this.interested = false;
        this.bitfield = null;
        bitfieldInitialized = false;
    }
    public int getID(){
        return ID;
    }
    public boolean checkChoke(){
        return choked;
    }
    public boolean checkInterested(){
        return interested;
    }
    public byte [] getBitfield(){
        return bitfield;
    }


    public void initializeBitfield(byte[] bitfield){

        if(!bitfieldInitialized) {
            bitfieldInitialized = true;

            this.bitfield = bitfield;
        }else{

            System.out.println("Error: bitfield already initialized");
        }
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
