package com.pora.test.connector;

import com.porua.core.tag.ConfigProperty;

public class SkypeConnectorConfig {
	enum METHODS1 {
		GET, POST
	}

	@ConfigProperty(enumClass = METHODS1.class)
	private String method;

	@ConfigProperty
	private String login = "login";

	@ConfigProperty
	private String password = "password";

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

}