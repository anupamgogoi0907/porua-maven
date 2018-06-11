package com.pora.test.connector;

import com.porua.core.processor.MessageProcessor;
import com.porua.core.tag.ConfigProperty;
import com.porua.core.tag.Connector;

@Connector(tagName = "my", tagNamespace = "http://www.porua.org/skype", tagSchemaLocation = "http://www.porua.org/skype/skype.xsd", imageName = "")
public class MyConnector extends MessageProcessor {

	enum METHODS {
		GET, POST
	}

	@ConfigProperty
	private String name;

	@ConfigProperty(enumClass = METHODS.class)
	private String method;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
