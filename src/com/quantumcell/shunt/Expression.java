package com.quantumcell.shunt;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

abstract public class Expression<T>{
	public String encoded;
	protected final Array<Token> RPN = new Array<Token>();
	@Deprecated protected Token<T> evaluated;
	
	private ObjectMap<String, T> variables;
	private static final Array<Token> _stack = new Array<Token>(); //temporary static resources
	private static final Array<Token> _infix = new Array<Token>(); //temporary static resources

	private static final Pattern _whitespaces = Pattern.compile("\\s");
	private static final Token _startClosureToken = new Token(){{
		encoded="{STARTCLOSURE}";
		type=TokenType.StartClosure;
	}};
	private static final Token _endClosureToken = new Token(){{
		encoded="{ENDCLOSURE}";
		type=TokenType.EndClosure;
	}};

	public void init(String s) {
		
		s = _whitespaces.matcher(s).replaceAll("");  //remove whitespaces
		
		this.encoded = this.sanitize(s);

//		log("Parsing sanitized expression to infix: "+this.encoded);
		
		clearTokenArray(_infix);
		tokenize(this.encoded, _infix);
		
//		Utils.printTokens(_infix,"Generated Infix","Parsing infix to RPN...");
		
		//if(this.evaluated != null){Token.free(this.evaluated);this.evaluated = null;}
		
		clearTokenArray(this.RPN); // in case the Expression object is being reused
		shuntingYard(_infix);
//		Utils.printTokens(this.RPN,"Generated RPN","Ready for eval()...");
	}
	
	private static void clearTokenArray(Array<Token> tokens) {
		tokens.clear();
	}

	protected abstract String sanitize(String encoded);

	protected abstract void tokenize(String expression, Array<Token> tokens);

	protected abstract T parseLiteral(String value);
	
	protected abstract T parseCustom(String encoded);

	public void assign(String name, T value){//variables
		if(variables==null) { variables = new ObjectMap<String,T>(); }
		variables.put(name,value);
	}
	
	private void shuntingYard(Array<Token> infix){	
			// prepare temporary static resources
			Array<Token> _operators = _stack;
			clearTokenArray(_operators);

			for(Token<T> token:infix) {
				switch(token.type) {
					case Literal: //number
						this.RPN.add(token);
						break;
					case Variable: //number
						this.RPN.add(token);
						break;	
					case Custom: //number
						this.RPN.add(token);
						break;					
					case Function: //number
						_operators.add(_startClosureToken);
						_operators.add(token);
						break;
					case StartArgs:
						_operators.add(_startClosureToken);
						break;
					case ArgsDelim:
						while(!(_operators.size == 0 || _operators.peek().type == TokenType.StartClosure)){
							this.RPN.add(_operators.pop());
						}
						_operators.pop();
						_operators.add(_startClosureToken);
						break;
					case EndArgs:
						while(!(_operators.size == 0 || _operators.peek().type == TokenType.StartClosure)){
							this.RPN.add(_operators.pop());
						}
						_operators.pop(); //last arg closure
						this.RPN.add(_operators.pop()); //this should be the function
						_operators.pop(); //function closure
						
						break;
					case Operator:
						int precedence = OPERATORS.get(token.encoded).precedence;
						if(_operators.size != 0) {
							//should stop if _operators is empty OR peek _operators is an actual operator and current operator has lte precedence the the peeked one)
							//continue == !stop;
							while( !(_operators.size == 0 || !_operators.peek().type.equals(TokenType.Operator) || precedence > OPERATORS.get(_operators.peek().encoded).precedence ) ) {
								this.RPN.add(_operators.pop());
							}
						}
	
						_operators.add(token);
						
						break;
					case StartClosure:
						_operators.add(token);
						break;
					case EndClosure:
						if(_operators.size != 0) {
							while(!(_operators.size == 0 || _operators.peek().type == TokenType.StartClosure)){
								this.RPN.add(_operators.pop());
							}
						}
						_operators.pop();
						break;
				}
	
	//			print("token:"+token.encoded+", type:"+token.type+", ");
	//			Utils.printTokens(_operators,"_operators");
	//			print(", ");
	//			Utils.printTokens(RPN,"_RPN");
	//			log("");
			}
	
			while(_operators.size > 0) {
				this.RPN.add(_operators.pop());
			}
		}

