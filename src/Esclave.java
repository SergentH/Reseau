import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Esclave {

	public void supprimer(File f) throws FileNotFoundException {
		if (f.isDirectory()) {
			for (File c : f.listFiles()) {
				supprimer(c);
			}
		}
		if (!f.delete()) {
			throw new FileNotFoundException("Failed to delete file: " + f);
		}
	}

	public static void main(String[] args) {
		/*
		 * hote:port PathEsclave PathServeur
		 */
		String infosConnection = args[0];
		String[] infos = infosConnection.split(":");
		String serveur = infos[0];
		int port = Integer.parseInt(infos[1]);

		String PathEsclave = args[1];
		String PathServeur = args[2];

		try {
			System.out.println("Connected");
			Socket esclave = new Socket(serveur, port);
			FileWalker EsclaveWalker = new FileWalker();
			OutputStream clientout = esclave.getOutputStream();
			BufferedWriter pout = new BufferedWriter(new OutputStreamWriter(clientout));
			InputStream clientInput = esclave.getInputStream();
			BufferedReader pin = new BufferedReader(new InputStreamReader(clientInput));

			/*
			 * Envoi des infos pour savoir que c'est un pull
			 */
			String envoiInfos = PathServeur + "*pull"; // Quel dossier on veut du serveur
			System.out.println(envoiInfos);
			pout.write(envoiInfos);
			pout.flush();
			pout.newLine();

			String ServerWalker = pin.readLine();
			File PathsServeur = new File("D:/FileTreeEsclave.txt");// création d'un fichier tampon
			ArrayList<Fichier> fichiersServeur = new ArrayList<Fichier>(); // liste des fichiers du serveur
			EsclaveWalker.savePath(PathsServeur.toPath(), ServerWalker);
			fichiersServeur = EsclaveWalker.getFiles(PathsServeur.toPath());
			PathsServeur.delete(); // on supprime ce fichier tampon qui ne sera plus utiliser

			ArrayList<Fichier> fichiersLocaux = new ArrayList<Fichier>();
			fichiersLocaux = EsclaveWalker.walk(Paths.get(PathEsclave));

			/*
			 * Si un fichier est présent sur le client mais pas sur ceux du serveur on doit
			 * le supprimer
			 */
			for (Fichier flocaux : fichiersLocaux) {
				boolean present = false;

				for (Fichier fserveur : fichiersServeur) {
					if (flocaux.equals(fserveur)) {
						present = true;
					}
				}
				if (present == false) {
					File f = new File(PathEsclave +"/"+ flocaux.getPath().toString());
					Esclave e = new Esclave();
					e.supprimer(f);
				}
			}
			
			fichiersLocaux = EsclaveWalker.walk(Paths.get(PathEsclave));
			File APull = new File("D:/FileTreeEsclave.txt");// création d'un fichier tampon
			EsclaveWalker.sendPath(Paths.get(PathEsclave), APull.toPath(), esclave); 
			APull.delete(); //on supprime ce fichier qui ne sera plus utiliser
			
			Path pathToReceive = Paths.get(PathEsclave+"/");
			String read;
			while ((read =pin.readLine()) != "STOP") {
				System.out.println(read);
				pout.write("ok");
				pout.flush();
				pout.newLine();
				EsclaveWalker.saveFile(esclave, pathToReceive);
			}

			esclave.close();
		} catch (UnknownHostException e) {
			System.out.println("UnknownHostException");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException");
			e.printStackTrace();
		}
	}

}
