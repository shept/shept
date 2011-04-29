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

import javax.servlet.http.HttpServletRequest;

import org.shept.org.springframework.web.bind.support.ComponentDataBinder;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentUtils;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.SegmentConfiguration;
import org.springframework.validation.Validator;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

/** 
 * @version $$Id: AbstractEditingComponent.java 94 2010-12-22 15:12:29Z aha $$
 *
 * @author Andi
 *
 */
public abstract class AbstractEditingComponent extends AbstractPersistenceComponent {

	private Validator[] validators;
	
	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param request
	 * @param token
	 * @param binder
	 * @return
	 * @throws Exception
	 */
	protected ModelAndView doActionInternal(HttpServletRequest request,  ComponentToken token) throws Exception {
		
		String method = token.getToken().getMethod();
		
		if (method.equals("onCancel")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Cancelled request: " + token.toString());
			}
			return doCancel(request, token);
			
		} else if (method.equals("onSave")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Save request " + (this.enableSave ? "" : "BLOCKED") + ": " + token.toString());
			}
			if (this.enableSave) {
				ComponentDataBinder binder = (ComponentDataBinder) token.getBinder();
				binder.bindAndValidate(new ServletWebRequest(request), token);
				onBindAndValidate(request, token);
				
				if (hasErrorsInPath(binder.getBindingResult(), token, "") ||
					binder.getBindingResult().getGlobalErrorCount() > 0) {
					ModelAndView mav = new ModelAndView();
					mav.addAllObjects(binder.getBindingResult().getModel());
					return mav;
				}
				return doSave(request, token);
			}
			return modelRedirectClip(request, token);

		
		} else if (method.equals("onDelete")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Delete request " + (this.enableDelete ? "" : "BLOCKED") + ": " + token.toString());
			}
			if (this.enableDelete) {
				return doDelete (request, token);
			}
			return modelRedirectClip(request, token);			
		} 
		
		return modelUnhandled(request, token);
	}

	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param request
	 * @param token
	 * @throws Exception
	 */
	protected void onBindAndValidate(HttpServletRequest request, ComponentToken token) throws Exception {};

	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param request
	 * @param token
	 * @param binder
	 * @return
	 */
	abstract protected ModelAndView doCancel(HttpServletRequest request, ComponentToken token) throws Exception;
	
	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param request
	 * @param token
	 * @param binder
	 * @return
	 */
	abstract protected ModelAndView doSave(HttpServletRequest request, ComponentToken token) throws Exception;
	
	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param request
	 * @param token
	 * @param binder
	 * @return
	 */
	abstract protected ModelAndView doDelete(HttpServletRequest request, ComponentToken token) throws Exception;
	
	/**
	 * 
	 */
	protected String getCustomSaveTransaction(HttpServletRequest request, ComponentToken token) {
		// check submission for a transactional parameter
		SegmentConfiguration config = ComponentUtils.getConfiguration(request, token);
		if (config != null && config.getTransaction() != null ) {
			return config.getTransaction().getSave();
		}
		return null;
	}

	/**
	 * 
	 */
	protected String getCustomDeleteTransaction(HttpServletRequest request, ComponentToken token) {
		// check submission for a transactional parameter
		SegmentConfiguration config = ComponentUtils.getConfiguration(request, token);
		if (config != null && config.getTransaction() != null ) {
			return config.getTransaction().getDelete();
		}
		return null;
	}

	public Map<String, String> getDefaultMappings() {
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.put("submitCancel", "onCancel");				// resets the visible page columns to the original state (=cancels edits)
		mappings.put("submitSave", "onSave");						// promotes all changes to the pageColumns to the underlying persitence provider
		mappings.put("submitDelete", "onDelete");				// promotes all deletions of pageColumns to the underlying persistence provider
		return mappings;
	}

	/**
	 * @return the validators
	 */
	public Validator[] getValidators() {
		return validators;
	}

	/**
	 * @param validators the validators to set
	 */
	public void setValidators(Validator[] validators) {
		this.validators = validators;
	}

}
