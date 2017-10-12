import java.net.Socket;

public interface PSI {		
	public void sendDB(Socket socket);		
	
	public void answerQuery(Socket socket);	
}
