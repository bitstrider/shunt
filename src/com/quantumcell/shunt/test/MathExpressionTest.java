package com.quantumcell.shunt.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.quantumcell.shunt.expressions.MathExpression;

public class MathExpressionTest {
	static float precision = .001f;
	@Test
	public void functions() {
		assertEquals(MathExpression.eval(Float.class,"@random[1,abc]").floatValue(), 0f, precision);
	}
	
	@Test
	public void floats() {
		assertEquals(MathExpression.eval(Float.class,"1.23+3.2").floatValue(), 4.43f, precision);
		assertEquals(MathExpression.eval(Float.class,"(1.23+3.2)*-2").floatValue(), -8.86f, precision);		
		assertEquals(MathExpression.eval(Float.class,"10/3").floatValue(), 10.0f/3.0f, precision);				

	}
	@Test	
	public void bytes(){
		assertEquals(MathExpression.eval(Byte.class,"1+2-3*4").byteValue(),(byte) -9);
		assertEquals(MathExpression.eval(Byte.class,"2*4").byteValue(), (byte) 8);
		assertEquals(MathExpression.eval(Float.class,"5/3").byteValue(), (byte) 1);
	}

	@Test
	public void ints() {
		assertEquals(MathExpression.eval(Integer.class,"1+3").intValue(), 4);
		assertEquals(MathExpression.eval(Integer.class,"2*4").intValue(), 8);
		assertEquals(MathExpression.eval(Integer.class,"(1+3)*-2").intValue(), -8);		
		assertEquals(MathExpression.eval(Integer.class,"10/3").intValue(), 10/3);		
	}

}
