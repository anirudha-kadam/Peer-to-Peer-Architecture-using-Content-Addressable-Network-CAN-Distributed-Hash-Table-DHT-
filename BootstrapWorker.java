

import java.io.BufferedInputStream;


import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



/**
 * 
 * @author anirudha
 *
 */
public class BootstrapWorker implements Runnable {
	
	DatagramSocket datagramSocket = null;
	DatagramPacket datagramPacket = null;
	ByteArrayOutputStream baos = null;
	ObjectOutputStream oos = null;
	ByteArrayInputStream bais = null;
	ObjectInputStream ois = null;
	byte dataBuffer[] = null;
	List<Peer> activeNodesList = null;
	int totalActiveNodes = 0;
	Message message = null;
	
	/**
	 * parameterized constructor
	 * @param datagramSocketIn
	 */
	public BootstrapWorker(DatagramSocket datagramSocketIn) {
		message = new Message();
		datagramSocket = datagramSocketIn;
		dataBuffer = new byte[65536];
		datagramPacket = new DatagramPacket(dataBuffer, dataBuffer.length);
		activeNodesList = new ArrayList<Peer>();
	}
	
	@Override
	public void run() {
		System.out.println("Welcome to Bootstrap\n Bootstrap ip: "+datagramSocket.getLocalAddress()+"\n Bootstrap port: "+datagramSocket.getLocalPort());
		while(true){
			try {
				datagramSocket.receive(datagramPacket);
				bais = new ByteArrayInputStream(dataBuffer, datagramPacket.getOffset(), datagramPacket.getLength());
				ois = new ObjectInputStream(new BufferedInputStream(bais));
				message = (Message) ois.readObject();
				baos  = new ByteArrayOutputStream();
				oos  = new ObjectOutputStream(new BufferedOutputStream(baos));
	            if(message.header.equals("join")){
	            	if(totalActiveNodes == 0){
	            		message.senderType = "bootstrap";
	    				message.header = "all_yours";
	    				activeNodesList.add(message.recentSender);
	    				totalActiveNodes++;
	            	}else if(totalActiveNodes == 1){
	            		message.senderType = "bootstrap";
	    				message.header = "active_node";
	    				message.activeNode = activeNodesList.get(0);
	    				activeNodesList.add(message.recentSender);
	    				totalActiveNodes++;
	            	}else if(totalActiveNodes >= 10){
	            		message.senderType = "bootstrap";
	    				message.header = "network_full";
	            	}else {
	            		message.senderType = "bootstrap";
	    				message.header = "active_node";
	    				message.activeNode = activeNodesList.get(new Random().nextInt(totalActiveNodes - 1));
	    				activeNodesList.add(message.recentSender);
	    				totalActiveNodes++;
	            	}
	            }else if(message.header.equals("leaving_network")){
	            	for(int i = 0; i < activeNodesList.size(); i++){
	            		if(activeNodesList.get(i).name.equals(message.recentSender.name)){
	            			activeNodesList.remove(i);
	            		}
	            	}
	            }
	            
	            oos.writeObject((Object)message);
	            oos.flush();
	            oos.close();
	            datagramSocket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, datagramPacket.getAddress(), datagramPacket.getPort()));
				
			} catch (IOException e) {
				System.err.println("Found IOException: "+e.getMessage());
			} catch (ClassNotFoundException e) {
				System.err.println("Found ClassNotFoundException: "+e.getMessage());
			}
		}
		
	}

}
