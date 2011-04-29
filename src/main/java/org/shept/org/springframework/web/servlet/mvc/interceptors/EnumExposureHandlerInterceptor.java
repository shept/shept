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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/** 
 * @version $$Id: EnumExposureHandlerInterceptor.java,v 1.1 2009/11/27 18:53:21 oops.oops Exp $$
 *
 * @author Andi
 * 
 *  Copied/Derived from http://tadaya.wordpress.com/2008/07/28/expose-java5-enum-to-jsp-jstl/#
 *
 */
public class EnumExposureHandlerInterceptor extends HandlerInterceptorAdapter {

	@SuppressWarnings("rawtypes")
	private Set<Class<? extends Enum>> enumClasses = new HashSet<Class<? extends Enum>>();

    @SuppressWarnings("rawtypes")
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

        for (Class<? extends Enum> enumClass : enumClasses) {
            final String enumClassName = enumClass.getSimpleName();  // class of enum : Language
            final Enum[] enums = enumClass.getEnumConstants();  // enum instances : EN, JP,...

            // put them into map, so that we can access like this : Language.EN, Language.JP ...
            Map<String, Enum> map = new HashMap<String, Enum>(enums.length);
            for (Enum anEnum : enums) {
                map.put(anEnum.name(), anEnum);
            }

            // setting to scope
            request.setAttribute(enumClassName, map);
        }

    }

    public void setEnumClasses(@SuppressWarnings("rawtypes") Set<Class<? extends Enum>> enumClasses) {
        this.enumClasses = enumClasses;
    }
}
