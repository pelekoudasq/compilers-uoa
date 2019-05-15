package components;

import java.util.*;

public class Class {
	
	public String name;
	public String parentName;
	public List<Variable> vars;
	public List<Method> methods;
	public int varOffset;
	public int methOffset;

	public Class(String name, String parentName){
		this.name = name;
		this.parentName = parentName;
		this.vars = new ArrayList<>();
		this.methods = new ArrayList<>();
		this.varOffset = 0;
		this.methOffset = 0;
	}

	public boolean putVar(Variable newVar) {
		for (Variable v : this.vars)
			if (v.name.equals(newVar.name))
				return false;
		this.vars.add(newVar);
		return true;
	}

	public boolean putMeth(Method meth, SymbolTable table) {
		for (Method m : this.methods)
			if (m.name.equals(meth.name))
				return false;
		Method tempMeth = getMeth(meth.name, table, 0);
		if ( tempMeth != null ) {
			if ( !meth.getSignature().equals(tempMeth.getSignature()) )
				return false;
			return true;
		}
		this.methods.add(meth);
		return true;
	}

	public Method getMeth(String name, SymbolTable table, int depth) {
		for (Method m : this.methods)
			if (m.name.equals(name))
				return m;
		if ( this.parentName != null && (depth > 1 || depth <= 0 ))
			return table.getClass(parentName).getMeth(name, table, depth-1);
		return null;
	}

	public Variable getVar(String name, SymbolTable table) {
		for (Variable v : this.vars)
			if (v.name.equals(name))
				return v;
		if ( this.parentName != null )
			return table.getClass(parentName).getVar(name, table);
		return null;
	}

	public boolean inFamilyHistory(String quest, SymbolTable table) {
		if ( this.name.equals(quest) )
			return true;
		else {
			if ( this.parentName == null )
				return false;
			return table.getClass(parentName).inFamilyHistory(quest, table);
		}
	}

	public void printOffsets() {
		System.out.println("-----------Class " + this.name + "-----------");
		System.out.println("--Variables---");
		for (Variable v : this.vars)
			System.out.println(this.name + "." + v.name + " : " + v.offset);
		System.out.println("--Methods---");
		for (Method m : this.methods)
			System.out.println(this.name + "." + m.name + " : " + m.offset);
		System.out.println("");
		return;
	}
}
