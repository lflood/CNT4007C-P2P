import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Message {

	public Message() {

	}

	public byte[] getChokeMessage(){

		int messageLength = 2;

		byte[] typeBytes = ByteBuffer.allocate(1).putInt(0).array();
		byte[] lengthBytes = ByteBuffer.allocate(2).putInt(messageLength).array();

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

		int messageLength = 2;

		byte[] typeBytes = ByteBuffer.allocate(1).putInt(1).array();
		byte[] lengthBytes = ByteBuffer.allocate(2).putInt(messageLength).array();

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

		int messageLength = 2;

		byte[] typeBytes = ByteBuffer.allocate(1).putInt(2).array();
		byte[] lengthBytes = ByteBuffer.allocate(2).putInt(messageLength).array();

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

		int messageLength = 2;

		byte[] typeBytes = ByteBuffer.allocate(1).putInt(3).array();
		byte[] lengthBytes = ByteBuffer.allocate(2).putInt(messageLength).array();

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

	public byte[] getHaveMessage(byte[] index){

		int messageLength = 5;

		byte[] typeBytes = ByteBuffer.allocate(1).putInt(5).array();
		byte[] lengthBytes = ByteBuffer.allocate(1).putInt(messageLength).array();
		byte[] indexBytes = index; // TODO MAKE LEGIT

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

	public byte[] getBitfieldMessage(byte[] bitfield){

		int messageLength = bitfield.length + 1;

		byte[] typeBytes = ByteBuffer.allocate(1).putInt(5).array();
		byte[] lengthBytes = ByteBuffer.allocate(1).putInt(messageLength).array();


		byte[] result = null;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			outputStream.write(lengthBytes);
			outputStream.write(typeBytes);
			outputStream.write(bitfield);

			result = outputStream.toByteArray();
		} catch (IOException ie){
			ie.printStackTrace();
		}

		return result;
	}

	public byte[] getRequestMessage(byte[] index){

		int messageLength = 5;

		byte[] typeBytes = ByteBuffer.allocate(1).putInt(6).array();
		byte[] lengthBytes = ByteBuffer.allocate(1).putInt(messageLength).array();
		byte[] indexBytes = index; // TODO MAKE LEGIT

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

	public byte[] getPieceMessage(byte[] index, byte[] content){

		int messageLength = 5;

		byte[] typeBytes = ByteBuffer.allocate(1).putInt(7).array();
		byte[] lengthBytes = ByteBuffer.allocate(1).putInt(messageLength).array();
		byte[] indexBytes = index; // TODO MAKE LEGIT
		byte[] contentBytes = content;

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
