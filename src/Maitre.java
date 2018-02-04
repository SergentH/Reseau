import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;

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
			System.out.println("Connected");
			Socket maitre = new Socket(serveur, port);

			FileWalker MaitreWalker = new FileWalker();
			
			OutputStream clientout = maitre.getOutputStream();
			BufferedWriter pout = new BufferedWriter(new OutputStreamWriter(clientout));
			InputStream clientInput = maitre.getInputStream();
			BufferedReader pin = new BufferedReader(new InputStreamReader(clientInput));
			
			/*
			 * Envoi des infos pour savoir que c'est un pull
			 */
			System.out.println("envoi des infos");
			String envoiInfos = PathServeur + "*push"; // Quel dossier on veut du serveur
			System.out.println(envoiInfos);
			pout.write(envoiInfos);
			pout.newLine();
			pout.flush();
			
			System.out.println("envoi des infos done");
			
			System.out.println("Reception des chemins du serveur");
			String ServerWalker = pin.readLine();
			File PathsServeur = new File("D:/FileTreeEsclave.txt");// création d'un fichier tampon
			ArrayList<Fichier> fichiersServeur = new ArrayList<Fichier>(); // liste des fichiers du serveur
			MaitreWalker.savePath(PathsServeur.toPath(), ServerWalker);
			fichiersServeur = MaitreWalker.getFiles(PathsServeur.toPath());
			PathsServeur.delete(); // on supprime ce fichier tampon qui ne sera plus utiliser
			
			System.out.println("Création des fichiers du client");
			ArrayList<Fichier> fichiersLocaux = new ArrayList<Fichier>();
			fichiersLocaux = MaitreWalker.walk(Paths.get(PathMaitre));
			
			String pathToSend = PathMaitre.toString() + "/";
			System.out.println(pathToSend.toString());
			
			for (Fichier fserveur : fichiersLocaux) {
				pout.write("ok");
				pout.newLine();
				pout.flush();
				boolean send = false;
				for (Fichier f : fichiersServeur) {
					if (f.getPath().toString().equals(fserveur.getPath().toString())) {
						if (fserveur.getLastModify() >= f.getLastModify()) {
							try {
								MaitreWalker.sendFile(fserveur, clientout, pout, pathToSend);
								send = true;
							} catch (InterruptedException e) {
								System.out.println("Erreur lors de l'envoi du fichier");
								e.printStackTrace();
							}
						}
					}
				}
				if (send == false) {
					try {
						MaitreWalker.sendFile(fserveur, clientout, pout, pathToSend);
					} catch (InterruptedException e) {
						System.out.println("Erreur lors de l'envoi du fichier");
						e.printStackTrace();
					}
				}

				send = false;
			}
			
			pout.write("STOP");
			pout.newLine();
			pout.flush();
			
			
			maitre.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
