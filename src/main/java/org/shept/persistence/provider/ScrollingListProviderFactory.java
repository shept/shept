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

import org.shept.beans.support.ExampleDefinition;
import org.shept.beans.support.FilterDefinition;
import org.shept.beans.support.QueryDefinition;
import org.shept.beans.support.ReloadableAssociation;
import org.shept.org.springframework.orm.hibernate3.support.HibernateDaoSupportExtended;
import org.shept.persistence.provider.hibernate.HibernateAssociationProvider;
import org.shept.persistence.provider.hibernate.HibernateCriteriaDefinition;
import org.shept.persistence.provider.hibernate.HibernateCriteriaListProviderImpl;
import org.shept.persistence.provider.hibernate.HibernateExampleListProviderImpl;
import org.shept.persistence.provider.hibernate.HibernateQueryListProviderImpl;
import org.shept.persistence.provider.hibernate.UnsupportedFilterException;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.support.DaoSupport;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/** 
 * @author $Author: aha $
 * @version $Revision: 33 $
 * 
 * $Id: ScrollingListProviderFactory.java 33 2010-08-20 13:40:07Z aha $
 *
 */
public class ScrollingListProviderFactory  {
	
	private DaoSupport dao;

	private Integer initialLoadSize = 10;

	public ScrollingListProvider getScrollingList(FilterDefinition filterProvider) throws UnsupportedFilterException {
		Class<?> clazz;
		// Hibernate support
		if (HibernateDaoSupportExtended.class.isAssignableFrom(getDao().getClass()) ) {
			if (filterProvider instanceof QueryDefinition) {
				clazz = HibernateQueryListProviderImpl.class;
			} else if  (filterProvider instanceof HibernateCriteriaDefinition) {
				clazz = HibernateCriteriaListProviderImpl.class;
			} else if (filterProvider instanceof ExampleDefinition) {
				clazz = HibernateExampleListProviderImpl.class;
			} else if (filterProvider instanceof ReloadableAssociation) {
				clazz = HibernateAssociationProvider.class;;
			} else {
				throw new UnsupportedFilterException(
						"The filterProvider class: " + filterProvider.getClass() + " is not supported for Hibernate access");
			}
		} else if ((HibernateDaoSupport.class.isAssignableFrom(getDao().getClass()))) {
			throw new UnsupportedDataProviderException(
					"For DataAccess via Hibernate the extended version of HibernateSupport is neccessary. " +
					"Please use HibernateDaoSupportExtended instead of HibernateDaoSupport. " +
					"If your dao object inherits from HibernateDaoSupport please inherit from HibernateDaoSupportExtended instead");	
		} 
		// support of other wrappers
		else {
			throw new UnsupportedDataProviderException("This version only supports Hibernate as the dao wrapper.");
		}
		ScrollingListProvider sp = (ScrollingListProvider) BeanUtils.instantiateClass(clazz);
		sp.setDao(dao);
		sp.setLoadSize(initialLoadSize);
		return sp;
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
	public void setDao(DaoSupport dao) {
		this.dao = dao;
	}

	/**
	 * @return the initialLoadSize
	 */
	public Integer getInitialLoadSize() {
		return initialLoadSize;
	}

	/**
	 * @param initialLoadSize the initialLoadSize to set
	 */
	public void setInitialLoadSize(Integer initialLoadSize) {
		this.initialLoadSize = initialLoadSize;
	}

}
