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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Entity;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shept.org.springframework.beans.support.CommandWrapper;
import org.shept.org.springframework.beans.support.MultiChoice;
import org.shept.org.springframework.beans.support.PageableList;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.ChainConfiguration;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.DataGridConfiguration;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.SegmentConfiguration;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.SheptBean;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.TargetConfiguration;
import org.shept.org.springframework.web.servlet.mvc.support.InfoItem;
import org.shept.org.springframework.web.servlet.mvc.support.ModelUtils;
import org.shept.org.springframework.web.servlet.mvc.support.RequestValueUtils;
import org.shept.org.springframework.web.servlet.mvc.support.RequestValueUtils.ChainParameter;
import org.shept.util.SheptBeanFactoryUtils;
import org.shept.util.StringUtilsExtended;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BindingResultUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

/** 
 * @version $$Id: ComponentUtils.java 119 2011-02-21 17:28:18Z aha $$
 *
 * @author Andi
 *
 */
public abstract class ComponentUtils {
	
	/** Logger that is available to subclasses */
	protected static Log logger = LogFactory.getLog(ComponentUtils.class);
	
	public static String DEFAULT_SELECTOR = "";

	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param token
	 * @return
	 */
	public static Object getModel(ComponentToken token) {
		if (token == null) {
			return null;
		}
		Object model = null;
		
		Object unwrapped = ModelUtils.unwrapIfNecessary(token.getComponent());
		if (null != AnnotationUtils.findAnnotation(unwrapped.getClass(),Entity.class)) {
			model = unwrapped;
		}
		if (model == null) {
			model = ModelUtils.unwrapIfNecessary(getModelFromList(token));
		}
		return model;
	}
	
	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param token
	 * @return
	 */
	public static Object getModelFromList(ComponentToken token) {
		Object model = null;
		String val = token.getToken().getValue();
		Integer index = -1;
		try {	// checking for a direct index first
			index = Integer.valueOf(val);
		} catch (Exception e) {
			// if not a direct index then check for submission parameter
			ChainParameter jp = RequestValueUtils.getChainParameter(val);
			if (jp.isIndexed()) {
				index = jp.getIndex();
			} else {
				logger.info("Trying to find an object model index but there is no index specified in token " + token.toString());
			}
		}
		if (index == -1) {
			return null;	// no index found so far
		}
		PageableList<?> pagedList = null;
		if (PageableList.class.isAssignableFrom(token.getComponent().getClass())) {
			pagedList = (PageableList<?>) token.getComponent();
			if (pagedList.isVisible(index)) {
				model = pagedList.getSource().get(index);
			} else {
				if (logger.isErrorEnabled()) {
					logger.error("The index " + index.toString() + " is not visible within the boundaries of the pagedList (" + 
						String.valueOf(pagedList.getFirstElementOnPage()) + "-" + String.valueOf(pagedList.getLastElementOnPage()) + ")" );
				}
				return null;
			}
		} else {
			if (logger.isErrorEnabled()) {
				logger.error("The token component does not implement PageableList so we cannot extract a row object from list. Token is: " + token.toString());
			}
		}
		return model;
	}

	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param token
	 * @return
	 */
	public static List<?> getModelsSelected(ComponentToken token) {
		if (! (token.getComponent() instanceof MultiChoice)) return Collections.EMPTY_LIST;
		return ((MultiChoice<?>) token.getComponent()).getSelectedItems();
	}

