import java.nio.file.Path;

public class Fichier {
	public boolean isDirectory;
	public long lastModify;
	public Path path;
	public long longeur;
	
	public long getLongeur() {
		return longeur;
	}
	public void setLongeur(long longeur) {
		this.longeur = longeur;
	}
	public boolean getIsDirectory() {
		return isDirectory;
	}
	public void setIsDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}
	public long getLastModify() {
		return lastModify;
	}
	public void setLastModify(long lastModify) {
		this.lastModify = lastModify;
	}
	public Path getPath() {
		return path;
	}
	public void setPath(Path path) {
		this.path = path;
	}
}
