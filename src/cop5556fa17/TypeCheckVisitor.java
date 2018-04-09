package cop5556fa17;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {


		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}
		}


	SymbolTable symbol_table = new SymbolTable();
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 *
	 * @throws Exception
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//Declaration_Variable d = declaration_Variable;
		Expression e = declaration_Variable.e;

		if(e!=null)
		{
			e.visit(this, arg);
			declaration_Variable.setType(e.getType());
		}
		if(symbol_table.lookupType(declaration_Variable.name) == Type.NONE)
		{
			if(declaration_Variable.type.kind==Kind.KW_int)
			{
				declaration_Variable.setType(Type.INTEGER);
			}
			if(declaration_Variable.type.kind==Kind.KW_boolean)
			{
				declaration_Variable.setType(Type.BOOLEAN);
			}
			symbol_table.insert(declaration_Variable.name, declaration_Variable);

			if(e != null)
			{
				//e.visit(this, arg);
				if(declaration_Variable.getType() != e.getType())
					throw new SemanticException(declaration_Variable.firstToken, "");
			}
		}
		else
			throw new SemanticException(declaration_Variable.firstToken, "");
		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression_Binary eBin = expression_Binary;
		Expression e0 = expression_Binary.e0, e1 = expression_Binary.e1;
		e0.visit(this, arg);
		if(e1!=null)
			e1.visit(this, arg);
		if((e0.getType() == e1.getType()))
		{
			if(eBin.op==Kind.OP_EQ || eBin.op==Kind.OP_NEQ)
			{
				expression_Binary.setType(Type.BOOLEAN);
			}
			else if(eBin.op==Kind.OP_LE || eBin.op==Kind.OP_LT || eBin.op==Kind.OP_GE || eBin.op==Kind.OP_GT)
			{
				if(e0.getType()==Type.INTEGER)
				{
					expression_Binary.setType(Type.BOOLEAN);
				}
			}
			else if(eBin.op==Kind.OP_AND || eBin.op==Kind.OP_OR)
			{
				if(e0.getType()==Type.INTEGER || e0.getType()==Type.BOOLEAN)
				{
					expression_Binary.setType(e0.getType());
				}
			}
			else if(eBin.op==Kind.OP_DIV || eBin.op==Kind.OP_MINUS || eBin.op==Kind.OP_MOD || eBin.op==Kind.OP_PLUS || eBin.op==Kind.OP_POWER || eBin.op==Kind.OP_TIMES)
			{
				if(e0.getType()==Type.INTEGER)
				{
					expression_Binary.setType(Type.INTEGER);
				}
			}
			else
			{
				expression_Binary.setType(Type.NONE);
			}

		}
		else
			throw new SemanticException(expression_Binary.firstToken, "");

		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = expression_Unary.e;
		Expression_Unary eu = expression_Unary;
		e.visit(this, arg);
		Type t = e.getType();
		if(eu.getType() != Type.NONE)
		{
			if(eu.op == Kind.OP_EXCL && (t == Type.BOOLEAN || t == Type.INTEGER))
				expression_Unary.setType(t);
			else if((eu.op == Kind.OP_PLUS || eu.op == Kind.OP_MINUS) && t == Type.INTEGER)
				expression_Unary.setType(Type.INTEGER);
			else
				expression_Unary.setType(Type.NONE);
		}
		else
			throw new SemanticException(eu.firstToken,"");
		return null;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = index.e0, e1 = index.e1;
		//Index in = index;
		e0.visit(this, arg);
		e1.visit(this, arg);
		if(e0.getType() == Type.INTEGER && e1.getType() == Type.INTEGER)
			index.setCartesian(!(e0.firstToken.kind == Kind.KW_r && e1.firstToken.kind == Kind.KW_a));
		else
			throw new SemanticException (index.firstToken, "");
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//Expression_PixelSelector e = expression_PixelSelector;
		Type t;//changedhere
		Index in = expression_PixelSelector.index;
		if(in!=null)
			in.visit(this, arg);
		if(expression_PixelSelector.getType() != Type.NONE)
		{
			t = symbol_table.lookupType(expression_PixelSelector.name);
			if(t == Type.IMAGE)
				expression_PixelSelector.setType(Type.INTEGER);
			else if(in == null)
				expression_PixelSelector.setType(t);
			else
				expression_PixelSelector.setType(Type.NONE);
		}
		else
			throw new SemanticException(expression_PixelSelector.firstToken, "");
		return null;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
	//	Expression_Conditional eCond = expression_Conditional;
		Expression eTrue = expression_Conditional.trueExpression;
		Expression eFalse = expression_Conditional.falseExpression;
		Expression ec = expression_Conditional.condition;
		ec.visit(this, arg);
		eTrue.visit(this, arg);
		eFalse.visit(this, arg);
		if(ec.getType() == Type.BOOLEAN && eTrue.getType() == eFalse.getType())
			expression_Conditional.setType(eTrue.getType());
		else
			throw new SemanticException(expression_Conditional.firstToken, "");
		return null;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		//Declaration_Image d = declaration_Image;
		Expression xSize = declaration_Image.xSize;
		Expression ySize = declaration_Image.ySize;
		Source s = declaration_Image.source;
		if(symbol_table.lookupType(declaration_Image.name) == Type.NONE)
		{
			declaration_Image.setType(Type.IMAGE);
			symbol_table.insert(declaration_Image.name, declaration_Image);

			if(xSize!=null && ySize!=null)
			{
				xSize.visit(this, arg);
				ySize.visit(this, arg);
				if(xSize.getType() != Type.INTEGER && ySize.getType() != Type.INTEGER)
				{
					throw new SemanticException(declaration_Image.firstToken, "");
				}
			}
		}
		else
			throw new SemanticException(declaration_Image.firstToken, "");
		return null;
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//Source_StringLiteral s = source_StringLiteral;
		URL u = null;
		try
		{
			u = new URL(source_StringLiteral.fileOrUrl);
			source_StringLiteral.setType(Type.URL);
		}
		catch (MalformedURLException e){
			source_StringLiteral.setType(Type.FILE);
	    }
		return null;
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//Expression e = source_CommandLineParam.paramNum;
		//Source_CommandLineParam s = source_CommandLineParam;
