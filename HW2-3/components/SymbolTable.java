package components;

import java.util.*;

public class SymbolTable {

	public Map<String, Class> cls;
	public String filename;

	public SymbolTable(String filename) {
		cls = new LinkedHashMap<>();
		this.filename = filename.substring(filename.lastIndexOf("/") + 1);
		this.filename = this.filename.substring(0, this.filename.indexOf("."));
	}

	public Class getClass(String className) {
		return cls.get(className);
	}

	public void printOffsets() {
		ArrayList<String> keys = new ArrayList<String>(cls.keySet());
		for(int i = 0; i < keys.size(); i++) {
			if ( !this.filename.equals(keys.get(i)) )
				cls.get(keys.get(i)).printOffsets();
		}
		System.out.println("");
		return;
	}
}
