#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package runner;

import ${package}.container.PoruaContainer;

public class AppRunner {

	public static void main(String[] a) throws Exception {
		 PoruaContainer.scanSingleApp();
	}

}
