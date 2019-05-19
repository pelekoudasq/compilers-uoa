package components;

import syntaxtree.*;
import visitor.GJNoArguDepthFirst;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class LLVMVisitor extends GJNoArguDepthFirst<String> {

	SymbolTable table;
	Path file;
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
		String llname = "./" + this.table.filename + ".ll";
		this.file = Paths.get(llname);
		
		ArrayList<String> keys = new ArrayList<String>(this.table.cls.keySet());
		for(int i = 0; i < keys.size(); i++) {
			Class tempClass = this.table.cls.get(keys.get(i));
			if ( this.table.filename.equals(keys.get(i)) ) {
				try {
					this.emit("@." + tempClass.name + "_vtable = global [0 x i8*] []\n");
					continue;
				} catch(IOException ex) {
					System.out.println(ex.getMessage());
				}
			}
			try {
				this.emit("@." + tempClass.name + "_vtable = global [" + tempClass.methods.size() + " x i8*] [");
			} catch(IOException ex) {
				System.out.println(ex.getMessage());
			}
			boolean first = true;
			for (Method m : tempClass.methods) {
				if (!first) {
					try {
						this.emit(", ");
					} catch(IOException ex) {
						System.out.println(ex.getMessage());
					}
				}
				String retType = typeJavaToLLVM(m.type);
				try {
					this.emit("i8* bitcast (" + retType + " (i8*");
				} catch(IOException ex) {
					System.out.println(ex.getMessage());
				}
				String[] argsSignature = m.getSignature().split(",");
				String typeArg;
				for (int j = 0; j < argsSignature.length; j++) {
					typeArg = "";
					if ( argsSignature[j] == null || argsSignature[j].isEmpty() )
						continue;
					typeArg = typeJavaToLLVM(argsSignature[j]);
					try {
						this.emit(","+typeArg);
					} catch(IOException ex) {
						System.out.println(ex.getMessage());
					}
				}
				try {
					this.emit(")* @" + tempClass.name + "." + m.name + " to i8*)");
				} catch(IOException ex) {
					System.out.println(ex.getMessage());
				}
				
				first = false;
			}
			
			try {
				this.emit("]\n");
			} catch(IOException ex) {
				System.out.println(ex.getMessage());
			}
		}

		try {
			this.emit("\n\ndeclare i8* @calloc(i32, i32)\ndeclare i32 @printf(i8*, ...)\ndeclare void @exit(i32)\n\n@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\ndefine void @print_int(i32 %i) {\n\t%_str = bitcast [4 x i8]* @_cint to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n\tret void\n}\n\ndefine void @throw_oob() {\n\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str)\n\tcall void @exit(i32 1)\n\tret void\n}\n\n");
		} catch(IOException ex) {
			System.out.println(ex.getMessage());
		}

	}

	public String visit(MainClass n) {
		return null;
	}

}