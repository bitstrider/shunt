package com.quantumcell.shunt;

abstract public class Operator<T>{
	public int precedence;
	abstract public T eval(T a, T b);
}