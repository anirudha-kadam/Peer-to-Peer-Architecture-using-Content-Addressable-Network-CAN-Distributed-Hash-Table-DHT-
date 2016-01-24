

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class BootstrapSocket {
	
	private DatagramSocket datagramSocket = null;
	
	/**
	 * creates bootstrapsocket
	 * @param portIn
	 */
	public BootstrapSocket(Integer portIn){
		try {
			datagramSocket = new DatagramSocket(portIn, InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()));
		} catch (SocketException e) {
			System.err.println("Socket Exception:"+ e.getMessage());
			System.exit(0);
		} catch (UnknownHostException e) {
			System.err.println("UnknownHostException found: "+ e.getMessage());
			System.exit(0);
		}
	}
	
	/**
	 * starts bootstrap
	 */
	public void startBootstrapServer(){
		BootstrapWorker bootstrapWorker = new BootstrapWorker(datagramSocket);
		Thread worker = new Thread(bootstrapWorker);
		worker.start();
	}
}
