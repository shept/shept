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

import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentUtils;
import org.shept.util.JarUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

/** 
 * @version $$Id: FormControlComponent.java 94 2010-12-22 15:12:29Z aha $$
 *
 * @author Andi
 *
 */
public class FormControlComponent extends AbstractComponent {

	public ModelAndView excecuteAction(HttpServletRequest request,
			HttpServletResponse response, ComponentToken token)
			throws Exception {
		String method = token.getToken().getMethod();

		if (method.equals("onClose")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Close the current and all subsequent subforms: " + token.toString());
			}
			int idx = ComponentUtils.lookupComponentIndex(token) ;
			return modelRedirect(request, token, idx - 1);
		}
		else if (method.equals("onRefresh")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Do a refresh with token: " + token.toString());
			}
			token.getBinder().bindAndValidate(new ServletWebRequest(request), token);
			return modelRedirectClip(request, token);
		}
		return modelUnhandled(request, token);	
	}

	public Map<String, String> getDefaultMappings() {
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.put("submitClose", "onClose");		// do a refresh action
		mappings.put("submitRefresh", "onRefresh");		// do a refresh action
		return mappings;
	}

	public boolean supports(Object commandObject) {
		return true;		// always supported by any component
	}
	
}
