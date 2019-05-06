package components;

import java.util.*;

public class Method {

	public String type;
	public String name;
	public List<Variable> params;
	public List<Variable> vars;

	public Method(String type, String name) {
		this.type = type;
		this.name = name;
		this.params = new ArrayList<>();
		this.vars = new ArrayList<>();
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
}
