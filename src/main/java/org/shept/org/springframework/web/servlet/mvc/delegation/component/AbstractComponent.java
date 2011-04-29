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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentUtils;
import org.shept.org.springframework.web.servlet.mvc.delegation.SubCommandProvider;
import org.shept.util.ResourceInitializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.springframework.web.servlet.view.RedirectView;

/** 
 * @version $$Id: AbstractComponent.java 110 2011-02-21 09:16:15Z aha $$
 *
 * @author Andi
 *
 */
public abstract class AbstractComponent extends WebContentGenerator implements WebComponent {
	
	public static String ANCHOR_PREFIX = "#a";
	
	private String anchorPrefix = ANCHOR_PREFIX;

	private Map<String, String> mappings = getDefaultMappings();

	private boolean copyResources = true;
	
	private ResourceInitializer[] resources;		// resources initializer

	/**
	 * @param
	 * @return
	 *
	 * @param binder
	 * @return
	 */
	protected ModelAndView modelWithErrors(DataBinder binder) {
		ModelAndView mav = new ModelAndView();
		mav.addAllObjects(binder.getBindingResult().getModel());
		return mav;
	}
	
	/**
	 * Cuts off all children beyond the given index, except when index < 0
	 */
	protected ModelAndView modelRedirect (HttpServletRequest request,  ComponentToken token, Integer index) {
		int idx = 0;
		if (index != null && index < 0) {
			// do not remove, just jump to element
			idx = ComponentUtils.lookupComponentIndex(token);
		} 
		else {
			idx = ComponentUtils.removeComponentsAfterIndex(token, index);			
		}
		String jump = "";
		if (idx > 0) {
			int lastIdx = ((SubCommandProvider) token.getCommand()).getChildren().size() -1;
			idx = Math.min(idx, lastIdx);
			jump = getAnchorPrefix() + String.valueOf(idx);
			return new ModelAndView(new RedirectView(jump, true));
		}
		return new ModelAndView (new RedirectView());
	}
	
	/**
	 * Cuts off all children beyond the last one where the command was issued
	 */
	protected ModelAndView modelRedirectClip (HttpServletRequest request, ComponentToken token) {
		return modelRedirect(request, token, null);
	}
	
	/**
	 * Cuts off all children beyond the last one where the command was issued
	 */
	protected ModelAndView modelRedirect (HttpServletRequest request, ComponentToken token) {
		return modelRedirect(request, token, -1);
	}
	
	/**
	 * 
	 */
	protected ModelAndView modelUnhandled (HttpServletRequest request,  ComponentToken token) {
		if (logger.isErrorEnabled()) {
			logger.error("The active handler " + this.getClass() +" could not handle command token " + token.toString());
		}
		return new ModelAndView (new RedirectView());
	}

	public abstract Map<String, String> getDefaultMappings();

	public boolean supportsAction(ComponentToken token) {
		return CollectionUtils.arrayToList(supportedActions()).contains(token.getToken().getMethod());
	}

	public String[] supportedActions() {
		return getMappings().values().toArray(new String[]{});
	}

	public Map<String, String> getMappings() {
		return mappings;
	}

	/**
	 * @param mappings the mappings to set
	 */
	public void setMappings(Map<String, String> mappings) {
		this.mappings = mappings;
	}

	
	protected boolean hasErrorsInPath(BindingResult errors, ComponentToken token, String suffix) {
		 
		String tokenPath = token.getToken().getPathName();
		if (tokenPath.endsWith(PropertyAccessor.NESTED_PROPERTY_SEPARATOR)) {
			tokenPath = tokenPath.substring(0,	tokenPath.lastIndexOf(PropertyAccessor.NESTED_PROPERTY_SEPARATOR));
		}
		if (StringUtils.hasText(suffix)){
			tokenPath = tokenPath + PropertyAccessor.NESTED_PROPERTY_SEPARATOR + suffix;
		}
		List<FieldError> err = errors.getFieldErrors();
		for (FieldError fe : err) {
			if (fe.getField().startsWith(tokenPath)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return the anchorPrefix
	 */
	public String getAnchorPrefix() {
		return anchorPrefix;
	}

	/**
	 * @param anchorPrefix the anchorPrefix to set
	 */
	public void setAnchorPrefix(String anchorPrefix) {
		this.anchorPrefix = anchorPrefix;
	}

	@Override
	protected void initApplicationContext() throws BeansException {
		super.initApplicationContext();
		if (isCopyResources()) {
			if (resources != null) {
				for (int i = 0; i < resources.length; i++) {
					resources[i].initializeResources(getServletContext());
				}
			}
		}
	}

	/**
	 * @return the copyResources
	 */
	public boolean isCopyResources() {
		return copyResources;
	}

	/**
	 * @param copyResources the copyResources to set
	 */
	public void setCopyResources(boolean copyResources) {
		this.copyResources = copyResources;
	}

	/**
	 * @return the resources
	 */
	public ResourceInitializer[] getResources() {
		return resources;
	}

	/**
	 * @param resources the resources to set
	 */
	public void setResources(ResourceInitializer[] resources) {
		this.resources = resources;
	}


}
