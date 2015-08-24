package com.quantumcell.shunt.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.quantumcell.shunt.ByteExpression;

public class ByteExpressionTest {

	@Test
	public void simple() {
		assertEquals((byte) ByteExpression.eval("9+24/8-4*2"), (byte) 4 );
	}	

	@Test
	public void precdence() {
		assertEquals((byte) ByteExpression.eval("1+2-8"), (byte) -5 );
		assertEquals((byte) ByteExpression.eval("1-2+8"), (byte) 7 );
		assertEquals((byte) ByteExpression.eval("4/2*8"), (byte) 16 );
		assertEquals((byte) ByteExpression.eval("4*2/8"), (byte) 1 );
		assertEquals((byte) ByteExpression.eval("9+24/4-3*2"), (byte) 9 );
	}
	
	@Test
	public void lastValueCache() {
		ByteExpression b = ByteExpression.create("1+2-8");
		b.eval();
		assertEquals((byte) b.eval(), (byte) -5 );
	}
	
		

	
	@Test
	public void enclosure() {
		assertEquals((byte) ByteExpression.eval("9+24/(8-4)"), (byte) 15 );
		assertEquals((byte) ByteExpression.eval("9+24/(8-4)*2"), (byte) 21 );
		assertEquals((byte) ByteExpression.eval("(1+((2-1)*5)/3"), (byte) 2 );
		assertEquals((byte) ByteExpression.eval("(1)+(2)"), (byte) 3 );
		assertEquals((byte) ByteExpression.eval("((1))"), (byte) 1 ); //redundancy
	}	
	
	
	@Test
	public void leadingSign(){
		assertEquals((byte) ByteExpression.eval("-9+24/8-4*2"), (byte) -14 );
		assertEquals((byte) ByteExpression.eval("+9+24/8-4*2"), (byte) 4 );
	}
	
	@Test
	public void embeddedSign(){
		assertEquals((byte) ByteExpression.eval("-9+24/8-4*2"), (byte) -14 );
		assertEquals((byte) ByteExpression.eval("-9+24/-8-4*2"), (byte) -20 );
	}
}
