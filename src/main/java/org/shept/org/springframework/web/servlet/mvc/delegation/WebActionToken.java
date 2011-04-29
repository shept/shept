/*
 * Copyright 2007-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.shept.org.springframework.web.servlet.mvc.delegation;

import org.shept.org.springframework.web.servlet.mvc.delegation.component.WebComponent;
import org.springframework.util.StringUtils;

/** 
 * @version $$Id: WebActionToken.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class WebActionToken {
	
	private String method;
	
	private String value;
	
	private String pathName;
	
	private WebComponent handler;

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the pathName
	 */
	public String getPathName() {
		return pathName;
	}

	/**
	 * @param pathName the pathName to set
	 */
	public void setPathName(String pathName) {
		this.pathName = pathName;
	}


	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Action " + StringUtils.quote(method));
		if (StringUtils.hasText(pathName)) {
			sb.append(" (form submission path named " + StringUtils.quote(pathName) + ")");
		} else {
			sb.append(" in rooPath");
		}
		if (StringUtils.hasText(value)) {
			sb.append(" with Value " + StringUtils.quote(value));
		}
		return sb.toString();
	}

	/**
	 * @return the handler
	 */
	public WebComponent getHandler() {
		return handler;
	}

	/**
	 * @param handler the handler to set
	 */
	public void setHandler(WebComponent handler) {
		this.handler = handler;
	}

}
