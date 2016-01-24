

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sound.midi.Track;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

public class Peer implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String name = null;
	public InetAddress peerIpAddress = null;
	public InetAddress bootStrapIpAddress = null;
	public Integer peerPort = null; 
	public Integer bootstrapPort = null;
	public Double x1Coordinate = null;
	public Double y1Coordinate = null;
	public Double x2Coordinate = null;
	public Double y2Coordniate = null;
	public Double randomX = null;
	public Double randomY = null;
	public Map<String, Peer> neighbors= null;
	public Map<String, byte[]> contentHashTable = null;
	public static String peerName = null;
	public String viewRequestType = null;
	public Map<String, Peer> mergedNodesMap = null;;
	public List<Peer> newNodesList = null;
	public boolean viewDoneFlag;
	public String viewPeerName = null;
	public Peer takeover = null;
	public Peer() {
		
	}
	
	public Peer(InetAddress peerAddressIn, Integer peerPortIn, String nameIn, Double x1CoordinateIn, Double y1CoordinateIn, Double x2CoordinateIn, Double y2CoordinateIn){
		peerIpAddress = peerAddressIn;
		peerPort = peerPortIn;
		name = nameIn;
		x1Coordinate = x1CoordinateIn;
		y1Coordinate = y1CoordinateIn;
		x2Coordinate = x2CoordinateIn;
		y2Coordniate = y2CoordinateIn;

	}
	
	
	
	public Peer(InetAddress bootStrapIpAddressIn, Integer bootStrapPortIn, String nameIn, Integer peerPortIn) {
		name = nameIn;
		peerPort = peerPortIn;
		bootStrapIpAddress = bootStrapIpAddressIn;
		bootstrapPort = bootStrapPortIn;
		neighbors = new HashMap<String, Peer>();
		contentHashTable = new HashMap<String, byte[]>();
		mergedNodesMap = new HashMap<String, Peer>();
		newNodesList = new ArrayList<Peer>();
		viewPeerName = new String();
		viewRequestType = new String();
		viewDoneFlag = false;
		
		try {
			peerIpAddress = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			System.err.println("UnknownHostException found: "+e.getMessage());
		}
		randomX = Double.valueOf(Math.random() * 10);
		randomY = Double.valueOf(Math.random() * 10);
	}
	
	
	public void setCoordinates(Double x1, Double y1, Double x2, Double y2){
		x1Coordinate = x1;
		y1Coordinate = y1;
		x2Coordinate = x2;
		y2Coordniate = y2;
	}
	
	public boolean isMyZone(Message message) {
		return (message.randomX > x1Coordinate && message.randomX < x2Coordinate && message.randomY > y1Coordinate && message.randomY < y2Coordniate);
	}
	
	public boolean isMyTakeoversZone(Message message){
		if(takeover != null){
			return (message.randomX > takeover.x1Coordinate && message.randomX < takeover.x2Coordinate && message.randomY > takeover.y1Coordinate && message.randomY < takeover.y2Coordniate);
		}
		return false;
	}
	
	public Message splitZone(Message messageIn){
		Message message = messageIn;
		Double midX = null;
		Double midY = null;
		Double newNodeX1 = null;
		Double newNodeY1 = null;
		Double newNodeX2 = null;
		Double newNodeY2 = null;
		if((y2Coordniate - y1Coordinate) == (x2Coordinate - x1Coordinate)){
			midX = (x1Coordinate + x2Coordinate) / 2;
			if(message.randomX < midX){
				newNodeX1 = x1Coordinate;
				newNodeY1 = y1Coordinate;
				newNodeX2 = midX;
				newNodeY2 = y2Coordniate;
				x1Coordinate = midX;
			}else if(message.randomX >= midX){
				newNodeX1 = midX;
				newNodeY1 = y1Coordinate;
				newNodeX2 = x2Coordinate;
				newNodeY2 = y2Coordniate;
				x2Coordinate = midX;
			}
		}else if((y2Coordniate - y1Coordinate) > (x2Coordinate - x1Coordinate)){
			midY = (y1Coordinate + y2Coordniate) / 2;
			if(message.randomY < midY){
				newNodeX1 = x1Coordinate;
				newNodeY1 = y1Coordinate;
				newNodeX2 = x2Coordinate;
				newNodeY2 = midY;
				y1Coordinate = midY;
			}else if(message.randomY >= midY){
				newNodeX1 = x1Coordinate;
				newNodeY1 = midY;
				newNodeX2 = x2Coordinate;
				newNodeY2 = y2Coordniate;
				y2Coordniate = midY;
			}
		}else if((y2Coordniate - y1Coordinate) < (x2Coordinate - x1Coordinate)){
			midX = (x1Coordinate + x2Coordinate) / 2;
			if(message.randomX < midX){
				newNodeX1 = x1Coordinate;
				newNodeY1 = y1Coordinate;
				newNodeX2 = midX;
				newNodeY2 = y2Coordniate;
				x1Coordinate = midX;
			}else if(message.randomX >= midX){
				newNodeX1 = midX;
				newNodeY1 = y1Coordinate;
				newNodeX2 = x2Coordinate;
				newNodeY2 = y2Coordniate;
				x2Coordinate = midX;
			}
		}
		else{
			message.header = "join_failure";
			return message;
		}
		message.requestor.setCoordinates(newNodeX1, newNodeY1, newNodeX2, newNodeY2);
		return message;
	}
	
	public boolean isNeighbor(Peer peer, Peer current){

		if(  (current.y2Coordniate.equals(peer.y1Coordinate) || current.y1Coordinate.equals(peer.y2Coordniate)) &&
			 (
					 (current.x1Coordinate.equals(peer.x1Coordinate) && current.x2Coordinate.equals(peer.x2Coordinate)) ||
					 (current.x1Coordinate > peer.x1Coordinate && current.x1Coordinate < peer.x2Coordinate) ||
					 (current.x2Coordinate > peer.x1Coordinate && current.x2Coordinate < peer.x2Coordinate) ||
					 (current.x1Coordinate.equals(peer.x1Coordinate) && current.x2Coordinate > peer.x2Coordinate) ||
					 (current.x1Coordinate < peer.x1Coordinate && current.x2Coordinate.equals(peer.x2Coordinate)) ||
					 (current.x1Coordinate < peer.x1Coordinate && current.x2Coordinate > peer.x2Coordinate) ||
					 (current.x1Coordinate > peer.x1Coordinate && current.x2Coordinate < peer.x2Coordinate) ||
					 (current.x1Coordinate.equals(peer.x1Coordinate) && current.x2Coordinate < peer.x2Coordinate) ||
					 (current.x1Coordinate > peer.x1Coordinate && current.x2Coordinate.equals(peer.x2Coordinate))
			 )	
		  ){return true;}
		
		if(  (current.x2Coordinate.equals(peer.x1Coordinate) || current.x1Coordinate.equals(peer.x2Coordinate)) &&
				 (
						 (current.y1Coordinate.equals(peer.y1Coordinate) && current.y2Coordniate.equals(peer.y2Coordniate)) ||
						 (current.y1Coordinate > peer.y1Coordinate && current.y1Coordinate < peer.y2Coordniate) ||
						 (current.y2Coordniate > peer.y1Coordinate && current.y2Coordniate < peer.y2Coordniate) ||
						 (current.y1Coordinate.equals(peer.y1Coordinate) && current.y2Coordniate > peer.y2Coordniate) ||
						 (current.y1Coordinate < peer.y1Coordinate && current.y2Coordniate.equals(peer.y2Coordniate)) ||
						 (current.y1Coordinate < peer.y1Coordinate && current.y2Coordniate > peer.y2Coordniate) ||
						 (current.y1Coordinate > peer.y1Coordinate && current.y2Coordniate < peer.y2Coordniate) ||
						 (current.y1Coordinate.equals(peer.y1Coordinate) && current.y2Coordniate < peer.y2Coordniate) ||
						 (current.y1Coordinate > peer.y1Coordinate && current.y2Coordniate.equals(peer.y2Coordniate))
				 )	
		){return true;}
		
		return false;
		
	}
	
	public Message updateNeighbors(DatagramSocket datagramSocket, Message messageIn){
		Message message = messageIn;
		Map<String, Peer> updatedNeighbors = new HashMap<String, Peer>();
		Map<String, Peer> newNodeNeighbors = new HashMap<String, Peer>();
		Map<String, Peer> removedNeighbors = new HashMap<String, Peer>();
		for (Map.Entry<String, Peer> entry : neighbors.entrySet()) {
			String name = entry.getKey();
			Peer peer = entry.getValue();
			if(isNeighbor(peer, this)){
				updatedNeighbors.put(name, peer);
				if(isNeighbor(peer, message.requestor))
					newNodeNeighbors.put(name, peer);
			}else{
				removedNeighbors.put(name, peer);
				newNodeNeighbors.put(name, peer);
			}
		}
		
		neighbors = updatedNeighbors;
		message.requestor.neighbors = newNodeNeighbors;
		
		neighbors.put(message.requestor.name,new Peer(message.requestor.peerIpAddress, message.requestor.peerPort, message.requestor.name, message.requestor.x1Coordinate, message.requestor.y1Coordinate, message.requestor.x2Coordinate, message.requestor.y2Coordniate));
		message.requestor.neighbors.put(this.name, new Peer(peerIpAddress, peerPort, name, x1Coordinate, y1Coordinate, x2Coordinate, y2Coordniate));
		
		
		sendRemoveMeNotification(datagramSocket, removedNeighbors);
		sendAddMeNotification(datagramSocket, updatedNeighbors);
			
		return message;
	}
	
	public void sendRemoveMeNotification(DatagramSocket datagramSocket, Map<String, Peer> removedNeighbors){
		for (Map.Entry<String, Peer> entry : removedNeighbors.entrySet()) {
			Peer peer = entry.getValue();
			Message message = new Message();
			message.senderType = "peer";
			message.header = "remove_me";
			message.recentSender = new Peer(peerIpAddress, peerPort, name, x1Coordinate, y1Coordinate, x2Coordinate, y2Coordniate);
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(baos));
				oos.flush();
	            oos.writeObject((Object)message);
	            oos.close();
	            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, peer.peerIpAddress, peer.peerPort));
			} catch (IOException e) {
				System.err.println("IOException found: "+ e.getMessage());
			}
			
		}
		
	}
	
	public void sendAddMeNotification(DatagramSocket datagramSocket, Map<String, Peer> updatedNeighbors){
		
		for (Map.Entry<String, Peer> entry : updatedNeighbors.entrySet()) {
			
			Peer peer = entry.getValue();
			Message message = new Message();
			message.senderType = "peer";
			message.header = "add_me";
			message.recentSender = new Peer(peerIpAddress, peerPort, name, x1Coordinate, y1Coordinate, x2Coordinate, y2Coordniate);
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(baos));
				oos.flush();
	            oos.writeObject((Object)message);
	            oos.close();
	            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, peer.peerIpAddress, peer.peerPort));
			} catch (IOException e) {
				System.err.println("IOException found: "+e.getMessage());
			}
			
		}
	
	}
	
	public void notifyRemoveMe(Message messageIn){
		neighbors.remove(messageIn.recentSender.name);
	}
	
	public void notifyAddMe(Message messageIn){
		neighbors.put(messageIn.recentSender.name, messageIn.recentSender);
	}
	
	public Peer findNearestNeighbor(Message message) {
		Peer nearestNeighbor = null;
		Peer currentValue = null;
		Double randomX = message.randomX;
		Double randomY = message.randomY;
		boolean isFirst = true;
		double previousDistance = 0, currentDistance = 0; 
		for (Map.Entry<String, Peer> entry : neighbors.entrySet()) {
			currentValue = entry.getValue();
			if(currentValue.name.equals(message.recentSender.name))
				continue;
			double midX = (currentValue.x1Coordinate + currentValue.x2Coordinate)/2;
			double midY = (currentValue.y1Coordinate + currentValue.y2Coordniate)/2;
			currentDistance = Math.sqrt(((randomX - midX) * (randomX - midX)) + ((randomY - midY) * (randomY - midY)));
			if(isFirst || currentDistance < previousDistance){
				nearestNeighbor = currentValue;
				previousDistance = currentDistance;
			}
			isFirst = false;
		}
		return nearestNeighbor;
	}
	
	public void updateNode(Message message){
		x1Coordinate = message.requestor.x1Coordinate;
		x2Coordinate = message.requestor.x2Coordinate;
		y1Coordinate = message.requestor.y1Coordinate;
		y2Coordniate = message.requestor.y2Coordniate;
		name = message.requestor.name;
		peerIpAddress = message.requestor.peerIpAddress;
		peerPort = message.requestor.peerPort;
		neighbors = message.requestor.neighbors;
		contentHashTable = message.requestor.contentHashTable;
	}
	
	public void insertFile(Message message){
		contentHashTable.put(message.fileName, message.fileContent);
		message.header = "insert_success";
		/*File file = new File("reached.txt");
		try {
			Files.write(file.toPath(), message.fileContent);
		} catch (IOException e) {
			System.err.println("IOException found: "+ e.getMessage());
		}*/
	}
	
	public void printRequestStatus(Message message){
		
			StringBuilder sb = new StringBuilder();
			sb.append("Success->");
			for(int i = 0; i < message.path.size(); i++){
				sb.append(message.path.get(i).name+":"+message.path.get(i).peerIpAddress+":"+message.path.get(i).peerPort+"->");
			}
			sb.append("Success");
			System.out.println(sb.toString());
		
	}
	 public Message searchFile(Message messageIn){
		 Message message = messageIn;
		 if(contentHashTable.get(message.fileName) != null){
			 message.header = "search_success";
			 message.path.add(new Peer(this.peerIpAddress, this.peerPort, this.name, this.x1Coordinate, this.y1Coordinate, this.x2Coordinate, this.y2Coordniate));
		 }else{
			 message.header = "search_failure";
		 }
		return message; 
	 }
	 
	 public Message updateContent(Message messageIn){
		Message message = messageIn;
		Set<String> keys = contentHashTable.keySet();
        for(String key: keys){
            String fileName = key;
            int charAtOdd = 0, charAtEven = 0;
			for(int i = 0; i < fileName.length(); i++){
				if((i+1) % 2 == 0){
					charAtEven = charAtEven + fileName.charAt(i);
				} else{
					charAtOdd = charAtOdd + fileName.charAt(i);
				}
			}
			message.randomX = (double) (charAtOdd % 10);
			message.randomY = (double) (charAtEven % 10);
			if(!isMyZone(message)){
				message.requestor.contentHashTable.put(fileName, contentHashTable.get(fileName));
				contentHashTable.remove(fileName);
			}
        }
		return message;
	 }
	
	 
	 public void viewFunction(Peer peer){
		 StringBuilder sb = new StringBuilder();
		 sb.append("----------------------------------------------------");
		 sb.append(System.getProperty("line.separator"));
		 sb.append("Name: "+peer.name);
		 sb.append(System.getProperty("line.separator"));
		 sb.append(System.getProperty("line.separator"));
		 sb.append("ipAddress: "+peer.peerIpAddress);
		 sb.append(System.getProperty("line.separator"));
		 sb.append(System.getProperty("line.separator"));
		 sb.append("x1: "+peer.x1Coordinate+" y1: "+peer.y1Coordinate+" x2: "+peer.x2Coordinate+" y2: "+peer.y2Coordniate);
		 sb.append(System.getProperty("line.separator"));
		 sb.append(System.getProperty("line.separator"));
		 sb.append("----------------------------------------------------");
		 System.out.println(sb.toString());
	 }
	 
	 public void view(Message messageIn, DatagramSocket datagramSocket){
			ByteArrayOutputStream baos = null;
			ObjectOutputStream oos = null;
			newNodesList.clear();
			for (Map.Entry<String, Peer> entry : messageIn.myNeighbors.entrySet()) {
				Peer peer = entry.getValue();
				if(mergedNodesMap.get(peer.name) == null){
					newNodesList.add(peer);
					mergedNodesMap.put(peer.name, peer);
				}							
			}
			if(newNodesList.size() == 0){
				for (Map.Entry<String, Peer> entry : mergedNodesMap.entrySet()) {
					Peer peer = entry.getValue();
					viewFunction(peer);			
				}
				
				viewDoneFlag = true;
				mergedNodesMap = new HashMap<String, Peer>();
				newNodesList.clear();
				viewPeerName = new String();
			}
			if(viewDoneFlag == false){
				Message message = new Message();
				message.senderType = "peer";
				message.recentSender = new Peer(this.peerIpAddress, this.peerPort, this.name, this.x1Coordinate, this.y1Coordinate, this.x2Coordinate, this.y2Coordniate);
				message.requestor = new Peer(this.peerIpAddress, this.peerPort, this.name, this.x1Coordinate, this.y1Coordinate, this.x2Coordinate, this.y2Coordniate);
				message.header = "send_neighbors";
				for (int i = 0; i < newNodesList.size(); i++) {
					try {
						baos = new ByteArrayOutputStream();
						oos = new ObjectOutputStream(new BufferedOutputStream(baos));
						oos.flush();
						oos.writeObject((Object)message);
						oos.close();
						datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, newNodesList.get(i).peerIpAddress, newNodesList.get(i).peerPort));
					} catch (IOException e) {
				
						e.printStackTrace();
					}
					
				}
				
				
			}
			newNodesList.clear();

	 }
	 
	 public void viewPeer(Message message, DatagramSocket datagramSocket){
			ByteArrayOutputStream baos = null;
			ObjectOutputStream oos = null;
			newNodesList.clear();
			for (Map.Entry<String, Peer> entry : message.myNeighbors.entrySet()) {
				Peer peer = entry.getValue();
				if(mergedNodesMap.get(peer.name) == null){
					if(peer.name.equals(this.viewPeerName)){
						viewFunction(peer);
						viewDoneFlag = true;
						mergedNodesMap = new HashMap<String, Peer>();
						newNodesList = new ArrayList<Peer>();
						viewPeerName = new String();
						break;
					}
					newNodesList.add(peer);
					mergedNodesMap.put(peer.name, peer);
				}
			}
			if(viewDoneFlag == false){
				message = new Message();
				message.senderType = "peer";
				message.recentSender = new Peer(this.peerIpAddress, this.peerPort, this.name, this.x1Coordinate, this.y1Coordinate, this.x2Coordinate, this.y2Coordniate);
				message.requestor = new Peer(this.peerIpAddress, this.peerPort, this.name, this.x1Coordinate, this.y1Coordinate, this.x2Coordinate, this.y2Coordniate);
				message.header = "send_neighbors";
				for (int i = 0; i < newNodesList.size(); i++) {
					try {
						baos = new ByteArrayOutputStream();
						oos = new ObjectOutputStream(new BufferedOutputStream(baos));
						oos.flush();
						oos.writeObject((Object)message);
						oos.close();
						datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, newNodesList.get(i).peerIpAddress, newNodesList.get(i).peerPort));
					} catch (IOException e) {
				
						e.printStackTrace();
					}
					
				}
				newNodesList.clear();
			}

	 }
	 
	 public int findPerfectNeighbor(Peer peer, Peer current){
		 if(  (current.y2Coordniate.equals(peer.y1Coordinate) ) &&
				 (
						 (current.x1Coordinate.equals(peer.x1Coordinate) && current.x2Coordinate.equals(peer.x2Coordinate)) 			  
			
				 )	
			  ){return 1;}//up
		 
		 if(  (current.y1Coordinate.equals(peer.y2Coordniate) ) &&
				 (
						 (current.x1Coordinate.equals(peer.x1Coordinate) && current.x2Coordinate.equals(peer.x2Coordinate)) 			  
			
				 )	
			  ){return 2;}//down
		 
		 if(  (current.x2Coordinate.equals(peer.x1Coordinate) ) &&
				 (
						 (current.y1Coordinate.equals(peer.y1Coordinate) && current.y2Coordniate.equals(peer.y2Coordniate)) 
						 
				 )	
		){return 3;} //right
		
		
		if(  (current.x1Coordinate.equals(peer.x2Coordinate) ) &&
				 (
						 (current.y1Coordinate.equals(peer.y1Coordinate) && current.y2Coordniate.equals(peer.y2Coordniate)) 
						 
				 )	
		){return 4;} //left
		return 5;
	 }
	 
	 public Peer findSmallestNeighbor(){
		 	Peer smallestNeighbor = null;
			Peer currentValue = null;
			boolean isFirst = true;
			double previousArea = 0, currentArea = 0; 
			for (Map.Entry<String, Peer> entry : neighbors.entrySet()) {
				currentValue = entry.getValue();
				currentArea = ((currentValue.x2Coordinate - currentValue.x1Coordinate) * (currentValue.y2Coordniate - currentValue.y1Coordinate));
				if(isFirst || currentArea < previousArea){
					smallestNeighbor = currentValue;
					previousArea = currentArea;
				}
				isFirst = false;
			}
			return smallestNeighbor;
	 }
	 
	 public Peer findZoneToMergeWith(){
		
		Peer peer = null;
		for (Map.Entry<String, Peer> entry : this.neighbors.entrySet()) {
			 peer = entry.getValue();
			 int type = findPerfectNeighbor(peer, this);
			 if(type != 5){
				 return peer;
			 }
		}
		return null;
	 }
	 
	 public void mergeZones(Message message){
		 
		 if(message.mergeNodeType == 1 || message.mergeNodeType == 3){
			 x1Coordinate = message.requestor.x1Coordinate;
			 y1Coordinate = message.requestor.y1Coordinate;
		 }else if(message.mergeNodeType == 2 || message.mergeNodeType == 4){
			 x2Coordinate = message.requestor.x2Coordinate;
			 y2Coordniate = message.requestor.y2Coordniate;
		 }
		 
		 neighbors.remove(message.requestor.name);
		 for (Map.Entry<String, Peer> entry : message.requestor.neighbors.entrySet()) {
			 Peer peer = entry.getValue();
			 if(!peer.name.equals(name))
				 neighbors.put(peer.name, peer);
		 }
		 
		 for (Entry<String, byte[]> entry : message.requestor.contentHashTable.entrySet()) {
			 String name = entry.getKey();
			 byte[] data = entry.getValue();
			 contentHashTable.put(name, data);
		 }
		 
	 }
	 
	 public Message takeover(Message message){
		this.takeover = message.requestor;
		return message;
	 }
	 
	 public void viewInfo(){
		 StringBuilder sb = new StringBuilder();
		 sb.append("----------------------------------------------------");
		 sb.append(System.getProperty("line.separator"));
		 sb.append("Name: "+this.name);
		 sb.append(System.getProperty("line.separator"));
		 sb.append(System.getProperty("line.separator"));
		 sb.append("ipAddress: "+this.peerIpAddress);
		 sb.append(System.getProperty("line.separator"));
		 sb.append(System.getProperty("line.separator"));
		 sb.append("x1: "+this.x1Coordinate+" y1: "+this.y1Coordinate+" x2: "+this.x2Coordinate+" y2: "+this.y2Coordniate);
		 sb.append(System.getProperty("line.separator"));
		 sb.append(System.getProperty("line.separator"));
		 sb.append("---NEIGHBORS---");
		 sb.append(System.getProperty("line.separator"));
		 for (Entry<String, Peer> entry : this.neighbors.entrySet()) {
			 String name = entry.getKey();
			 sb.append(name);
			 sb.append(System.getProperty("line.separator"));
		 }
		 sb.append(System.getProperty("line.separator"));
		 sb.append("---FILES---");
		 sb.append(System.getProperty("line.separator"));
		 for (Entry<String, byte[]> entry : this.contentHashTable.entrySet()) {
			 String name = entry.getKey();
			 sb.append(name);
			 sb.append(System.getProperty("line.separator"));
		 }
		 sb.append(System.getProperty("line.separator"));
		 sb.append("----------------------------------------------------");
		 System.out.println(sb.toString());
	 }
}
