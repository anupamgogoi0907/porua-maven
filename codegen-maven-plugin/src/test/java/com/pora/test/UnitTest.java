package com.pora.test;

import java.util.ArrayList;
import java.util.List;

import com.pora.test.connector.MyConnector;
import com.pora.test.connector.SkypeConnector;
import com.porua.codegen.GenerateCode;

public class UnitTest {
	public static void main(String[] args) throws Exception {
		List<String> list = new ArrayList<>();
		// list.add(SkypeConnector.class.getName());
		list.add(MyConnector.class.getName());

		//GenerateCode.generateJavaAssets("com.porua.generated", list, UnitTest.class.getClassLoader());
		GenerateCode.generateXsdAssets("com.porua.generated", list, UnitTest.class.getClassLoader());

	}

}
