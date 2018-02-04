import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Serveur implements Runnable {

	Socket clientSocket;
	static Path ServeurPath;

	/*
	 * Constructeur de la classe
	 */
	Serveur(Socket cSocket) {
		this.clientSocket = cSocket;
	}

	public static void main(String[] args) {
		/*
		 * Définiton du port du serveur passé en 1er argument java Serveur port
		 * PathServeur
		 */
		Integer port;
		if (args.length <= 0) {
			port = new Integer("12345");
			ServeurPath = Paths.get("D:/Default");
		} else {
			port = new Integer(args[0]);
			ServeurPath = Paths.get(args[1]);
		}

		System.out.println("Serveur On");

		try {
			// ouverture du socket serveur
			@SuppressWarnings("resource")
			ServerSocket ss = new ServerSocket(port.intValue());
			while (true) {
				// attente d'un client
				Socket client = ss.accept();
				System.out.println("Connected");
				// Nouveau thread avec le nouveau client
				new Thread(new Serveur(client)).start();
			} // fin while
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {

		try {
			System.out.println("Starting Thread");
			// Reception des options provenant du client
			OutputStream serveurOut = clientSocket.getOutputStream();
			BufferedWriter pout = new BufferedWriter(new OutputStreamWriter(serveurOut));
			InputStream serveurInput = clientSocket.getInputStream();
			BufferedReader pin = new BufferedReader(new InputStreamReader(serveurInput));
			System.out.println("Reception des infos");
			String message = pin.readLine();
			System.out.println(message);
			String[] commande = message.split("\\*");

			// Le dossier auquel le client refere
			Path Racine = Paths.get(commande[0]);
			// soit push soit pull
			String ToDo = commande[1];

			System.out.println(Racine + " " + ToDo);

			if (ToDo.equals("pull")) {

				System.out.println("Starting Pull");
				// Referencement des fichiers sur le serveur depuis la racine
				System.out.println("Referencement des fichiers sur le serveur depuis la racine ");
				FileWalker walkerToPull = new FileWalker();
				File PathsToPull = new File("D:/FileTree.txt");// création d'un fichier tampon
				walkerToPull.sendPath(Racine, PathsToPull.toPath(), pout);
				PathsToPull.delete(); // on supprime ce fichier qui ne sera plus utiliser

				System.out.println("Reception des Paths des fichiers à envoyer");
				String ServerWalker = pin.readLine();

				ArrayList<Fichier> fichiersClient = new ArrayList<Fichier>(); // liste des fichiers du serveur

				if (!ServerWalker.equals("empty")) {
					File PathsServeur = new File("D:/FileTree2.txt");// création d'un fichier tampon

					walkerToPull.savePath(PathsServeur.toPath(), ServerWalker);
					fichiersClient = walkerToPull.getFiles(PathsServeur.toPath());
					PathsServeur.delete(); // on supprime ce fichier tampon qui ne sera plus utiliser
				}

				System.out.println("Envoi des fichiers");
				ArrayList<Fichier> fichiersServeur = walkerToPull.walk(Racine);
				String pathToSend = Racine.toString() + "/";
				System.out.println(pathToSend.toString());

				for (Fichier fserveur : fichiersServeur) {
					boolean send = false;
					for (Fichier f : fichiersClient) {
						if (f.getPath().toString().equals(fserveur.getPath().toString())) {
							if (fserveur.getLastModify() >= f.getLastModify()) {
								try {
									walkerToPull.sendFile(fserveur, pout, pin, pathToSend);
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
							walkerToPull.sendFile(fserveur, pout, pin, pathToSend);
						} catch (InterruptedException e) {
							System.out.println("Erreur lors de l'envoi du fichier");
							e.printStackTrace();
						}
					}

					send = false;

				}

				

			} else if (ToDo.equals("push")) {

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
