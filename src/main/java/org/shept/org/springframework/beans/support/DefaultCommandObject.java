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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.shept.org.springframework.web.servlet.mvc.delegation.SubCommandProvider;


/** 
 * @version $$Id: DefaultCommandObject.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */

public class DefaultCommandObject implements SubCommandProvider, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6096066668597850965L;

	public static final String CHILD_BINDING_NAME = "children";

	private List<CommandWrapper> children = new LinkedList<CommandWrapper>();

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.delegation.ComponentProvider#getComponents()
	 */
	public Map<String, Object> getSubCommands() {
		Map<String, Object> components = new LinkedHashMap<String, Object>(children.size());
		for (int i=0; i < children.size(); i++ ) {
			Object subCommand = children.get(i).getCommand();
			components.put(CHILD_BINDING_NAME + "[" + i + "].command", subCommand);
		}
		return components;
	}
	
	public List<CommandWrapper> getChildren() {
		return this.children;
	}

	/**
	 * @param chain the chain to set
	 */
	public void setChildren(List<CommandWrapper> chain) {
		this.children = chain;
	}
}
