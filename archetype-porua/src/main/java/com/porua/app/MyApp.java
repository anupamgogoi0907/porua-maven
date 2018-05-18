package com.porua.app;

import java.io.File;

import com.porua.container.PoruaContainer;

public class MyApp {

	public static void main(String[] a) throws Exception {
		PoruaContainer.scanSingleApp(new File("./target/archetype-porua-1.0.0.jar"));
	}

}
