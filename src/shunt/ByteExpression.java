package com.quantumcell.shunt;

import java.util.regex.Pattern;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.quantumcell.utils.Sugar;

public class ByteExpression extends Expression<Byte>{
	
	private static final ObjectMap<String,Operator<Byte>> FUNCTIONS = new ObjectMap<String,Operator<Byte>>(){{
		Operator<Byte> add = new Operator<Byte>(){
			@Override
			Byte eval(Byte a, Byte b) {
				return (byte) (a + b);
			}
		};

		Operator<Byte> sub = new Operator<Byte>(){
			@Override
			Byte eval(Byte a, Byte b) {
				return (byte) (a - b);
			}
		};
		
		Operator<Byte> mul = new Operator<Byte>(){
			@Override
			Byte eval(Byte a, Byte b) {
				return (byte) (a * b);
			}
		};

		Operator<Byte> div = new Operator<Byte>(){
			@Override
			Byte eval(Byte a, Byte b) {
				return (byte) (a / b);
			}
		};
		
		
		add.precedence = 10;
		sub.precedence = 10;
		mul.precedence = 20;
		div.precedence = 20;

		put("+", add);
		put("-", sub);
		put("*", mul);
		put("/", div);
		
	}};
	
	protected static Pattern _leadingMinus = Pattern.compile("\\-([0-9]+)");
	protected static Pattern _enclosedLeadingMinus = Pattern.compile("\\(\\-([0-9]+)");
	protected static Pattern _enclosedLeadingPlus = Pattern.compile("\\(\\+");
	protected static Pattern _signAfterOperator = Pattern.compile("([/*])([+-])([0-9]+)");
	
	protected static Pattern _elementNonCapture = Pattern.compile("((?<=[\\+\\-\\*\\/()])|(?=[\\+\\-\\*\\/()]))");
	
	@Override 
	public void init(String s){ // hunt down the edge cases
		switch(s.charAt(0)){
		case '+': // redundant + at the beginning of the expression
			s = s.substring(1);
			break;
		case '-':
			s = _leadingMinus.matcher(s).replaceFirst("(0-$1)"); // s.replaceFirst("-([0-9]+)", "(0-$1)");
			break;
		}
		s = _signAfterOperator.matcher(s).replaceAll("$1($2$3)"); // converts /-1 to /(-1), same with +;
		s = _enclosedLeadingPlus.matcher(s).replaceAll("("); // converts (+1) to (1)
		s = _enclosedLeadingMinus.matcher(s).replaceAll("((0-$1)"); // converts (-1) to (0-1);
		//s = s.replaceAll("", "(0-");
		Sugar.log("(sanitized)"+s);
		super.init(s);
	}
	
	@Override
	public ObjectMap<String, Operator<Byte>> getOperatorMap() {		
		return FUNCTIONS;
	}

	@Override
	protected Byte parseLiteral(String value) {
		return Byte.parseByte(value);
	}
	
	//http://stackoverflow.com/questions/9856916/java-string-split-regex
	private static String elementNonCapture = "((?<=[\\+\\-\\*\\/()])|(?=[\\+\\-\\*\\/()]))"; // includes enclosure (), though technically not operators
	
	@Override
	public void tokenize(String encoded, Array<Token> _infix) {
		String[] elements = _elementNonCapture.split(encoded);
		for(int i=0; i<elements.length; i++) {		
			_infix.add(createToken(elements[i]));
		}	
	}

	private static Token createToken(String encoded) {
		Token t = new Token();
		t.encoded = encoded; // expressions always end with an operand
		
		char c = encoded.charAt(0);
		if(Character.isDigit(c)) {
			t.type = "literal"; // if the first character is a digit, its an operand, otherwise an operator
		}else if(encoded.equals("(")){
			t.type="lparen";
		}else if(encoded.equals(")")){
			t.type="rparen";
		}else{ //if(getOperatorMap().containsKey(encoded)){
			t.type="operator";
		}
		return t;
	}
	
	public static ByteExpression create(String s){
		ByteExpression b = new ByteExpression();
		b.init(s);
		return b;
	}
	
	public static Byte eval(String s){
		return create(s).eval();
	}
}
