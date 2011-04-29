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

package org.shept.org.springframework.web.servlet.mvc.delegation.configuration;

import java.util.List;

import org.springframework.util.StringUtils;

/**
 * @author Andreas
 *
 */
public class ActionConfiguration {
	
	private boolean create = false;
	
	private boolean delete = false;
	
	private boolean update = false;

	public ActionConfiguration() {
		super();
	}
	
	public ActionConfiguration(List<String> actions) {
		for (String action : actions) {
			String cmp = StringUtils.trimWhitespace(action).toLowerCase();
			if (cmp.equals("c") || cmp.equals("create")) {
				create = true;
			} 
			else if (cmp.equals("d") || cmp.equals("delete")) {
				delete = true;
			} 
			else if (cmp.equals("u") || cmp.equals("update")) {
				update = true;
			} 
			else throw new ChainConfigurationException(
				"Action Configuration specifies invalid action: '" + action 
				+ "' Should be one or more of 'create', 'update', 'delete' ");
		}
	}
	
	/**
	 * @return the create
	 */
	public boolean isCreate() {
		return create;
	}

	/**
	 * @param create the create to set
	 */
	public void setCreate(boolean create) {
		this.create = create;
	}

	/**
	 * @return the delete
	 */
	public boolean isDelete() {
		return delete;
	}

	/**
	 * @param delete the delete to set
	 */
	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	/**
	 * @return the update
	 */
	public boolean isUpdate() {
		return update;
	}

	/**
	 * @param update the update to set
	 */
	public void setUpdate(boolean update) {
		this.update = update;
	}
	

}
