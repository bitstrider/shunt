package com.quantumcell.shunt;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.quantumcell.utils.Sugar;

abstract public class Expression<T>{
	private String encoded;
	private final Array<Token<T>> RPN = new Array<Token<T>>();
	public Token<T> evaluated;
	private static final Array<Token> _stack = new Array<Token>(); //temporary static resources
	private static final Array<Token> _infix = new Array<Token>(); //temporary static resources

	
	public void init(String s) {
		
		this.encoded = s.replaceAll("\\s", "");  //remove whitespaces
		//Sugar.log("Parsing sanitized expression to infix: "+this.expression);
		
		_infix.clear();
		tokenize(this.encoded, _infix);
		//Utils.printTokens(_infix,"Generated Infix","Parsing infix to RPN...");

		this.evaluated = null;
		this.RPN.clear(); // in case the Expression object is being reused
		shuntingYard(_infix);
		//Utils.printTokens(this.RPN,"Generated RPN","Ready for eval()...");
	}

	public abstract ObjectMap<String,Operator<T>> getOperatorMap();
	protected abstract void tokenize(String expression, Array<Token> tokens);
	protected abstract T parseLiteral(String value);

	private void shuntingYard(Array<Token> infix){	
		
		// prepare temporary static resources
		Array<Token> _operators = _stack;
		_operators.clear();
		
		for(Token<T> token:infix) {
			switch(token.type) {
				case "literal": //number
					this.RPN.add(token);
					break;
				case "operator":
					int precedence = getOperatorMap().get(token.encoded).precedence;
					if(_operators.size != 0) {
						//should stop if _operators is empty OR peek _operators is an actual operator and current operator has lte precedence the the peeked one)
						//continue == !stop;
						while( !(_operators.size == 0 || !_operators.peek().type.equals("operator") || precedence > getOperatorMap().get(_operators.peek().encoded).precedence ) ) {
							this.RPN.add(_operators.pop());
						}
					}

					_operators.add(token);
					
					break;
				case "lparen":
					_operators.add(token);
					break;
				case "rparen":
					if(_operators.size != 0) {
						while(!(_operators.size == 0 || _operators.peek().type == "lparen")){
							this.RPN.add(_operators.pop());
						}
					}
					_operators.pop();
					break;
			}

			//Sugar.print("token:"+token.encoded+", type:"+token.type+", ");
			//Utils.printTokens(_operators,"_operators");
			//Sugar.print(", ");
			//Utils.printTokens(RPN,"_RPN");
			//Sugar.log("");
		}

		while(_operators.size > 0) {
			this.RPN.add(_operators.pop());
		}
	}
	
	public T eval(){
		
		if(evaluated!=null) { // expression has not changed since last call to #eval
			return evaluated.value; // stored value from last call
		}
		//Sugar.log("Evaluating RPN...");
		
		Array<Token> _tokens = _stack;
		_tokens.clear();
		
		for(Token<T> token:RPN) {
			switch(token.type){
			case "literal":
				token.value = parseLiteral(token.encoded); // good time to parse literals (before possibly passing them to Function objects, where a T argument will be required for Function#eval(T,T) 
				_tokens.add(token);
				break;
			case "operator":
				Operator<T> operator = getOperatorMap().get(token.encoded);
				Token<T> a = (Token<T>) _tokens.pop();
				Token<T> b = (Token<T>) _tokens.pop();
				
				// recycles one of the Token objects to stay green.
				
				a.type = "literal";
				a.encoded = "("+a.encoded + token.encoded + b.encoded+")"; // information about the popped tokens and operator saved in this field
				a.value = operator.eval(b.value,a.value); // a and b are reversed because they were popped, hence "Reverse" Polish notation i suppose
				//a.encoded = a.value.toString();
				
				_tokens.add(a);
				break;			
			}
			//Utils.printTokens(_tokens,"token:"+token.encoded+", _stack");
			//Sugar.log("");
		}
		evaluated = _tokens.first();
		//Sugar.log("*Optimized infix notation: " + evaluated.encoded);		
		return evaluated.value;
	}
}
