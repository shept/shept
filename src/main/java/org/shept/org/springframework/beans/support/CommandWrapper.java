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

package org.shept.org.springframework.beans.support;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/** 
 * @version $$Id: CommandWrapper.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class CommandWrapper implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Object command;

	private String tagName;
	
	private Map<String, Object> properties;

	/**
	 * @return the command
	 */
	public Object getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(Object command) {
		this.command = command;
	}

	/**
	 * @return the tagName
	 */
	public String getTagName() {
		return tagName;
	}

	/**
	 * @param tagName the tagName to set
	 */
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/**
	 * @return the properties
	 */
	public Map<String, Object> getProperties() {
		if (properties == null) {
			properties = new LinkedHashMap<String, Object>(4);
		}
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	
}