//		e.visit(this, arg);
//		source_CommandLineParam.setType(e.getType());
//		if(source_CommandLineParam.getType() != Type.INTEGER)
//			throw new SemanticException(source_CommandLineParam.firstToken, "");
//		return null;
		source_CommandLineParam.paramNum.visit(this, arg);
		if(source_CommandLineParam.paramNum.getType() == Type.INTEGER)//changehere
		{
			source_CommandLineParam.setType(Type.NONE);
		}
		else
			throw new SemanticException(source_CommandLineParam.firstToken, "");
		return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//Source_Ident s = source_Ident;
		source_Ident.setType(symbol_table.lookupType(source_Ident.name));
		if(source_Ident.getType() != Type.FILE && source_Ident.getType() != Type.URL)
			throw new SemanticException(source_Ident.firstToken, "");
		return null;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//Declaration_SourceSink d = declaration_SourceSink;
		Source s = declaration_SourceSink.source;
		if(symbol_table.lookupType(declaration_SourceSink.name) == Type.NONE)
		{
			if(declaration_SourceSink.type == Kind.KW_url)
				declaration_SourceSink.setType(Type.URL);
			if(declaration_SourceSink.type == Kind.KW_file)
				declaration_SourceSink.setType(Type.FILE);
			symbol_table.insert(declaration_SourceSink.name, declaration_SourceSink);
			s.visit(this, arg);
			if(s!= null)
			{
				if(declaration_SourceSink.getType() == s.getType() || s.getType() == Type.NONE)//changedhere
					return null;
				else
					throw new SemanticException(s.firstToken, "");
			}
		}
		else
			throw new SemanticException(declaration_SourceSink.firstToken, "");
		return null;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_IntLit.setType(Type.INTEGER);
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		//Expression_FunctionAppWithExprArg eFunc = expression_FunctionAppWithExprArg;
		Expression e = expression_FunctionAppWithExprArg.arg;
		e.visit(this, arg);
		if(e.getType() == Type.INTEGER)
			expression_FunctionAppWithExprArg.setType(Type.INTEGER);
		else
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, "");
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_FunctionAppWithIndexArg.setType(Type.INTEGER);
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expression_PredefinedName.setType(Type.INTEGER);
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		//Statement_Out st = statement_Out;
		Declaration d = symbol_table.lookupDec(statement_Out.name);
		Sink s = statement_Out.sink;
		s.visit(this, arg);
		Type t = symbol_table.lookupType(statement_Out.name);
		if(d!=null)
		{

			if(((t==Type.INTEGER || t==Type.BOOLEAN) && s.getType()==Type.SCREEN) || (t==Type.IMAGE && (s.getType()==Type.FILE || s.getType()==Type.SCREEN) ) )
			{
				statement_Out.setType(t);
				statement_Out.setDec(d);
			}

		}
		else
		{
			throw new SemanticException(statement_Out.firstToken,"exception");
		}
		return null;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		// TODO Auto-generated method stub

		//Statement_In si = statement_In;
		Source s = statement_In.source;
		s.visit(this, arg);
		Declaration d = symbol_table.lookupDec(statement_In.name);
		Type t = symbol_table.lookupType(statement_In.name);
		//made changes in asg 5
		statement_In.setDec(d);
		return null;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		//Statement_Assign s = statement_Assign;
		//LHS l = statement_Assign.lhs;
		//Expression e = statement_Assign.e;
		//Index i = l.index;
		statement_Assign.lhs.visit(this, arg);
		statement_Assign.e.visit(this, arg);

		if((statement_Assign.lhs.getType() == statement_Assign.e.getType()) || (statement_Assign.lhs.getType() == Type.IMAGE && statement_Assign.e.getType() == Type.INTEGER)) //changedhere
		{
			if(statement_Assign.lhs.index!=null)
			{
				statement_Assign.setCartesian(statement_Assign.lhs.index.isCartesian());
			}
		}
		else
			throw new SemanticException(statement_Assign.firstToken,"exception");
		return null;



		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if(lhs.index != null)
			lhs.index.visit(this, arg);

		Declaration d = symbol_table.lookupDec(lhs.name);
		if(d !=null)
		{
			//d.visit(this, arg);
			lhs.setType(d.getType());
		}
		else
			throw new SemanticException(lhs.firstToken,"exception");
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		sink_SCREEN.setType(Type.SCREEN);
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		sink_Ident.setType(symbol_table.lookupType(sink_Ident.name));
		if(sink_Ident.getType()!=Type.FILE)
		{
			throw new SemanticException(sink_Ident.firstToken,"exception");
		}
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expression_BooleanLit.setType(Type.BOOLEAN);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_Ident.setType(symbol_table.lookupType(expression_Ident.name));
		return null;
	}

}
