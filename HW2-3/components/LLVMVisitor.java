package components;

import syntaxtree.*;
import visitor.GJDepthFirst;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class LLVMVisitor extends GJDepthFirst<String, String> {

	SymbolTable table;
	Path file;
	String buffer;
	Class currentClass;
	Method currentMethod;
	boolean inMethod;
	boolean isBoolean;
	int registerCounter;
	int ifLabelCounter;
	int whileLabelCounter;
	String loadedRegister;
	String auxString;
	String allocExprIdentifier;
	boolean isThis;

	public static boolean isNumeric(String strNum) {
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException | NullPointerException nfe) {
			return false;
		}
		return true;
	}

	void emit(String str) throws IOException {
		Files.write(this.file, str.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}

	String typeJavaToLLVM (String type) {
		if ( type.equals("boolean") ) 
			return "i1";
		else if ( type.equals("int") )
			return "i32";
		else if ( type.equals("int[]") )
			return "i32*";
		else
			return "i8*";
	}

	public LLVMVisitor(SymbolTable table) {
		this.isThis = false;
		this.allocExprIdentifier = "";
		this.isBoolean = false;
		this.auxString = "";
		this.loadedRegister = "";
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

	public String visit(Goal n, String argu) {
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		try {
			this.emit(this.buffer);
		} catch(IOException ex) {
			System.out.println(ex.getMessage());
		}
		return null;
	}

	/** * f0 -> "class" * f1 -> Identifier() * f2 -> "{" * f3 -> "public" * f4 -> "static" * f5 -> "void" * f6 -> "main" * f7 -> "(" * f8 -> "String" * f9 -> "[" * f10 -> "]" * f11 -> Identifier() * f12 -> ")" * f13 -> "{" * f14 -> ( VarDeclaration() )* * f15 -> ( Statement() )* * f16 -> "}" * f17 -> "}" */
	public String visit(MainClass n, String argu) {
		String className = n.f1.accept(this, argu);
		this.currentClass = table.getClass(className);
		this.currentMethod = this.currentClass.getMeth("main", table, 0);
		this.inMethod = true;
		this.buffer = this.buffer + "define i32 @main() {\n";
		n.f14.accept(this, argu);
		n.f15.accept(this, argu);
		this.buffer = this.buffer + "\tret i32 0\n";
		this.buffer = this.buffer + "}\n\n";
		this.inMethod = false;
		this.currentMethod = null;
		this.currentClass = null;
		return null;
	}

	/** * f0 -> "class" * f1 -> Identifier() * f2 -> "{" * f3 -> ( VarDeclaration() )* * f4 -> ( MethodDeclaration() )* * f5 -> "}" */
	public String visit(ClassDeclaration n, String argu) {
		String className = n.f1.accept(this, argu);
		this.currentClass = table.getClass(className);
		n.f4.accept(this, argu);
		this.currentClass = null;
		return null;
	}

	/** * f0 -> "class" * f1 -> Identifier() * f2 -> "extends" * f3 -> Identifier() * f4 -> "{" * f5 -> ( VarDeclaration() )* * f6 -> ( MethodDeclaration() )* * f7 -> "}" */
	public String visit(ClassExtendsDeclaration n, String argu) {
		String className = n.f1.accept(this, argu);
		this.currentClass = table.getClass(className);
		n.f6.accept(this, argu);
		this.currentClass = null;
		return null;
	}

	/** * f0 -> "public" * f1 -> Type() * f2 -> Identifier() * f3 -> "(" * f4 -> ( FormalParameterList() )? * f5 -> ")" * f6 -> "{" * f7 -> ( VarDeclaration() )* * f8 -> ( Statement() )* * f9 -> "return" * f10 -> Expression() * f11 -> ";" * f12 -> "}" */
	public String visit(MethodDeclaration n, String argu) {
		this.inMethod = true;
		this.registerCounter = 0;
		this.ifLabelCounter = 0;
		this.whileLabelCounter = 0;
		String type = n.f1.accept(this, argu);
		String name = n.f2.accept(this, argu);
		this.currentMethod = this.currentClass.getMeth(name, table, 0);


		this.buffer = this.buffer + "\ndefine " + typeJavaToLLVM(this.currentMethod.type) + " @" + this.currentClass.name + "." + this.currentMethod.name + "(i8* %this";
		for (Variable param : this.currentMethod.params) {
			this.buffer = this.buffer + ", " + typeJavaToLLVM(param.type) + " %." + param.name;
		}
		this.buffer = this.buffer + ") {\n";
		for (Variable param : this.currentMethod.params) {
			this.buffer = this.buffer + "\t%" + param.name + " = alloca " + typeJavaToLLVM(param.type) + "\n";
			this.buffer = this.buffer + "\tstore " + typeJavaToLLVM(param.type) + " %." + param.name + ", " + typeJavaToLLVM(param.type) + "* %" + param.name + "\n";
		}
		for (Variable varr : this.currentMethod.vars) {
			this.buffer = this.buffer + "\t%" + varr.name + " = alloca " + typeJavaToLLVM(varr.type) + "\n";
		}
		n.f8.accept(this, argu);
		String retExpr = n.f10.accept(this, "load_var");
		this.buffer = this.buffer + "\tret " + typeJavaToLLVM(this.currentMethod.type) + " " + retExpr + "\n";
		this.buffer = this.buffer + "}\n";
		this.currentMethod = null;
		this.inMethod = false;
		return null;
	}

	public String visit(Statement n, String argu) {
		return n.f0.accept(this, argu);
	}

	/** * f1 -> "=" * f3 -> ";" * f0 -> Identifier() * f2 -> Expression() */ //done
	public String visit(AssignmentStatement n, String argu) {
		
		//to be stored -> get bitcast aka address
		String retReg = n.f0.accept(this, "load_var_1");
		String id = n.f0.accept(this, argu);
		Variable varr = this.currentMethod.getVar(id);
		if (varr == null)
			varr = this.currentClass.getVar(id, table);

		//address to store to f0
		String exprReg = n.f2.accept(this, "load_var");
		if (!exprReg.startsWith("%") && !isNumeric(exprReg)) {
			exprReg = "%" + exprReg;
			this.buffer = this.buffer + "\t%_" + this.registerCounter + " = load " + typeJavaToLLVM(varr.type) + ", " + typeJavaToLLVM(varr.type) + "* " + exprReg + "\n";
			exprReg = "%_" + this.registerCounter;
			this.registerCounter += 1;
		}
		if (!retReg.startsWith("%")) {
			retReg = "%" + retReg;
		}
		this.buffer = this.buffer + "\tstore " + typeJavaToLLVM(varr.type) + " " + exprReg + ", " + typeJavaToLLVM(varr.type) + "* " + retReg + "\n";

		return null;
	}

	/** * f0 -> Identifier() * f1 -> "[" * f2 -> Expression() * f3 -> "]" * f4 -> "=" * f5 -> Expression() * f6 -> ";" */	
	public String visit(ArrayAssignmentStatement n, String argu) {
		//System.out.println("in ArrayAssignmentStatement");
		String retReg = n.f0.accept(this, "load_var");
		String retExpr1 = n.f2.accept(this, argu);
		if (!retExpr1.startsWith("%") && !isNumeric(retExpr1)) {
			retExpr1 = "%" + retExpr1;
			this.buffer = this.buffer + "\t%_" + this.registerCounter + " = load i32, i32* " + retExpr1 + "\n";
			retExpr1 = "%_" + this.registerCounter;
			this.registerCounter += 1;
		}
		String retExpr2 = n.f5.accept(this, argu);
		if (!retExpr2.startsWith("%") && !isNumeric(retExpr2)) {
			retExpr2 = "%" + retExpr2;
			this.buffer = this.buffer + "\t%_" + this.registerCounter + " = load i32, i32* " + retExpr2 + "\n";
			retExpr2 = "%_" + this.registerCounter;
			this.registerCounter += 1;
		}
		this.registerCounter += 2;
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 2) + " = add i32 " + retExpr1 + ", 1\n";
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 1) + " = getelementptr i32, i32* " + retReg + ", i32 %_" + (this.registerCounter - 2) + "\n";
		this.buffer = this.buffer + "\tstore i32 " + retExpr2 + ", i32* %_" + (this.registerCounter - 1) + "\n";
		return null;
	}

	/** * f0 -> "if" * f1 -> "(" * f2 -> Expression() * f3 -> ")" * f4 -> Statement() * f5 -> "else" * f6 -> Statement() */
	public String visit(IfStatement n, String argu) {
		//System.out.println("in IfStatement");
		this.ifLabelCounter += 3;
		int if_lbl1 = this.ifLabelCounter - 3;
		int if_lbl2 = this.ifLabelCounter - 2;
		int if_lbl3 = this.ifLabelCounter - 1;

		String retExpr = n.f2.accept(this, "load_var");
		this.buffer = this.buffer + "\tbr i1 " + retExpr + ", label %if" + if_lbl1 + ", label %if" + if_lbl2 + "\n";
		this.buffer = this.buffer + "if" + if_lbl1 + ":\n";
		n.f4.accept(this, argu);
		this.buffer = this.buffer + "\tbr label %if" + if_lbl3 + "\n";
		this.buffer = this.buffer + "if" + if_lbl2 + ":\n";
		n.f6.accept(this, argu);
		this.buffer = this.buffer + "\tbr label %if" + if_lbl3 + "\n";
		this.buffer = this.buffer + "if" + if_lbl3 + ":\n";
		return null;
	}

	/** * f0 -> "while" * f1 -> "(" * f2 -> Expression() * f3 -> ")" * f4 -> Statement() */
	public String visit(WhileStatement n, String argu) {
		//System.out.println("in WhileStatement");
		this.whileLabelCounter += 2;
		this.buffer = this.buffer + "\tbr label %loop" + (this.whileLabelCounter - 2) + "\n";
		this.buffer = this.buffer + "loop" + (this.whileLabelCounter - 2) + ":\n";
		String retExpr = n.f2.accept(this, "load_var");
		this.buffer = this.buffer + "\tbr i1 " + retExpr + ", label %loop" + (this.whileLabelCounter - 1) + ", label %loop" + (this.whileLabelCounter - 2) + "\n";
		this.buffer = this.buffer + "loop" + (this.whileLabelCounter - 1) + ":\n";
		n.f4.accept(this, argu);
		this.buffer = this.buffer + "\tbr label %loop" + this.whileLabelCounter + "\n";
		this.buffer = this.buffer + "loop" + this.whileLabelCounter + ":\n";
		this.whileLabelCounter += 1;
		return null;
	}

	/** * f0 -> "System.out.println" * f1 -> "(" * f2 -> Expression() * f3 -> ")" * f4 -> ";" */
	public String visit(PrintStatement n, String argu) {
		//System.out.println("in PrintStatement");
		String retExpr = n.f2.accept(this, "load_var");
		// if ( this.isBoolean == true ) {
		// 	System.out.println("in isBoolean");
		// 	this.buffer = this.buffer + "\t%_" + this.registerCounter + " = zext i1 " + retExpr + " to i32\n";
		// 	this.buffer = this.buffer + "\tcall void (i32) @print_int(i32 %_" + this.registerCounter + ")\n";
		// 	this.registerCounter += 1;
		// 	this.isBoolean = false;
		// } else {
			this.buffer = this.buffer + "\tcall void (i32) @print_int(i32 " + retExpr + ")\n";
		// }
		return null;
	}

	public String visit(Expression n, String argu) {
		return n.f0.accept(this, argu);
	}

	public String visit(Clause n, String argu) {
		return n.f0.accept(this, argu);
	}

	public String visit(AndExpression n, String argu) {
		//this.buffer = this.buffer + "\n\t; AndExpression\n";
		String clause1reg = n.f0.accept(this, "load_var");
		String clause2reg = n.f2.accept(this, "load_var");
		this.registerCounter += 2;
		this.ifLabelCounter += 3;
		this.buffer = this.buffer + "\tbr i1 " + clause1reg + ", label %if" + (this.ifLabelCounter - 3) + ", label %if" + (this.ifLabelCounter - 2) + "\n";
		this.buffer = this.buffer + "if" + (this.ifLabelCounter - 3) + ":\n";
		this.buffer = this.buffer + "\tbr label %if" + (this.ifLabelCounter - 1) + "\n";
		this.buffer = this.buffer + "if" + (this.ifLabelCounter - 2) + ":\n";
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 2) + " = icmp eq i1 " + clause1reg + ", 1\n";
		this.buffer = this.buffer + "\tbr label %if" + (this.ifLabelCounter - 1) + "\n";
		this.buffer = this.buffer + "if" + (this.ifLabelCounter - 1) + ":\n";
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 1) + " = phi i1 [" + clause2reg + ", %if" + (this.ifLabelCounter - 3) + "], [ %_" + (this.registerCounter - 2) + ", %if" + (this.ifLabelCounter - 2) + "]\n";
		this.isBoolean = true;
		return "%_" + (this.registerCounter - 1);
	}

	public String visit(CompareExpression n, String argu) {
		//this.buffer = this.buffer + "\n\t; CompareExpression\n";
		String expr1reg = n.f0.accept(this, "load_var");
		if (!expr1reg.startsWith("%") && !isNumeric(expr1reg)) {
			expr1reg = "%" + expr1reg;
		}
		String expr2reg = n.f2.accept(this, "load_var");
		if (!expr2reg.startsWith("%") && !isNumeric(expr2reg)) {
			expr2reg = "%" + expr2reg;
		}
		this.buffer = this.buffer + "\t%_" + this.registerCounter + " = icmp slt i32 " + expr1reg + ", " + expr2reg + "\n";
		this.registerCounter += 1;
		this.isBoolean = true;
		return "%_" + (this.registerCounter - 1);
	}

	/** * f0 -> PrimaryExpression() * f1 -> "+" * f2 -> PrimaryExpression() */
	public String visit(PlusExpression n, String argu) {
		//this.buffer = this.buffer + "\n\t; PlusExpression\n";
		String expr1reg = n.f0.accept(this, "load_var");
		if (!expr1reg.startsWith("%") && !isNumeric(expr1reg)) {
			expr1reg = "%" + expr1reg;
		}
		String expr2reg = n.f2.accept(this, "load_var");
		if (!expr2reg.startsWith("%") && !isNumeric(expr2reg)) {
			expr2reg = "%" + expr2reg;
		}
		this.buffer = this.buffer + "\t%_" + this.registerCounter + " = add i32 " + expr1reg + ", " + expr2reg + "\n";
		this.registerCounter += 1;
		return "%_" + (this.registerCounter - 1);
	}

	public String visit(MinusExpression n, String argu) {
		//this.buffer = this.buffer + "\n\t; MinusExpression\n";
		String expr1reg = n.f0.accept(this, "load_var");
		if (!expr1reg.startsWith("%") && !isNumeric(expr1reg)) {
			expr1reg = "%" + expr1reg;
		}
		String expr2reg = n.f2.accept(this, "load_var");
		if (!expr2reg.startsWith("%") && !isNumeric(expr2reg)) {
			expr2reg = "%" + expr2reg;
		}
		this.buffer = this.buffer + "\t%_" + this.registerCounter + " = sub i32 " + expr1reg + ", " + expr2reg + "\n";
		this.registerCounter += 1;
		return "%_" + (this.registerCounter - 1);
	}

	public String visit(TimesExpression n, String argu) {
		//this.buffer = this.buffer + "\n\t; TimesExpression\n";
		String expr1reg = n.f0.accept(this, "load_var");
		if (!expr1reg.startsWith("%") && !isNumeric(expr1reg)) {
			expr1reg = "%" + expr1reg;
		}
		String expr2reg = n.f2.accept(this, "load_var");
		if (!expr2reg.startsWith("%") && !isNumeric(expr2reg)) {
			expr2reg = "%" + expr2reg;
		}
		this.buffer = this.buffer + "\t%_" + this.registerCounter + " = mul i32 " + expr1reg + ", " + expr2reg + "\n";
		this.registerCounter += 1;
		return "%_" + (this.registerCounter - 1);
	}

	public String visit(ArrayLookup n, String argu) {
		//this.buffer = this.buffer + "\n\t; ArrayLookup\n";
		String expr1reg = n.f0.accept(this, "load_var");
		if (!isNumeric(expr1reg)) {
			this.buffer = this.buffer + "\t%_" + this.registerCounter + " = load i32, i32* " + expr1reg + "\n";
			//for out of bounds
			//expr1reg = "%_" + this.registerCounter;
			this.registerCounter += 1;
		}
		String expr2reg = n.f2.accept(this, "load_var");
		if (!expr2reg.startsWith("%") && !isNumeric(expr2reg)) {
			expr2reg = "%" + expr2reg;
			this.buffer = this.buffer + "\t%_" + this.registerCounter + " = load i32, i32* " + expr2reg + "\n";
			expr2reg = "%_" + this.registerCounter;
			this.registerCounter += 1;
		}
		this.registerCounter += 3;
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 3) + " = add i32 " + expr2reg + ", 1\n";
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 2) + " = getelementptr i32, i32* " + expr1reg + ", i32 %_" + (this.registerCounter - 3) + "\n";
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 1) + " = load i32, i32* %_" + (this.registerCounter - 2) + "\n";
		this.isBoolean = false;
		return "%_" + (this.registerCounter - 1);
	}

	public String visit(ArrayLength n, String argu) {
		//this.buffer = this.buffer + "\n\t; ArrayLength\n";
		String retReg = n.f0.accept(this, "load_var");
		this.buffer = this.buffer + "\t%_" + this.registerCounter + " = load i32, i32* " + retReg + "\n";
		this.registerCounter += 1;
		return "%_" + (this.registerCounter - 1);
	}

	/** * f1 -> "." * f3 -> "(" * f5 -> ")" * f0 -> PrimaryExpression() * f2 -> Identifier() * f4 -> ( ExpressionList() )? */
	public String visit(MessageSend n, String argu) {
		
		String retReg =  n.f0.accept(this, "load_var");
		String className = this.allocExprIdentifier;
		Class classCalled;
		Variable varIdent = this.currentMethod.getVar(className);
		if (varIdent == null) {
			varIdent = this.currentClass.getVar(className, table);
			classCalled = this.table.getClass(className);
			if (varIdent == null && classCalled == null) {
				classCalled = this.currentClass;
			}
		} else {
			classCalled = this.table.getClass(varIdent.type);
		}

		String methodName = n.f2.accept(this, null);

		Method meth = classCalled.getMeth(methodName, table, 0);

		String[] argsSignature = meth.getSignature().split(",");
		String argList = n.f4.accept(this, "load_var");
		this.registerCounter += 6;
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 6) + " = bitcast i8* " + retReg + " to i8***\n";
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 5) + " = load i8**, i8*** %_" + (this.registerCounter - 6) + "\n";
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 4) + " = getelementptr i8*, i8** %_" + (this.registerCounter - 5) + ", i32 " + (meth.offset/8) + "\n";
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 3) + " = load i8*, i8** %_" + (this.registerCounter - 4) + "\n";
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 2) + " = bitcast i8* %_" + (this.registerCounter - 3) + " to " + typeJavaToLLVM(meth.type) + " (i8*";
		String typeArg;
		for (int j = 0; j < argsSignature.length; j++) {
			typeArg = "";
			if ( argsSignature[j] == null || argsSignature[j].isEmpty() )
				continue;
			typeArg = typeJavaToLLVM(argsSignature[j]);
			this.buffer = this.buffer + "," + typeArg;
		}

		this.buffer = this.buffer + ")*\n";
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 1) + " = call " + typeJavaToLLVM(meth.type) + " %_" + (this.registerCounter - 2) + "(i8* ";
		if ( this.isThis == true ) {
			this.buffer = this.buffer + "%this";
			this.isThis = false;
		} else {
			this.buffer = this.buffer + retReg;
		}
		String[] argsCalled = null;
		if (argList != null)
			argsCalled = argList.split(",");
		for (int j = 0; j < argsSignature.length; j++) {
			if (argsCalled != null)
				this.buffer = this.buffer + ", " + typeJavaToLLVM(argsSignature[j]) + " " + argsCalled[j];
		}
		this.buffer = this.buffer + ")\n";
		if (meth.type.equals("boolean"))
			this.isBoolean = true;
		return "%_" + (this.registerCounter - 1);
	}

	/** * f0 -> Expression() * f1 -> ExpressionTail() */
	public String visit(ExpressionList n, String argu) {
		this.auxString = "";
		String firstExpr = n.f0.accept(this, argu);
		String restExpr = n.f1.accept(this, argu);
		if ( restExpr == null )
			return firstExpr + ",";
		else
			return firstExpr + "," + restExpr;
	}

	/** * f0 -> ( ExpressionTerm() )* */
	public String visit(ExpressionTail n, String argu) {
		n.f0.accept(this, argu);
		return this.auxString;
	}

	/** * f0 -> "," * f1 -> Expression() */
	public String visit(ExpressionTerm n, String argu) {
		String firstExpr = n.f1.accept(this, argu);
		if ( firstExpr == null )
			this.auxString = this.auxString + ",";
		else
			this.auxString = this.auxString + firstExpr + ",";
		return null;
	}


	////////////PRIMARY EXPR

	public String visit(PrimaryExpression n, String argu) {
		// int choice = n.f0.which;
		// String retVal = n.f0.accept(this, argu);
		// if ( choice == 3 && argu.equals("get_class") ) {
		// }
		return n.f0.accept(this, argu);
	}

	public String visit(IntegerLiteral n, String argu) {
		return n.f0.toString();
	}

	public String visit(TrueLiteral n, String argu) {
		return "1";
	}

	public String visit(FalseLiteral n, String argu) {
		return "0";
	}

	/* f0 -> <IDENTIFIER> */
	public String visit(Identifier n, String argu) {
		//this.buffer = this.buffer + "\n\t; Identifier\n";
		//System.out.println("in Identifier");
		//this.allocExprIdentifier = "";
		//this.allocExprIdentifier = n.f0.toString();
		if (argu == "load_var") {
			String id = n.f0.toString();
			Variable varr = this.currentMethod.getVar(id);
			if ( varr != null ) {
				if ( varr.type.equals("boolean") )
					this.isBoolean = true;
				this.buffer = this.buffer + "\t%_" + this.registerCounter + " = load " + typeJavaToLLVM(varr.type) + ", " + typeJavaToLLVM(varr.type) + "* " +  "%" + varr.name + "\n";
				this.registerCounter +=1;
			} else {
				varr = this.currentClass.getVar(id, table);
				if ( varr.type.equals("boolean") )
					this.isBoolean = true;
				//get value from vtable
				this.buffer = this.buffer + "\t%_" + this.registerCounter + " = getelementptr i8, i8* %this, i32 " + varr.offset + "\n";
				this.registerCounter += 1;
				//cast it to its type
				this.buffer = this.buffer + "\t%_" + this.registerCounter + " = bitcast i8* %_" + (this.registerCounter - 1) + " to " + typeJavaToLLVM(varr.type) + "*\n";
				this.registerCounter += 1;
				this.buffer = this.buffer + "\t%_" + this.registerCounter + " = load " + typeJavaToLLVM(varr.type) + ", " + typeJavaToLLVM(varr.type) + "* %_" + (this.registerCounter - 1) + "\n";
				this.registerCounter +=1;
			}
			return "%_" + (this.registerCounter - 1);
		} else if (argu == "load_var_1") {
			String id = n.f0.toString();
			Variable varr = this.currentMethod.getVar(id);
			if ( varr != null ) {
				if ( varr.type.equals("boolean") )
					this.isBoolean = true;
				return n.f0.toString();
			} else {
				varr = this.currentClass.getVar(id, table);
				if ( varr.type.equals("boolean") )
					this.isBoolean = true;
				//get value from vtable
				this.buffer = this.buffer + "\t%_" + this.registerCounter + " = getelementptr i8, i8* %this, i32 " + varr.offset + "\n";
				this.registerCounter += 1;
				//cast it to its type
				this.buffer = this.buffer + "\t%_" + this.registerCounter + " = bitcast i8* %_" + (this.registerCounter - 1) + " to " + typeJavaToLLVM(varr.type) + "*\n";
				this.registerCounter += 1;
			}
			return "%_" + (this.registerCounter - 1);
		}
		return n.f0.toString();
	}

	public String visit(ThisExpression n, String argu) {
		//this.buffer = this.buffer + "\n\t; ThisExpression\n";
		this.isThis = true;
		this.allocExprIdentifier = "";
		this.allocExprIdentifier = this.currentClass.name;
		return "%this";
	}

	/** * f0 -> "new" * f1 -> "int" * f2 -> "[" * f3 -> Expression() * f4 -> "]" */
	public String visit(ArrayAllocationExpression n, String argu) {
		//this.buffer = this.buffer + "\n\t; arrallocexpr\n";
		String exprReg = n.f3.accept(this, argu);
		if (!exprReg.startsWith("%") && !isNumeric(exprReg)) {
			exprReg = "%" + exprReg;
			this.buffer = this.buffer + "\t%_" + this.registerCounter + " = load i32, i32* " + exprReg + "\n";
			exprReg = "%_" + this.registerCounter;
			this.registerCounter += 1;
		}
		this.registerCounter += 3;
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 3) + " = add i32 " + exprReg + ", 1\n";
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 2) + " = call i8* @calloc(i32 4, i32 %_" + (this.registerCounter - 3) + ")\n";
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 1) + " = bitcast i8* %_" + (this.registerCounter - 2) + " to i32*\n";
		this.buffer = this.buffer + "\tstore i32 " + exprReg + ", i32* %_" + (this.registerCounter - 1) + "\n";
		return "%_" + (this.registerCounter - 1);
	}

	/** * f0 -> "new" * f1 -> Identifier() * f2 -> "(" * f3 -> ")" */
	public String visit(AllocationExpression n, String argu) {
		//this.buffer = this.buffer + "\n\t; allocexpr\n";
		//System.out.println("in AllocationExpression");
		String id = n.f1.accept(this, null);
		this.allocExprIdentifier = id;
		Class classCalled = this.table.getClass(id);
		if (classCalled == null) {
			//System.out.println("HEREEEE");
		} else {
			//System.out.println(classCalled.name);
		}
		this.registerCounter += 3;
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 3) + " = call i8* @calloc(i32 1, i32 " + (8 + classCalled.varOffset) + ")\n";
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 2) + " = bitcast i8* %_" + (this.registerCounter - 3) + " to i8***\n";
		this.buffer = this.buffer + "\t%_" + (this.registerCounter - 1) + " = getelementptr [" + classCalled.methods.size() + " x i8*], [" + classCalled.methods.size() + " x i8*]* @." + id + "_vtable, i32 0, i32 0\n";
		this.buffer = this.buffer + "\tstore i8** %_" + (this.registerCounter - 1) + ", i8*** %_" + (this.registerCounter - 2) + "\n";
		return "%_" + (this.registerCounter - 3);
	}

	public String visit(NotExpression n, String argu) {
		//this.buffer = this.buffer + "\n\t; not\n";
		String expr = n.f1.accept(this, argu);
		this.buffer = this.buffer + "\t%_" + this.registerCounter + " = xor i1 1, " + expr + "\n";
		this.registerCounter += 1;
		this.isBoolean = true;
		return "%_" + (this.registerCounter - 1);
	}

	public String visit(BracketExpression n, String argu) {
		return n.f1.accept(this, argu);
	}

	/** * f0 -> "int" * f1 -> "[" * f2 -> "]" */
	public String visit(ArrayType n, String argu) {
		return "int[]";
	}

	/* f0 -> "boolean" */
	public String visit(BooleanType n, String argu) {
		return n.f0.accept(this, argu);
	}

	/* f0 -> "int" */
	public String visit(IntegerType n, String argu) {
		return n.f0.accept(this, argu);
	}

}