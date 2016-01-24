

public class BootstrapDriver {

	/**
	 * bootstrap execution starts here
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length != 1){
			System.err.println("Exactly 1 argument is expected: bootstrap Port");
			System.exit(0);
		}
		int port = 0;
		try {
			port = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.err.println("Port Number should be an integer"+e.getMessage());
			System.exit(0);
		}
			
		BootstrapSocket bootstrapSocket = new BootstrapSocket(port);
		bootstrapSocket.startBootstrapServer();
	}

}
