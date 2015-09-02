package com.quantumcell.shunt;

import com.badlogic.gdx.utils.Array;
import com.quantumcell.utils.Sugar;

public class Utils {

	public static <V> void printTokens(Array<Token> tokens, String label) {
		printTokens(tokens,label,"");
	}
	public static <V> void printTokens(Array<Token> tokens, String label, String last) {
		Sugar.print(label+":[");
		for(Token token:tokens){
			String s = token.encoded;
			//String v = (token.value!=null && token.value instanceof Number) ? ":"+((Number)token.value).intValue() : "?";
			Sugar.print(s+" ");
		}
		Sugar.print("]");
		if(last!="") {
			Sugar.log("");Sugar.log(last);
		}
	}
}
