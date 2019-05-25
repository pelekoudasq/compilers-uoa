package components;

import syntaxtree.*;
import visitor.GJNoArguDepthFirst;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class LLVMVisitor extends GJNoArguDepthFirst<String> {

	SymbolTable table;
	Path file;
	String buffer;
	// Class currentClass;
	// Method currentMethod;
	// boolean inMethod;

	void emit(String str) throws IOException {
		Files.write(this.file, str.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}

	String typeJavaToLLVM (String type) {
		if ( type.equals("boolean") ) 
			return "i1";
		else if ( type.equals("int") )
			return "i32";
		else
			return "i8*";
	}

	public LLVMVisitor(SymbolTable table) {
		this.table = table;
		this.buffer = "";
		String llname = "./" + this.table.filename + ".ll";
		this.file = Paths.get(llname);
		
		ArrayList<String> keys = new ArrayList<String>(this.table.cls.keySet());
		for(int i = 0; i < keys.size(); i++) {
			Class tempClass = this.table.cls.get(keys.get(i));
			if ( this.table.filename.equals(keys.get(i)) ) {
				this.buffer = this.buffer + "@." + tempClass.name + "_vtable = global [0 x i8*] []\n";
				continue;
			}
			this.buffer = this.buffer + "@." + tempClass.name + "_vtable = global [" + tempClass.methods.size() + " x i8*] [";
			boolean first = true;
			for (Method m : tempClass.methods) {
				if (!first)
					this.buffer = this.buffer + ", ";
				String retType = typeJavaToLLVM(m.type);
				this.buffer = this.buffer + "i8* bitcast (" + retType + " (i8*";
				String[] argsSignature = m.getSignature().split(",");
				String typeArg;
				for (int j = 0; j < argsSignature.length; j++) {
					typeArg = "";
					if ( argsSignature[j] == null || argsSignature[j].isEmpty() )
						continue;
					typeArg = typeJavaToLLVM(argsSignature[j]);
					this.buffer = this.buffer + "," + typeArg;
				}
				this.buffer = this.buffer + ")* @" + tempClass.name + "." + m.name + " to i8*)";
				first = false;
			}
			this.buffer = this.buffer + "]\n";
		}
		this.buffer = this.buffer + "\n\ndeclare i8* @calloc(i32, i32)\ndeclare i32 @printf(i8*, ...)\ndeclare void @exit(i32)\n\n@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\ndefine void @print_int(i32 %i) {\n\t%_str = bitcast [4 x i8]* @_cint to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n\tret void\n}\n\ndefine void @throw_oob() {\n\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str)\n\tcall void @exit(i32 1)\n\tret void\n}\n\n";
	}

	public String visit(Goal n) {
		n.f0.accept(this);
		n.f1.accept(this);
		n.f2.accept(this);
		try {
			this.emit(this.buffer);
		} catch(IOException ex) {
			System.out.println(ex.getMessage());
		}
		return null;
	}

	/** * f1 -> Identifier() * f11 -> Identifier() * f14 -> ( VarDeclaration() )* * f15 -> ( Statement() )* */
	public String visit(MainClass n) {
		this.buffer = this.buffer + "define i32 @main() {\n";
		n.f11.accept(this);
		this.buffer = this.buffer + "}\n\n";
		return null;
	}

}