package com.pora.test;

import java.util.ArrayList;
import java.util.List;

import com.porua.codegen.GenerateCode;

public class UnitTest {
	public static void main(String[] args) throws Exception {
		List<String> list = new ArrayList<>();
		list.add(SkypeConnector.class.getName());
		list.add(MyConnector.class.getName());
		// GenerateCode.generateJaxbAssets("com.porua.test", list,
		// Main.class.getClassLoader());
		GenerateCode.generateXsdAssets("com.porua.test", list, UnitTest.class.getClassLoader());

	}

}