	public T eval(){
			
			//if(evaluated!=null) { // expression has not changed since last call to #eval
			//	return evaluated.value; // stored value from last call
			//}
			//log("Evaluating RPN... => "+encoded);
			Array<Token> _tokens = new Array<Token>();  //MUST USE A UNIQUE STACK TO AVOID CONFLICT WHEN eval() is nested in other eval calls!

//			Utils.printTokens(this.RPN,"Generated RPN","Ready for eval()...");
	
			for(Token<T> token:RPN) {
				
				switch(token.type){
				case Variable:
					token.value = variables.get(token.encoded);
					//token.encoded = token.value.toString(); 
					_tokens.add(token);
					break;
				case Custom:
					token.value = parseCustom(token.encoded); //when i get here, i end up reusing the static variable _tokens since eval is nested in there somewhere... 
	
					//token.type = TokenType.Literal;
					_tokens.add(token);				
					break;
				case Literal:
					token.value = parseLiteral(token.encoded); // good time to parse literals (before possibly passing them to Function objects, where a T argument will be required for Function#eval(T,T) 
					_tokens.add(token);
					break;
				case Operator:
					Operator<T> operator = OPERATORS.get(token.encoded);
					Token<T> a = (Token<T>) _tokens.pop();
					Token<T> b = (Token<T>) _tokens.pop();
					
					// recycles one of the Token objects to stay green.
					// but this is a bad idea, since it mutates RPN
					Token t = new Token();
					t.type = TokenType.Literal;
					t.encoded = "("+b.encoded + token.encoded + a.encoded+")"; // information about the popped tokens and operator saved in this field
					t.value = operator.eval(b.value,a.value); // a and b are reversed because they were popped, hence "Reverse" Polish notation i suppose
					
					_tokens.add(t);
					break;
				case Function: //similar to Operator routine, but with a variable number of parameters (Operators must have 2)
					Function<T> function = FUNCTIONS.get(token.encoded);
					Token<T>[] args = new Token[function.argCount];
					for(int i=args.length-1; i>=0; i--){ // reverse iterator to get the args in the right order, since we are popping!
						args[i] = (Token<T>) _tokens.pop();
					}
					
					Token tt = new Token();
					tt.type = TokenType.Literal;
					tt.encoded = token.encoded+Arrays.toString(args);
					tt.value = function.eval(args);
					
					_tokens.add(tt);
					
					
					break;
				}
//				Utils.printTokens(_tokens,"token:"+token.encoded+", _stack");
//				log("");
			}
			
//			Token<T> solution = _tokens.first();
//			evaluated = new Token();
//			evaluated.encoded = solution.encoded;
//			evaluated.value = solution.value;
//			evaluated.type = solution.type;
			evaluated = _tokens.first();
			
			//log("EVAL: " + encoded +" == "+evaluated.value);
			return evaluated.value;
		}

	protected static final ObjectMap<String, TokenType> TOKEN_TYPES = new ObjectMap<String, TokenType>();
	protected static final ObjectMap<String,Operator> OPERATORS = new ObjectMap<String,Operator>();
	protected static final ObjectMap<String,Function> FUNCTIONS = new ObjectMap<String,Function>();

	public static void defineTokenType(String key, TokenType type) {
		TOKEN_TYPES.put(key, type);
	}
	public static void defineFunction(String key,int argCount, Function function) {
		function.argCount = argCount;
		FUNCTIONS.put(key, function);
	}
	public static void defineOperator(String key,int precedence, Operator operator) {
		operator.precedence = precedence;
		OPERATORS.put(key, operator);
	}
}
