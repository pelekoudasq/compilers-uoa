package components;

import syntaxtree.*;
import visitor.GJNoArguDepthFirst;

public class ClassVisitor extends GJNoArguDepthFirst<String> {

	public SymbolTable table;

	public ClassVisitor(SymbolTable table) {
		this.table = table;
	}

	/* f0 -> <IDENTIFIER> */
	public String visit(Identifier n) {
		return n.f0.toString();
	}

	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "{"
	* f3 -> ( VarDeclaration() )*
	* f4 -> ( MethodDeclaration() )*
	* f5 -> "}"
	*/
	public String visit(ClassDeclaration n) {
		String className = n.f1.accept(this);
		if ( table.getClass(className) != null ) {
			System.out.println("Class '" + className + "' already declared");
			System.exit(-1);
		}
		Class newClass = new Class(className, null);
		table.cls.put(className, newClass);
		System.out.println("Class Visitor: Class '" + className + "' declared");
		return null;
	}

	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "extends"
	* f3 -> Identifier()
	* f4 -> "{"
	* f5 -> ( VarDeclaration() )*
	* f6 -> ( MethodDeclaration() )*
	* f7 -> "}"
	*/
	public String visit(ClassExtendsDeclaration n) {
		String className = n.f1.accept(this);
		if ( table.getClass(className) != null ) {
			System.out.println("Class '" + className + "' already declared");
			System.exit(-1);
		}
		String parentClass = n.f3.accept(this);
		if ( table.getClass(parentClass) == null ) {
			System.out.println("Class '" + parentClass + "' has not been declared");
			System.exit(-1);
		}
		Class newClass = new Class(className, parentClass);
		table.cls.put(className, newClass);
		System.out.println("Class Visitor: Class '" + className + "' declared");
		return null;
	}

	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "{"
	* f3 -> "public"
	* f4 -> "static"
	* f5 -> "void"
	* f6 -> "main"
	* f7 -> "("
	* f8 -> "String"
	* f9 -> "["
	* f10 -> "]"
	* f11 -> Identifier()
	* f12 -> ")"
	* f13 -> "{"
	* f14 -> ( VarDeclaration() )*
	* f15 -> ( Statement() )*
	* f16 -> "}"
	* f17 -> "}"
	*/
	public String visit(MainClass n) {
		String className = n.f1.accept(this);
		Class newClass = new Class(className, null);
		String argsName = n.f11.toString();
		Method meth = new Method("void", "main");
		Variable param = new Variable("String[]", argsName);
		meth.putParam(param);
		newClass.putMeth(meth);
		table.cls.put(className, newClass);
		System.out.println("Class Visitor: Class '" + className + "' declared");
		return null;
	}
}
