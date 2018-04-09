package cop5556fa17.AST;

import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;

public abstract class Source extends ASTNode{

	public abstract Type getType();
	
	public abstract void setType(Type type);
	
	public Source(Token firstToken) {
		super(firstToken);
	}
	
	
}
