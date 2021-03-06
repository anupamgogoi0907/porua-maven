package com.pora.test.connector;

import com.porua.core.processor.MessageProcessor;
import com.porua.core.tag.ConfigProperty;
import com.porua.core.tag.Connector;
import com.porua.core.tag.ConnectorConfig;

@Connector(tagName = "skype", tagNamespace = "http://www.porua.org/skype", tagSchemaLocation = "http://www.porua.org/skype/skype.xsd", imageName = "")
public class SkypeConnector extends MessageProcessor {

	@ConfigProperty
	private String name;

	@ConfigProperty
	private int a;

	@ConfigProperty
	private Integer b;

	@ConnectorConfig(configName = "config-ref", tagName = "skype-config")
	SkypeConnectorConfig config;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SkypeConnectorConfig getConfig() {
		return config;
	}

	public void setConfig(SkypeConnectorConfig config) {
		this.config = config;
	}

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public Integer getB() {
		return b;
	}

	public void setB(Integer b) {
		this.b = b;
	}

}
