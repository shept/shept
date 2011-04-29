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

package org.shept.org.springframework.web.servlet.mvc.formcache;

import javax.servlet.http.HttpServletRequest;

/** 
 * <p> This Cache emulates the way that the AbstractFormController handled form caching.
 * The {@link org.shept.org.springframework.web.servlet.mvc.classic.AbstractFormController} 
 * handled form caching by saving just the current form in the session. The form was 
 * removed from the session when obtaining the command object. When the workflow
 * through the Controller completed, {@link org.shept.org.springframework.web.servlet.mvc.classic.AbstractFormController#showForm}
 * exposed the form again to the session. This was a somewhat inflexible design. It makes sparse use of session memory but 
 * this was hardcoded and it didn't allow for a configurable stategy where
 * session memory resources are not a bottleneck. </p>  
 * 
 * @version $$Id: ReplacementSessionFormCache.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class ReplacementSessionFormCache extends SimpleSessionFormCache {
	
	private boolean keepForms = false;		

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.SessionFormCache#getForm(javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public Object getForm(HttpServletRequest request,
			String formAttrName)  {

		Object sessionFormObject = super.getForm(request, formAttrName);

		// Remove form object from HTTP session: we might finish the form workflow
		// in this request. If it turns out that we need to show the form view again,
		// we'll re-bind the form object to the HTTP session.
		if ( ! isKeepForms() ) {
			if (logger.isDebugEnabled()) {
				logger.debug("Removing form from session form cache [" + formAttrName + "]");
			}
			super.saveForm(request, formAttrName, null);
		}
		
		return sessionFormObject;
	}

	public boolean isKeepForms() {
		return keepForms;
	}

	public void setKeepForms(boolean keepForms) {
		this.keepForms = keepForms;
	}

}
