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

package org.shept.persistence.provider.hibernate;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.shept.org.springframework.web.servlet.mvc.support.ModelUtils;
import org.shept.persistence.ModelCreation;
import org.springframework.beans.support.SortDefinition;
import org.springframework.util.StringUtils;

/**
 *  
 * @version $Revision: 73 $
 * @author Andreas Hahn
 *
 */
public abstract class HibernateCriteriaFilter implements HibernateCriteriaDefinition, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** Logger that is available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private Class<?> entityClass;
	
	private SortDefinition defaultSortDefinition;
	
	/**
	 * @return a default criteria definition by simply return an empty entity object
	 * @see http://stackoverflow.com/questions/1926618/hibernate-sort-by-properties-of-inner-bean
	 * 
	 */
	public DetachedCriteria getCriteria(SortDefinition sortDefinition) {
		SortDefinition sd = defaultSortDefinition;
		DetachedCriteria crit = DetachedCriteria.forClass(getEntityClass());
		// set sort criteria from FormFilter
		if (null != sortDefinition && StringUtils.hasText(sortDefinition.getProperty())) {
			sd = sortDefinition;
		}
		if (null != sd && StringUtils.hasText(sd.getProperty())) {
			String prop = sd.getProperty();
			String[] pathArr = StringUtils.split(prop, ".");
			if (pathArr == null) {
				pathArr = new String[] {prop};
			}
			if (pathArr.length > 2) {
				throw new UnsupportedOperationException("Sort Criteria Definition '" + prop + "' may only nest one level deep");
			}
			if (pathArr.length == 2) {
				crit.createAlias(pathArr[0], pathArr[0]);
			}
			if (sortDefinition.isAscending())
				crit.addOrder(Order.asc(prop));
			else
				crit.addOrder(Order.desc(prop));
		}
		return crit;
	}

	/**
	 * @return a ModelCreation object instance
	 */
	public ModelCreation getNewModelTemplate() {
		return ModelUtils.getNewModelTemplate(entityClass, "???");
	}

	/**
	 * @return the entityClass
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}

	/**
	 * @param entityClass the entityClass to set
	 */
	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * @return the defaultSortDefinition
	 */
	public SortDefinition getDefaultSortDefinition() {
		return defaultSortDefinition;
	}

	/**
	 * @param defaultSortDefinition the defaultSortDefinition to set
	 */
	public void setDefaultSortDefinition(SortDefinition defaultSortDefinition) {
		this.defaultSortDefinition = defaultSortDefinition;
	}

}
