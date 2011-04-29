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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.shept.org.springframework.beans.CustomCalendarEditor;
import org.shept.org.springframework.beans.PropertyEditorHolder;
import org.shept.org.springframework.beans.support.CommandWrapper;
import org.shept.org.springframework.beans.support.FilteredListHolder;
import org.shept.org.springframework.beans.support.PageableList;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentUtils;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentValidator;
import org.shept.org.springframework.web.servlet.mvc.delegation.SubCommandProvider;
import org.shept.org.springframework.web.servlet.mvc.support.ConfigurableLocaleDependentFormatResolver;
import org.shept.org.springframework.web.servlet.mvc.support.DateTimeLocaleConstants;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.support.RequestContextUtils;

/** 
 * @version $$Id: DefaultComponentBindingInitializer.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class DefaultComponentBindingInitializer implements ComponentBindingInitializer, InitializingBean {
	
	private ConfigurableLocaleDependentFormatResolver formatResolver;

	private Map<String, DateTimeLocaleConstants> dateEditors;

	private Map<String, DateTimeLocaleConstants> calendarEditors;

	private Map<String, PropertyEditorHolder> customEditors;
	
	private Map<Class<?>, ComponentBindingInitializer> initializersForType;
	
	private ComponentValidator validator;

	private boolean pageableListDetection = true;
	
	private boolean filterDetection = true;
	
	public void initBinder(WebRequest request, ComponentDataBinder binder, String componentPath) {
		Locale locale = RequestContextUtils.getLocale(((ServletWebRequest)request).getRequest());

		Map<String, CommandWrapper> pathMap = Collections.emptyMap();
		if (binder.getTarget() instanceof SubCommandProvider) {
			pathMap = ComponentUtils.getComponentPathMap((SubCommandProvider)binder.getTarget());
		}
		
		if (getCustomEditors() != null) {
			for (Entry<String, PropertyEditorHolder> editorEntry : getCustomEditors().entrySet()) {
				String fieldPath = getFieldPath(componentPath, pathMap);
				binder.registerCustomEditor(
						editorEntry.getValue().getRegisteredType(),
						fieldPath + editorEntry.getKey(), 
						editorEntry.getValue().getPropertyEditor());
			}			
		}
		if (getDateEditors() != null) {
			for (Entry<String, DateTimeLocaleConstants> localeEntry : getDateEditors().entrySet()) {
				String fieldPath = getFieldPath(componentPath, pathMap);
				String format = formatResolver.resolveProperty(locale, localeEntry.getValue());				
				binder.registerCustomEditor(
						Date.class,
						fieldPath + localeEntry.getKey(), 
						 new CustomDateEditor(
								 new SimpleDateFormat(format), true));
			}			
		}
		if (getCalendarEditors() != null) {
			for (Entry<String, DateTimeLocaleConstants> localeEntry : getCalendarEditors().entrySet()) {
				String fieldPath = getFieldPath(componentPath, pathMap);
				String format = formatResolver.resolveProperty(locale, localeEntry.getValue());
				binder.registerCustomEditor(
						Calendar.class,
						fieldPath + localeEntry.getKey(), 
						 new CustomCalendarEditor(
								 new SimpleDateFormat(format), true));
			}			
		}
		if (getInitializersForType() != null) {
			for (Entry<Class<?>, ComponentBindingInitializer> typeInitializer : getInitializersForType().entrySet()) {
				CommandWrapper component = pathMap.get(componentPath);
				if (typeInitializer.getKey().isAssignableFrom(component.getCommand().getClass())) {
					typeInitializer.getValue().initBinder(request, binder, componentPath);
				}
			}
		}
		
		if (validator != null) {
			binder.setValidator(componentPath, validator);
		}
	}
	
	protected String getFieldPath(String path, Map<String, CommandWrapper> pathMap) {
		String fieldPath = "";
		if (StringUtils.hasText(path)) {
			fieldPath = ComponentUtils.getPropertyPathPrefix(path);
			CommandWrapper wrapper = pathMap.get(path);
			if (wrapper != null) {
				if (wrapper.getCommand() instanceof PageableList && pageableListDetection) {
					fieldPath = fieldPath + PageableList.LIST_BINDING_NAME + PropertyAccessor.NESTED_PROPERTY_SEPARATOR;
				}
			}
		}
		return fieldPath;
	}

	/**
	 * @return the customEditors
	 */
	public Map<String, PropertyEditorHolder> getCustomEditors() {
		if (this.customEditors == null) {
			setCustomEditors(new LinkedHashMap<String, PropertyEditorHolder>(1));
		}
		return this.customEditors;
	}

	/**
	 * @param customEditors the customEditors to set
	 */
	public void setCustomEditors(
			Map<String, PropertyEditorHolder> customEditorsForPath) {
		this.customEditors = customEditorsForPath;
	}

	/**
	 * @return the dateEditors
	 */
	public Map<String, DateTimeLocaleConstants> getDateEditors() {
		if (this.dateEditors == null) {
			setDateEditors(new LinkedHashMap<String, DateTimeLocaleConstants>(1));
		}
		return this.dateEditors;
	}

	/**
	 * @param dateEditors the dateEditors to set
	 */
	public void setDateEditors(Map<String, DateTimeLocaleConstants> dateEditors) {
		this.dateEditors = dateEditors;
	}

	/**
	 * @return the dateEditors
	 */
	public Map<String, DateTimeLocaleConstants> getCalendarEditors() {
		if (this.calendarEditors == null) {
			setCalendarEditors(new LinkedHashMap<String, DateTimeLocaleConstants>(1));
		}
		return this.calendarEditors;
	}

	/**
	 * @param dateEditors the dateEditors to set
	 */
	public void setCalendarEditors(Map<String, DateTimeLocaleConstants> calendarEditors) {
		this.calendarEditors = calendarEditors;
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
	 * @return the pageableListDetection
	 */
	public boolean isPageableListDetection() {
		return pageableListDetection;
	}

	/**
	 * @param pageableListDetection the pageableListDetection to set
	 */
	public void setPageableListDetection(boolean pageableListDetection) {
		this.pageableListDetection = pageableListDetection;
	}

	/**
	 * @return the initializersForType
	 */
	public Map<Class<?>, ComponentBindingInitializer> getInitializersForType() {
		if (this.initializersForType == null) {
			setInitializersForType(new LinkedHashMap<Class<?>, ComponentBindingInitializer>(1));
		}
		return this.initializersForType;
	}

	/**
	 * @param initializersForType the initializersForType to set
	 */
	public void setInitializersForType(
			Map<Class<?>, ComponentBindingInitializer> initializersForType) {
		this.initializersForType = initializersForType;
	}

	/**
	 * @return the filterDetection
	 */
	public boolean isFilterDetection() {
		return filterDetection;
	}

	/**
	 * @param filterDetection the filterDetection to set
	 */
	public void setFilterDetection(boolean filterDetection) {
		this.filterDetection = filterDetection;
	}

	public void afterPropertiesSet() throws Exception {
		if (isFilterDetection()) {			
			if (!getInitializersForType().containsKey(FilteredListHolder.class)) {
				FilterBindingInitializer fbi = new FilterBindingInitializer();
				fbi.setFormatResolver(getFormatResolver());
				getInitializersForType().put(FilteredListHolder.class, fbi );
			}
		}
	}

	/**
	 * @return the validator
	 */
	public ComponentValidator getValidator() {
		return validator;
	}

	/**
	 * @param validator the validator to set
	 */
	public void setValidator(ComponentValidator validator) {
		this.validator = validator;
	}

}
