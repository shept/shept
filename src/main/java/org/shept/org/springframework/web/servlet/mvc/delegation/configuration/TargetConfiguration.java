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

import java.util.Arrays;

import javax.annotation.Resource;
import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shept.beans.support.FilterDefinition;
import org.shept.org.springframework.web.servlet.mvc.delegation.command.AbstractCommandFactory;
import org.shept.org.springframework.web.servlet.mvc.delegation.command.CommandFactory;
import org.shept.org.springframework.web.servlet.mvc.delegation.command.RefreshableListCommandFactory;
import org.shept.org.springframework.web.servlet.mvc.support.InfoItem;
import org.shept.util.PageHolderFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * @author Andreas Hahn
 *
 */
public class TargetConfiguration implements InitializingBean, ApplicationContextAware, BeanNameAware {
	
	/** Logger that is available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());
	
	protected ApplicationContext context;

	// the bean name of this configuration must match a subCommand tagName
	private String name;
	
	// entity class for creating new objects of this class
	private Class<?> entityClass;
	
	// filter definition class for resultset creation
	// defaults to entity class if entity implements FilterDefinition
	private Class<FilterDefinition> filterClass;
	
	// intialize filter via #filterInitMethod() selector;
	private String filterInitMethod;

	private SegmentConfiguration to;
	
	private CommandFactory commandFactory;
	
	// instantiate an empty item to allow easier configuration
	private InfoItem info = new InfoItem();
	
	private PageHolderFactory pageHolderFactory;
	
	// array of disabled actions
	// shorthand notation: c=create, u=update, d=delete
	// example: property="disabled" value="create, update"
	private String[] disabled = new String[]{};
	
	private ActionConfiguration disabledActions = new ActionConfiguration();
	
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
	 * @return the info
	 */
	public InfoItem getInfo() {
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(InfoItem info) {
		this.info = info;
	}
	
	public void setInfo(String code) {
		this.info = new InfoItem();
		this.info.setCode(code);
	}

	/**
	 * @return the to
	 */
	public SegmentConfiguration getTo() {
		return to;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(SegmentConfiguration to) {
		this.to = to;
	}

	/**
	 * @return the commandFactory
	 */
	public CommandFactory getCommandFactory() {
		return commandFactory;
	}

	/**
	 * @param commandFactory the commandFactory to set
	 */
	public void setCommandFactory(CommandFactory commandFactory) {
		this.commandFactory = commandFactory;
	}

	/**
	 * @return the name
	 */
	public String getBeanName() {
		return name;
	}

	public void setBeanName(String name) {
		this.name = name;
	}

	/**
	 * @param pageHolderFactory the pageHolderFactory to set
	 */
	@Resource
	public void setPageHolderFactory(PageHolderFactory pageHolderFactory) {
		this.pageHolderFactory = pageHolderFactory;
	}

	public String getChainNameDisplay() {
		String name = "Chain" ;
		if (getBeanName() != null) {
			name = name + " " + getBeanName();
		}
		name = name + " to " + to.getBeanName();
		return name;
	}
	
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		context = applicationContext;
	}

	/**
	 * 
	 */
	protected CommandFactory createCommandFactory() {
		return new RefreshableListCommandFactory();
	}

	/**
	 * @return the filterInitMethod
	 */
	public String getFilterInitMethod() {
		return filterInitMethod;
	}

	/**
	 * @param disabled the disabled to set
	 */
	public void setDisabled(String[] disabled) {
		this.disabled = disabled;
	}

	/**
	 * @return the disabledActions
	 */
	public ActionConfiguration getDisabledActions() {
		return disabledActions;
	}

	/**
	 * @param disabledActions the disabledActions to set
	 */
	public void setDisabledActions(ActionConfiguration disabledActions) {
		this.disabledActions = disabledActions;
	}
	
	/**
	 * @param filterInitMethod the filterInitMethod to set
	 */
	public void setFilterInitMethod(String filterInitMethod) {
		this.filterInitMethod = filterInitMethod;
	}
	@SuppressWarnings("unchecked")
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(to);
		Assert.notNull(to.getBeanName());
		if (entityClass == null) {
			entityClass = to.getEntityClass();
		}
		if (filterClass == null) {
			filterClass = to.getFilterClass();
		}
		Class<?> ec = entityClass;
		Class<?> fc = filterClass;
		if (ec != null) {
			Entity ann = AnnotationUtils.findAnnotation(ec, Entity.class);
			Assert.notNull(ann, "Chain Configuration for '" + getChainNameDisplay() 
					+ "' specifies Class '" + getEntityClass()
					+ "' which is not a vaild Entity class");
			if (fc == null) {
				if (FilterDefinition.class.isAssignableFrom(ec)) {
					setFilterClass((Class<FilterDefinition>)ec);
				}
			}
			logger.info(getClass().getSimpleName() + " " + getChainNameDisplay() + " for entity class '" + entityClass + "'");
		}
		if (fc != null) {
			if (filterInitMethod != null) {
				Assert.notNull(ReflectionUtils.findMethod(getFilterClass(), filterInitMethod), 
						"Chain configuration for '" + getChainNameDisplay() + "' specifies an invalid filter initialization ('"
						+ filterInitMethod + "') for filter class '" + filterClass + "'");				
			}
			logger.info(getClass().getSimpleName() + " " + getChainNameDisplay() + " for filter class '" + filterClass + "'");			
		}
		if (commandFactory == null) {
			Assert.notNull(pageHolderFactory,
					getClass().getSimpleName() + " " + getChainNameDisplay()
					+ " has neither commandFactory nor a pageHolderFactory defined. You need to declare one of both");
			commandFactory = createCommandFactory();
			if (commandFactory instanceof AbstractCommandFactory) {
				AbstractCommandFactory acf = (AbstractCommandFactory) commandFactory;
				acf.setPageHolderFactory(pageHolderFactory);
				acf.setApplicationContext(context);				
			}
		}
		setDisabledActions(new ActionConfiguration(Arrays.asList(this.disabled)));
	}

}
