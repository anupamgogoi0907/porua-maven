package com.porua.app;

import java.io.File;
import java.net.URL;

import com.porua.container.PoruaContainer;

public class MyApp {

	public static void main(String[] a) throws Exception {
		porua();
	}

	public static void porua() throws Exception {
		String file1 = "file:/Users/ac-agogoi/git/porua-maven/archetype-porua/src/main/app/my-app-1.xml";
		String[] apps = new String[] { file1 };
		URL jarUrl = new File("/Users/ac-agogoi/git/porua-maven/archetype-porua/target/archetype-porua-1.0.0.jar").toURI().toURL();
		PoruaContainer.loadSingleApp(jarUrl, apps);
	}
}
