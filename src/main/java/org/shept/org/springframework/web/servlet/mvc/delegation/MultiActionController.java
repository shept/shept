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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.shept.org.springframework.web.bind.support.CommandObjectProvider;
import org.shept.org.springframework.web.servlet.mvc.formcache.LastModifiedSessionFormCache;
import org.shept.org.springframework.web.servlet.mvc.formcache.SessionFormCache;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/** 
 * @version $$Id: MultiActionController.java 110 2011-02-21 09:16:15Z aha $$
 *
 * @author Andi
 *
 */
public class MultiActionController extends DelegatingController {

	private static String CLEAR_FORM_CACHE_PARAM = "clearCache";
	
	private static String FORM_TITLE_MODEL_ATTRIBUTE = "formTitle";
	
	protected String clearCacheParam = CLEAR_FORM_CACHE_PARAM;
	
	protected boolean enableClearCache = true;
	
	protected SessionFormCache formCache = new LastModifiedSessionFormCache();
	
	protected CommandObjectProvider objectProvider;
	
	private String formTitleAttribute = FORM_TITLE_MODEL_ATTRIBUTE;
	
	private String titleCode = "";
	
	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.delegation.DelegatingController#createCommandObject(javax.servlet.http.HttpServletRequest, java.lang.Class)
	 */
	@Override
	protected Object getCommandObject(HttpServletRequest request, Class clazz)
			throws Exception {
		Object sessionFormObject = formCache.getForm(request, getRedirectUrl());
		if (sessionFormObject == null) {
			sessionFormObject = buildCommandObject(request);
			formCache.saveForm(request, getRedirectUrl(), sessionFormObject);
		} 
		return sessionFormObject;
	}
	
	protected Object buildCommandObject(HttpServletRequest request) {
		Object command = objectProvider.getObject(request);
		if (command == null) {
			throw new UnsupportedOperationException("Object Provider returned a null object " +
					"during creation of a new object model in controller handling " + getRedirectUrl());
		}
		return command;
	}
 
	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.delegation.DelegatingController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String clear = ServletRequestUtils.getStringParameter(request, clearCacheParam,"");
		if (StringUtils.hasText(clear) && enableClearCache) {
			formCache.clearCache(request);
			return new ModelAndView(new RedirectView(request.getRequestURI()));
		}
		return super.handleRequestInternal(request, response);
	}

	@Override
	protected void postProcessModel(HttpServletRequest request, ModelAndView mav) throws Exception {
		super.postProcessModel(request, mav);
		if (StringUtils.hasText(titleCode)) {
			String title = getMessageSourceAccessor().getMessage(titleCode);
			mav.addObject(formTitleAttribute, title);
			
		}
	}

	/**
	 * @return the sessionFormCache
	 */
	public SessionFormCache getFormCache() {
		return formCache;
	}


	/**
	 * @param sessionFormCache the sessionFormCache to set
	 */
	public void setFormCache(SessionFormCache formCache) {
		this.formCache = formCache;
	}


	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.delegation.DelegatingController#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public long getLastModified(HttpServletRequest request) {
		return formCache.getLastModified(request, getRedirectUrl());
	}

	/**
	 * @param objectProvider the objectProvider to set
	 */
	public void setObjectProvider(CommandObjectProvider objectProvider) {
		this.objectProvider = objectProvider;
	}

	/**
	 * @return the clearCacheParam
	 */
	public String getClearCacheParam() {
		return clearCacheParam;
	}

	/**
	 * @param clearCacheParam the clearCacheParam to set
	 */
	public void setClearCacheParam(String clearCacheParam) {
		this.clearCacheParam = clearCacheParam;
	}

	/**
	 * @param enableClearCache the enableClearCache to set
	 */
	public void setEnableClearCache(boolean enableClearCache) {
		this.enableClearCache = enableClearCache;
	}

	/**
	 * @param titleCode the titleCode to set
	 */
	public void setTitleCode(String titleCode) {
		this.titleCode = titleCode;
	}

	/**
	 * @return the formTitleAttribute
	 */
	public String getFormTitleAttribute() {
		return formTitleAttribute;
	}

	/**
	 * @param formTitleAttribute the formTitleAttribute to set
	 */
	public void setFormTitleAttribute(String formTitleAttribute) {
		this.formTitleAttribute = formTitleAttribute;
	}

}
