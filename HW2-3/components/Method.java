package components;

import java.util.*;

public class Method {

	public String type;
	public String name;
	public List<Variable> params;
	public List<Variable> vars;
	public int offset;

	public Method(String type, String name) {
		this.type = type;
		this.name = name;
		this.params = new ArrayList<>();
		this.vars = new ArrayList<>();
		this.offset = 0;
	}

	public boolean putParam(Variable newVar) {
		for (Variable v : this.params) {
			if (v.name.equals(newVar.name))
				return false;
		}
		this.params.add(newVar);
		return true;
	}

	public boolean putVar(Variable newVar) {
		for (Variable varr : this.vars) {
			if (varr.name.equals(newVar.name))
				return false;
		}
		for (Variable param : this.params) {
			if (param.name.equals(newVar.name))
				return false;
		}
		this.vars.add(newVar);
		return true;
	}

	public Variable getVar(String name) {
		for (Variable varr : this.vars) {
			if (varr.name.equals(name))
				return varr;
		}
		for (Variable param : this.params) {
			if (param.name.equals(name))
				return param;
		}
		return null;
	}

	public String getSignature() {
		String paramSign = "";
		for (Variable param : this.params) {
			paramSign = paramSign + param.type + ",";
		}
		return paramSign;
	}
}
