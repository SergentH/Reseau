import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Maitre {
	
	public static void main(String[] args) {
		/*
		 * hote:port PathMaitre PathServeur
		 */
		String infosConnection = args[0];
		String[] infos = infosConnection.split(":");
		String serveur = infos[0];
		int port = Integer.parseInt(infos[1]);
		
		String PathMaitre = args[1];
		String PathServeur = args[2];

		try {
			Socket Maitre = new Socket(serveur, port);

			System.out.println(args[1]);

			Maitre.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
