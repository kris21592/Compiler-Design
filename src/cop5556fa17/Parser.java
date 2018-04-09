package cop5556fa17;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.*;
import cop5556fa17.Parser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;
	Token identToken;
	HashMap<Kind, String> words = new HashMap<>();
	HashMap<Kind, String> funcNames = new HashMap<>();
	HashMap<Kind, String> unaryExpr = new HashMap<>();


	void setUnaryExpr()
	{
		unaryExpr.put(Kind.KW_x, "KW_x");
		unaryExpr.put(Kind.KW_y, "KW_y");
		unaryExpr.put(Kind.KW_r, "KW_r");
		unaryExpr.put(Kind.KW_a, "KW_a");
		unaryExpr.put(Kind.KW_X, "KW_X");
		unaryExpr.put(Kind.KW_Y, "KW_Y");
		unaryExpr.put(Kind.KW_Z, "KW_Z");
		unaryExpr.put(Kind.KW_A, "KW_A");
		unaryExpr.put(Kind.KW_R, "KW_R");
		unaryExpr.put(Kind.KW_DEF_X , "KW_DEF_X");
		unaryExpr.put(Kind.KW_DEF_Y , "KW_DEF_Y");
	}
	void setfuncNames()
	{
		funcNames.put(Kind.KW_sin, "KW_sin");
		funcNames.put(Kind.KW_cos, "KW_cos");
		funcNames.put(Kind.KW_atan, "KW_atan");
		funcNames.put(Kind.KW_abs, "KW_abs");
		funcNames.put(Kind.KW_cart_x, "KW_cart_x");
		funcNames.put(Kind.KW_cart_y,  "KW_cart_y");
		funcNames.put(Kind.KW_polar_a, "KW_polar_a");
		funcNames.put(Kind.KW_polar_r, "KW_polar_r");
	}
	void setwords()
	{
		words.put(Kind.OP_EXCL, "op_EXCL");
		words.put(Kind.KW_x, "KW_x");
		words.put(Kind.KW_y, "KW_y");
		words.put(Kind.KW_r, "KW_r");
		words.put(Kind.KW_a, "KW_a");
		words.put(Kind.KW_X, "KW_X");
		words.put(Kind.KW_Y, "KW_Y");
		words.put(Kind.KW_Z, "KW_Z");
		words.put(Kind.KW_A, "KW_A");
		words.put(Kind.KW_R, "KW_R");
		words.put(Kind.KW_DEF_X , "KW_DEF_X");
		words.put(Kind.KW_DEF_Y , "KW_DEF_Y");
		words.put(Kind.IDENTIFIER, "IDENTIFIER");
		words.put(Kind.INTEGER_LITERAL, "INTEGER_LITERAL");
		words.put(Kind.LPAREN, "LPAREN");
		words.put(Kind.KW_sin, "KW_sin");
		words.put(Kind.KW_cos, "KW_cos");
		words.put(Kind.KW_atan, "KW_atan");
		words.put(Kind.KW_abs, "KW_abs");
		words.put(Kind.KW_cart_x, "KW_cart_x");
		words.put(Kind.KW_cart_y,  "KW_cart_y");
		words.put(Kind.KW_polar_a, "KW_polar_a");
		words.put(Kind.KW_polar_r, "KW_polar_r");
		words.put(Kind.BOOLEAN_LITERAL, "true/false");
	}

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
		setwords();
		setfuncNames();
		setUnaryExpr();
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 *
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {

		Program p = program();

		matchEOF();

		return p;
	}


	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*
	 *
	 * Program is start symbol of our grammar.
	 *
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException {
		//TODO  implement this
		Token name = t;
		Token firstToken = t;
		ArrayList<ASTNode> decsAndStatements = new ArrayList<ASTNode>();
		match(Kind.IDENTIFIER);
		while(t.kind == Kind.KW_int || t.kind == Kind.KW_boolean || t.kind == Kind.KW_image || t.kind == Kind.KW_url || t.kind == Kind.KW_file || t.kind == Kind.IDENTIFIER)
		{
			if(t.kind == Kind.IDENTIFIER)
			{
				decsAndStatements.add(Statement());
				match(Kind.SEMI);
			}

			if(t.kind == KW_int || t.kind == Kind.KW_boolean || t.kind == Kind.KW_image || t.kind == Kind.KW_url || t.kind == Kind.KW_file)
			{
				decsAndStatements.add(Declaration());
				match(Kind.SEMI);
			}
		}
		return new Program(firstToken, name, decsAndStatements);
		//throw new UnsupportedOperationException();
	}



	/**
	 * expression ::=  Orexpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 *
	 * Our test cases may invoke this routine directly to support incremental development.
	 *
	 * @throws SyntaxException
	 */
	Expression expression() throws SyntaxException {
		//TODO implement this.
		//System.out.println("expr");
		Expression condition, falseExpression, trueExpression, e;
		Token firstToken =t;
		condition = OrExpression();
		if(t.kind == Kind.OP_Q)
		{
			//Token token = t;
			match(t.kind);
			trueExpression = expression();
			match(Kind.OP_COLON);
			falseExpression = expression();
			return new Expression_Conditional(firstToken, condition, trueExpression, falseExpression);
		}
		return condition;
		//throw new UnsupportedOperationException();
	}

	/** Statement  ::= AssignmentStatement | ImageOutStatement | ImageInStatement */

	Statement Statement() throws SyntaxException
	{
		Statement s;
		if(t.kind == Kind.IDENTIFIER)
		{
			identToken = t;
			match(Kind.IDENTIFIER);
			//t = scanner.nextToken();
			if(t.kind == Kind.OP_RARROW)
				s = ImageOutStatement();
			else if(t.kind == Kind.OP_LARROW)
				s = ImageInStatement();
			else if(t.kind == Kind.LSQUARE || t.kind == Kind.OP_ASSIGN)
				s = AssignmentStatement();
			else
			{
				String message = "Was " + t.kind + " at " + t.line + ":" + t.pos_in_line;
				throw new SyntaxException(t, message);
			}
		}
		else
		{
			String message = "Was " + t.kind + " at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		return s;
	}

	/** Declaration :: =  VariableDeclaration     |    ImageDeclaration   |   SourceSinkDeclaration  */

	Declaration Declaration() throws SyntaxException
	{
		Declaration d;
		if(t.kind == Kind.KW_url || t.kind == Kind.KW_file)
		{
			d = SourceSinkDeclaration();
		}
		else if(t.kind == Kind.KW_int || t.kind == Kind.KW_boolean)
		{
			d = VariableDeclaration();
		}
		else if(t.kind == Kind.KW_image)
		{
			d = ImageDeclaration();
		}
		else
		{
			String message = "Was " + t.kind + " at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		return d;
	}

	/** OrExpression ::= AndExpression   (  OP_OR  AndExpression)* */
	Expression OrExpression() throws SyntaxException
	{
		Expression e0, e1 = null;
		Token firstToken = t;

		e0 = AndExpression();

		while(t.kind == Kind.OP_OR)
		{
			Token op = t;
			match(t.kind);
			e1 = AndExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}

	/** AndExpression ::= EqExpression ( OP_AND  EqExpression )* */
	Expression AndExpression() throws SyntaxException
	{
		Expression e0, e1 = null;
		Token firstToken = t;

		e0 = EqExpression();

		while(t.kind == Kind.OP_AND)
		{
			Token op = t;
			match(t.kind);
			e1 = EqExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}

	/** EqExpression ::= RelExpression  (  (OP_EQ | OP_NEQ )  RelExpression )* */
	Expression EqExpression() throws SyntaxException
	{
		Expression e0, e1 = null;
		Token firstToken = t;

		e0 = RelExpression();

		while(t.kind == Kind.OP_EQ || t.kind == Kind.OP_NEQ)
		{
			Token op = t;
			match(t.kind);
			e1 = RelExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}

	/** RelExpression ::= AddExpression (  ( OP_LT  | OP_GT |  OP_LE  | OP_GE )   AddExpression)* */
	Expression RelExpression() throws SyntaxException
	{
		Expression e0, e1 = null;
		Token firstToken = t;
		e0 = AddExpression();
		while(t.kind == Kind.OP_LT || t.kind == Kind.OP_GT || t.kind == Kind.OP_LE || t.kind == Kind.OP_GE)
		{
			Token op = t;
			match(t.kind);
			e1 = AddExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}

	/** AddExpression ::= MultExpression   (  (OP_PLUS | OP_MINUS ) MultExpression )* */
	Expression AddExpression() throws SyntaxException
	{
		Expression e0, e1 = null;
		Token firstToken = t;
		e0 = MultExpression();
		while(t.kind == Kind.OP_PLUS || t.kind == Kind.OP_MINUS)
		{
			Token op = t;
			match(t.kind);
			e1 = MultExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;

	}

	/** MultExpression := UnaryExpression ( ( OP_TIMES | OP_DIV  | OP_MOD ) UnaryExpression )* */
	Expression MultExpression() throws SyntaxException
	{
		Expression e0, e1 = null;
		Token firstToken = t;
		e0 = UnaryExpression();

		while(t.kind == Kind.OP_TIMES || t.kind == Kind.OP_DIV || t.kind == Kind.OP_MOD )
		{
			Token op = t;
			match(t.kind);
			e1 = UnaryExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}

	/** UnaryExpression ::= OP_PLUS UnaryExpression | OP_MINUS UnaryExpression | UnaryExpressionNotPlusMinus */
	Expression UnaryExpression() throws SyntaxException
	{
		Token firstToken = t;
		Expression e0, e;
		if(t.kind == Kind.OP_PLUS)
		{
			Token op = t;
			match(Kind.OP_PLUS);
			e0 = UnaryExpression();
			e = new Expression_Unary(firstToken, op, e0);
		}
		else if(t.kind == Kind.OP_MINUS)
		{
			Token op = t;
			match(Kind.OP_MINUS);
			e0 = UnaryExpression();
			e = new Expression_Unary(firstToken, op, e0);
		}
		else if(words.containsKey(t.kind) || t.kind == Kind.BOOLEAN_LITERAL)
		{
			e = UnaryExpressionNotPlusMinus();
		}
		else
		{
			String message = "Was " + t.kind + " at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		return e;
	}

	/** UnaryExpressionNotPlusMinus ::=  OP_EXCL  UnaryExpression  | Primary
		| IdentOrPixelSelectorExpression | KW_x | KW_y | KW_r | KW_a | KW_X
		| KW_Y | KW_Z | KW_A | KW_R | KW_DEF_X | KW_DEF_Y
	*/
	Expression UnaryExpressionNotPlusMinus() throws SyntaxException
	{
		Token firstToken = t;
		Expression e0=null, e=null;
		if(t.kind == Kind.OP_EXCL)
		{
			Token op = t;
			match(Kind.OP_EXCL);
			e0 = UnaryExpression();
			e = new Expression_Unary(firstToken, op, e0);
		}
		else if(t.kind == Kind.INTEGER_LITERAL || funcNames.containsKey(t.kind) || t.kind == Kind.LPAREN || t.kind == Kind.BOOLEAN_LITERAL)
		{
			e = Primary();
		}
		else if(t.kind == Kind.IDENTIFIER)
		{
			e = IdentOrPixelSelectorExpression();
		}
		else if(unaryExpr.containsKey(t.kind))
		{
			e = new Expression_PredefinedName(firstToken, t.kind);
			match(t.kind);
		}
		else
		{
			String message = "Was " + t.kind + " at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		return e;
	}

	/** Sink ::= IDENTIFIER | KW_SCREEN  //ident must be file */
	Sink Sink() throws SyntaxException
	{
		Sink s;
		Token firstToken = t;
		if(t.kind == Kind.IDENTIFIER)
		{
			Token name = t;
			s = new Sink_Ident(firstToken, name);
			match(Kind.IDENTIFIER);
		}
		else if(t.kind == Kind.KW_SCREEN)
		{
			Token name = t;
			s= new Sink_SCREEN(name);
			match(Kind.KW_SCREEN);
		}
		else
		{
			String message = "Was " + t.kind + " at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		return s;
	}

	/** ImageOutStatement ::= IDENTIFIER OP_RARROW Sink */
	Statement ImageOutStatement() throws SyntaxException
	{
		//match(Kind.IDENTIFIER);
		Token name = identToken;
		Token firstToken = identToken;
		Sink sink;
		Statement s;
		match(Kind.OP_RARROW);
		sink = Sink();
		s = new Statement_Out(firstToken, name, sink);
		return s;
	}

	/**     Source ::= STRING_LITERAL
			Source ::= OP_AT expression
			Source ::= IDENTIFIER
	 */
	Source Source() throws SyntaxException
	{
		Source source;
		Token firstToken = t;
		if(t.kind == Kind.STRING_LITERAL)
		{
			String string = t.getText();
			source = new Source_StringLiteral(firstToken, string);
			match(Kind.STRING_LITERAL);
		}
		else if(t.kind == Kind.OP_AT)
		{
			match(Kind.OP_AT);
			Expression e = expression();
			source = new Source_CommandLineParam(firstToken, e);
		}
		else if(t.kind == Kind.IDENTIFIER)
		{
			Token name = t;
			source = new Source_Ident(firstToken, name);
			match(Kind.IDENTIFIER);
		}
		else
		{
			String message = "Was " + t.kind + " at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		return source;
	}

	/** ImageInStatement ::= IDENTIFIER OP_LARROW Source */
	Statement ImageInStatement() throws SyntaxException
	{
		Token firstToken = identToken;
		//match(Kind.IDENTIFIER);
		Statement s;
		Token name = identToken;
		Source source;
		match(Kind.OP_LARROW);
		source = Source();
		s = new Statement_In(firstToken, name, source);
		return s;
	}

	/** SourceSinkType := KW_url | KW_file */
	void SourceSinkType() throws SyntaxException
	{
		if(t.kind == Kind.KW_url)
			match(Kind.KW_url);
		else if(t.kind == Kind.KW_file)
			match(Kind.KW_file);
		else
		{
			String message = "Was " + t.kind + " at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
	}

	/** SourceSinkDeclaration ::= SourceSinkType IDENTIFIER  OP_ASSIGN  Source */
	Declaration SourceSinkDeclaration() throws SyntaxException
	{
		Token firstToken = t;
		Declaration d;
		Source src;
		Token type = t;
		SourceSinkType();
		Token name = t;
		match(Kind.IDENTIFIER);
		match(Kind.OP_ASSIGN);
		src = Source();
		d = new Declaration_SourceSink(firstToken, type, name, src);
		return d;
	}

	/** VarType ::= KW_int | KW_boolean */
	void VarType() throws SyntaxException
	{
		if(t.kind == Kind.KW_int)
			match(Kind.KW_int);
		else if(t.kind == Kind.KW_boolean)
			match(Kind.KW_boolean);
		else
		{
			String message = "Was " + t.kind + " at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
	}


	/** FunctionName ::= KW_sin | KW_cos | KW_atan | KW_abs | KW_cart_x | KW_cart_y | KW_polar_a | KW_polar_r */
	Kind FunctionName() throws SyntaxException
	{
		if(funcNames.containsKey(t.kind))
		{
			Kind kind = t.kind;
			match(t.kind);
			return kind;
		}
		else
		{
			String message = "Was " + t.kind + " at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
	}

	/** Primary ::= INTEGER_LITERAL | LPAREN expression RPAREN | FunctionApplication  */
	Expression Primary() throws SyntaxException
	{
		Expression e = null;
		Token firstToken = t;
		if(t.kind == Kind.INTEGER_LITERAL)
		{
			int value = t.intVal();
			e = new Expression_IntLit(firstToken, value);
			match(Kind.INTEGER_LITERAL);
		}
		else if(t.kind == Kind.LPAREN)
		{
			match(Kind.LPAREN);
			e = expression();
			match(Kind.RPAREN);
		}
		else if(funcNames.containsKey(t.kind))
			e = FunctionApplication();
		else if(t.kind == Kind.BOOLEAN_LITERAL)
		{
			String value = t.getText();
			e = new Expression_BooleanLit(firstToken, Boolean.parseBoolean(value));
			match(Kind.BOOLEAN_LITERAL);
		}
		else
		{
			String message = "Was " + t.kind + " at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		return e;
	}

	/** FunctionApplication ::= FunctionName LPAREN Expression RPAREN  | FunctionName  LSQUARE Selector RSQUARE */

	Expression FunctionApplication() throws SyntaxException
	{
		Expression e0,e;
		Token firstToken = t;
		Index in;
		Kind function = FunctionName();


		if(t.kind == Kind.LPAREN || t.kind == Kind.LSQUARE)
		{
			if(t.kind == Kind.LPAREN)
			{
				match(Kind.LPAREN);
				e0 = expression();
				match(Kind.RPAREN);
				e = new Expression_FunctionAppWithExprArg(firstToken, function, e0);
			}
			else if(t.kind == Kind.LSQUARE)
			{
				match(Kind.LSQUARE);
				in = Selector();
				match(Kind.RSQUARE);
				e = new Expression_FunctionAppWithIndexArg(firstToken, function, in);
			}
			else
			{
				String message = "Was " + t.kind + " at " + t.line + ":" + t.pos_in_line;
				throw new SyntaxException(t, message);
			}
		}
		else
		{
			String message = "Was " + t.kind + " at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		return e;
	}

	/** IdentOrPixelSelectorExpression::=  IDENTIFIER LSQUARE Selector RSQUARE   | IDENTIFIER */

	Expression IdentOrPixelSelectorExpression() throws SyntaxException
	{
		Token firstToken = t;
		Expression e = null;
		Index index;
		if(t.kind == Kind.IDENTIFIER)
		{
			Token ident = t;
			e = new Expression_Ident(firstToken, ident);
			match(Kind.IDENTIFIER);
			if(t.kind == Kind.LSQUARE)
			{
				match(Kind.LSQUARE);
				index = Selector();
				e = new Expression_PixelSelector(firstToken, ident, index);
				match(Kind.RSQUARE);
			}
		}
		return e;
	}

	/** AssignmentStatement ::= Lhs OP_ASSIGN Expression */
	Statement AssignmentStatement() throws SyntaxException
	{
		LHS l=Lhs();
		Token firstToken = t;
		Expression e;
		Statement s;
		if(t.kind == Kind.LSQUARE)
			l = Lhs();
		match(Kind.OP_ASSIGN);
		e = expression();
		s = new Statement_Assign(firstToken, l, e);
		return s;
	}

	/** Lhs::=  IDENTIFIER ( LSQUARE LhsSelector RSQUARE   | E ) */

	LHS Lhs() throws SyntaxException
	{
		LHS l = null;
		Index in = null;
		Token firstToken = identToken;
		Token name = identToken;
		if(t.kind == Kind.LSQUARE)
		{
			match(Kind.LSQUARE);
			in = LhsSelector();
			match(Kind.RSQUARE);

		}
		l = new LHS(firstToken, name, in);
		return l;
	}

	/** ImageDeclaration ::=  KW_image  (LSQUARE Expression COMMA Expression RSQUARE | E) IDENTIFIER ( OP_LARROW Source | E ) */
	Declaration ImageDeclaration() throws SyntaxException
	{
		Declaration d = null;
		Expression xSize = null, ySize = null;
		Source src = null;
		Token firstToken =t;
		Token name = null;
		if(t.kind == Kind.KW_image)
		{
			match(Kind.KW_image);
			if(t.kind == Kind.LSQUARE)
			{
				match(Kind.LSQUARE);
				xSize = expression();
				match(Kind.COMMA);
				ySize = expression();
				match(Kind.RSQUARE);
			}

			//if(t.kind == Kind.IDENTIFIER)
			//{
				name = t;
				match(Kind.IDENTIFIER);
				if(t.kind == Kind.OP_LARROW)
				{
					match(Kind.OP_LARROW);
					src = Source();
				}
			//}
			d = new Declaration_Image(firstToken, xSize, ySize, name, src);
		}
		return d;
	}

	/** VariableDeclaration  ::=  VarType IDENTIFIER  (  OP_ASSIGN  Expression  | E ) */
	Declaration VariableDeclaration() throws SyntaxException
	{
		Declaration d = null;
		Expression e = null;
		Token firstToken = t;
		Token type = t;
		VarType();
		if(t.kind == Kind.IDENTIFIER)
		{
			Token name = t;
			match(Kind.IDENTIFIER);
			if(t.kind == Kind.OP_ASSIGN)
			{
				match(Kind.OP_ASSIGN);
				e = expression();
			}
			d = new Declaration_Variable(firstToken, type, name, e);
		}
		return d;
	}


	/** LhsSelector ::= LSQUARE  ( XySelector  | RaSelector  )   RSQUARE */
	Index LhsSelector() throws SyntaxException
	{
		Index in = null;
		match(Kind.LSQUARE);
		if(t.kind== Kind.KW_r)
			in = RaSelector();
		else if(t.kind == Kind.KW_x)
			in = XySelector();
		else
		{
			String message = "Was " + t.kind + " at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		match(Kind.RSQUARE);
		return in;
	}

	/** Selector ::=  expression COMMA expression */
	Index Selector() throws SyntaxException
	{
		Token firstToken = t;
		Index in = null;
		Expression e0 = expression();
		match(Kind.COMMA);
		Expression e1 = expression();
		in = new Index(firstToken, e0, e1);
		return in;
	}

	/** RaSelector ::= KW_r COMMA KW_A */
	Index RaSelector() throws SyntaxException
	{
		Token firstToken = t;
		Expression e0 = new Expression_PredefinedName(firstToken, firstToken.kind);
		match(Kind.KW_r);
		Token secondToken = t;
		match(Kind.COMMA);
		Token thirdToken = t;
		Expression e1 = new Expression_PredefinedName(thirdToken, thirdToken.kind);
		match(Kind.KW_a);//changedhere
		Index in = new Index(secondToken, e0, e1);
		return in;
	}

	/** XySelector ::= KW_x COMMA KW_y */
	Index XySelector() throws SyntaxException
	{
		Token firstToken = t;
		Expression e0 = new Expression_PredefinedName(firstToken, firstToken.kind);
		match(Kind.KW_x);
		Token secondToken = t;
		match(Kind.COMMA);
		Token thirdToken = t;
		Expression e1 = new Expression_PredefinedName(thirdToken, thirdToken.kind);
		match(Kind.KW_y);
		Index in = new Index(secondToken, e0, e1);
		return in;
	}


	private Token consume() throws SyntaxException
	{
		Token temp = t;
		t = scanner.nextToken();
		return temp;
	}


	private void match(Kind kind) throws SyntaxException {
		if (t.kind == kind)
		{
			consume();
		}
		else
		{
			String message = "Was " + t.kind + " at " + t.line + ":" + t.pos_in_line + " but Expected " + kind;
			throw new SyntaxException(t, message);
		}
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 *
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}
