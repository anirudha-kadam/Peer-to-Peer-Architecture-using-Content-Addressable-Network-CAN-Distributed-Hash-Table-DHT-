
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Peer recentSender = null;
	public Peer activeNode = null;
	public Peer requestor = null;
	public String senderType = null;
	public String header = null;
	public String peerName = null;
	public String fileName = null;
	public String mergeType = null;
	public List<Peer> path = null;
	public Double randomX = null;
	public Double randomY = null;
	public int mergeNodeType;
	public byte fileContent [] = null;
	public String replaceFor = null;
	public Map<String, Peer> myNeighbors = null;
	public Message() {
		path = new ArrayList<Peer>();
		senderType = new String();
		header  = new String();
		activeNode = new Peer();
		recentSender = new Peer();
		requestor = new Peer();
		peerName = new String();
		mergeType  = new String();
		myNeighbors = new HashMap<String, Peer>();
		mergeNodeType = 0;
		replaceFor = new String();
	}
	
}
