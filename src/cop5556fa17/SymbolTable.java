package cop5556fa17;

import java.util.HashMap;

import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.Declaration;

public class SymbolTable {

	HashMap<String, Declaration> symbolTable;

	public SymbolTable(){
		symbolTable = new HashMap<>();
	}

	public void insert(String name, Declaration dec)
	{
		symbolTable.put(name, dec);
	}

	public Type lookupType(String name)
	{
		if(symbolTable.containsKey(name))
			return symbolTable.get(name).getType();
		else
			return Type.NONE;
	}

	public Declaration lookupDec(String name)
	{
		if(symbolTable.containsKey(name))
			return symbolTable.get(name);
		else
			return null;
	}
}
