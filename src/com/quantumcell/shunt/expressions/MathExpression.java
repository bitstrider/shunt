package com.quantumcell.shunt.expressions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.utils.Array;
import com.quantumcell.shunt.Expression;
import com.quantumcell.shunt.Function;
import com.quantumcell.shunt.Operator;
import com.quantumcell.shunt.Token;
import com.quantumcell.shunt.TokenType;
import com.quantumcell.shunt.Utils;

public class MathExpression extends Expression<Number>{
	static {{
		
		defineTokenType("(", TokenType.StartClosure);
		defineTokenType(")", TokenType.EndClosure);
		defineTokenType("[", TokenType.StartArgs);
		defineTokenType(",", TokenType.ArgsDelim);
		defineTokenType("]", TokenType.EndArgs);
		
		defineOperator("+",10, new Operator<Number>(){
			@Override
			public Number eval(Number a, Number b) {
				if(a instanceof Float){
					return a.floatValue() + b.floatValue();
				}else if(a instanceof Integer){
					return a.intValue() + b.intValue();	
				}else{
					return (byte) (a.byteValue()+b.byteValue());					
				}
			}
		});
		defineOperator("-",10, new Operator<Number>(){
			@Override
			public Number eval(Number a, Number b) {
				if(a instanceof Float){
					return a.floatValue() - b.floatValue();
				}else if(a instanceof Integer){
					return a.intValue() - b.intValue();	
				}else{
					return (byte) (a.byteValue()-b.byteValue());					
				}
			}
		});
		
		defineOperator("*",20, new Operator<Number>(){
			@Override
			public Number eval(Number a, Number b) {
				if(a instanceof Float){
					return a.floatValue() * b.floatValue();
				}else if(a instanceof Integer){
					return a.intValue() * b.intValue();	
				}else{
					return (byte) (a.byteValue()*b.byteValue());					
				}
			}
		});

		defineOperator("/",20, new Operator<Number>(){
			@Override
			public Number eval(Number a, Number b) {
				if(a instanceof Float){
					return a.floatValue() / b.floatValue();
				}else if(a instanceof Integer){
					return a.intValue() / b.intValue();	
				}else{
					return (byte) (a.byteValue()/b.byteValue());					
				}		
			}
		});
		
		
		defineOperator("#",100, new Operator<Number>(){
			@Override
			public Number eval(Number a, Number b) {
				if(a instanceof Float){
					return a.floatValue() - b.floatValue();
				}else if(a instanceof Integer){
					return a.intValue() - b.intValue();	
				}else{
					return (byte) (a.byteValue()-b.byteValue());					
				}
			}
		});
		
		
		defineFunction("@sum",2, new Function<Number>(){
			@Override
			public Number eval(Token<Number>[] args){
				Number a = args[0].value;
				Number b = args[1].value;
				if(a instanceof Float){
					return a.floatValue() + b.floatValue();
				}else if(a instanceof Integer){
					return a.intValue() + b.intValue();	
				}else{
					return (byte) (a.byteValue()+b.byteValue());					
				}
			}
		});
	}};

	public <V extends Number> void init(Class<V> literalClass, String s){ // hunt down the edge cases
		this.literalClass = literalClass;
		this.init(s);
	}


	protected final static Pattern _unaryMinus = Pattern.compile("(^\\-|(?<=[\\=\\,\\[\\(\\*\\/])[\\-])");
	protected final static Pattern _unaryPlus = Pattern.compile("(^\\+|(?<=[\\=\\,\\[\\(\\*\\/])[\\+])");
	
	@Override
	public String sanitize(String s) {
		s = _unaryPlus.matcher(s).replaceAll(""); //does everything the old routine used to do!
		s = _unaryMinus.matcher(s).replaceAll("0#");		

		//Sugar.log("( sanitized ) => "+s);		
		return s;
	}
	
	@Override
	protected Number parseLiteral(String value) {
		if(literalClass.equals(Byte.class)){
			return Byte.parseByte(value);
		}else if(literalClass.equals(Integer.class)){
			return Integer.parseInt(value);
		}else{
			return Float.parseFloat(value);
		}
		// the object returned here will eventually be passed as an arg to an operator, where it will be upcast back to the literalClass
	}
	
	@Override
	protected Number parseCustom(String encoded) {
		return null; //TODO figure out a better solution
	}


	//http://stackoverflow.com/questions/9856916/java-string-split-regex
	//private static String elementNonCapture = "((?<=[#\\+\\-\\*\\/()])|(?=[#\\+\\-\\*\\/()]))"; // includes enclosure (), though technically not operators
	
	private static Pattern elementCapture = Pattern.compile("((?:[$@a-z0-9.]+)|(?:\\<\\=)|(?:[\\+\\-\\/\\*\\#\\(\\)\\,\\[\\]]))");
	
	@Override
	public void tokenize(String encoded, Array<Token> _infix) {
		
		Matcher m = elementCapture.matcher(encoded);
		while(m.find()){
			Token t = createToken(m.group(0));
			_infix.add(t);
		}
	}

	protected static Token createToken(String encoded) {
		Token t = new Token();
		
		t.encoded = encoded; // expressions always end with an operand
		
		char c = encoded.charAt(0);
		if(c=='@'){
			t.type = TokenType.Function;
		}else if(c=='$'){
			t.type = TokenType.Custom;
		}else if(Character.isLetter(c)){
			t.type = TokenType.Variable;
		}else if(Character.isDigit(c)||c=='.') {		
			t.type = TokenType.Literal; // if the first character is a digit, its an operand, otherwise an operator
		}else if(OPERATORS.containsKey(encoded)){
			t.type = TokenType.Operator;
		}else{
			t.type = TOKEN_TYPES.get(encoded, TokenType.Unknown); 
		}

		//Sugar.log("fresh new (non-pooled) token! "+t.encoded+" for " + t.encoded +"#"+t.type);
		
		return t;
	}
	
	private Class literalClass;
	public static <V extends Number> MathExpression create(Class<V> literalClass, String s){
		MathExpression b = new MathExpression();
		b.init(literalClass,s);
		return b;
	}
	
	protected static final MathExpression _expression = new MathExpression();
	public static <V extends Number> V eval(Class<V> literalClass,String s){
		_expression.init(literalClass,s);
		return (V) _expression.eval();
	}	
}
