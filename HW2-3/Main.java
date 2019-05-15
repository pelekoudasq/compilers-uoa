import java.io.*;
import components.*;
import visitor.*;
import syntaxtree.*;

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
				Node root = parser.Goal();
				SymbolTable st = new SymbolTable();
				ClassVisitor cv = new ClassVisitor(st);
				root.accept(cv);
				FillClassVisitor fcv = new FillClassVisitor(st);
				root.accept(fcv);
				CheckVisitor ckv = new CheckVisitor(st);
				root.accept(ckv);
				st.printOffsets(filename);
				fis.close();
			}
			catch (Exception ex) {
				System.err.println(ex.getMessage());
			}
		}
	}
}