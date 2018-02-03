import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class FileWalker {
	static int depart = 0;
	static int longueurPath;

	public ArrayList<Fichier> walk(Path path) {
		final int passage = depart++;

		ArrayList<Fichier> Fichiers = new ArrayList<Fichier>();

		File root = new File(path.toString());
		File[] list = root.listFiles();

		String[] pathData = path.toString().split("/");

		int longueur = 0;
		int nbString = 0;

		if (passage == 0) {
			for (@SuppressWarnings("unused")
			String s : pathData) {
				nbString++;
			}
			for (int i = 0; i < nbString; i++) {
				longueur = longueur + (pathData[i].length()) + 1;
				longueurPath = longueur;
			}
		}

		final int aDecouper = longueurPath;
		if (list != null) {
			for (File f : list) {
				Fichier nouveauFichier = new Fichier();
				nouveauFichier.setLastModify(f.lastModified());
				String filePath = f.getAbsolutePath();
				nouveauFichier.setLongeur(f.length());
				if (f.isDirectory()) {
					nouveauFichier.setIsDirectory(true);

					nouveauFichier.setPath(Paths.get(filePath.substring(aDecouper)));
					Fichiers.add(nouveauFichier);
					Fichiers.addAll(walk(Paths.get(filePath)));
				} else {
					nouveauFichier.setIsDirectory(false);
					nouveauFichier.setPath(Paths.get(filePath.substring(aDecouper)));
					Fichiers.add(nouveauFichier);
				}
			}
		}
		return Fichiers;
	}

	/*
	 * save a file after receiving from the server
	 * 
	 */
	@SuppressWarnings("resource")
	public void savePath(Path WhereToSave, String message) throws IOException {
		FileWriter writer = new FileWriter(WhereToSave.toString());
		writer.write(message);
		writer.flush();
		writer.close();
		/*
		 * System.out.println("getFiles Attention les yeux");
		 * 
		 * ArrayList<Fichier> fichiers = getFiles(WhereToSave); for(Fichier fichier :
		 * fichiers) {
		 * System.out.println(fichier.getPath()+" "+fichier.getLastModify()+" "+fichier.
		 * getIsDirectory()); }
		 */

		System.out.println("Paths save done");
	}

	/*
	 * A UTILISER APRES UN SAVEPATH Methode qui permet de convertir un fichier de
	 * path en objet ArrayList<Fichier>
	 * 
	 * exemple d'utilisation dans savePath en commentaire
	 */
	public ArrayList<Fichier> getFiles(Path file) throws IOException {
		ArrayList<Fichier> Fichiers = new ArrayList<Fichier>();
		String total = null;
		boolean premierPassage = true;
		BufferedReader in = new BufferedReader(new FileReader(file.toString()));

		String str;
		while ((str = in.readLine()) != null) {
			if (premierPassage) {
				total = str;
				premierPassage = false;
			} else {
				total = total + str;
			}

		}
		in.close();
		// total prends bien tout
		// System.out.println("total :"+ total);

		String[] firstSplit = total.split("\\^");
		boolean infos = true;
		for (String premiere : firstSplit) {
			System.out.println("firstsplit: " + premiere);

			if (infos == false) {
				String[] seconde = premiere.split("\\*");
				Fichier nouveauFichier = new Fichier();

				int i = 0;
				for (String troisieme : seconde) {
					if (i == 0) {
						nouveauFichier.setPath(Paths.get(troisieme));
					} else if (i == 1) {
						nouveauFichier.setLastModify(Long.parseLong(troisieme));
					} else if (i == 2) {
						nouveauFichier.setIsDirectory(Boolean.parseBoolean(troisieme));
					} else if (i == 3) {
						nouveauFichier.setLongeur(Long.parseLong(troisieme));
					}
					i++;
				}
				Fichiers.add(nouveauFichier);
			} else {
				infos = false;
			}
		}
		return Fichiers;
	}

	/*
	 * send a file to the client FileToSend = path : File must be serialized before
	 * using this method
	 */

	public void sendPath(Path WhereToWalk, Path FileToSend, Socket s) throws IOException {
		FileWalker walker = new FileWalker();
		ArrayList<Fichier> paths = walker.walk(WhereToWalk);

		FileWriter writer = new FileWriter(FileToSend.toString());
		for (Fichier fl : paths) {
			writer.write("^");
			writer.write(fl.getPath().toString());
			writer.write("*");
			writer.write(Long.toString(fl.getLastModify()));
			writer.write("*");
			writer.write(Boolean.toString(fl.getIsDirectory()));
			writer.write("*");
			writer.write(Long.toString(fl.getLongeur()));
			writer.flush();
		}
		writer.close();
		
		OutputStream out = s.getOutputStream();
		BufferedWriter sender = new BufferedWriter(new OutputStreamWriter(out));

		BufferedReader in = new BufferedReader(new FileReader(FileToSend.toString()));
		String str;
		while ((str = in.readLine()) != null) {
			sender.write(str);
			sender.flush();
			sender.newLine();
		}

		in.close();

		System.out.println("Paths sent");
	}

	public void sendFile(Fichier file, Socket s, Path FromWhere) throws IOException, InterruptedException {
		System.out.println("send File");
		OutputStream out = s.getOutputStream();
		BufferedWriter pout = new BufferedWriter(new OutputStreamWriter(out));
		InputStream clientInput = s.getInputStream();
		BufferedReader buffReadin = new BufferedReader(new InputStreamReader(clientInput));

		System.out.println(file.getPath());
		pout.write(file.getPath().toString());
		pout.flush();
		pout.newLine();
		
		String reponse = buffReadin.readLine();
		System.out.println(reponse);

		BufferedReader in = new BufferedReader(new FileReader((FromWhere + file.getPath().toString())));
		String str;

		while ((str = in.readLine()) != null) {
			System.out.println(str);
			pout.write(str);
			pout.flush();
			pout.newLine();
		}
		
		in.close();

		System.out.println("Fin send File");
	}


	public void saveFile(Socket clientSocket,Path General) throws IOException {
		System.out.println("save File");
		InputStream in = clientSocket.getInputStream();
		BufferedReader buffReadin = new BufferedReader(new InputStreamReader(in));
		OutputStream out = clientSocket.getOutputStream();
		BufferedWriter pout = new BufferedWriter(new OutputStreamWriter(out));

		String path = buffReadin.readLine();
		System.out.println(path);

		pout.write("path recu");
		pout.flush();
		pout.newLine();
		
		FileWriter writer = new FileWriter(General + path);

		String read;

		while ((read = buffReadin.readLine()) != null) {
			writer.write(read);
			writer.flush();
		}

		String saved = "Saved";
		writer.write(saved);
		writer.flush();
		writer.close();
		
		System.out.println("fin save File");
	}

}
