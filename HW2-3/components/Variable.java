package components;

public class Variable {

	public String name;
	public String type;
	public int offset;

	public Variable(String type, String name) {
		this.type = type;
		this.name = name;
		this.offset = 0;
	}
	
}
