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

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shept.beans.support.FilterDefinition;
import org.shept.org.springframework.beans.CustomCalendarEditor;
import org.shept.org.springframework.beans.support.CommandWrapper;
import org.shept.org.springframework.beans.support.FilteredListHolder;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentUtils;
import org.shept.org.springframework.web.servlet.mvc.delegation.SubCommandProvider;
import org.shept.org.springframework.web.servlet.mvc.support.ConfigurableLocaleDependentFormatResolver;
import org.shept.org.springframework.web.servlet.mvc.support.DateTimeLocaleConstants;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.support.RequestContextUtils;

/** 
 * @version $$Id: FilterBindingInitializer.java 70 2010-11-04 17:28:46Z aha $$
 *
 * @author Andi
 *
 */
public class FilterBindingInitializer implements ComponentBindingInitializer  {
	
	
	/** Logger that is available to subclasses */
	protected static final Log logger = LogFactory.getLog(FilterBindingInitializer.class);
	
	/** specify a max depth to prevent recursive initializations */
	protected static final Integer MAX_DEPTH = 5;

	private Integer maxDepth = MAX_DEPTH;
	
	private ConfigurableLocaleDependentFormatResolver formatResolver;
		
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.bind.support.ConfigurableWebBindingInitializer#initBinder(org.springframework.web.bind.WebDataBinder,
	 *      org.springframework.web.context.request.WebRequest)
	 */
	public void initBinder(WebRequest request, ComponentDataBinder binder, String componentPath) {
		if (logger.isInfoEnabled()) {
			logger.info("Register custom binding initializers");
		}

		// Locale dependent registry - we should improve that - use java.text.spi ?
		Locale locale = RequestContextUtils.getLocale(((ServletWebRequest)request).getRequest());
		
		String format = formatResolver.resolveProperty(locale, DateTimeLocaleConstants.DATETIME_FORMAT_LONG);

		// register 'nullable' bean editors for all Strings within filter  objects
		// This way empty fields will not be part of the search criteria

		Map<String, CommandWrapper> pathMap = Collections.emptyMap();
		if (binder.getTarget() instanceof SubCommandProvider) {
			pathMap = ComponentUtils.getComponentPathMap((SubCommandProvider)binder.getTarget());
			CommandWrapper wrapper = pathMap.get(componentPath);
			if (wrapper.getCommand() instanceof FilteredListHolder) {
				FilteredListHolder flh = (FilteredListHolder) wrapper.getCommand();
				FilterDefinition filterDef = flh.getFilter();
				String path = 	ComponentUtils.getPropertyPathPrefix(componentPath) +
					FilteredListHolder.FILTER_BINDING_NAME + PropertyAccessor.NESTED_PROPERTY_SEPARATOR;

				for (Field field : filterDef.getClass().getDeclaredFields()) {
					String propPath = path + field.getName();
					if (field.getType().equals(String.class)) {
						binder.registerCustomEditor(String.class, propPath,
								new StringTrimmerEditor(true));
						if (logger.isInfoEnabled()) {
							logger.info("Registered nullable StringEditor for "
									+ propPath);
						}
					} else if (field.getType().equals(Calendar.class)) {
						format = formatResolver.resolveProperty(locale, DateTimeLocaleConstants.DATE_FORMAT_SHORT);
						binder.registerCustomEditor(Calendar.class, propPath,
								new CustomCalendarEditor(new SimpleDateFormat(
										format), true));
						if (logger.isInfoEnabled()) {
							logger.info("Registered Calendar Editor for "
									+ propPath);
						}
					} else {
						registerDependendEntities(binder, field.getType(), path + field.getName());
					}
				}
			}
		}		
	}
	
	private void registerDependendEntities(ComponentDataBinder binder, Class clazz, String path) {
		Object ann = AnnotationUtils.findAnnotation(clazz, Entity.class);
		if (ann == null) return;
		for (Field field : clazz.getDeclaredFields()) {
			String propPath = path + PropertyAccessor.NESTED_PROPERTY_SEPARATOR + field.getName();
			if (field.getType().equals(String.class)) {
				binder.registerCustomEditor(String.class, propPath,
						new StringTrimmerEditor(true));
				if (logger.isInfoEnabled()) {
					logger.info("Registered nullable StringEditor for "
							+ propPath);
				}
			} else {
				// here we need to prevent infinte recursions for example if a user contains a user contains a user ...
				Integer depth = StringUtils.countOccurrencesOf(path, PropertyAccessor.NESTED_PROPERTY_SEPARATOR);
				if ( depth <= maxDepth) {
					registerDependendEntities(binder, field.getType(), propPath);	
				}				
			}
		}
	}

	/**
	 * @return the formatResolver
	 */
	public ConfigurableLocaleDependentFormatResolver getFormatResolver() {
		return formatResolver;
	}

	/**
	 * @param formatResolver the formatResolver to set
	 */
	public void setFormatResolver(
			ConfigurableLocaleDependentFormatResolver formatResolver) {
		this.formatResolver = formatResolver;
	}

	/**
	 * @param maxDepth the maxDepth to set
	 */
	public void setMaxDepth(Integer maxDepth) {
		this.maxDepth = maxDepth;
	}

}
