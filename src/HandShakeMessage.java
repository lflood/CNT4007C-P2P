import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class HandShakeMessage {

	private String expectedHeader;

	public HandShakeMessage() {

		expectedHeader = "P2PFILESHARINGPROJ";
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

	public int accept(byte[] message){

		byte[] headerBytes = Arrays.copyOfRange(message, 0, 18);
		byte[] zeroBytes = Arrays.copyOfRange(message, 18, 28);
		byte[] idBytes = Arrays.copyOfRange(message, 28, 32);

		ByteBuffer bb = ByteBuffer.wrap(idBytes);
		int peerid = bb.getInt();

		// Still needs to check validity, but shows P2P communication
		String header = new String(headerBytes);

		if(!header.equals(expectedHeader)){

			System.out.println(header + " " + expectedHeader);
			System.out.println("Error: wrong header received");
			return -1;
		}

		System.out.println("Peer " + peerid + " is sending handshake message.");


		return peerid;
	}
}
