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

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * @author lucid64
 *
 */
public class SheptBeanFactoryUtils {

	/**
	 * @param ctx
	 * @param sourceName
	 */
	public static String getParentBeanName(ApplicationContext ctx, String beanName) {
		ApplicationContext pc = getRootContext(ctx);
		AutowireCapableBeanFactory factory = pc.getAutowireCapableBeanFactory();
		if (factory instanceof DefaultListableBeanFactory) {
			BeanDefinition def = ((DefaultListableBeanFactory)  factory).getBeanDefinition(beanName);
			if (def != null) {
				return def.getParentName();
			}
			
		}
		return null;
	}
	
	public static ApplicationContext getRootContext(ApplicationContext ctx) {
		if (ctx.getParent() == null) {
			return ctx;
		}
		return ctx.getParent();
	}


}
