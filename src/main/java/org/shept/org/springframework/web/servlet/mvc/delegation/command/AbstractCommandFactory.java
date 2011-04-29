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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentUtils;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.TargetConfiguration;
import org.shept.util.PageHolderFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

/**
 * @author Andreas Hahn
 *
 */
public abstract class AbstractCommandFactory  
	implements TargetCommandFactory, ApplicationContextAware, InitializingBean {

	/** Logger that is available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private ApplicationContext ctx;
	
	private PageHolderFactory pageHolderFactory;

	public Object getCommand(ComponentToken token) {
		Object model = ComponentUtils.getModel(token);
		TargetConfiguration cc = ComponentUtils.getChainConfiguration(getApplicationContext(), token);
		return getCommand(cc, model);
	}
	
	public Object getCommand(HttpServletRequest request, ComponentToken token) {
		return getCommand(token);
	}
	
	public abstract Object getCommand(TargetConfiguration config, Object referencedModel);

	/**
	 * @return the pageHolderFactory
	 */
	public PageHolderFactory getPageHolderFactory() {
		return pageHolderFactory;
	}

	/**
	 * @param pageHolderFactory the pageHolderFactory to set
	 */
	@Resource
	public void setPageHolderFactory(PageHolderFactory pageHolderFactory) {
		this.pageHolderFactory = pageHolderFactory;
	}

	/**
	 * @return the ctx
	 */
	public ApplicationContext getApplicationContext() {
		return ctx;
	}

	/**
	 * @param ctx the ctx to set
	 */
	public void setApplicationContext(ApplicationContext ctx) {
		this.ctx = ctx;
	}
	
	public void afterPropertiesSet(){
		Assert.notNull(pageHolderFactory, "pageHolderFactory must be initialized");
	}


}
