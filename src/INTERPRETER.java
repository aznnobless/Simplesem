// INTERPRETER.java

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;	//	extends BufferedReader
import java.io.PrintStream;			// 	extends FilterOutputStream
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class INTERPRETER {

	static String studentName = "Byung Ho Lee";
	static String studentID = "60626811";
	static String uciNetID = "byunghl";

	class SyntaxException extends Exception {

		private static final long serialVersionUID = 1L;

		public SyntaxException() {

		}

	}

	public static void main(String[] args) {
		INTERPRETER i = new INTERPRETER(args[0]);	// Create an instance of an INTERPRETER
		i.runProgram();		// CALL runProgram() method
	}

	private LineNumberReader codeIn;
	private LineNumberReader inputIn;
	private FileOutputStream outFile;
	private PrintStream outStream;

	private static final int DATA_SEG_SIZE = 100;
	private ArrayList<String> C;	// Memory that store the instruction
	private int[] D;				// Memory that store data values for the use by program.
	private int PC;					// Program Counter that points to the current or next instruction
	private String IR;				// Instruction Register that contains an instruction for the current cycle
	private boolean run_bit;		// a bit that can be turned off to halt the machine

	private int curIRIndex = 0;

	// Constructor
	public INTERPRETER(String sourceFile) {

		// Read the DATA in input.txt file
		try {
			inputIn = new LineNumberReader(new FileReader("input.txt"));
			inputIn.setLineNumber(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Init: Errors accessing input.txt");
			System.exit(-2);
		}

		// For the OUTPUT
		try {
			outFile = new FileOutputStream(sourceFile + ".out");
			outStream = new PrintStream(outFile);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Init: Errors accessing " + sourceFile + ".out");
			System.exit(-2);
		}

		// Initialize the SIMPLESEM processor state
		try {
			// Initialize the Code segment
			C = new ArrayList<String>();
			codeIn = new LineNumberReader(new FileReader(sourceFile)); // Read the DATA in sourceFile.
			codeIn.setLineNumber(1);
			while (codeIn.ready()) { 
				// if a buffered character stream is ready (if buffer is not empty)
				C.add(codeIn.readLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Init: Errors accessing source file " + sourceFile);
			System.exit(-2);
		}

		// Initialize the Data segment
		D = new int[DATA_SEG_SIZE];
		for (int i=0; i<DATA_SEG_SIZE ;i++) {
			D[i]=0;
		}
		PC = 0; // Every SIMPLESEM program begins at instruction 0
		IR = null;
		run_bit = true; // Enable the processor
	}

	// Step1: 
	public void runProgram() {

		int counter = 1;
		// FETCH-INCREMENT-EXECUTE CYCLE
		while(run_bit) {
			////System.out.println("LOOP:"+ counter);
			fetch();
			incrementPC();
			execute();
			counter++;
		} 

		printDataSeg();
	}

	private void printDataSeg() {
		outStream.println("Data Segment Contents");
		for(int i=0; i < DATA_SEG_SIZE; i++){
			outStream.println(i+": "+D[i]);
		}
	}

	// fetch(): The IR is updated with the instruction pointed at by PC; IR=C[PC]
	private void fetch() {
		IR = C.get(PC);
		curIRIndex = 0;
		// Debug
		////System.out.println("@fetch(): " + IR);
	}

	private void incrementPC() {
		PC += 1;
		//System.out.println("@incrementPC(): " + PC);
	}

	private void execute() {
		// TODO
		try {
			//System.out.println("@execute():" + IR);
			parseStatement(IR);
			
		} catch (SyntaxException e) {
			// TODO Auto-generated catch block
			System.err.println("ERROR @execute() :");
			e.printStackTrace();
		}
	}

	//Output: used in the case of: set write, source
	private void write(int source){
		outStream.println(source);
	}

	//Input: used in the case of: set destination, read
	private int read() {
		int value=Integer.MIN_VALUE;
		try{value = new Integer((inputIn.readLine())).intValue();}catch(IOException e){e.printStackTrace();}
		return value;
	}

	/**
	 * Checks and returns if the character c is found at the current
	 * position in IR. If c is found, advance to the next
	 * (non-whitespace) character.
	 */
	private boolean accept(char c) {
		if (curIRIndex >= IR.length())
			return false;

		if (IR.charAt(curIRIndex) == c) {
			curIRIndex++;
			skipWhitespace();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks and returns if the string s is found at the current
	 * position in IR. If s is found, advance to the next
	 * (non-whitespace) character.
	 */
	private boolean accept(String s) {
		//System.out.println("@accept: " +  curIRIndex);
		if (curIRIndex >= IR.length()) // if IR.length equal to 0 or less then 0
			return false;

		if (curIRIndex+s.length() <= IR.length() && s.equals(IR.substring(curIRIndex, curIRIndex+s.length()))) {
			//System.out.println("@accept(string s) :" + s);
			//System.out.println("@accpet; IR:" + IR);
			curIRIndex += s.length();
			skipWhitespace();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if the character c is found at the current position in
	 * IR. Throws a syntax error if c is not found at the current
	 * position.
	 */
	private void expect(char c) throws SyntaxException {
		if (!accept(c))
			throw new SyntaxException();
	}

	/**
	 * Checks if the string s is found at the current position in
	 * IR. Throws a syntax error if s is not found at the current
	 * position.
	 */
	private void expect(String s) throws SyntaxException {
		if (!accept(s))
			throw new SyntaxException();
	}

	private void skipWhitespace() {
		while (curIRIndex < IR.length() && Character.isWhitespace(IR.charAt(curIRIndex)))
			curIRIndex++;
	}

	// Parse start from here
	private void parseStatement(String statement) throws SyntaxException {
		System.err.println("Statement");
		//System.out.println("@parseStatement:" + statement);
		
		if (accept("halt"))
		{
			//System.out.println("@halt");
			run_bit = false;
			return;
		}
		else if(accept("set")) 
		{
			//System.out.println("@set method start");
			//System.out.println(statement);
			parseSet(statement);
		}	
		else if(accept("jumpt"))
		{
			//System.out.println("@jumpt method start");
			parseJumpt(statement);
		}
		else if(accept("jump"))
		{
			//System.out.println("@jump method start");
			parseJump(statement);
		}
	}

	/**
	 * Note: 
	 * 	Format: 	set destination*, source**
	 * 	Semantic:	D[destination] = source
	 * 
	 */

	private void parseSet(String statement) throws SyntaxException {
		System.err.println("Set");

		Matcher m = Pattern.compile("set\\s([^,]*),\\s(.*)").matcher(IR);
		boolean isFound = m.find();
		//System.out.println("@parseSet() : " + isFound);

		String expression1 = m.group(1);
		String expression2 = m.group(2);

		//System.out.println("### expr1 =" + expression1);
		//System.out.println("### expr2 =" + expression2);

		boolean isWrite = false;

		// Check expression 1 
		if(!Pattern.matches("write.*", expression1)) // if expression1 does not contain write
		{
			//System.out.println("@@@expression1" + expression1);
			expression1 = parseExpr(expression1);
		} 
		else 
			isWrite = true;

		//System.out.println("-------");
		
		// Check expression 2
		if(!Pattern.matches("read.*", expression2)) {
			//System.out.println("Call parseExpr from parseSet");
			//System.out.println("expression2 :" + expression2);
			expression2 = parseExpr(expression2);

			if(isWrite == false) {
				D[Integer.parseInt(expression1)] = Integer.parseInt(expression2);
			}
		}
		else 
		{
			//System.out.println("D-point1 ; expression2 read processing");
			D[Integer.parseInt(expression1)] = read();
		}

		// If first expression was write, isWrite flag is true
		if(isWrite == true) {
			//System.out.println("D-point1 ; isWriteTrue");
			write(Integer.parseInt(expression2));
		}

	}

	// Parsing expression like ( 9 + 5 )
	private String parseExpr(String expression) throws SyntaxException {
		System.err.println("Expr");

		//System.out.println("EXPRESSION = " + expression);

		Matcher m;

		String tempStr = "";
		int i;
		for (i = 0 ; i < expression.length(); i++) {

			if(expression.charAt(i) == '(') 
			{
				tempStr += expression.charAt(i);
				int j = 1;	// to count open parenthesis
				while( j != 0) 
				{
					i++;
					// if open parenthesis is found increase counter
					if(expression.charAt(i) == '(') 
					{ 
						j++;
					}
					// if close parenthesis is found decrease counter
					else if (expression.charAt(i) == ')' ) 
					{
						j--;
					}

					tempStr += expression.charAt(i);
				}
			}

			if(expression.charAt(i) == '+' || expression.charAt(i) == '-')
				break;
			else if(expression.charAt(i) != ')')
				tempStr += expression.charAt(i);

			//System.out.println("!!!" + tempStr);
		}// End for
		
		//System.out.println("!!!!" + i);
		//System.out.println("!!!!" + expression.length());
		
		if( i != expression.length() )
			expression = expression.substring(i, expression.length());
		else
			expression = "";

		//System.out.println("@parseExpr(): Before :" + tempStr);

		tempStr = parseTerm(tempStr);

		//System.out.println("@parseExpr(): After :" + tempStr);

		int returnValue = Integer.parseInt(tempStr);
		char temp = ' ';
		if(!expression.equals("")) {
			while(Pattern.matches("[+-].*", expression)) {
				if(expression.charAt(0) == '+') {
					temp = '+';
				} 
				else if(expression.charAt(0) == '-') {
					temp = '-';
				}
				//System.out.println("####"+expression);
				(m = Pattern.compile("[+-]\\s*(.*)").matcher(expression)).find();
				//System.out.println("~~~" + m.group(1));
				expression = parseTerm(m.group(1));
				
				if(temp == '+')
					returnValue += Integer.parseInt(expression);
				else if(temp == '-')
					returnValue -= Integer.parseInt(expression);
			}
		}

		expression = "" + returnValue;

		return expression;
	}
	// Parse terms like x and y in expression (x + y)
	private String parseTerm(String expression) throws SyntaxException {
		System.err.println("Term");
		//System.out.println("@Term:expression" + expression); //OK
		Matcher m;

		String tempStr = "";
		int i;
		for(i = 0; i < expression.length(); i++) 
		{
			if(expression.charAt(i) == '(') 
			{
				tempStr += expression.charAt(i);
				int j = 1;
				while(j != 0 ) 
				{
					i++;
					if(expression.charAt(i) == '(')
					{
						j++;
					}
					else if(expression.charAt(i) == ')'){
						j--;						
					}

					tempStr += expression.charAt(i);
				} // end of while
			} // end of if

			if(expression.charAt(i) == '*' || expression.charAt(i) == '/' || expression.charAt(i) == '%')
				break;
			else if(expression.charAt(i) != ')')
				tempStr += expression.charAt(i);

			//System.out.println("@Term:tmpStr: " + tempStr ); // OK
		}

		//System.out.println("!!!!" + i);
		//System.out.println("!!!!" + expression.length());
		//get the second part
		if(i != expression.length())
		{
			expression = expression.substring(i, expression.length());
		}
		else{
			expression = "";
		}	

		//System.out.println("expression = " + expression);

		//System.out.println("@Term() : before = " + tempStr);
		//parse the first part
		tempStr = parseFactor(tempStr);

		//System.out.println("@Term() : after = " + tempStr);

		int returnValue = 0;
		returnValue = Integer.parseInt(tempStr);		
		
		char temp = ' '; // for getting *, / or %
		//parsing the rest after first
		if(!expression.equals(""))
		{
			while(Pattern.matches("[\\*\\/\\%].*", expression))
			{
				if(expression.charAt(0) == '*')
					temp = '*';
				else if(expression.charAt(0) == '/')
					temp = '/';
				else if(expression.charAt(0) == '%')
					temp = '%';

				(m = Pattern.compile("[\\*\\/\\%]\\s*(.*)").matcher(expression)).find();
				//System.out.println("**"+expression);
				expression = parseFactor(m.group(1));

				//System.out.println("****"+expression);
				if(temp == '*')
					returnValue *= Integer.parseInt(expression);
				else if(temp == '/')
					returnValue /= Integer.parseInt(expression);
				else if(temp == '%')
					returnValue %= Integer.parseInt(expression);			
			}
		}


		return "" + returnValue;
	}

	private String parseFactor(String expression) throws SyntaxException {
		System.err.println("Factor");
		//System.out.println("@Factor:expression" + expression ); // OK
		Matcher m;
		int returnValue;

		if(Pattern.matches("D\\[.*", expression) )
		{
			//System.out.println("@parseFACTOR point 1");
			//System.out.println("expression===="+expression);
			(m = Pattern.compile("D\\[(.*)]").matcher(expression)).find();
			expression = parseExpr(m.group(1));
			returnValue = D[Integer.parseInt(expression)];
			expression = "" + returnValue;
		} 
		else if(Pattern.matches("\\(.*", expression)){
			//System.out.println("@parseFACTOR point 2");
			(m = Pattern.compile("\\((.*)").matcher(expression)).find();
			expression = parseExpr(m.group(1));			
			expression = "" + Integer.parseInt(expression);

		}
		// parsing to number
		else if(Pattern.matches("\\d*.*", expression)){
			//System.out.println("@parseFACTOR point 3");
			//System.out.println("####"+expression);
			//expression = expression.replaceAll("\\s", "");
			//System.out.println("####"+expression);
			(m = Pattern.compile("(\\d*)(.*)").matcher(expression)).find();
			//System.out.println("HERE");
			expression = m.group(1);		
			////System.out.println("FINAL" + expression);
			//expr = m.group(2);		
		}
		
		//System.out.println("RIGHT BEFORE RETURN expression=" + expression);
		return expression;
	}

	@SuppressWarnings("unused")
	private void parseNumber() throws SyntaxException {
		System.err.println("Number");
		if (curIRIndex >= IR.length())
			throw new SyntaxException();

		if (IR.charAt(curIRIndex) == '0') {
			curIRIndex++;
			skipWhitespace();
			return;
		} else if (Character.isDigit(IR.charAt(curIRIndex))) {
			while (curIRIndex < IR.length() &&
					Character.isDigit(IR.charAt(curIRIndex))) {
				curIRIndex++;
			}
			skipWhitespace();
		} else {
			throw new SyntaxException();
		}
	}

	private void parseJump(String statement) throws SyntaxException {
		System.err.println("Jump");
		String expression;
		Matcher m = Pattern.compile("jump\\s*(.*)").matcher(statement);
		m.find();
		expression = parseExpr(m.group(1));
		PC = Integer.parseInt(expression);
		//		parseExpr();
	}

	//<Jumpt>-> jumpt <Expr>, <Expr> ( != | == | > | < | >= | <= ) <Expr>
	private void parseJumpt(String statement) throws SyntaxException {
		System.err.println("Jumpt");
		//System.out.println("@Jumpt()" + statement);
		String expr1;
		String expr2;
		String expr3;

		Matcher m;
		(m = Pattern.compile("jumpt\\s([^,]*),\\s(.*)").matcher(statement)).find();
		expr1 = m.group(1); 
		//System.out.println("@Jumpt() expression1 =" + expr1);
		expr2 = m.group(2); 
		//System.out.println("@Jumpt() expression2 =" + expr2);
		
		expr1 = parseExpr(expr1); ///TODO
		//System.out.println("@Jumpt() expression1 #2 =" + expr1);

		//get the boolean expression
		String temp = "";
		if(expr2.contains("=="))		
			temp = "==";		
		else if(expr2.contains("!="))
			temp = "!=";		
		else if(expr2.contains("<="))
			temp = "<=";
		else if(expr2.contains(">="))
			temp = ">=";
		else if(expr2.contains(">"))
			temp = ">";
		else if(expr2.contains("<"))
			temp = "<";

		//System.out.println("@Jumpt() temp:"+ temp);

		(m = Pattern.compile("([^=!><]*)\\s([=!><]*)\\s(.*)").matcher(expr2)).find();
		expr2 = m.group(1);		
		//System.out.println("@Jumpt() expression2 @@ =" + expr2);
		expr3 = m.group(3);
		//System.out.println("@Jumpt() expression3 @@ =" + expr3); // WORK SO FAR

		expr2 = parseExpr(expr2);
		
		expr3 = parseExpr(expr3);
		

		int temp2 = Integer.parseInt(expr2);
		int temp3 = Integer.parseInt(expr3);

		//cases for each boolean expression
		boolean condition = false;;
		if(temp.equals("=="))
		{

			if(temp2 == temp3)
			{
				condition = true;

			}				
			else
				condition = false;
		}
		else if(temp.equals("!="))
		{
			if(temp2 != temp3)
				condition = true;
			else
				condition = false;
		}
		else if(temp.equals(">="))
		{
			if(temp2 >= temp3)
				condition = true;
			else
				condition = false;
		}
		else if(temp.equals("<="))
		{
			if(temp2 <= temp3)
				condition = true;
			else
				condition = false;
		}
		else if(temp.equals(">"))
		{
			if(temp2 > temp3)
				condition = true;
			else
				condition = false;
		}
		else if(temp.equals("<"))
		{
			if(temp2 < temp3)
				condition = true;
			else
				condition = false;
		}		

		//change the PC if condition is true
		if(condition)
		{
			PC = Integer.parseInt(expr1);
		}

	}
}