	/**
	 * Looking up the token's component index in the command list
	 * 
	 * @param token
	 * @return
	 */
	public static int lookupComponentIndex(ComponentToken token) {
		if (!(token.getCommand() instanceof SubCommandProvider)) {
			return -1;
		}
		SubCommandProvider scp = (SubCommandProvider) token.getCommand();
		for (int i = 0; i < scp.getChildren().size(); i++) {
			CommandWrapper cmdWr = scp.getChildren().get(i);
			if (cmdWr.getCommand().equals(token.getComponent())) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * Looking up the token's component path
	 * 
	 * @param token
	 * @return
	 */
	public static String lookupComponentPath(ComponentToken token) {
		if (!(token.getCommand() instanceof SubCommandProvider)) {
			return "";
		}
		SubCommandProvider scp = (SubCommandProvider) token.getCommand();
		for (Entry<String, Object> pathAndCommand : scp.getSubCommands().entrySet()) {
			if (pathAndCommand.getValue().equals(token.getComponent())) {
				return pathAndCommand.getKey();
			}
		}
		return "";
	}

	/**
	 * Return a map contaning bind path as key and CommandWrapper as value
	 * 
	 * @param token
	 * @return
	 */
	public static Map<String, CommandWrapper> getComponentPathMap (SubCommandProvider command) {
		Map<String, Object> cmdMap = command.getSubCommands();
		String[] cmdPath = cmdMap.keySet().toArray(new String[0]);
		Map<String, CommandWrapper> pathMap = new LinkedHashMap<String, CommandWrapper>();
		for (int i = 0; i < cmdMap.size(); i++) {
			pathMap.put(cmdPath[i], command.getChildren().get(i));
		}
		return pathMap;
	}

	/**
	 * Looking up the token's component wrapper
	 * 
	 * @param token
	 * @return
	 */
	public static CommandWrapper lookupComponentWrapper(ComponentToken token) {
		return lookupComponentWrapper(token, 0);
	}

	/**
	 * Looking up the token's component wrapper
	 * 
	 * @param token
	 * @return
	 */
	public static CommandWrapper lookupComponentWrapper(ComponentToken token, int offset) {
		if (!(token.getCommand() instanceof SubCommandProvider)) {
			return null;
		}
		SubCommandProvider scp = (SubCommandProvider) token.getCommand();
		Integer idx = lookupComponentIndex(token);
		return scp.getChildren().get(idx + offset);
	}

	/**
	 * Remove all components after the specified index. If no index is specified
	 * then the passed component is taken as the last component
	 * 
	 * @param token
	 * @param index
	 * @return
	 */
	public static int removeComponentsAfterIndex(ComponentToken token, Integer index) {
		if (!(token.getCommand() instanceof SubCommandProvider)) {
			return -1;
		}
		// jump to the fragment we just added (identified by token)
		SubCommandProvider scp = (SubCommandProvider) token.getCommand();
		List<CommandWrapper> children = scp.getChildren();
		Integer idx = index;
		if (index == null) {
			idx = lookupComponentIndex(token);
		}
		idx = Math.min(idx, children.size() - 1);
		while (children.size() - 1 > idx) {
			scp.getChildren().remove(children.size() - 1);
		}
		return idx;
	}
	
	/**
	 * Add the Component wrapper after the position specified by token
	 *
	 * @param token
	 * @param wrapper
	 * @param index
	 */
	public static int addComponent (ComponentToken token, CommandWrapper wrapper) {
		return addComponent(token, wrapper, null);
	}


	/**
	 * Add the Component wrapper after the position specified by token
	 * If theres a valid index argument then add the compoent after the index
	 *
	 * @param token
	 * @param wrapper
	 * @param index
	 */
	public static int addComponent (ComponentToken token, CommandWrapper wrapper, Integer index) {
		if (!(token.getCommand() instanceof SubCommandProvider)) {
			return -1;
		}
		SubCommandProvider scp = (SubCommandProvider) token.getCommand();
		List<CommandWrapper> children = scp.getChildren();
		Integer idx = index;
		if (index == null) {
			idx = lookupComponentIndex(token);
		}
		idx = Math.min(idx, children.size() - 1) +1;
		scp.getChildren().add(idx, wrapper);	
		return idx;
	}

	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param ctx
	 * @param tagName
	 * @return
	 */
	public static SegmentConfiguration getConfiguration(ApplicationContext ctx, String tagName) {
		SegmentConfiguration config = null;
		try {
			config = ctx.getBean(tagName,SegmentConfiguration.class);
		} catch (BeansException ex) {
			// no configuration found, this is ok
		}				
		return config;
	}
	
	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param ctx
	 * @param wrapper
	 */
	public static void applyConfiguration(CommandWrapper wrapper, ApplicationContext ctx) {
		// fill in values from the configuration
		SegmentConfiguration config = ComponentUtils.getConfiguration(ctx, wrapper.getTagName());
		if (config instanceof DataGridConfiguration &&
				wrapper.getCommand() instanceof PageableList) {
			PageableList<?> listHolder = (PageableList<?>) wrapper.getCommand();
			DataGridConfiguration listConfig = (DataGridConfiguration) config;
			if (listConfig.getNewModelSize() != null && 
					listHolder.getNewModelSize() == PageableList.DEFAULT_NEW_MODEL_SIZE ) {
				listHolder.setNewModelSize(listConfig.getNewModelSize());
			}
			if (listConfig.getPageSize() != null && 
					listHolder.getPageSize() == PageableList.DEFAULT_PAGE_SIZE) {
				listHolder.setPageSize(listConfig.getPageSize());
			}
		}
	}
	
	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param wrapper
	 * @param ctx
	 * @param model
	 * @param selector
	 * @return
	 */
	public static String getComponentInfo(HttpServletRequest request, InfoItem item, Object model) {
		if (item == null) return null;
		if (item.getCode() == null) return null;
		WebApplicationContext ctx = RequestContextUtils.getWebApplicationContext(request);
		Locale locale = RequestContextUtils.getLocale(request);
		String arg = null;
		Method mth;
		if (model != null) {
			if (StringUtils.hasText(item.getSelector())) {
				mth = ReflectionUtils.findMethod(model.getClass(), StringUtilsExtended.getReadAccessor(item.getSelector()));				
				if (mth != null) {
					arg = (String) ReflectionUtils.invokeMethod(mth, model);
				}
			}
		}
		if (StringUtils.hasText(arg)) {
			return ctx.getMessage(item.getCode(), new String[]{arg}, "???", locale );					
		} else {
			return ctx.getMessage(item.getCode(), null, "???", locale );		
		}
	}
	
	/**
	 * 
	 * @param request
	 * @param cc
	 * @param model
	 * @return
	 */
	public static String getComponentInfo(HttpServletRequest request, TargetConfiguration cc, Object model) {
		return getComponentInfo(request, cc.getInfo(), model);
	}

	/**
	 * 
	 */
	public static SegmentConfiguration getConfiguration(ApplicationContext ctx, CommandWrapper wrapper) {
		return ComponentUtils.getConfiguration(ctx, wrapper.getTagName());
	}
	
	/**
	 * 
	 */
	public static SegmentConfiguration getConfiguration(ApplicationContext ctx, ComponentToken token) {
		CommandWrapper wrapper = lookupComponentWrapper(token);
		if (wrapper == null) return null;
		return getConfiguration(ctx, wrapper);
	}
	
	/**
	 * 
	 */
	public static SegmentConfiguration getConfiguration(HttpServletRequest request, CommandWrapper wrapper) {
		WebApplicationContext ctx = RequestContextUtils.getWebApplicationContext(request);
		if (ctx == null) return null;
		return ComponentUtils.getConfiguration(ctx, wrapper);
	}
	
	/**
	 * 
	 */
	public static SegmentConfiguration getConfiguration(HttpServletRequest request, ComponentToken token) {
		CommandWrapper wrapper = lookupComponentWrapper(token);
		if (wrapper == null) return null;
		return getConfiguration(request, wrapper);
	}
	
	/**
	 * 
	 */
	public static TargetConfiguration getChainConfiguration(HttpServletRequest request, ComponentToken token) {
		WebApplicationContext ctx = RequestContextUtils.getWebApplicationContext(request);
		if (ctx == null) return null;
		return getChainConfiguration(ctx, token);
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	public static TargetConfiguration getChainConfiguration(ApplicationContext ctx, ComponentToken token) {
		ChainParameter jp = RequestValueUtils.getChainParameter(token.getToken().getValue());
		String linkName = jp.getName();
		String sourceName = lookupComponentWrapper(token).getTagName();
		
		TargetConfiguration tc = getChainConfiguration(ctx, linkName, sourceName);
		if (tc != null) {
			return tc;
		}
		
		String parentName = SheptBeanFactoryUtils.getParentBeanName(ctx, sourceName);
		while (parentName != null) {
			tc = getChainConfiguration(ctx, linkName, parentName);
			if (tc != null) {
				return tc;
			}
			parentName = SheptBeanFactoryUtils.getParentBeanName(ctx, parentName);
		}

		String errStr = "No chain configuration found for link: '" 
			+ linkName + "' from source: '" + sourceName + "'";
		if (parentName != null) {
			errStr = errStr + " parent: '" + parentName + "'";
		}
		logger.error(errStr);
		return null;
	}

	/**
	 * @param ctx
	 * @param linkName
	 * @param sourceName
	 * @return
	 */
	private static TargetConfiguration getChainConfiguration(
			ApplicationContext ctx, String linkName, String sourceName) {
		// TODO introduce a ChainConfigurationHolder instead
		// ChainConfigurationHolder should do initialization checks (duplicate names ...)

		List<TargetConfiguration> chains = getChainConfigurations(ctx, sourceName);
		
		// check configuration bean names
		for (TargetConfiguration cc : chains) {
			if (StringUtils.hasText(cc.getBeanName())) {
				if (cc.getBeanName().equals(linkName)) {
					logger.info("Chain configuration found for (chain-)bean named '" + linkName + "' from source '" + sourceName + "'");
					return cc;					
				}
			}
		}
		
		// check relation values (w getter)
		String relName = linkName;
		if (! relName.startsWith("get")) {
			relName = "get" + StringUtils.capitalize(relName);
		}
		for (TargetConfiguration cc : chains) {
			if (cc instanceof ChainConfiguration) {
				String rel = ((ChainConfiguration)cc).getRelation();
				if (StringUtils.hasText(rel)) {
					if (rel.equals(relName)) {
						logger.info("Chain configuration found for relation named '" + linkName + "' from source '" + sourceName + "'");
						return cc;					
					}
				}
				
			}
		}

		// last but not least check for destination name
		for (TargetConfiguration cc : chains) {
			if (StringUtils.hasText(cc.getTo().getBeanName())) {
				if (cc.getTo().getBeanName().equals(linkName)) {
					logger.info("Chain configuration found for target bean named '" + linkName + "' from source '" + sourceName + "'");
					return cc;					
				}
			}
		}
		return null;
	}

	/**
	 * @param ctx
	 * @param sourceName
	 * @return
	 */
	protected static List<TargetConfiguration> getChainConfigurations(
			ApplicationContext ctx, String sourceName) {
		// select all configurations from the given source segment
		@SuppressWarnings("unchecked")
		List<TargetConfiguration> allChains = (List<TargetConfiguration>) ctx.getBean(SheptBean.CHAINS.getBeanName());
		List<TargetConfiguration> chains = new ArrayList<TargetConfiguration>();
		for (TargetConfiguration tC : allChains) {
			if ((tC instanceof ChainConfiguration) && (((ChainConfiguration) tC).getFrom().getBeanName().equals(sourceName))) {
				chains.add(tC);
			}
		}
		return chains;
	}

	/**
	 * 
	 */
	public static String getChainConfigurationErrorMessage(HttpServletRequest request, ComponentToken token) {
		WebApplicationContext ctx = RequestContextUtils.getWebApplicationContext(request);
		if (ctx == null) return "";
		return getChainConfigurationErrorMessage(ctx, token);
	}
	
	/**
	 * 
	 */
	public static String getChainConfigurationErrorMessage(ApplicationContext ctx, ComponentToken token) {
		SegmentConfiguration config = ComponentUtils.getConfiguration(ctx, token);
		ChainParameter jp = RequestValueUtils.getChainParameter(token.getToken().getValue());
		String linkName = jp.getName();
		return "Missing chain configuration in '" + config.getBeanName() + "' for '" + linkName + "'";
	}
	/**
	 * 
	 * @param
	 * @return the prefixed path name and append always the NESTED_PROPERTY_SEPARATOR to the end
	 *
	 * @param path
	 * @return
	 */
	public static String getPropertyPathPrefix(String pathName) {
		if (StringUtils.hasLength(pathName) && ! pathName.endsWith(PropertyAccessor.NESTED_PROPERTY_SEPARATOR)){
			pathName = pathName + PropertyAccessor.NESTED_PROPERTY_SEPARATOR;
		} 
		return pathName;
	}
	
	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param target
	 * @param pathName
	 * @return
	 */
	public static Object getComponent(Object target, String pathName) {
		Object component = null;
		if (target instanceof SubCommandProvider) {
			Map<String, CommandWrapper> pathMap = getComponentPathMap((SubCommandProvider) target);
			CommandWrapper wrapper = pathMap.get(pathName);
			component = wrapper.getCommand();
		} 
		return component;
	}
	
	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param modelAndView
	 * @return
	 */
	public static SubCommandProvider getCommand (ModelAndView modelAndView) {
		String prefix = BindingResult.MODEL_KEY_PREFIX;
		if (modelAndView == null) {
			return null;
		}
		for (String name : modelAndView.getModel().keySet()) {
			if (name.startsWith(prefix)) {
				BindingResult res = BindingResultUtils.getBindingResult(modelAndView.getModel(), 
						name.substring(prefix.length()));
				if (res != null && res.getTarget() instanceof SubCommandProvider) {
					return (SubCommandProvider) res.getTarget();
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param modelAndView
	 * @param pathName
	 * @return
	 */
	public static Object getComponent (ModelAndView modelAndView, String pathName) {
		SubCommandProvider command = getCommand(modelAndView);
		return getComponent(command, pathName);
	}
}
