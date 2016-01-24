

import java.io.BufferedInputStream;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;



public class PeerWorker implements Runnable {

	DatagramSocket datagramSocket = null;
	DatagramPacket datagramPacket = null;
	ByteArrayOutputStream baos = null;
	ObjectOutputStream oos  = null;
	ByteArrayInputStream bais = null;
	ObjectInputStream ois = null;
    

	Peer node = null;
	byte[] dataBuffer = null;
	Message message = null;

	public PeerWorker(DatagramSocket datagramSocketIn, Peer nodeIn) {
		node = nodeIn;
		message = new Message();
		dataBuffer = new byte[65536];
		datagramSocket = datagramSocketIn;
		datagramPacket = new DatagramPacket(dataBuffer, dataBuffer.length);
	}

	@Override
	public void run() {
	
		while (true) {
			try {
				
				datagramSocket.receive(datagramPacket);
				ByteArrayInputStream bais  = new ByteArrayInputStream(dataBuffer, datagramPacket.getOffset(), datagramPacket.getLength());
				ObjectInputStream ois  = new ObjectInputStream(new BufferedInputStream(bais));
				message = (Message) ois.readObject();
				baos = new ByteArrayOutputStream();
				oos  = new ObjectOutputStream(new BufferedOutputStream(baos));
				if(message.senderType.equals("peer")){
					if(message.header.equals("join")){
						if(node.isMyZone(message)){
							message = node.splitZone(message);
							if(!message.header.equals("join_failure")){
								message = node.updateNeighbors(datagramSocket,message);
								message = node.updateContent(message);
								message.header = "join_success";
								message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
								message.path.add(new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate));
							}
							
				            oos.writeObject((Object)message);
				            oos.close();
				            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, message.requestor.peerIpAddress, message.requestor.peerPort));
						}else if(node.isMyTakeoversZone(message)){
							message.senderType = "peer";
							message.header = "join_success";
							message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
							message.path.add(new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate));
							message.requestor.x1Coordinate = node.takeover.x1Coordinate;
							message.requestor.x2Coordinate = node.takeover.x2Coordinate;
							message.requestor.y1Coordinate = node.takeover.y1Coordinate;
							message.requestor.y2Coordniate = node.takeover.y2Coordniate;
							message.requestor.neighbors = node.takeover.neighbors;
							message.requestor.contentHashTable = node.takeover.contentHashTable;
							oos.flush();
				            oos.writeObject((Object)message);
				            oos.close();
				            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, message.requestor.peerIpAddress, message.requestor.peerPort));
						}else{
							Peer nearestNeighbor = node.findNearestNeighbor(message);
							message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
							message.path.add(new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate));
							oos.flush();
				            oos.writeObject((Object)message);
				            oos.close();
				            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, nearestNeighbor.peerIpAddress, nearestNeighbor.peerPort));
						}
					}else if(message.header.equals("insert")){
						
						if(node.isMyZone(message)){
							node.insertFile(message);
							message.header = "insert_success";
							message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
							message.path.add(new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate));
				            oos.writeObject((Object)message);
				            oos.close();
				            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, message.requestor.peerIpAddress, message.requestor.peerPort));
						}else{
							
							Peer nearestNeighbor = node.findNearestNeighbor(message);
							message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
							message.path.add(new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate));
							oos.flush();
				            oos.writeObject((Object)message);
				            oos.close();
				            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, nearestNeighbor.peerIpAddress, nearestNeighbor.peerPort));
						}
						
						
					}else if(message.header.equals("search")){
						if(node.isMyZone(message)){
							message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
							message = node.searchFile(message);
							oos.flush();
							oos.writeObject((Object)message);
				            oos.close();
				            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, message.requestor.peerIpAddress, message.requestor.peerPort));
						}else if(node.isMyTakeoversZone(message)){
							 if(node.takeover.contentHashTable.get(message.fileName) != null){
								 message.header = "search_success";
								 message.path.add(new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate));
							 }else{
								 message.header = "search_failure";
							 }
							oos.flush();
							oos.writeObject((Object)message);
					        oos.close();
					        datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, message.requestor.peerIpAddress, message.requestor.peerPort));
						}else{
							Peer nearestNeighbor = node.findNearestNeighbor(message);
							message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
							message.path.add(new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate));
							oos.flush();
				            oos.writeObject((Object)message);
				            oos.close();
				            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, nearestNeighbor.peerIpAddress, nearestNeighbor.peerPort));
						}
					}else if(message.header.equals("sent_neighbors")){
						if(node.viewRequestType.equals("view") && node.viewDoneFlag == false){
							node.view(message, datagramSocket);
						}else if(node.viewRequestType.equals("view_peer") && node.viewDoneFlag == false){
							node.viewPeer(message, datagramSocket);
						}
						
					}else if(message.header.equals("send_neighbors")){
						message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
						message.myNeighbors = node.neighbors;
						message.header = "sent_neighbors";
						oos.flush();
			            oos.writeObject((Object)message);
			            oos.close();
			            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, message.requestor.peerIpAddress, message.requestor.peerPort));
			            
					}else if(message.header.equals("join_success")){
						node.updateNode(message);
						node.sendAddMeNotification(datagramSocket, node.neighbors);
						message.path.add(new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate));
						node.viewInfo();
						
					}else if(message.header.equals("insert_success")){
						System.out.println("-------------------------OUTPUT----------------------------\n");
						System.out.println("Peer "+message.recentSender.name+" stores the file\n");
						node.printRequestStatus(message);
						System.out.println("\n-----------------------------------------------------------");
						
					}else if(message.header.equals("search_success")){
						System.out.println("-------------------------OUTPUT----------------------------\n");
						System.out.println("Peer "+message.recentSender.name+" stores the file\n");
						node.printRequestStatus(message);
						System.out.println("\n-----------------------------------------------------------");
						
					}else if(message.header.equals("remove_me")){
						node.notifyRemoveMe(message);
						
					}else if(message.header.equals("remove_me_leave")){
						node.notifyRemoveMe(message);
						node.viewInfo();
					}else if(message.header.equals("add_me")){
						node.notifyAddMe(message);
						
					}else if(message.header.equals("leave")){
						message.header = "i_am_leaving";
						Peer perfectNeighbor = node.findZoneToMergeWith();
						if(perfectNeighbor != null){
							for (Map.Entry<String, Peer> entry : node.neighbors.entrySet()) {
								Peer peer = entry.getValue();
								Message message = new Message();
								message.senderType = "peer";
								message.header = "remove_me_leave";
								message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
								try {
									ByteArrayOutputStream baos = new ByteArrayOutputStream();
									ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(baos));
									oos.flush();
						            oos.writeObject((Object)message);
						            oos.close();
						            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, peer.peerIpAddress, peer.peerPort));
								} catch (IOException e) {
									System.err.println("IOException found:"+ e.getMessage());
								}
								
							}
							
							message.mergeType = "merge";
							message.mergeNodeType = node.findPerfectNeighbor(perfectNeighbor, node);
							oos.flush();
				            oos.writeObject((Object)message);
				            oos.close();
				            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, perfectNeighbor.peerIpAddress, perfectNeighbor.peerPort));
				            
						}else{
							
							for (Map.Entry<String, Peer> entry : node.neighbors.entrySet()) {
								Peer peer = entry.getValue();
								Message message = new Message();
								message.senderType = "peer";
								message.header = "remove_me_leave";
								message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
								try {
									ByteArrayOutputStream baos = new ByteArrayOutputStream();
									ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(baos));
									oos.flush();
						            oos.writeObject((Object)message);
						            oos.close();
						            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, peer.peerIpAddress, peer.peerPort));
								} catch (IOException e) {
									System.err.println("IOException: "+ e.getMessage());
								}
								
							}
							
							Peer smallestNeighbor = node.findSmallestNeighbor();
							if(smallestNeighbor != null){
								message.mergeType = "takeover";
								oos.flush();
					            oos.writeObject((Object)message);
					            oos.close();
					            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, smallestNeighbor.peerIpAddress, smallestNeighbor.peerPort));
							}else{
								message.header = "leaving_network";
								message.senderType = "peer";
								message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
								oos.flush();
					            oos.writeObject((Object)message);
					            oos.close();
					            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, node.bootStrapIpAddress, node.bootstrapPort));
					            System.exit(0);
							}
				
						}
						
						
					}else if(message.header.equals("you_may_leave")){
						message.header = "leaving_network";
						message.senderType = "peer";
						message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
						oos.flush();
			            oos.writeObject((Object)message);
			            oos.close();
			            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, node.bootStrapIpAddress, node.bootstrapPort));
			            System.exit(0);
					}else if(message.header.equals("i_am_leaving")){
						if(message.mergeType.equals("merge")){
							
							node.mergeZones(message);
							node.sendAddMeNotification(datagramSocket, node.neighbors);
							message.header = "you_may_leave";
							message.senderType = "peer";
							message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
							oos.flush();
				            oos.writeObject((Object)message);
				            oos.close();
				            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, message.requestor.peerIpAddress, message.requestor.peerPort));
				            
						}else{
							
							message = node.takeover(message);
							message.header = "you_may_leave";
							message.senderType = "peer";
							message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
							oos.flush();
				            oos.writeObject((Object)message);
				            oos.close();
				            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, message.requestor.peerIpAddress, message.requestor.peerPort));
						}
						
					}else if(message.header.endsWith("failure")){
						System.out.println("Failure");
						
					}
				}else if(message.senderType.equals("bootstrap")){
					
					if(message.header.equals("all_yours")){
						
						node.setCoordinates(0.0, 0.0, 10.0, 10.0);
						message.header = "join_success";
						node.viewInfo();
						
					}else if(message.header.equals("active_node")){
						
						message.senderType = "peer";
						message.header = "join";
						message.recentSender = new Peer(node.peerIpAddress, node.peerPort, node.name, node.x1Coordinate, node.y1Coordinate, node.x2Coordinate, node.y2Coordniate);
						message.requestor = node;
						message.randomX = node.randomX;
						message.randomY = node.randomY;
						oos.flush();
			            oos.writeObject((Object)message);
			            oos.close();
			            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, message.activeNode.peerIpAddress, message.activeNode.peerPort));
			            
					}else if(message.header.equals("network_full")){
						
						System.out.println("Network full");
					}
				}
				
			} catch (IOException e) {
				System.err.println("IOException found: "+ e.getMessage());
			} catch (ClassNotFoundException e) {
				System.err.println("ClassNotFoundException found: "+ e.getMessage());
			}

		}

	}

}
