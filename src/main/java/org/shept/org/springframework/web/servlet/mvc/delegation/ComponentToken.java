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

import org.shept.org.springframework.web.bind.support.ComponentDataBinder;

/** 
 * @version $$Id: ComponentToken.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class ComponentToken {
	
	private WebActionToken token;
	
	private Object component;
	
	private ComponentDataBinder binder;
	
	/**
	 * @return the token
	 */
	public WebActionToken getToken() {
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(WebActionToken token) {
		this.token = token;
	}

	/**
	 * @return the component
	 */
	public Object getComponent() {
		return component;
	}

	/**
	 * @param component the component to set
	 */
	public void setComponent(Object component) {
		this.component = component;
	}

	/**
	 * @return the command
	 */
	public Object getCommand() {
		return binder.getTarget();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Execute " + token.toString());
		sb.append(" for ");
		if (component != null) {
			sb.append(component.getClass().getSimpleName() + " in ");
		}
		if (getCommand() != null) {
			sb.append(getCommand().getClass().getName());
		}
//		sb.append(" with model named " + StringUtils.quote(commandName));
		return sb.toString();
	}

	/**
	 * @return the binder
	 */
	public ComponentDataBinder getBinder() {
		return binder;
	}

	/**
	 * @param binder the binder to set
	 */
	public void setBinder(ComponentDataBinder binder) {
		this.binder = binder;
	}

}
