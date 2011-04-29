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

package org.shept.org.springframework.web.servlet.mvc.delegation.component;


import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.Entity;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.shept.org.springframework.beans.support.CommandWrapper;
import org.shept.org.springframework.beans.support.ModelSupplier;
import org.shept.org.springframework.beans.support.PageableList;
import org.shept.org.springframework.beans.support.Refreshable;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentUtils;
import org.shept.org.springframework.web.servlet.mvc.delegation.command.CommandFactory;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.ChainConfigurationException;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.TargetConfiguration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.support.DaoSupport;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;


/** 
 * @version $$Id: ChainingComponent.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class ChainingComponent extends AbstractComponent {

	protected boolean enableChain = true;
	
	protected boolean enableInfo = true;

	protected DaoSupport dao;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.shept.org.springframework.web.servlet.mvc.delegation.component.
	 * WebComponent#excecuteAction(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse,
	 * org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken)
	 */
	public ModelAndView excecuteAction(HttpServletRequest request,
			HttpServletResponse response, ComponentToken token)
			throws Exception {
		String method = token.getToken().getMethod();
		if (method.equals("onChain")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Chain request "
						+ (this.enableChain ? "" : "BLOCKED") + ": "
						+ token.toString());
			}
			if (this.enableChain) {
				return doChain(request, token);
			}
			
			return modelRedirectClip(request, token);
		}
		
		return modelUnhandled(request, token);
	}

	/**
	 * @param jp
	 * @param listHolder
	 * @param
	 * @return
	 */
	protected ModelAndView doChain(HttpServletRequest request, ComponentToken token) {
		Object model = ComponentUtils.getModel(token);
		
		TargetConfiguration cc = ComponentUtils.getChainConfiguration(request, token);
		if (cc == null) {
			if (logger.isErrorEnabled()) {
				logger.error(ComponentUtils.getChainConfigurationErrorMessage(request, token));
			}
			return modelRedirectClip(request, token);
		}

		CommandFactory cf = cc.getCommandFactory();
		if (cf == null) {
			logger.error("No CommandFactory configured for chain '" + cc.getChainNameDisplay() + "'");
			return modelRedirectClip(request, token);
		}
		
		Object cmd = null;
		try {
			cmd = cf.getCommand(request, token);
		} catch (ChainConfigurationException ex) {
			logger.error("Runtime error during chain processing ", ex);
		}
		if (cmd == null) {
			return modelWithErrors(token.getBinder());
		}
		
		CommandWrapper cw = new CommandWrapper();
		if (cmd instanceof CommandWrapper) {
			// special case: returning a command wrapper instead of a command object
			cw = (CommandWrapper) cmd;
		} else {
			cw.setTagName(cc.getTo().getBeanName());
			cw.setCommand(cmd);			
		}

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

		if (enableInfo) {
			String info = ComponentUtils.getComponentInfo(request, cc.getInfo(), model);
			if (StringUtils.hasText(info)) {
				cw.getProperties().put(DefaultProperties.INFO, info);									
			}
		}

		int idx = ComponentUtils.addComponent(token, cw);		
		return modelRedirect(request, token, idx);			
	}

	protected void initComponentCommandWrapper(CommandWrapper cw) {
		ComponentUtils.applyConfiguration(cw, getWebApplicationContext());
	}


	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.delegation.component.WebComponent#supports(java.lang.Class)
	 */
	public boolean supports(Object commandObject) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.delegation.component.WebComponent#supports(java.lang.Class)
	 */
	public boolean supports_oldVersion(Object commandObject) {
		Class<?> clazz = commandObject.getClass();
		// Entity ann = clazz.getAnnotation(Entity.class);
		Entity ann = AnnotationUtils.findAnnotation(clazz, Entity.class);
		boolean isPl = PageableList.class.isAssignableFrom(clazz);
		boolean isMw = ModelSupplier.class.isAssignableFrom(clazz) ;
		boolean isEntity = ann != null;
		return isPl || isMw || isEntity;
	}

	public Map<String, String> getDefaultMappings() {
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.put("submitChain", "onChain");						// open a chained subForm
		return mappings;
	}

	/**
	 * @param enableInfo the enableInfo to set
	 */
	public void setEnableInfo(boolean enableInfo) {
		this.enableInfo = enableInfo;
	}

	/**
	 * @param dao the dao to set
	 */
	@Resource
	public void setDao(DaoSupport dao) {
		this.dao = dao;
	}

	/**
	 * @return the dao
	 */
	public DaoSupport getDao() {
		return dao;
	}

}
