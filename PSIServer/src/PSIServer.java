import java.io.*;
import java.net.*;

public class PSIServer {  
    private static final int PORT=8000;  
    private static PSI psi;
    private static ServerSocket serverSocket;
    private static InetAddress inetAddress;
    
    private static final int DB_SIZE = (int)Math.pow(2, 15);
    
    public static void main(String[] args) {    
    	psi = new PSIRSA(DB_SIZE);
    //	psi = new PSIDH(DB_SIZE);
    // 	psi = new PSIGC(DB_SIZE);
    // 	psi = new PSINR(DB_SIZE);
    //	psi = new PSIDH2(DB_SIZE);
    	
    	listen();
    } 
    
    private static void listen() {
		try {
			serverSocket = new ServerSocket(PORT);
			inetAddress = InetAddress.getByName(null);  			  
	        System.out.println("Server@"+inetAddress+" start!");  
	        while(true){  
                Socket socket = serverSocket.accept();// listen PORT;   
                new ServerOne(socket, psi);  
            }  
		} catch (IOException e) {
			e.printStackTrace();
		} finally{  
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}  
            System.out.println("Server stop!");  
        } 
    }
}  

class ServerOne extends Thread {  		
	private Socket socket;  
    
    private PSI psi; 
    
    public ServerOne(Socket socket, PSI psi) throws IOException {  
        this.socket = socket;  
        this.psi = psi;
        
        start();  
    }  
    
    public void run(){  
    	while(true) {  
    		String type = Utils.receiveString(socket);
    		System.out.println("client: " + type);
    		
    		if (type == null) {
    			break;
    		} else if (type.equals("END")) { 
    			System.out.println("------------------------------------");
    			break;  
    		} else if (type.equals("DB")) { // send DB
               	System.out.println("Sending...");
               	psi.sendDB(socket);    
               	System.out.println("DB has been sent!");
    		} else if (type.equals("QUERY")){ // test an App
               	psi.answerQuery(socket);   
               	System.out.println("Query has been answered!");
    		} else {
    			break;
    		}
    	}  
    	try{  
    		socket.close();  
    	}catch(IOException e){
    		e.printStackTrace();
    	}   
    }  
} 