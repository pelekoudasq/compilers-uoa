package components;

import java.util.*;

public class SymbolTable {

	public Map<String, Class> cls;

	public SymbolTable() {
		cls = new LinkedHashMap<>();
	}

	public Class getClass(String className) {
		return cls.get(className);
	}

	public void printOffsets(String filename) {
		ArrayList<String> keys = new ArrayList<String>(cls.keySet());
		filename = filename.substring(filename.lastIndexOf("/") + 1);
		filename = filename.substring(0, filename.indexOf("."));
		for(int i = 0; i < keys.size(); i++) {
			if ( !filename.equals(keys.get(i)) )
				cls.get(keys.get(i)).printOffsets();
		}
		System.out.println("");
		return;
	}
}
