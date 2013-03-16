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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.shept.org.springframework.beans.support.FilterType;
import org.shept.org.springframework.beans.support.PageableList;
import org.shept.org.springframework.beans.support.Refreshable;
import org.shept.org.springframework.web.bind.support.ComponentDataBinder;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

/** 
 * @version $$Id: FilteredListComponent.java 110 2011-02-21 09:16:15Z aha $$
 *
 * @author Andi
 *
 */
public class FilteredListComponent extends AbstractComponent implements WebComponent {
	
	protected int newModelSize = PageableList.DEFAULT_NEW_MODEL_SIZE;

	public ModelAndView excecuteAction(HttpServletRequest request,
			HttpServletResponse response, ComponentToken token) throws Exception {
		
		return doActionInternal( request, token);
	}

	protected ModelAndView doActionInternal(HttpServletRequest request,  ComponentToken token) throws Exception {
		
		String method = token.getToken().getMethod();
		Refreshable filter = (Refreshable) token.getComponent();
		PageableList<?> pagedList = (PageableList<?>) token.getComponent();
		
		ComponentDataBinder binder = (ComponentDataBinder) token.getBinder();
		
		if (method.equals("onFilterCancel")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Cancel filter operation: " + token.toString());
			}
			binder.bindAndValidate(new ServletWebRequest(request), token);
			if ( hasErrorsInPath(binder.getBindingResult(), token, Refreshable.FILTER_BINDING_NAME )) {
				filter.setUseFilter(FilterType.FILTER_LAST_USED);
			} else {
				filter.setUseFilter(FilterType.FILTER_INITIAL);
			}
			pagedList.setPage(0);
			filter.refresh();
			return modelRedirectClip(request, token);

			// clear errors = use binder to return the default model (without binding the request)
//			ServletRequestDataBinder binder = new ServletRequestDataBinder(command, errors.getObjectName());
//			initBinder(binder, new ServletWebRequest(request));
			//			return redirectView
			
		} else if (method.equals("onFilter")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Excecute filter: " + token.toString());
			}
			binder.bindAndValidate(new ServletWebRequest(request), token);
			if ( hasErrorsInPath(binder.getBindingResult(), token, Refreshable.FILTER_BINDING_NAME)) {
				return modelWithErrors(binder);
			}
			pagedList.setPage(0);
			filter.refresh();
			return modelRedirectClip(request, token);
			
		} else if (method.equals("onAddTransient")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Add transient model(s): " + token.toString());
			}
			int savedSize = pagedList.getNewModelSize();
			pagedList.setNewModelSize(newModelSize);
			List src = pagedList.getSource();
			pagedList.setSource(src);
			pagedList.setPage(0);
			pagedList.setNewModelSize(savedSize);
			return modelRedirectClip(request, token);

		} 		
		return modelUnhandled(request, token);
	}

	public Map<String, String> getDefaultMappings() {
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.put("submitFilter", "onFilter");				// applies the values of the FilterDefinition and reloads the page data
		mappings.put("submitFilterReset", "onFilterCancel");	// resets the filter definition to the original state or in case of errors to the state before the error
		mappings.put("submitAddTransient", "onAddTransient");	// adds one transient model in front of all others
		return mappings;
	}

	public boolean supports(Object commandObject) {
		return commandObject instanceof PageableList && 
			commandObject instanceof Refreshable;
	}

	/**
	 * @return the newModelSize
	 */
	public int getNewModelSize() {
		return newModelSize;
	}

	/**
	 * @param newModelSize the newModelSize to set
	 */
	public void setNewModelSize(int newModelSize) {
		this.newModelSize = newModelSize;
	}

}
