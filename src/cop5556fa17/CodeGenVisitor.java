package cop5556fa17;

import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	int x_slot = 1;
	int y_slot = 2;
	int X_slot = 3;
	int Y_slot = 4;
	int r_slot = 5;
	int a_slot = 6;
	int R_slot = 7;
	int A_slot = 8;


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		//cw = new ClassWriter(0);
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

		// initialize
		mv.visitCode();
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// if GRADE, generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "entering main");


		// visit decs and statements to add field to class
		//  and instructions to main method, respectively
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "leaving main");

		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);

		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

		mv.visitLocalVariable("x", "I", null, mainStart, mainEnd, x_slot);
		mv.visitLocalVariable("y", "I", null, mainStart, mainEnd, y_slot);
		mv.visitLocalVariable("X", "I", null, mainStart, mainEnd, X_slot);
		mv.visitLocalVariable("Y", "I", null, mainStart, mainEnd, Y_slot);
		mv.visitLocalVariable("r", "I", null, mainStart, mainEnd, r_slot);
		mv.visitLocalVariable("a", "I", null, mainStart, mainEnd, a_slot);
		mv.visitLocalVariable("R", "I", null, mainStart, mainEnd, R_slot);
		mv.visitLocalVariable("A", "I", null, mainStart, mainEnd, A_slot);


		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);

		//terminate construction of main method
		mv.visitEnd();

		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		// TODO
		FieldVisitor fv = cw.visitField(ACC_STATIC, declaration_Variable.name, declaration_Variable.getType()==Type.BOOLEAN?"Z":"I", null, null);
		fv.visitEnd();
		if(declaration_Variable.e!=null)
		{
			declaration_Variable.e.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, declaration_Variable.getType()==Type.BOOLEAN?"Z":"I");
		}
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO
		//throw new UnsupportedOperationException();
		Expression e0 = expression_Binary.e0;
		Expression e1 = expression_Binary.e1;
		Kind op = expression_Binary.op;

		e0.visit(this, arg);
		e1.visit(this, arg);

		Label l1 = new Label();
        Label l2 = new Label();

		if(op == Kind.OP_EQ)
		{
			mv.visitJumpInsn(IF_ICMPEQ, l1);
			mv.visitInsn(ICONST_0);
    		mv.visitJumpInsn(GOTO, l2);
    		mv.visitLabel(l1);
    		mv.visitInsn(ICONST_1);
    		mv.visitLabel(l2);
		}
		else if(op == Kind.OP_NEQ)
		{
			mv.visitJumpInsn(IF_ICMPNE, l1);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, l2);
    		mv.visitLabel(l1);
    		mv.visitInsn(ICONST_1);
    		mv.visitLabel(l2);
		}
		else if(op == Kind.OP_LE)
		{
			mv.visitJumpInsn(IF_ICMPLE, l1);
			mv.visitInsn(ICONST_0);
    		mv.visitJumpInsn(GOTO, l2);
    		mv.visitLabel(l1);
    		mv.visitInsn(ICONST_1);
    		mv.visitLabel(l2);
		}
		else if(op == Kind.OP_LT)
		{
			mv.visitJumpInsn(IF_ICMPLT, l1);
			mv.visitInsn(ICONST_0);
    		mv.visitJumpInsn(GOTO, l2);
    		mv.visitLabel(l1);
    		mv.visitInsn(ICONST_1);
    		mv.visitLabel(l2);
		}
		else if(op == Kind.OP_GE)
		{
			mv.visitJumpInsn(IF_ICMPGE, l1);
			mv.visitInsn(ICONST_0);
    		mv.visitJumpInsn(GOTO, l2);
    		mv.visitLabel(l1);
    		mv.visitInsn(ICONST_1);
    		mv.visitLabel(l2);
		}
		else if(op == Kind.OP_GT)
		{
			mv.visitJumpInsn(IF_ICMPGT, l1);
			mv.visitInsn(ICONST_0);
    		mv.visitJumpInsn(GOTO, l2);
    		mv.visitLabel(l1);
    		mv.visitInsn(ICONST_1);
    		mv.visitLabel(l2);
		}
		else if(op == Kind.OP_PLUS)
		{
			mv.visitInsn(IADD);
		}
		else if(op == Kind.OP_MINUS)
		{
			mv.visitInsn(ISUB);
		}
		else if(op == Kind.OP_TIMES)
		{
			mv.visitInsn(IMUL);
		}
		else if(op == Kind.OP_DIV)
		{
			mv.visitInsn(IDIV);
		}
		else if(op == Kind.OP_MOD)
		{
			mv.visitInsn(IREM);
		}
		else if(op == Kind.OP_AND)
		{
			mv.visitInsn(IAND);
		}
		else if(op == Kind.OP_OR)
		{
			mv.visitInsn(IOR);
		}
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.getType());
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO
		Expression e = expression_Unary.e;
		Kind operator = expression_Unary.op;
		Type type = expression_Unary.getType();
		e.visit(this, arg);

		if(operator == Kind.OP_PLUS)
		{
		}
		else if(operator == Kind.OP_MINUS)
		{
			mv.visitInsn(INEG);
		}
		else if(operator == Kind.OP_EXCL)
		{
			if(type == Type.BOOLEAN)
			{
				Label falseLabel = new Label();
				Label trueLabel = new Label();

                mv.visitJumpInsn(IFEQ, falseLabel);
                mv.visitInsn(ICONST_0);
                mv.visitJumpInsn(GOTO, trueLabel);

                mv.visitLabel(falseLabel);
                mv.visitInsn(ICONST_1);

                mv.visitLabel(trueLabel);
			}
			else
			{
				//xor with max value
                mv.visitLdcInsn(INTEGER.MAX_VALUE);
                mv.visitInsn(IXOR);
            }
		}

       // CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.getType());
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		if(!index.isCartesian())
		{
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		}
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6

		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, "Ljava/awt/image/BufferedImage;");
		expression_PixelSelector.index.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel",ImageSupport.getPixelSig, false);
		//"(Ljava/awt/image/BufferedImage;II)I"
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO
		Expression eCondition = expression_Conditional.condition;
		Expression eTrue = expression_Conditional.trueExpression;
		Expression eFalse = expression_Conditional.falseExpression;

		Label trueLabel = new Label();
		Label falseLabel = new Label();

		eCondition.visit(this, arg);
		mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(IF_ICMPEQ,falseLabel);
        eTrue.visit(this, arg);
        mv.visitJumpInsn(GOTO, trueLabel);
        mv.visitLabel(falseLabel);
        eFalse.visit(this, arg);
        mv.visitLabel(trueLabel);

		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.getType());
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6

		FieldVisitor fv = cw.visitField(ACC_STATIC, declaration_Image.name, "Ljava/awt/image/BufferedImage;", null, null);
	    fv.visitEnd();

		if(declaration_Image.source!=null)
		{
			declaration_Image.source.visit(this, arg);
			if(declaration_Image.xSize!=null)
			{
				declaration_Image.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;)Ljava/lang/Integer;",false);
			}
			else
				mv.visitInsn(ACONST_NULL);

			if(declaration_Image.ySize!=null)
			{
				declaration_Image.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;)Ljava/lang/Integer;",false);
			}
			else
				mv.visitInsn(ACONST_NULL);

			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage",ImageSupport.readImageSig, false);
			//"(Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/awt/image/BufferedImage;"
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, "Ljava/awt/image/BufferedImage;");
		}
		else
		{
			if(declaration_Image.xSize!=null)
				declaration_Image.xSize.visit(this, arg);
			else
				mv.visitLdcInsn(256);

			if(declaration_Image.ySize!=null)
				declaration_Image.ySize.visit(this, arg);
			else
				mv.visitLdcInsn(256);

			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage",ImageSupport.makeImageSig, false);
			//"(II)Ljava/awt/image/BufferedImage;"
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, "Ljava/awt/image/BufferedImage;");
		}

		return null;
		//throw new UnsupportedOperationException();
	}


	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
		//mv.visitFieldInsn(GETSTATIC, className, source_StringLiteral.fileOrUrl, "Ljava/lang/String;");
		//CodeGenUtils.genLogTOS(GRADE, mv, source_StringLiteral.getType());
		return null;
		//throw new UnsupportedOperationException();
	}



	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO
		//System.out.println("inside command line param");
		mv.visitVarInsn(ALOAD, 0);
		source_CommandLineParam.paramNum.visit(this, arg);
		mv.visitInsn(AALOAD);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6 //getURL fetFILE
		//mv.visitLdcInsn(source_Ident.name);
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, "Ljava/lang/String;");
		//mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, "Ljava/lang/String;");
		//CodeGenUtils.genLogTOS(GRADE, mv, source_Ident.getType());
		return null;
		//throw new UnsupportedOperationException();
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		FieldVisitor fv = cw.visitField(ACC_STATIC, declaration_SourceSink.name, ImageSupport.StringDesc, null, null);
	    fv.visitEnd();
	    if(declaration_SourceSink.source != null)
	    {
    		declaration_SourceSink.source.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_SourceSink.name, ImageSupport.StringDesc);
		}
	//	throw new UnsupportedOperationException();
		return null;
	}



	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO
	//	throw new UnsupportedOperationException();
		mv.visitLdcInsn(expression_IntLit.value);
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		// TODO HW6
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		if(expression_FunctionAppWithExprArg.function == Kind.KW_abs)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig, false);
		}
		if(expression_FunctionAppWithExprArg.function == Kind.KW_log)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig, false);
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6
		Expression e0 = expression_FunctionAppWithIndexArg.arg.e0;
		Expression e1 = expression_FunctionAppWithIndexArg.arg.e1;
		Kind function = expression_FunctionAppWithIndexArg.function;
		e0.visit(this, arg);
		e1.visit(this, arg);

		if(function == Kind.KW_cart_x)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
		}
		if(function == Kind.KW_cart_y)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		}
		if(function == Kind.KW_polar_a)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
		}
		if(function == Kind.KW_polar_r)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		Kind kind = expression_PredefinedName.kind;
		switch(kind)
		{
			case KW_a:
			{
				mv.visitVarInsn(ILOAD, x_slot);
				mv.visitVarInsn(ILOAD, y_slot);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig,false);
				mv.visitVarInsn(ISTORE, a_slot);
				mv.visitVarInsn(ILOAD, a_slot);
				//mv.visitFieldInsn(GETSTATIC, className, "a", "I");
				break;
			}
			case KW_r:
			{
				mv.visitVarInsn(ILOAD, x_slot);
				mv.visitVarInsn(ILOAD, y_slot);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig,false);
				mv.visitVarInsn(ISTORE, r_slot);
				mv.visitVarInsn(ILOAD, r_slot);
				//mv.visitFieldInsn(GETSTATIC, className, "r", "I");
				break;
			}
			case KW_R:
			{
				mv.visitVarInsn(ILOAD, R_slot);
				//mv.visitFieldInsn(GETSTATIC, className, "R", "I");
				break;
			}
			case KW_A:
			{
				mv.visitVarInsn(ILOAD, A_slot);
				//mv.visitFieldInsn(GETSTATIC, className, "A", "I");
				break;
			}
			case KW_x:
			{
				mv.visitVarInsn(ILOAD, x_slot);
				//mv.visitFieldInsn(GETSTATIC, className, "x", "I");
				break;
			}
			case KW_y:
			{
				mv.visitVarInsn(ILOAD, y_slot);
				//mv.visitFieldInsn(GETSTATIC, className, "y", "I");
				break;
			}
			case KW_X:
			{
				mv.visitVarInsn(ILOAD, X_slot);
				//mv.visitFieldInsn(GETSTATIC, className, "X", "I");
				break;
			}
			case KW_Y:
			{
				mv.visitVarInsn(ILOAD, Y_slot);
				//mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
				break;
			}
			case KW_Z:
			{
				mv.visitLdcInsn(16777215);
				break;
			}
			case KW_DEF_X:
			{
				mv.visitLdcInsn(256);
				break;
			}
			case KW_DEF_Y:
			{
				mv.visitLdcInsn(256);
				break;
			}
			default:
				break;
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		Type type = statement_Out.getDec().getType();
		if(type == Type.BOOLEAN)
		{
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className,statement_Out.name, "Z");
			CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.typeName);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
		}
		if(type == Type.INTEGER)
		{
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className,statement_Out.name, "I");
			CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.typeName);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
		}
		if(type == Type.IMAGE)
		{
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Ljava/awt/image/BufferedImage;");
			CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.typeName);
			statement_Out.sink.visit(this, arg);
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 *
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean
	 *  to convert String to actual type.
	 *
	 *  TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		//System.out.println("Inside Statement IN");
		statement_In.source.visit(this, arg);
		Type type = statement_In.getDec().getType();
		if(type == Type.INTEGER)
		{
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);//here
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "I");
		}
		else if(type == Type.BOOLEAN)
		{
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);//here
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Z");
		}
		else if(type == Type.IMAGE)
		{
			Declaration_Image image = (Declaration_Image) statement_In.getDec();
	    	if(image.xSize != null)
	    	{
		    	image.xSize.visit(this, arg);
		    	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
		    }
	    	else
	    	{
		    	mv.visitInsn(ACONST_NULL);
		    }
	    	if(image.ySize != null)
	    	{
		    	image.ySize.visit(this, arg);
		    	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
		    }
	    	else
	    	{
		    	mv.visitInsn(ACONST_NULL);
		    }
		    mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig,false);
			mv.visitFieldInsn(PUTSTATIC, className, image.name, "Ljava/awt/image/BufferedImage;");//changedhere
		}
		//throw new UnsupportedOperationException();
		return null;
	}


	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		//TODO  (see comment)
		Type type = statement_Assign.lhs.getType();
		if(type == Type.BOOLEAN || type == Type.INTEGER )
		{
				statement_Assign.e.visit(this, arg);
				statement_Assign.lhs.visit(this, arg);
		}
		else if(type == Type.IMAGE)
		{

			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, "Ljava/awt/image/BufferedImage;");
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX",ImageSupport.getXSig, false);
			mv.visitVarInsn(ISTORE, X_slot);

			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, "Ljava/awt/image/BufferedImage;");
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY",ImageSupport.getYSig, false);
			mv.visitVarInsn(ISTORE, Y_slot);

			//start of nested for loop
			Label l2 = new Label();
			mv.visitLabel(l2);
			//mv.visitLineNumber(15, l2);
			//initializing loop for x
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, x_slot);
			Label l3 = new Label();
			mv.visitLabel(l3);
			Label l4 = new Label();
			mv.visitJumpInsn(GOTO, l4);
			Label l5 = new Label();
			mv.visitLabel(l5);
			//mv.visitLineNumber(17, l5);
			mv.visitFrame(Opcodes.F_FULL, 5, new Object[] {"[Ljava/lang/String;", Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.TOP, Opcodes.INTEGER}, 0, new Object[] {});
			//initializing loop for y
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, y_slot);
			Label l6 = new Label();
			mv.visitLabel(l6);
			Label l7 = new Label();
			mv.visitJumpInsn(GOTO, l7);
			Label l8 = new Label();
			mv.visitLabel(l8);
			//mv.visitLineNumber(19, l8);
			mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);

			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);

			Label l9 = new Label();
			mv.visitLabel(l9);
			//mv.visitLineNumber(17, l9);
			//incrementing y
			mv.visitIincInsn(y_slot, 1);
			mv.visitLabel(l7);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			//checking if y is less than Y
			mv.visitVarInsn(ILOAD, y_slot);//5 yslot
			mv.visitVarInsn(ILOAD, Y_slot);
			mv.visitJumpInsn(IF_ICMPLT, l8);
			Label l10 = new Label();
			mv.visitLabel(l10);
			//mv.visitLineNumber(15, l10);
			//incrementing x
			mv.visitIincInsn(x_slot, 1);
			mv.visitLabel(l4);
			mv.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
			//checking if x is less than X
			mv.visitVarInsn(ILOAD, x_slot);//4 xslot
			mv.visitVarInsn(ILOAD, X_slot);
			mv.visitJumpInsn(IF_ICMPLT, l5);
			Label l11 = new Label();
			mv.visitLabel(l11);
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
		//if(lhs.index!=null)
			//lhs.index.visit(this, arg);
		if(lhs.getType() == Type.INTEGER)
		{
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "I");
		}
		if(lhs.getType() == Type.BOOLEAN)
		{
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "Z");
		}
		if(lhs.getType() == Type.IMAGE)
		{
			mv.visitFieldInsn(GETSTATIC, className, lhs.name, "Ljava/awt/image/BufferedImage;");
			mv.visitVarInsn(ILOAD, x_slot);
			mv.visitVarInsn(ILOAD, y_slot);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel",ImageSupport.setPixelSig, false);
		}
		//throw new UnsupportedOperationException();
		return null;
	}


	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeFrame", ImageSupport.makeFrameSig, false);
		mv.visitInsn(POP);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name, "Ljava/lang/String;");
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", ImageSupport.writeSig, false);//shufalse
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
		//throw new UnsupportedOperationException();
		mv.visitLdcInsn(expression_BooleanLit.value);
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		//TODO
		//throw new UnsupportedOperationException();
		if(expression_Ident.getType()==Type.BOOLEAN)
			mv.visitFieldInsn(GETSTATIC, className,expression_Ident.name, "Z");
		if(expression_Ident.getType()==Type.INTEGER)
			mv.visitFieldInsn(GETSTATIC, className,expression_Ident.name, "I");
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.getType());
		return null;
	}

}
