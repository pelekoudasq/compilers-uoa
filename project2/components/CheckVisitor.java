package components;

import syntaxtree.*;
import visitor.GJNoArguDepthFirst;

public class CheckVisitor extends GJNoArguDepthFirst<String> {
	
	SymbolTable table;
	Class currentClass;
	Method currentMethod;
	boolean inMethod;
	String auxString;

	public CheckVisitor(SymbolTable table) {
		this.table = table;
		this.currentClass = null;
		this.currentMethod = null;
		this.inMethod = false;
		this.auxString = "";
	}

	public boolean acceptType(String ret, String as) {
		if ( ret.equals("int[]") || ret.equals("boolean") || ret.equals("int") ) {
			return ret.equals(as);
		} else {
			Class classType = table.getClass(ret);
			if ( classType != null )
				return classType.inFamilyHistory(as, this.table);
			else
				return false;
		}
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

	/** * f0 -> "public" * f1 -> Type() * f2 -> Identifier() * f3 -> "(" * f4 -> ( FormalParameterList() )? * f5 -> ")" * f6 -> "{" * f7 -> ( VarDeclaration() )* * f8 -> ( Statement() )* * f9 -> "return" * f10 -> Expression() * f11 -> ";" * f12 -> "}" */
	public String visit(MethodDeclaration n) {
		this.inMethod = true;
		String type = n.f1.accept(this);
		String name = n.f2.accept(this);
		// System.out.println("****************** METHOD: " + name);
		this.currentMethod = this.currentClass.getMeth(name, table, 0);
		//check statements
		n.f8.accept(this);
		//check return expression
		String retType = n.f10.accept(this);
		if ( !acceptType(retType, this.currentMethod.type) ){
			System.out.println("error in return type in method '" + this.currentMethod.name + "'");
			System.exit(-1);
		}
		// System.out.println(retType);
		this.currentMethod = null;
		this.inMethod = false;
		return null;
	}

	/** * f0 -> "class" * f1 -> Identifier() * f2 -> "{" * f3 -> ( VarDeclaration() )* * f4 -> ( MethodDeclaration() )* * f5 -> "}" */
	public String visit(ClassDeclaration n) {
		String className = n.f1.accept(this);
		// System.out.println("------------------ CLASS: " + className);
		this.currentClass = table.getClass(className);
		//method decl, check methods
		n.f4.accept(this);
		this.currentClass = null;
		return null;
	}

	/** * f0 -> "class" * f1 -> Identifier() * f2 -> "extends" * f3 -> Identifier() * f4 -> "{" * f5 -> ( VarDeclaration() )* * f6 -> ( MethodDeclaration() )* * f7 -> "}" */
	public String visit(ClassExtendsDeclaration n) {
		String className = n.f1.accept(this);
		// System.out.println("------------------ CLASS: " + className);
		this.currentClass = table.getClass(className);
		//method decl, check methods
		n.f6.accept(this);
		this.currentClass = null;
		return null;
	}

	/** * f0 -> "class" * f1 -> Identifier() * f2 -> "{" * f3 -> "public" * f4 -> "static" * f5 -> "void" * f6 -> "main" * f7 -> "(" * f8 -> "String" * f9 -> "[" * f10 -> "]" * f11 -> Identifier() * f12 -> ")" * f13 -> "{" * f14 -> ( VarDeclaration() )* * f15 -> ( Statement() )* * f16 -> "}" * f17 -> "}" */
	public String visit(MainClass n) {
		String className = n.f1.accept(this);
		// System.out.println("------------------ CLASS: " + className);
		this.currentClass = table.getClass(className);
		this.currentMethod = this.currentClass.getMeth("main", table, 0);
		this.inMethod = true;
		//check main method statements;
		n.f15.accept(this);
		this.inMethod = false;
		this.currentMethod = null;
		this.currentClass = null;
		return null;
	}

	/**
	* f1 -> "=" * f3 -> ";"
	* f0 -> Identifier()
	* f2 -> Expression()
	*/
	public String visit(AssignmentStatement n) {
		// System.out.println("->AssignmentStatement");
		String id = n.f0.accept(this);
		Variable tableVar = this.currentMethod.getVar(id);
		if ( tableVar == null && (tableVar = this.currentClass.getVar(id, table)) == null ){
			System.out.println("error in assignment for '" + id + "'");
			System.exit(-1);
		}
		String retType = n.f2.accept(this);
		if ( !acceptType(retType, tableVar.type) ) {
			System.out.println("error in assignment for '" + id + "'");
			System.exit(-1);
		}
		return null;
	}

	/**
	* f1 -> "[" * f3 -> "]" * f4 -> "=" * f6 -> ";"
	* f0 -> Identifier()
	* f2 -> Expression()
	* f5 -> Expression()
	*/
	public String visit(ArrayAssignmentStatement n) {
		// System.out.println("->ArrayAssignmentStatement");
		String id = n.f0.accept(this);
		Variable tableVar = this.currentMethod.getVar(id);
		if ( (tableVar == null && (tableVar = this.currentClass.getVar(id, table)) == null) || ( !tableVar.type.equals("int[]") ) ){
			System.out.println("error in array assignment for '" + id + "'");
			System.exit(-1);
		}
		String retType1 = n.f2.accept(this);
		if ( !acceptType(retType1, "int") ) {
			System.out.println("error in assignment for '" + id + "'");
			System.exit(-1);
		}
		String retType2 = n.f5.accept(this);
		if ( !acceptType(retType2, "int") ) {
			System.out.println("error in assignment for '" + id + "'");
			System.exit(-1);
		}
		return null;
	}

	/**
	* f0 -> "if" * f1 -> "(" * f3 -> ")" * f5 -> "else"
	* f2 -> Expression()
	* f4 -> Statement()
	* f6 -> Statement()
	*/
	public String visit(IfStatement n) {
		//System.out.println("->IfStatement");
		String retType = n.f2.accept(this);
		if ( !acceptType(retType, "boolean") ) {
			System.out.println("error in 'if' at expr type '" + retType + "'");
			System.exit(-1);
		}
		n.f4.accept(this);
		n.f6.accept(this);
		return null;
	}

	/**
	* f0 -> "while" * f1 -> "(" * f3 -> ")"
	* f2 -> Expression()
	* f4 -> Statement()
	*/
	public String visit(WhileStatement n) {
		// System.out.println("->WhileStatement");
		String retType = n.f2.accept(this);
		if ( !acceptType(retType, "boolean") ) {
			System.out.println("error in 'while' at expr type '" + retType + "'");
			System.exit(-1);
		}
		n.f4.accept(this);
		return null;
	}

	/**
	* f0 -> "System.out.println" * f1 -> "(" * f3 -> ")" * f4 -> ";"
	* f2 -> Expression()
	*/
	public String visit(PrintStatement n) {
		// System.out.println("->PrintStatement");
		String retType = n.f2.accept(this);
		if ( !acceptType(retType, "int") && !acceptType(retType, "boolean") ) {
			System.out.println("error in 'print' at expr type '" + retType + "'");
			System.exit(-1);
		}
		return null;
	}

	/**
	* f0 -> AndExpression() | CompareExpression() | PlusExpression() | MinusExpression() | TimesExpression() | ArrayLookup() | ArrayLength() | MessageSend() | Clause()
	*/
	public String visit(Expression n) {
		return n.f0.accept(this);
	}

	/** * f1 -> "&&"
	* f0 -> Clause() //as is, not only boolean
	* f2 -> Clause() //as is, not only boolean
	*/
	public String visit(AndExpression n) {
		n.f0.accept(this);
		n.f2.accept(this);
		return "boolean";
	}

	/** * f1 -> "<"
	* f0 -> PrimaryExpression()
	* f2 -> PrimaryExpression()
	*/
	public String visit(CompareExpression n) {
		// System.out.println("->CompareExpression");
		String retType1 = n.f0.accept(this);
		String retType2 = n.f2.accept(this);
		if ( !acceptType(retType1, "int") || !acceptType(retType2, "int") ) {
			System.out.println("error in '<' at expr type '" + retType1 + "', '" + retType2 + "'");
			System.exit(-1);
		}
		return "boolean";
	}

	/** * f1 -> "+"
	* f0 -> PrimaryExpression()
	* f2 -> PrimaryExpression()
	*/
	public String visit(PlusExpression n) {
		// System.out.println("->PlusExpression");
		String retType1 = n.f0.accept(this);
		String retType2 = n.f2.accept(this);
		if ( !acceptType(retType1, "int") || !acceptType(retType2, "int") ) {
			System.out.println("error in '+' at expr type '" + retType1 + "', '" + retType2 + "'");
			System.exit(-1);
		}
		return "int";
	}

	/** * f1 -> "-"
	* f0 -> PrimaryExpression()
	* f2 -> PrimaryExpression()
	*/
	public String visit(MinusExpression n) {
		// System.out.println("->MinusExpression");
		String retType1 = n.f0.accept(this);
		String retType2 = n.f2.accept(this);
		if ( !acceptType(retType1, "int") || !acceptType(retType2, "int") ) {
			System.out.println("error in '-' at expr type '" + retType1 + "', '" + retType2 + "'");
			System.exit(-1);
		}
		return "int";
	}

	/** * f1 -> "*"
	* f0 -> PrimaryExpression()
	* f2 -> PrimaryExpression()
	*/
	public String visit(TimesExpression n) {
		// System.out.println("->TimesExpression");
		String retType1 = n.f0.accept(this);
		String retType2 = n.f2.accept(this);
		if ( !acceptType(retType1, "int") || !acceptType(retType2, "int") ) {
			System.out.println("error in '*' at expr type '" + retType1 + "', '" + retType2 + "'");
			System.exit(-1);
		}
		return "int";
	}

	/** * f1 -> "[" * f3 -> "]"
	* f0 -> PrimaryExpression()
	* f2 -> PrimaryExpression()
	*/
	public String visit(ArrayLookup n) {
		// System.out.println("->ArrayLookup");
		String retType1 = n.f0.accept(this);
		String retType2 = n.f2.accept(this);
		if ( !acceptType(retType1, "int[]") || !acceptType(retType2, "int") ) {
			System.out.println("error in array[] at expr type '" + retType1 + "', '" + retType2 + "'");
			System.exit(-1);
		}
		return "int";
	}

	/** * f1 -> "." * f2 -> "length"
	* f0 -> PrimaryExpression()
	*/
	public String visit(ArrayLength n) {
		// System.out.println("->ArrayLength");
		String retType = n.f0.accept(this);
		if ( !acceptType(retType, "int[]") ) {
			System.out.println("error in '.length' at expr type '" + retType + "'");
			System.exit(-1);
		}
		return "int";
	}

	/** * f1 -> "." * f3 -> "(" * f5 -> ")"
	* f0 -> PrimaryExpression()
	* f2 -> Identifier()
	* f4 -> ( ExpressionList() )?
	*/
	public String visit(MessageSend n) {
		// System.out.println("->MessageSend");
		String className = n.f0.accept(this);
		Class classCalled = table.getClass(className);
		if ( classCalled == null ) {
			System.out.println("error in 'MessageSend' at expr class '" + className + "'");
			System.exit(-1);
		}
		String methodName = n.f2.accept(this);
		Method meth = classCalled.getMeth(methodName, table, 0);
		if ( meth == null ) {
			System.out.println("error in 'MessageSend' at expr method '" + methodName + "'");
			System.exit(-1);
		}
		String[] argsSignature = meth.getSignature().split(",");
		String argList = n.f4.accept(this);
		// System.out.println("----------------------->>>>Given: _"+argList+"_");
		// System.out.println("----------------------->>>>Table: _"+meth.getSignature()+"_");
		if ( argList == null ) {
			if ( meth.params.size() != 0 ){
				System.out.println("error in 'MessageSend' wrong number of args for method '" + methodName + "'");
				System.exit(-1);
			} else 
				return meth.type;
		}
		String[] argsCalled = argList.split(",");
		if ( argsCalled.length != argsSignature.length ) {
			System.out.println("error in 'MessageSend' wrong number of args for method '" + methodName + "'");
			System.exit(-1);
		}
		for (int i = 0; i < argsSignature.length; i++) {
			if ( !acceptType(argsCalled[i], argsSignature[i]) ) {
				System.out.println("error in 'MessageSend' wrong args for method '" + methodName + "'");
				System.exit(-1);
			}
		}
		// if ( !argList.equals(meth.getSignature()) ){
		// 	System.out.println("error in 'MessageSend' wrong args for method '" + methodName + "'");
		// 	System.exit(-1);
		// }
		return meth.type;
	}

	/**
	* f0 -> Expression()
	* f1 -> ExpressionTail()
	*/
	public String visit(ExpressionList n) {
		this.auxString = "";
		String firstExpr = n.f0.accept(this);
		String restExpr = n.f1.accept(this);
		if ( restExpr == null )
			return firstExpr + ",";
		else
			return firstExpr + "," + restExpr;
	}

	/**
	* f0 -> ( ExpressionTerm() )*
	*/
	public String visit(ExpressionTail n) {
		n.f0.accept(this);
		return this.auxString;
	}

	/** * f0 -> ","
	* f1 -> Expression()
	*/
	public String visit(ExpressionTerm n) {
		String firstExpr = n.f1.accept(this);
		if ( firstExpr == null )
			this.auxString = this.auxString + ",";
		else
			this.auxString = this.auxString + firstExpr + ",";
		return null;
	}

	/** * f0 -> NotExpression()
	*       | PrimaryExpression()
	*/
	public String visit(Clause n) {
		return n.f0.accept(this);
	}

	/** * f0 -> IntegerLiteral() | TrueLiteral() | FalseLiteral() | Identifier() | ThisExpression() | ArrayAllocationExpression() | AllocationExpression() | BracketExpression()
	*/
	public String visit(PrimaryExpression n) {
		int choice = n.f0.which;
		String retType = n.f0.accept(this);
		if ( choice == 3 ) {
			Variable varIdent = this.currentMethod.getVar(retType);
			if ( varIdent == null && (varIdent = this.currentClass.getVar(retType, table)) == null ){
				System.out.println("error in assignment for '" + retType + "'");
				System.exit(-1);
			}
			return varIdent.type;
		}
		return n.f0.accept(this);
	}

	public String visit(IntegerLiteral n) {
		return "int";
	}

	public String visit(TrueLiteral n) {
		return "boolean";
	}

	public String visit(FalseLiteral n) {
		return "boolean";
	}

	public String visit(ThisExpression n) {
		return this.currentClass.name;
	}

	/** * f0 -> "new" * f1 -> "int" * f2 -> "[" * f4 -> "]"
	* f3 -> Expression()
	*/
	public String visit(ArrayAllocationExpression n) {
		n.f3.accept(this);
		return "int[]";
	}

	/** * f0 -> "new" * f2 -> "(" * f3 -> ")"
	* f1 -> Identifier()
	*/
	public String visit(AllocationExpression n) {
		String className = n.f1.accept(this);
		Class classCalled = table.getClass(className);
		if ( classCalled == null ) {
			System.out.println("error in instance allocation of class '" + className + "'");
			System.exit(-1);
		}
		return classCalled.name;
	}

	/** * f0 -> "!"
	* f1 -> Clause()
	*/
	public String visit(NotExpression n) {
		n.f1.accept(this);
		return "boolean";
	}

	/** * f0 -> "(" * f2 -> ")"
	* f1 -> Expression()
	*/
	public String visit(BracketExpression n) {
		return n.f1.accept(this);
	}

}