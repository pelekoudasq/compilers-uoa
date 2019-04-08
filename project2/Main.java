import java.io.*;

public class Main {

	public static void main (String [] args) {
		if (args.length < 1) {
			System.err.println("Usage: java [MainClassName](Main) [file1] [file2] ... [fileN]");
			System.exit(1);
		}

		FileInputStream fis = null;

		for (String filename : args) {
			try {
				fis = new FileInputStream(filename);
				MiniJavaParser parser = new MiniJavaParser(fis);
				fis.close();
			}
			catch (Exception ex) {
				System.err.println(ex.getMessage());
			}
		}
	}
}