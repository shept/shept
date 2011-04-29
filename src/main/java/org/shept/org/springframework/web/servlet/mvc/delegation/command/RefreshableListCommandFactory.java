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

package org.shept.org.springframework.web.servlet.mvc.delegation.command;

import java.lang.reflect.Method;

import org.shept.beans.support.FilterDefinition;
import org.shept.org.springframework.beans.support.Refreshable;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.ChainConfigurationException;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.TargetConfiguration;
import org.shept.org.springframework.web.servlet.mvc.support.ModelUtils;
import org.shept.persistence.provider.hibernate.HibernateCriteriaDefinition;
import org.shept.persistence.provider.hibernate.HibernateCriteriaFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ReflectionUtils;

/**
 *  
 * @version $Rev$
 * @author Andreas Hahn
 *
 */
public class RefreshableListCommandFactory extends AbstractCommandFactory 
	implements CommandFactory, InitializingBean {

	// a filter instance can be injected otherwise it will be created from configuration
	private FilterDefinition filter;

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.delegation.command.CommandFactory#getCommand(org.shept.org.springframework.web.servlet.mvc.delegation.ComponentConfiguration, java.lang.Object, java.lang.String)
	 */
	@Override
	public Object getCommand(TargetConfiguration config, Object referencedModel) {

		Refreshable listHolder = getPageHolderFactory().getObject();
		FilterDefinition fil = filter;
		if (fil == null) {
			fil = createFilter(config);
		}
		initializeFilter(fil, config);
		ModelUtils.initialize(fil, referencedModel);
		listHolder.setFilter(fil);
		return listHolder;
	}

	/**
	 * Special case: if filter is a HibernateCriteriaFilter we must populate the entityClass property
	 * 
	 * @param fil
	 * @param config
	 */
	protected void initializeFilter(FilterDefinition fil, TargetConfiguration config) {
		if (HibernateCriteriaFilter.class.isAssignableFrom(fil.getClass())) {
			HibernateCriteriaFilter hcFilter = (HibernateCriteriaFilter) fil;
			if (hcFilter.getEntityClass() == null) {
				hcFilter.setEntityClass(config.getEntityClass());				
			}			
		}
	}

	/**
	 * build a filter and check the configuration
	 * 
	 * Criteria API: Both filterClass and entityClass must be specified
	 * Query API (HQL): Only filterClass must be specified
	 * Example: filterClass and entityClass must be specified
	 * 
	 * Note that the default ComponentConfiguration will initialize an empty filterClass with the entityClass
	 * @param config
	 * @param config
	 * @return
	 */
	protected FilterDefinition createFilter(TargetConfiguration config) {
		if (config.getFilterClass() == null) {
			throw new ChainConfigurationException("Filter Configuration error in '" 
					+ config.getChainNameDisplay() + "'. A filterClass must be specified");
		}
		if (HibernateCriteriaDefinition.class.isAssignableFrom(config.getFilterClass()) &&
				config.getEntityClass() == null) {
			throw new ChainConfigurationException("Filter Configuration error in '" 
					+ config.getChainNameDisplay() + "'. An entityClass must be specified");
		}
		FilterDefinition filter = BeanUtils.instantiate(config.getFilterClass());
		// initialize the filter if an initializer is specified
		if (config.getFilterInitMethod() != null) {
			Method mth = ReflectionUtils.findMethod(filter.getClass(), config.getFilterInitMethod());
			if (mth != null) {
				try {
					mth.invoke(filter);
				}
				catch (Exception ex) {
					throw new ChainConfigurationException("Filter Configuration error in '" 
							+ config.getChainNameDisplay() + "'. Filter initialization with '" 
							+ config.getFilterInitMethod() + "' threw Exception", ex);
				}
			}
		}
		return filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(FilterDefinition filter) {
		this.filter = filter;
	}

}
