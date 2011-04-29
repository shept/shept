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
import javax.servlet.http.HttpServletResponse;

import org.shept.org.springframework.beans.support.CommandWrapper;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentUtils;
import org.shept.org.springframework.web.servlet.mvc.support.RequestValueUtils;
import org.shept.org.springframework.web.servlet.mvc.support.RequestValueUtils.KeyValueParameter;
import org.springframework.web.servlet.ModelAndView;

/** 
 * @version $$Id: PropertyEditorComponent.java 110 2011-02-21 09:16:15Z aha $$
 *
 * @author Andi
 *
 */
public class PropertyEditorComponent extends AbstractComponent implements WebComponent {
	
	public ModelAndView excecuteAction(HttpServletRequest request,
			HttpServletResponse response, ComponentToken token) throws Exception {
		
		return doActionInternal( request, token);
	}

	protected ModelAndView doActionInternal(HttpServletRequest request,  ComponentToken token) throws Exception {
		
		String method = token.getToken().getMethod();
		Map<String, Object > properties = null;
		
		CommandWrapper wrapper = ComponentUtils.lookupComponentWrapper(token); 
		if (wrapper != null) {
			properties = wrapper.getProperties();				
		} else {
			if (logger.isErrorEnabled()) {
				logger.error("Could not find the command wrapper. This is an internal programming error on token: " + token.toString());
			}
			return modelUnhandled(request, token);			
		}
		
		KeyValueParameter kvp = RequestValueUtils.getKeyValueParameter(token.getToken().getValue());
		if (method.equals("onPropertyReset")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Property Reset operation: " + token.toString());
			}
			properties.remove(kvp.getKey());
			if (logger.isDebugEnabled()) {
				logger.debug("Property  " + kvp.getKey() + " successfully removed, Token: " + token.toString());
			}
			return modelRedirect(request, token);
			
		} else if (method.equals("onPropertySet")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Property Set operation: " + token.toString());
			}
			properties.put(kvp.getKey(),  kvp.getValue());
			if (logger.isDebugEnabled()) {
				logger.debug("Property  " + kvp.getKey() + " set to value " + kvp.getKey().toString() +" , Token: " + token.toString());
			}
			return modelRedirect(request, token);

		} else if (method.equals("onPropertyToggle")) {
			Object value = properties.get(kvp.getKey());
			if (value instanceof Boolean) {
				properties.put(kvp.getKey(), ! ((Boolean) value));
				if (logger.isDebugEnabled()) {
					logger.debug("Boolean value of property " + kvp.getKey() + " successfully toggled to " + !(Boolean)  value + " Token: " + token.toString());
				}
			} else if (logger.isErrorEnabled()) {
				logger.error("Could not toggle Boolean value of property " + kvp.getKey() + " because value is not of type Boolean: " + token.toString());
			}
			return modelRedirect(request, token);

		} 		
		return modelUnhandled(request, token);
	}

	public Map<String, String> getDefaultMappings() {
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.put("submitPropertyReset", "onPropertyReset");		// reset a property
		mappings.put("submitPropertySet", "onPropertySet");				// set a property
		mappings.put("submitPropertyToggle", "onPropertyToggle");	// toggle a property
		return mappings;
	}

	public boolean supports(Object commandObject) {
		return true;
	}

}
