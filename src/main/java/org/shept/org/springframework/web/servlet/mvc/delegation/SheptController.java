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

package org.shept.org.springframework.web.servlet.mvc.delegation;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.shept.org.springframework.beans.support.CommandWrapper;
import org.shept.org.springframework.beans.support.DefaultCommandObject;
import org.shept.org.springframework.beans.support.Refreshable;
import org.shept.org.springframework.web.servlet.mvc.delegation.command.CommandFactory;
import org.shept.org.springframework.web.servlet.mvc.delegation.command.TargetCommandFactory;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.TargetConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.support.DaoSupport;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/** 
 * @version $$Id: SheptController.java $$
 *
 * @author Andi
 *
 */
public class SheptController extends MultiActionController implements InitializingBean {
	
	private TargetConfiguration configuration;
	
	protected DaoSupport dao;

	@Override 
	protected Object buildCommandObject(HttpServletRequest request) {
		
			DefaultCommandObject dco = new DefaultCommandObject();
			CommandFactory cmdFac = configuration.getCommandFactory();
			Object cmd = null;
			if (cmdFac instanceof TargetCommandFactory) {
				cmd = ((TargetCommandFactory)cmdFac)
					.getCommand(configuration, null);
			} else {
				cmd = cmdFac.getCommand(request, null);
			}
			CommandWrapper cw = new CommandWrapper();
			cw.setCommand(cmd);
			cw.setTagName(configuration.getTo().getBeanName());
			dco.getChildren().clear();
			dco.getChildren().add(cw);
			ComponentUtils.applyConfiguration(cw, getWebApplicationContext());
			
			if (cw.getCommand() instanceof Refreshable) {
				Refreshable content = (Refreshable) cw.getCommand();
				// TODO change checked exception
				try {
					content.refresh();
				} catch (Exception ex) {
					logger.error("Error while loading content for component '" + cw.getTagName() 
						+ "'", ex);
				}
			}
			return dco;
		}

	@Override
	protected void postProcessModel(HttpServletRequest request, ModelAndView mav) throws Exception {
		super.postProcessModel(request, mav);
		if (! mav.getModelMap().containsAttribute(getFormTitleAttribute())) {
			if (configuration.getInfo() != null && StringUtils.hasText(configuration.getInfo().getCode())) {
				String title = getMessageSourceAccessor().getMessage(configuration.getInfo().getCode());
				mav.addObject(getFormTitleAttribute(), title);
			}			
		}
	}

	/**
	 * @param configuration the configuration to set
	 */
	public void setConfiguration(TargetConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public void afterPropertiesSet() {
		Assert.notNull(configuration);
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
