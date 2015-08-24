package com.quantumcell.shunt;

abstract public class Operator<T>{
	int precedence;
	abstract T eval(T a, T b);
}