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

package org.shept.util;

import javax.annotation.Resource;

import org.shept.org.springframework.beans.support.FilteredListHolder;
import org.shept.org.springframework.beans.support.Refreshable;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.support.DaoSupport;

/** 
 * @version $$Id: PageHolderFactory.java 110 2011-02-21 09:16:15Z aha $$
 *
 * @author Andi
 *
 */
public class PageHolderFactory {

	private Class<? extends Refreshable> listHolderClass = FilteredListHolder.class;
	
	private DaoSupport dao;
	
	public Refreshable getObject()  {

		Refreshable pageHolder = BeanUtils.instantiateClass(listHolderClass);
		pageHolder.setDao(dao);
		return pageHolder;
	}

	/**
	 * @return the listHolderClass
	 */
	public Class<? extends Refreshable> getListHolderClass() {
		return listHolderClass;
	}

	/**
	 * @param listHolderClass the listHolderClass to set
	 */
	public void setListHolderClass(
			Class<? extends Refreshable> listHolderClass) {
		this.listHolderClass = listHolderClass;
	}

	/**
	 * @return the dao
	 */
	public DaoSupport getDao() {
		return dao;
	}

	/**
	 * @param dao the dao to set
	 */
	@Resource
	public void setDao(DaoSupport dao) {
		this.dao = dao;
	}

}
