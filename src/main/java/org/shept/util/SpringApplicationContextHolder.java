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

package org.shept.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 *  
 * @version $Rev$
 * @author Andreas Hahn
 * 
 * This is a helper for getting a reference to the Spring ApplicationContext from
 * outside the container.
 * 
 * @see http://sujitpal.blogspot.com/2007/03/accessing-spring-beans-from-legacy-code.html
 * 
 * In applicationContext.xml we just need a reference to self to make it work seamlessly
 * 
 *   <bean id="springApplicationContextHolder" class="org.shept.util.SpringApplicationContextHolder" />
 *
 * **** NOTE ***************************************************************************
 * Use this with caution as this approach is limited to one ApplicationContext per JVM !
 * 
 * As a workaround we might use a ThreadLocale for holding the context or
 * we might want to copy and rename this class for each individual use case,
 * e.g. MyAppContextHolder
 * 
 * **** NOTE ***************************************************************************
 */
public class SpringApplicationContextHolder implements ApplicationContextAware {

	private static ApplicationContext CONTEXT;
	
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		CONTEXT = applicationContext;
	}

	/**
	 * @return the context
	 */
	public static ApplicationContext getContext() {
		return CONTEXT;
	}
	
	

}
