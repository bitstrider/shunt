package com.quantumcell.shunt;

public abstract class Function<T> {
	public int argCount;
	abstract public T eval(Token<T>[] args);	
}
