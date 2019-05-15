package components;

import syntaxtree.*;
import visitor.GJNoArguDepthFirst;

public class FillClassVisitor extends GJNoArguDepthFirst<String> {

	SymbolTable table;
	Class currentClass;
	Method currentMethod;
	boolean inMethod;

	public FillClassVisitor(SymbolTable table) {
		this.table = table;
		this.currentClass = null;
		this.currentMethod = null;
		this.inMethod = false;
	}

	public int offsetValueFromType(String type) {
		if ( type.equals("boolean") ) 
			return 1;
		else if ( type.equals("int") )
			return 4;
		else
			return 8;
	}

	/* f0 -> <IDENTIFIER> */
	public String visit(Identifier n) {
		return n.f0.toString();
	}

	/** * f0 -> "int" * f1 -> "[" * f2 -> "]" */
	public String visit(ArrayType n) {
		return "int[]";
	}

	/* f0 -> "boolean" */
	public String visit(BooleanType n) {
		return "boolean";
	}

	/* f0 -> "int" */
	public String visit(IntegerType n) {
		return "int";
	}

	/* f0 -> ArrayType() | BooleanType() | IntegerType() | Identifier() */
	public String visit(Type n) {
		String retType = n.f0.accept(this);
		if ( !retType.equals("int[]") && !retType.equals("boolean") && !retType.equals("int") ) {
			if ( table.getClass(retType) == null ) {
				System.out.println("Cannot recognize type '" + retType + "'");
				System.exit(-1);
			}
		}
		return retType;
	}

	/** * f0 -> Type() * f1 -> Identifier() * f2 -> ";" */
	public String visit(VarDeclaration n) {
		String type = n.f0.accept(this);
		String name = n.f1.accept(this);
		Variable retVar = new Variable(type, name);
		if ( this.inMethod == true ){
			if ( !this.currentMethod.putVar(retVar) ) {
				System.out.println("Variable '" + name + "' already declared in this scope");
				System.exit(-1);
			} else {
				// System.out.println("Fill Class Visitor: Variable '"  + name + "' declared for method '" + this.currentMethod.name + "' of class '" + this.currentClass.name + "'");
			}
		} else {
			retVar.offset = this.currentClass.varOffset;
			this.currentClass.varOffset = this.currentClass.varOffset + offsetValueFromType(retVar.type);
			if ( !this.currentClass.putVar(retVar) ) {
				System.out.println("Variable '"+ name + "' already declared in this scope");
				System.exit(-1);
			} else {
				// System.out.println("Fill Class Visitor: Variable '"  + name + "' declared for class '" + this.currentClass.name + "'");
			}
		}
		return null;
	}

	/** * f0 -> Type() * f1 -> Identifier() */
	public String visit(FormalParameter n) {
		String type = n.f0.accept(this);
		String name = n.f1.accept(this);
		Variable retVar = new Variable(type, name);
		if ( !this.currentMethod.putParam(retVar) ) {
			System.out.println("Parameter '" + name + "' already declared in this scope");
			System.exit(-1);
		} else {
			// System.out.println("Fill Class Visitor: Parameter '"  + name + "' declared for method '" + this.currentMethod.name + "' of class '" + this.currentClass.name + "'");
		}
		return null;
	}

	/** * f0 -> "public" * f1 -> Type() * f2 -> Identifier() * f3 -> "(" * f4 -> ( FormalParameterList() )? * f5 -> ")" * f6 -> "{" * f7 -> ( VarDeclaration() )* * f8 -> ( Statement() )* * f9 -> "return" * f10 -> Expression() * f11 -> ";" * f12 -> "}" */
	public String visit(MethodDeclaration n) {
		this.inMethod = true;
		String type = n.f1.accept(this);
		String name = n.f2.accept(this);
		this.currentMethod = new Method(type, name);
		//parameters
		n.f4.accept(this);
		if ( !this.currentClass.putMeth(this.currentMethod, table) ){
			System.out.println("Method '"+ name + "' already declared in class '" + this.currentClass.name + "'");
			System.exit(-1);
		} else {
			// System.out.println("Fill Class Visitor: Method '"  + name + "' declared for class '" + this.currentClass.name + "'");
		}
		Method temp = this.currentClass.getMeth(name, table, 1);
		if ( temp != null ) {
			this.currentMethod.offset = this.currentClass.methOffset;
			this.currentClass.methOffset = this.currentClass.methOffset + offsetValueFromType("method");
		}
		//var decl
		n.f7.accept(this);
		this.currentMethod = null;
		this.inMethod = false;
		return null;
	}

	/** * f0 -> "class" * f1 -> Identifier() * f2 -> "{" * f3 -> ( VarDeclaration() )* * f4 -> ( MethodDeclaration() )* * f5 -> "}" */
	public String visit(ClassDeclaration n) {
		String className = n.f1.accept(this);
		this.currentClass = table.getClass(className);
		this.currentClass.varOffset = 0;
		this.currentClass.methOffset = 0;
		//var decl
		n.f3.accept(this);
		//method decl
		n.f4.accept(this);
		this.currentClass = null;
		return null;
	}

	/** * f0 -> "class" * f1 -> Identifier() * f2 -> "extends" * f3 -> Identifier() * f4 -> "{" * f5 -> ( VarDeclaration() )* * f6 -> ( MethodDeclaration() )* * f7 -> "}" */
	public String visit(ClassExtendsDeclaration n) {
		String className = n.f1.accept(this);
		this.currentClass = table.getClass(className);
		this.currentClass.varOffset = table.getClass(this.currentClass.parentName).varOffset;
		this.currentClass.methOffset = table.getClass(this.currentClass.parentName).methOffset;
		//var decl
		n.f5.accept(this);
		//method decl
		n.f6.accept(this);
		this.currentClass = null;
		return null;
	}

	/** * f0 -> "class" * f1 -> Identifier() * f2 -> "{" * f3 -> "public" * f4 -> "static" * f5 -> "void" * f6 -> "main" * f7 -> "(" * f8 -> "String" * f9 -> "[" * f10 -> "]" * f11 -> Identifier() * f12 -> ")" * f13 -> "{" * f14 -> ( VarDeclaration() )* * f15 -> ( Statement() )* * f16 -> "}" * f17 -> "}" */
	public String visit(MainClass n) {
		String className = n.f1.accept(this);
		this.currentClass = table.getClass(className);
		this.currentMethod = this.currentClass.getMeth("main", table, 0);
		this.inMethod = true;
		n.f14.accept(this);
		this.inMethod = false;
		this.currentMethod = null;
		this.currentClass = null;
		return null;
	}
}
