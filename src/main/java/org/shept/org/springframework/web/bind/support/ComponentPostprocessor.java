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

package org.shept.org.springframework.web.bind.support;

import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentUtils;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

/** 
 * @version $$Id: ComponentPostprocessor.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public interface ComponentPostprocessor {

	/**
	 * Initialize a Postprocessor for the given request.
	 * Typical use cases are population of selection options
	 * or showing aggregations of the list (sums of elements, totals, ...)
	 * An implementation can retrieve the segment from the componentPath
	 * example:
	 * <code>
	 * PageableList<SomeEntity> list = (PageableList<SomeEntity>) ComponentUtils.getComponent(modelAndView, componentPath);
	 * // finally contribute to modelAndView
	 * if (modelAndView != null) { // should be checked because the same segment may occur multiple times
	 *	modelAndView("someName", someObject);
	 * }
	 * </code>
	 * 
	 * There are lots of examples in the shept demo implementations.
	 * Postprocessors should usually be located in the ../web/controller/postprocessors package of your app.
	 * 
	 * @param request the web request that the data binding happens within
	 * @param modelAndView the ModelAndView
	 * @param componentPath the path within the bound object
	 */
	void postHandle(WebRequest request, ModelAndView modelAndView, String componentPath );

}
