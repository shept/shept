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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shept.org.springframework.web.servlet.mvc.delegation.component.WebComponent;
import org.springframework.web.util.WebUtils;

/**
s * 
 * @version $$Id: SubmitActionResolver.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */

public class SubmitActionResolver  implements WebActionResolver {


	protected final Log logger = LogFactory.getLog(getClass());
	
	
	private Map<WebComponent, Map<String, String>> registeredHandlerMappings = 
		new HashMap<WebComponent, Map<String, String>>();

	
	/* 
	 * (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.delegation.WebActionResolver#getAction(javax.servlet.http.HttpServletRequest, java.util.Set, java.util.Map)
	 * 
	 * Scan the current request for a command and determine the action and component to handle that current command.
	 * 
	 */
	public WebActionToken getAction(HttpServletRequest request, Set<String> componentPathName, Map<String, List<WebComponent>> handlersForPath ) {
		if (componentPathName == null) {
			componentPathName = new HashSet<String>();
		}
		LinkedList<String> pathNames = new LinkedList<String>();
		pathNames.addAll(componentPathName);
		// sort longest strings to be searched first !
		Collections.sort(pathNames, 
				new Comparator<String>() {
					public int compare(String o1, String o2) {
						return Integer.valueOf(o2.length()).compareTo(o1.length());
					}
				});
			for (String pathCandidate : pathNames) {
				Map<String, Object> params = WebUtils.getParametersStartingWith(request, pathCandidate);
				if (params.size() > 0 ) {
					List<WebComponent> handlers = handlersForPath.get(pathCandidate);
					if (handlers != null ) {
						for (WebComponent handler : handlers) {
							Map<String, String> mappings = handler.getMappings();
							if (mappings == null) {
								throw new IllegalStateException("Configuration error for component " + handler.getClass() + 
										" Submission parameters are not mapped to Handlers action (excecution) methods" );
							}
							for(String candidate : mappings.keySet()) {
								String parameterValue = WebUtils.findParameterValue(params, candidate);
								if (parameterValue != null) {
									WebActionToken token = new WebActionToken();
									token.setHandler(handler);
									token.setPathName(pathCandidate);
									token.setMethod(getMappings(handler).get(candidate));
									token.setValue(parameterValue);
									if (logger.isDebugEnabled()) {
										logger.debug("Determined token: " + token );
									}
									return token;
								}
							}
						}
					}
				}
			}
			return null; // no action found - it's not a form submission
		}


	
	protected Map<String, String> getMappings (WebComponent handler) {
		Map<String, String> mappings = this.registeredHandlerMappings.get(handler);
		if (mappings == null) {
			mappings = handler.getMappings();
		}
		return mappings;
	}


	/**
	 * @param registeredHandlerMappings the registeredHandlerMappings to set
	 */
	public void setRegisteredHandlerMappings(
			Map<WebComponent, Map<String, String>> registeredHandlerMappings) {
		this.registeredHandlerMappings = registeredHandlerMappings;
	}

}
