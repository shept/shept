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

import org.shept.beans.support.FilterDefinition;
import org.shept.persistence.provider.ScrollingListProvider;
import org.springframework.dao.support.DaoSupport;

public interface Refreshable {

	public static final String FILTER_BINDING_NAME = "filter" ;	 // corresponds with 'setFilter'

	/**
	 * Reload the underlying list from the source provider if necessary
	 * (i.e. if the locale and/or the filter has changed), and resort it.
	 * @param force whether a reload should be performed in any case
	 * @throws Exception 
	 */
	@Deprecated
	public abstract void refresh(DaoSupport dao);
	
	/**
	 * Reload the underlying list from the source provider if necessary
	 * (i.e. if the locale and/or the filter has changed), and resort it.
	 * @param force whether a reload should be performed in any case
	 * @throws Exception 
	 */
	public abstract void refresh();

	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param filterProvider
	 */
	public abstract void setFilter(FilterDefinition filterProvider);
	

	/**
	 * 
	 * @param
	 * @return
	 *
	 * @return
	 */
	public abstract FilterDefinition  getFilter();
	

	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param filterLastUsed
	 * @throws Exception 
	 */
	public abstract void setUseFilter(FilterType filterLastUsed);

	/**
	 * 
	 * @param
	 * @return
	 *
	 * @return
	 */
	public abstract ScrollingListProvider getSourceProvider();

	/**
	 * 
	 */
	public abstract void setDao(DaoSupport dao);
	
}