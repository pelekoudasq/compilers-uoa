package components;

import java.util.HashMap;

public class SymbolTable {

	public HashMap<String, Class> cls;

	public SymbolTable() {
		cls = new HashMap<>();
	}

	public Class getClass(String className) {
		return cls.get(className);
	}
}
