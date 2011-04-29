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


/** 
 * @version $$Id: DataGridConfiguration.java 98 2011-01-08 12:05:32Z aha $$
 *
 * @author Andi
 *
 */
public class DataGridConfiguration extends SegmentConfiguration {
	
	protected Integer newModelSize;
	
	protected Integer pageSize;
	
	/**
	 * @return the newModelSize
	 */
	public Integer getNewModelSize() {
		return newModelSize;
	}

	/**
	 * @param newModelSize the newModelSize to set
	 */
	public void setNewModelSize(Integer newModelSize) {
		this.newModelSize = newModelSize;
	}

	/**
	 * @return the pageSize
	 */
	public Integer getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize the pageSize to set
	 */
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

}
