
import java.net.InetAddress;
import java.net.UnknownHostException;

public class PeerDriver {

	public static void main(String[] args) {
		if(args.length != 4){
			System.err.println("Exactly four arguments are expected:1) bootstrap ip 2) bootstrap port 3) peerName 4) peer port");
			System.exit(0);
		}
		InetAddress bootstrapip = null;
		int bootstrapport = 0;
		int peerport = 0;
		try {
			bootstrapip = InetAddress.getByName(args[0]);
			bootstrapport = Integer.parseInt(args[1]);
			peerport = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			System.err.println("ports should be integers"+ e.getMessage());
			System.exit(0);
		} catch (UnknownHostException e) {
			System.err.println("UnknownHostException :"+ e.getMessage());
			System.exit(0);
		}
		Peer node = new Peer(bootstrapip, bootstrapport, args[2], peerport);
		
		PeerSocket peerSocket = new PeerSocket(node);
		peerSocket.startPeer();
	}

}
