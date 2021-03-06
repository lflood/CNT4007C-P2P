import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;

public class Message {
	String peer;
	int payLoadLength;
	int type;
	byte [] payload;


	public Message() {

	}
	//have, bitfield, request, and piece message constructor
	public Message(String peer, int payLoadLength, int type, byte [] payload){
		this.peer = peer;
		this.payLoadLength = payLoadLength;
		this.type = type;
		this.payload = payload;
	}
	//choke, unchoke, interested, not interested message constructor
	public Message(String peer, int payLoadLength, int type){
		this.peer = peer;
		this.payLoadLength = payLoadLength;
		this.type = type;
	}

	public String getPeerID(){
		return peer;
	}
	public int getPayLoadLength(){
		return payLoadLength;
	}
	public int getType(){
		return type;
	}


	public byte[] getChokeMessage(){

		int messageLength = 1;

		byte typeBytes = 0;
		byte[] lengthBytes = ByteBuffer.allocate(4).putInt(messageLength).array();

		byte[] result = null;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			outputStream.write(lengthBytes);
			outputStream.write(typeBytes);

			result = outputStream.toByteArray();
		} catch (IOException ie){
			ie.printStackTrace();
		}

		return result;
	}

	public byte[] getUnchokeMessage(){

		int messageLength = 1;

		byte typeBytes = 1;
		byte[] lengthBytes = ByteBuffer.allocate(4).putInt(messageLength).array();

		byte[] result = null;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			outputStream.write(lengthBytes);
			outputStream.write(typeBytes);

			result = outputStream.toByteArray();
		} catch (IOException ie){
			ie.printStackTrace();
		}

		return result;
	}

	public byte[] getInterestedMessage(){

		int messageLength = 1;

		byte typeBytes = 2;
		byte[] lengthBytes = ByteBuffer.allocate(4).putInt(messageLength).array();

		byte[] result = null;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			outputStream.write(lengthBytes);
			outputStream.write(typeBytes);

			result = outputStream.toByteArray();
		} catch (IOException ie){
			ie.printStackTrace();
		}

		return result;
	}

	public byte[] getNotInterestedMessage(){

		int messageLength = 1;

		byte typeBytes = 3;
		byte[] lengthBytes = ByteBuffer.allocate(4).putInt(messageLength).array();

		byte[] result = null;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			outputStream.write(lengthBytes);
			outputStream.write(typeBytes);

			result = outputStream.toByteArray();
		} catch (IOException ie){
			ie.printStackTrace();
		}

		return result;
	}

	public byte[] getHaveMessage(int index){

		int messageLength = 5;

		byte typeBytes = 4;
		byte[] lengthBytes = ByteBuffer.allocate(4).putInt(messageLength).array();
		byte[] indexBytes = ByteBuffer.allocate(4).putInt(index).array(); // TODO MAKE LEGIT

		byte[] result = null;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			outputStream.write(lengthBytes);
			outputStream.write(typeBytes);
			outputStream.write(indexBytes);

			result = outputStream.toByteArray();
		} catch (IOException ie){
			ie.printStackTrace();
		}

		return result;
	}

	public byte[] getBitfieldMessage(BitSet bitfield){

		int messageLength = bitfield.toByteArray().length + 1;
		byte typeBytes = 5;
		byte[] lengthBytes = ByteBuffer.allocate(4).putInt(messageLength).array();

		byte[] result = null;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			outputStream.write(lengthBytes);
			outputStream.write(typeBytes);
			outputStream.write(bitfield.toByteArray());

			result = outputStream.toByteArray();
		} catch (IOException ie){
			ie.printStackTrace();
		}

		return result;
	}

	public byte[] getRequestMessage(int index){

		int messageLength = 5;

		byte typeBytes = 6;
		byte[] lengthBytes = ByteBuffer.allocate(4).putInt(messageLength).array();
		byte[] indexBytes = ByteBuffer.allocate(4).putInt(index).array();;

		byte[] result = null;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			outputStream.write(lengthBytes);
			outputStream.write(typeBytes);
			outputStream.write(indexBytes);

			result = outputStream.toByteArray();
		} catch (IOException ie){
			ie.printStackTrace();
		}

		return result;
	}

	public byte[] getPieceMessage(int index, byte[] piece){

		int messageLength = piece.length + 5;

		byte[] lengthBytes = ByteBuffer.allocate(4).putInt(messageLength).array();
		byte typeBytes = 7;
		byte[] indexBytes = ByteBuffer.allocate(4).putInt(index).array();
		byte[] contentBytes = piece;

		byte[] result = null;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			outputStream.write(lengthBytes);
			outputStream.write(typeBytes);
			outputStream.write(indexBytes);
			outputStream.write(contentBytes);

			result = outputStream.toByteArray();
		} catch (IOException ie){
			ie.printStackTrace();
		}

		return result;
	}
}
