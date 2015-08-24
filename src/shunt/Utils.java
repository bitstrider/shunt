package com.quantumcell.shunt;

import com.badlogic.gdx.utils.Array;
import com.quantumcell.utils.Sugar;

public class Utils {

	public static <V> void printTokens(Array<V> tokens, String label) {
		printTokens(tokens,label,"");
	}
	public static <V> void printTokens(Array<V> tokens, String label, String last) {
		Sugar.print(label+":[");
		for(Object token:tokens){
			Sugar.print(((Token)token).encoded + " " );
		}
		Sugar.print("]");
		if(last!="") {
			Sugar.log("");Sugar.log(last);
		}
	}
}
