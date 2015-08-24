package com.quantumcell.shunt;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.quantumcell.utils.Sugar;

public class MathExpression extends Expression<Number>{
	private static final ObjectMap<String,Operator<Number>> FUNCTIONS = new ObjectMap<String,Operator<Number>>(){{
		Operator<Number> add = new Operator<Number>(){
			@Override
			Number eval(Number a, Number b) {
				if(a instanceof Float){
					return a.floatValue() + b.floatValue();
				}else if(a instanceof Integer){
					return a.intValue() + b.intValue();	
				}else{
					return (byte) (a.byteValue()+b.byteValue());					
				}
			}
		};

		Operator<Number> sub = new Operator<Number>(){
			@Override
			Number eval(Number a, Number b) {
				if(a instanceof Float){
					return a.floatValue() - b.floatValue();
				}else if(a instanceof Integer){
					return a.intValue() - b.intValue();	
				}else{
					return (byte) (a.byteValue()-b.byteValue());					
				}
			}
		};
		
		Operator<Number> mul = new Operator<Number>(){
			@Override
			Number eval(Number a, Number b) {
				if(a instanceof Float){
					return a.floatValue() * b.floatValue();
				}else if(a instanceof Integer){
					return a.intValue() * b.intValue();	
				}else{
					return (byte) (a.byteValue()*b.byteValue());					
				}
			}
		};

		Operator<Number> div = new Operator<Number>(){
			@Override
			Number eval(Number a, Number b) {
				if(a instanceof Float){
					return a.floatValue() / b.floatValue();
				}else if(a instanceof Integer){
					return a.intValue() / b.intValue();	
				}else{
					return (byte) (a.byteValue()/b.byteValue());					
				}		
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
	
	protected static Pattern _leadingMinus = Pattern.compile("\\-([0-9.]+)");
	protected static Pattern _enclosedLeadingMinus = Pattern.compile("\\(\\-([0-9.]+)");
	protected static Pattern _enclosedLeadingPlus = Pattern.compile("\\(\\+");
	protected static Pattern _signAfterOperator = Pattern.compile("([/*])([+-])([0-9.]+)");
	
	protected static Pattern _elementNonCapture = Pattern.compile("(?<=[\\+\\-\\*\\/()])|(?=[\\+\\-\\*\\/()])"); // empty spaces before and after operators
	protected static Pattern _functionArgsCapture= Pattern.compile("[\\[\\,]([.0-9a-zA-Z]+)");
	protected static Pattern _functionArgsNonCapture = Pattern.compile("\\@[a-zA-Z]+\\[|\\,|\\]");
	protected static Pattern _functionArgs= Pattern.compile("\\,");
	
	public <V extends Number> void init(Class<V> literalClass, String s){ // hunt down the edge cases
		
		this.literalClass = literalClass;

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
		init(s);
	}
	
	@Override
	public ObjectMap<String, Operator<Number>> getOperatorMap() {		
		return FUNCTIONS;
	}

	@Override
	protected Number parseLiteral(String value) {
		if(literalClass.equals(Float.class)){
			return Float.parseFloat(value);
		}else if(literalClass.equals(Integer.class)){
			return Integer.parseInt(value);
		}else{
			return Byte.parseByte(value);
		}
		// the object returned here will eventually be passed as an arg to an operator, where it will be upcast back to the literalClass
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
		if(c=='@'){
			t.type = "function";
			String name = encoded.substring(0,encoded.indexOf('['));
			String args = encoded.substring(encoded.indexOf('[')+1, encoded.length()-1);
			//getFunctionMap().put(name,_functionArgs.split(args));

			//Sugar.log(Arrays.toString());
		}else if(Character.isDigit(c)||c=='.') {
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
	
	
	private Class literalClass;
	public static <V extends Number> MathExpression create(Class<V> literalClass, String s){
		MathExpression b = new MathExpression();
		b.init(literalClass,s);
		return b;
	}
	
	private static final MathExpression _expression = new MathExpression();
	public static <V extends Number> V eval(Class<V> literalClass,String s){
		_expression.init(literalClass,s);
		return (V) _expression.eval();
	}	
}
