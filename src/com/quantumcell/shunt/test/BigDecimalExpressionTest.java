package com.quantumcell.shunt.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.quantumcell.shunt.expressions.BigDecimalExpression;

public class BigDecimalExpressionTest {

	@Test
	public void doubles() {
		assertEquals(BigDecimalExpression.eval("1.23+3.2").doubleValue(), 4.43, 0);
		assertEquals(BigDecimalExpression.eval("(1.23+3.2)*-2").doubleValue(), -8.86, 0);		
		assertEquals(BigDecimalExpression.eval("10/3").doubleValue(), 10.0/3.0, 0);				

	}
	@Test	
	public void leadingAndTrailingDecimal(){
		assertEquals(BigDecimalExpression.eval(".5*0.2+1.25").doubleValue(), 1.35, 0);
		assertEquals(BigDecimalExpression.eval("1.5*3.").doubleValue(), 4.5, 0);
		assertEquals(BigDecimalExpression.eval(".5*3.").doubleValue(), 1.5, 0);
	}

	@Test
	public void ints() {
		assertEquals(BigDecimalExpression.eval("1.23+3.2").intValue(), 4, 0);
		assertEquals(BigDecimalExpression.eval("1.5*3").intValue(), 4, 0);
		assertEquals(BigDecimalExpression.eval("(1.23+3.2)*-2").intValue(), -8, 0);		
		assertEquals(BigDecimalExpression.eval("10/3").intValue(), 10/3, 0);		
	}

}
