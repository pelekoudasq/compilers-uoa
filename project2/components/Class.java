package components;

import java.util.*;

public class Class {
	
	public String name;
	public String parentName;
	public List<Variable> vars;
	public List<Method> methods;

	public Class(String name, String parentName){
		this.name = name;
		this.parentName = parentName;
		this.vars = new ArrayList<>();
		this.methods = new ArrayList<>();
	}

	public boolean putVar(Variable newVar) {
		for (Variable v : this.vars) {
			if (v.name.equals(newVar.name))
				return false;
		}
		this.vars.add(newVar);
		return true;
	}

	public boolean putMeth(Method meth) {
		for (Method m : this.methods) {
			if (m.name.equals(meth.name))
				return false;
		}
		this.methods.add(meth);
		return true;
	}

	public Method getMeth(String name) {
		for (Method m : this.methods) {
			if (m.name.equals(name))
				return m;
		}
		return null;
	}
}
