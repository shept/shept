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
import org.shept.org.springframework.web.servlet.mvc.formcache.SessionFormCache;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/** 
 * @version $$Id: LoginComponent.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class LoginComponent extends AbstractComponent {
	
	private SessionFormCache formCache;
	
	private boolean clearFormCacheOnLogin = true;

	public ModelAndView excecuteAction(HttpServletRequest request,
			HttpServletResponse response, ComponentToken token)
			throws Exception {
		String method = token.getToken().getMethod();

		if (method.equals("onLogin")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Login with token: " + token.toString());
			}
			Map<String, String> parms = new HashMap<String, String>();
			String usr = "j_username";
			String pwd = "j_password";
			parms.put(usr, ServletRequestUtils.getStringParameter(request, usr,""));
			parms.put(pwd, ServletRequestUtils.getStringParameter(request, pwd,""));
			if (getFormCache() != null && isClearFormCacheOnLogin()) {
				getFormCache().clearCache(request);		// clear the saved forms				
			}
			return new ModelAndView(new RedirectView("j_spring_security_check"),parms);
		}
		return modelUnhandled(request, token);	
	}

	public Map<String, String> getDefaultMappings() {
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.put("submitLogin", "onLogin");		// do login
		return mappings;
	}

	public boolean supports(Object commandObject) {
		return true;		// always supported by any component
	}

	/**
	 * @return the formCache
	 */
	public SessionFormCache getFormCache() {
		return formCache;
	}

	/**
	 * @param formCache the formCache to set
	 */
	public void setFormCache(SessionFormCache formCache) {
		this.formCache = formCache;
	}

	/**
	 * @return the clearFormCacheOnLogin
	 */
	public boolean isClearFormCacheOnLogin() {
		return clearFormCacheOnLogin;
	}

	/**
	 * @param clearFormCacheOnLogin the clearFormCacheOnLogin to set
	 */
	public void setClearFormCacheOnLogin(boolean clearFormCacheOnLogin) {
		this.clearFormCacheOnLogin = clearFormCacheOnLogin;
	}

}
