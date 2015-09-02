package com.quantumcell.shunt;

public class Token<T>{
	public String encoded;
	//String type;
	public TokenType type;
	public T value;
//	
	@Override
	public String toString(){
		//Sugar.log(encoded);
		return encoded;
	}
}
