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

import org.shept.util.PageHolderFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * @author Andreas Hahn
 *
 */
public class CommandFactoryBean extends AbstractFactoryBean<CommandFactory> {

	protected PageHolderFactory pageHolderFactory;

	@Override
	public Class<CommandFactory> getObjectType() {
		return CommandFactory.class;
	}

	@Override
	protected CommandFactory createInstance() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

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


}
