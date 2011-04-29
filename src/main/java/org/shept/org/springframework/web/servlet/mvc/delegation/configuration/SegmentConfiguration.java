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

import java.util.Map;

import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shept.beans.support.FilterDefinition;
import org.shept.org.springframework.web.bind.support.ComponentBindingInitializer;
import org.shept.org.springframework.web.bind.support.ComponentPostprocessor;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentTransaction;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentValidator;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

/** 
 * @version $$Id: SegmentConfiguration.java 98 2011-01-08 12:05:32Z aha $$
 *
 * @author Andi
 *
 */
public class SegmentConfiguration implements BeanNameAware, InitializingBean {

	/** Logger that is available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	// the bean name of this configuration must match a subCommand tagName
	private String name;
	
	// entity class for creating new objects of this class
	private Class<?> entityClass;
	
	// filter definition class for resultset creation
	// defaults to entity class if entity implements ModelCreation
	private Class<FilterDefinition> filterClass;

	// initializers for time / date / special numbers / isbn / ...
	private ComponentBindingInitializer[] componentBindingInitializers;
	
	// validators
	private ComponentValidator[] validators;
	
	// for custom transactions
	private ComponentTransaction transaction;
	
	// postprocessors to supply additional information
	// mostly use for selection, contents of drop downs / etc
	private ComponentPostprocessor[] componentPostprocessors;
	
	private Map<String, ChainConfiguration> chains;

	/**
	 * @param entityClass the entityClass to set
	 */
	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * @return the entityClass
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}

	/**
	 * @return the filterClass
	 */
	public Class<FilterDefinition> getFilterClass() {
		return filterClass;
	}

	/**
	 * @param filterClass the filterClass to set
	 */
	public void setFilterClass(Class<FilterDefinition> filterClass) {
		this.filterClass = filterClass;
	}

	/**
	 * @return the ComponentBindingInitializers
	 */
	public ComponentBindingInitializer[] getComponentBindingInitializers() {
		return componentBindingInitializers;
	}

	/**
	 * @param ComponentBindingInitializers the ComponentBindingInitializers to set
	 */
	public void setComponentBindingInitializers(
			ComponentBindingInitializer[] componentBindingInitializers) {
		this.componentBindingInitializers = componentBindingInitializers;
	}

	/**
	 * @param ComponentBindingInitializers the ComponentBindingInitializers to set
	 */
	public void setComponentBindingInitializer(
			ComponentBindingInitializer componentBindingInitializer) {
		this.componentBindingInitializers = new ComponentBindingInitializer[] {componentBindingInitializer};
	}

	/**
	 * @return the validators
	 */
	public ComponentValidator[] getValidators() {
		return validators;
	}

	/**
	 * @param validators the validators to set
	 */
	public void setValidators(ComponentValidator[] validators) {
		this.validators = validators;
	}
	
	/**
	 * @param validators the validators to set
	 */
	public void setValidator(ComponentValidator validator) {
		this.validators = new ComponentValidator[] {validator};
	}

	public void setBeanName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getBeanName() {
		return name;
	}

	/**
	 * @return the componentPostprocessors
	 */
	public ComponentPostprocessor[] getComponentPostprocessors() {
		return componentPostprocessors;
	}

	/**
	 * @param componentPostprocessors the componentPostprocessors to set
	 */
	public void setComponentPostprocessors(
			ComponentPostprocessor[] componentPostprocessors) {
		this.componentPostprocessors = componentPostprocessors;
	}

	/**
	 * @param componentPostprocessor the componentPostprocessor to set
	 */
	public void setComponentPostprocessor(
			ComponentPostprocessor componentPostprocessor) {
		this.componentPostprocessors = new ComponentPostprocessor[] {componentPostprocessor};
	}

	/**
	 * @return the transaction
	 */
	public ComponentTransaction getTransaction() {
		return transaction;
	}

	/**
	 * @param transaction the transaction to set
	 */
	public void setTransaction(ComponentTransaction transaction) {
		this.transaction = transaction;
	}

	/**
	 * @return the chains
	 */
	public Map<String, ChainConfiguration> getChains() {
		return chains;
	}

	/**
	 * @param chains the chains to set
	 */
	public void setChains(Map<String, ChainConfiguration> chains) {
		this.chains = chains;
	}

	@SuppressWarnings("unchecked")
	public void afterPropertiesSet() throws Exception {
		Class<?> ec = getEntityClass();
		if (ec != null) {
			Entity ann = AnnotationUtils.findAnnotation(ec, Entity.class);
			Assert.notNull(ann, "Segment Configuration for Segment '"
				+ getBeanName() + "' specifies Class '" + ec
				+ "' which is not a vaild Entity class");
			if (getFilterClass() == null) {
				if (FilterDefinition.class.isAssignableFrom(ec)) {
					setFilterClass((Class<FilterDefinition>)ec);
				}
			}
		}
	}

}
