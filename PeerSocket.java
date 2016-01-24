

import java.io.BufferedOutputStream;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;



public class PeerSocket {

	DatagramSocket datagramSocket = null;
	DatagramPacket datagramPacket = null;
	ByteArrayOutputStream baos = null;
	ObjectOutputStream oos = null;
	
	byte dataBuffer[] = null;
	Peer node = null;
	Message message = null;
	public PeerSocket(Peer nodeIn) {
		try {
			node = nodeIn;
			dataBuffer = new byte[65536];
			datagramSocket = new DatagramSocket(node.peerPort, node.peerIpAddress);
			datagramPacket = new DatagramPacket(dataBuffer, dataBuffer.length);
		} catch (SocketException e) {
			System.err.println("SocketException: "+e.getMessage());
		}
	}

	/**
	 * starts peer
	 */
	public void startPeer() {

		PeerWorker peerWorker = new PeerWorker(datagramSocket, node);
		Thread newWorker = new Thread(peerWorker);
		newWorker.start();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			System.out.println("USAGE: \n(1) join \n(2) insert <fileName> \n(3) search <fileName> \n(4) view <peerName> or view \n(5) leave");
			try {
				baos = new ByteArrayOutputStream();
				oos  = new ObjectOutputStream(new BufferedOutputStream(baos));
				System.out.println("Enter Command: ");
				String command = br.readLine();
				String userInput[] = command.split("\\s+");
				if (userInput[0].equalsIgnoreCase("join")) {
					message = new Message();
					message.senderType = "peer";
					message.header = "join";
					message.randomX = node.randomX;
					message.randomY = node.randomY;
					message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
					message.requestor = node;
					oos.flush();
		            oos.writeObject((Object)message);
		            oos.close();
		            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, node.bootStrapIpAddress, node.bootstrapPort));
		            
				}else if(userInput[0].equalsIgnoreCase("insert")){
					message = new Message();
					String fileName = new String();
					if(userInput.length == 2)
						fileName = userInput[1];
					else{
						System.err.println("USAGE: insert <fileName>");
						continue;
					}
					int charAtOdd = 0, charAtEven = 0;
					File file = new File(fileName);
					message.fileContent = Files.readAllBytes(file.toPath());
					for(int i = 0; i < fileName.length(); i++){
						if((i+1) % 2 == 0){
							charAtEven = charAtEven + fileName.charAt(i);
						} else{
							charAtOdd = charAtOdd + fileName.charAt(i);
						}
					}
					message.fileName = fileName;
					message.randomX = (double) (charAtOdd % 10);
					message.randomY = (double) (charAtEven % 10);
					message.senderType = "peer";
					message.header = "insert";
					message.path.add(new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate));
					message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
					message.requestor = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
					if(node.isMyZone(message)){
						node.insertFile(message);
						System.out.println("-------------------------OUTPUT----------------------------\n");
						System.out.println("Success->"+node.name+":"+node.peerIpAddress+":"+node.peerPort+"->Success");
						System.out.println("\n-----------------------------------------------------------");
					}else{
						Peer nearestNeighbor = node.findNearestNeighbor(message);
						oos.flush();
			            oos.writeObject((Object)message);
			            oos.close();
			            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, nearestNeighbor.peerIpAddress, nearestNeighbor.peerPort));
					}
	
				}else if(userInput[0].equalsIgnoreCase("search")){
					message = new Message();
					String fileName = new String();
					if(userInput.length == 2)
						fileName = userInput[1];
					else{
						System.err.println("USAGE: search <fileName>");
						continue;
					}
					int charAtOdd = 0, charAtEven = 0;
					for(int i = 0; i < fileName.length(); i++){
						if((i+1) % 2 == 0){
							charAtEven = charAtEven + fileName.charAt(i);
						} else{
							charAtOdd = charAtOdd + fileName.charAt(i);
						}
					}
					message.fileName = fileName;
					message.randomX = (double) (charAtOdd % 10);
					message.randomY = (double) (charAtEven % 10);
					message.senderType = "peer";
					message.header = "search";
					message.path.add(new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate));
					message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
					message.requestor = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
					if(node.isMyZone(message)){
						node.searchFile(message);
						if(message.header.equals("search_failure")){
							System.out.println("Failure");
						}
					}else{
						Peer nearestNeighbor = node.findNearestNeighbor(message);
						oos.flush();
			            oos.writeObject((Object)message);
			            oos.close();
			            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, nearestNeighbor.peerIpAddress, nearestNeighbor.peerPort));
					}
				}else if(userInput[0].equalsIgnoreCase("view")){
					message = new Message();
					message.senderType = "peer";
					message.header = "send_neighbors";
					message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
					message.requestor = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
					node.viewDoneFlag = false;
					if(userInput.length == 2){
						node.viewPeerName = userInput[1];
						node.viewRequestType = "view_peer";
					}else if(userInput.length == 1){
						node.viewRequestType = "view";
					}else{
						System.out.println("USAGE: (1) view <peerName> (2) view");
						continue;
					}
					
					if(node.viewRequestType.equals("view_peer") && node.neighbors.get(userInput[1]) != null){
						Peer peer = node.neighbors.get(userInput[1]);
							node.viewFunction(peer);
					}else{
						node.mergedNodesMap = new HashMap<String, Peer>(node.neighbors);
						for (Map.Entry<String, Peer> entry : node.neighbors.entrySet()) {
							Peer peer = entry.getValue();
							try {
								baos = new ByteArrayOutputStream();
								oos = new ObjectOutputStream(new BufferedOutputStream(baos));
								oos.flush();
								oos.writeObject((Object)message);
								oos.close();
								datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, peer.peerIpAddress, peer.peerPort));
							} catch (IOException e) {
						
								System.err.println("IOException found: ");
							}
						}
						
					}
					
					
				}else if(command.equalsIgnoreCase("leave")){
					message = new Message();
					message.senderType = "peer";
					message.header = "leave";
					message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
					message.requestor = node;
					oos.flush();
		            oos.writeObject((Object)message);
		            oos.close();
		            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, node.peerIpAddress, node.peerPort));
					
					
				}
				
			} catch (IOException e) {
				System.err.println("IOException found"+ e.getMessage());
			}
		}
	}
}
