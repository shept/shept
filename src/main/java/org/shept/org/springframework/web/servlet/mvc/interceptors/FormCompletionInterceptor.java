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

package org.shept.org.springframework.web.servlet.mvc.interceptors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindingResult;
import org.springframework.validation.BindingResultUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.RedirectView;

/** 
 * @version $$Id: FormCompletionInterceptor.java,v 1.1 2009/11/27 18:53:12 oops.oops Exp $$
 *
 * @author Andi
 *
 */
public class FormCompletionInterceptor extends HandlerInterceptorAdapter {
	
	public static final String ERROR_MSG = "formErrorMessage";

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.handler.HandlerInterceptorAdapter#postHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.web.servlet.ModelAndView)
	 */
	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		// do not apply model attributes to redirections
		if (modelAndView == null || modelAndView.getView() instanceof RedirectView) {
			return;
		}

		RequestContext rc = new RequestContext(request);
		BindingResult res = null;
		
		for (String name : modelAndView.getModel().keySet()) {
			if (name.startsWith(BindingResult.MODEL_KEY_PREFIX)) {
				res = BindingResultUtils.getBindingResult(modelAndView.getModel(), name.substring(BindingResult.MODEL_KEY_PREFIX.length()));
			}
		}
		
		HashMap<String, String> controlModel = new HashMap<String, String>();
		String errStr = "";
		if (res != null && res.hasErrors()) {
			// list errors
			@SuppressWarnings("rawtypes")
			List errList = res.getAllErrors();
			Iterator it = errList.iterator();
			if (it.hasNext()) {
				ObjectError error = (ObjectError) it.next();
				String myErrCode = error.getCode();
				// check all possible Errorcodes
				for (String eStr : error.getCodes()) {
					String errMsg = rc.getMessage(eStr, "");
					if (errMsg.length() > 0) {
						myErrCode = eStr;
						break;
					}
				}
				errStr = errStr
						+ rc.getMessage(myErrCode, error.getArguments(), error
								.getDefaultMessage());
				// rc.getMessage(error.getCode(), error.rejectedValue,
				// error.getDefaultMessage());
			}
			if (it.hasNext()) {
				errStr = errStr + "  ...";
			}
		}

		controlModel.put(ERROR_MSG, errStr);
		
		modelAndView.addAllObjects(controlModel);
	}
	
}
