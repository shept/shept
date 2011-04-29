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

package org.shept.persistence.provider;

import java.util.List;

import org.shept.beans.support.FilterDefinition;
import org.springframework.beans.support.SortDefinition;
import org.springframework.dao.support.DaoSupport;


/**
 * The ScrollingListProvider interface provides an interface to scroll the results of a datasource provider.
 * 
 * @author Andi
*  @version %version 1.0%
 *
 */
public interface ScrollingListProvider {

	
	/**
	*
	* Read the first chunk of elements from the underlying datasource.

	* @return the resulting list of elements
	*/
	public abstract List<?> loadListFirst();

	/**
	* Read a subsequent portion of elements from the underlying datasource
	* 
	* @return the resulting list of elements
	*/
	public abstract List<?> loadListNext();

	/**
	* 
	* @return true if end-of-lines is reached (no further results available)
	*/
	public abstract boolean isEol();


	/**
	 * @return  the data access object which depends on the persistence layer in use
	 *  
	 */
	public abstract DaoSupport getDao();
	
	/**
	 * Set the data access object which depends on the persistence layer in use
	 * 
	 */
	public abstract void setDao(DaoSupport dao);
	
	/**
	* 
	* @return Number of records to read from the database initially and subsequently
	* (if paging is supported)
	*
	*/
	public abstract int getLoadSize();

	/**
	 * set the load size which will be used for paging
	 */
	public abstract void setLoadSize(int page);
	

	/**
	 * 
	 */
	public SortDefinition getSortDefinition();
	

	/**
	 * 
	 */
	public void setSortDefinition(SortDefinition sortDefinition);

	
	/**
	 * 
	* @param
	* @return
	*
	* @return
	 */
	public FilterDefinition getFilterDefinition();
	
	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param filterDefinition
	 */
	public void setFilterDefinition (FilterDefinition filterDefinition);

}

