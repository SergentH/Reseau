import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
	public void savePath(Path WhereToSave, String message) throws IOException {
		FileWriter writer = new FileWriter(WhereToSave.toString());
		writer.write(message);
		writer.flush();
		writer.close();
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
	public void sendPath(Path WhereToWalk, Path FileToSend, BufferedWriter sender) throws IOException {
		FileWalker walker = new FileWalker();
		ArrayList<Fichier> paths = walker.walk(WhereToWalk);

		if (!paths.isEmpty()) {
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

			BufferedReader in = new BufferedReader(new FileReader(FileToSend.toString()));
			String str;
			while ((str = in.readLine()) != null) {
				sender.write(str);
				sender.newLine();
				sender.flush();
			}

			in.close();
		} else {
			FileWriter writer = new FileWriter(FileToSend.toString());
			writer.write("empty");

			writer.close();

			BufferedReader in = new BufferedReader(new FileReader(FileToSend.toString()));
			String str;
			while ((str = in.readLine()) != null) {
				sender.write(str);
				sender.newLine();
				sender.flush();
			}
			in.close();

		}

		System.out.println("Paths sent");
	}

	public void sendFile(Fichier file, BufferedWriter pout, BufferedReader buffReadin, String FromWhere)
			throws IOException, InterruptedException {
		System.out.println("send File");

		System.out.println("send file : " + file.getPath().toString());
		pout.write(file.getPath().toString());
		pout.newLine();
		pout.flush();

		BufferedReader in = new BufferedReader(new FileReader((FromWhere + file.getPath().toString())));
		System.out.println(FromWhere + file.getPath().toString());
		String str;

		while ((str = in.readLine()) != null) {
			System.out.println(str);
			pout.write(str);
			pout.newLine();
			pout.flush();
		}
		in.close();


		System.out.println("Fin send File");
	}

	public void saveFile(BufferedWriter pout, BufferedReader buffReadin, String General) throws IOException {
		System.out.println("save File");

		String path = buffReadin.readLine();
		System.out.println(path);

		System.out.println(General + path);
		File newFile = new File(General + path);
		FileWriter writer = new FileWriter(newFile);

		/*
		String read = buffReadin.readLine();
		while (read != null) {
			System.out.println("ecriture dans le fichier");
			writer.write(read);
			writer.flush();
			read = buffReadin.readLine();
		}
		*/
		for (String line = buffReadin.readLine(); line != null; line = buffReadin.readLine()) {
			System.out.println("ecriture dans le fichier");
			writer.write(line);
			writer.flush();
		}

		System.out.println("fin save File");
	}

}
