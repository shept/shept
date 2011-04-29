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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shept.beans.support.FilterDefinition;
import org.springframework.beans.support.SortDefinition;
import org.springframework.dao.support.DaoSupport;

/** 
 * @version $$Id: AbstractScrollingListProvider.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public abstract class AbstractScrollingListProvider implements ScrollingListProvider {

	/** Logger that is available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	protected int loadSize = 10; // number of records to read from the database initially  and subsequently
	
	protected int loaded = 0; // number of records already loaded
	
	protected boolean eol = false; // end-of-list
	
	protected FilterDefinition filterDefinition;
	
	protected SortDefinition sortDefinition;
	
	protected DaoSupport dao;

	public DaoSupport getDao() {
		return dao;
	}

	/**
	 * @param dao the dao to set
	 */
	public void setDao(DaoSupport dao) {
		this.dao = dao;
	}

	public int getLoadSize() {
		return loadSize;
	}

	public boolean isEol() {
		return eol;
	}

	public abstract List<?> loadListFirst();

	public abstract List<?> loadListNext();


	public void setLoadSize(int loadSize) {
		this.loadSize = loadSize;
	}

	/**
	 * @return the filterProvider
	 */
	public FilterDefinition getFilterDefinition() {
		return filterDefinition;
	}

	/**
	 * @param filterProvider the filterProvider to set
	 */
	public void setFilterDefinition(FilterDefinition filterProvider) {
		this.filterDefinition = filterProvider;
	}

	/**
	 * @return the sortDefinition
	 */
	public SortDefinition getSortDefinition() {
		return sortDefinition;
	}

	/**
	 * @param sortDefinition the sortDefinition to set
	 */
	public void setSortDefinition(SortDefinition sortDefinition) {
		this.sortDefinition = sortDefinition;
	}
	
	/**
	 * 
	* @param
	* @return
	*
	 */
	protected void incrementLoadSizeAfterFetch(int numberOfRows) {
		loaded +=numberOfRows;
		if (numberOfRows < loadSize) {
			eol = true;
		}

	}
}
