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

import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shept.org.springframework.beans.CustomCalendarEditor;
import org.shept.org.springframework.web.servlet.mvc.support.ConfigurableLocaleDependentFormatResolver;
import org.shept.org.springframework.web.servlet.mvc.support.DateTimeLocaleConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.support.RequestContextUtils;

/** 
 * @version $$Id: DefaultWebBindingInitializer.java 110 2011-02-21 09:16:15Z aha $$
 *
 * @author Andi
 *
 */
public class DefaultWebBindingInitializer implements WebBindingInitializer  {
	
	private static final DateTimeLocaleConstants DEFAULT_DATE_TIME = DateTimeLocaleConstants.DATETIME_FORMAT_SHORT;

	/** Logger that is available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());
	
	private ConfigurableLocaleDependentFormatResolver formatResolver;

	private Map<Class<?>, PropertyEditorSupport> customEditorsForType;

	private boolean registerDefaultEditors = true;
	
	private DateTimeLocaleConstants dateTimeFormat = DEFAULT_DATE_TIME;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.bind.support.ConfigurableWebBindingInitializer#initBinder(org.springframework.web.bind.WebDataBinder,
	 *      org.springframework.web.context.request.WebRequest)
	 */
	public void initBinder(WebDataBinder binder, WebRequest request) {
		if (logger.isInfoEnabled()) {
			logger.info("Register custom binding initializers");
		}
		
		if (getCustomEditorsForType() != null) {
			for (Entry<Class<?>, PropertyEditorSupport> editorEntry : getCustomEditorsForType().entrySet()) {
				binder.registerCustomEditor(editorEntry.getKey(), editorEntry.getValue());
			}			
		}
		
		if (isRegisterDefaultEditors()) {
			Locale locale = RequestContextUtils.getLocale(((ServletWebRequest)request).getRequest());
			String format = formatResolver.resolveProperty(locale, getDateTimeFormat());
			binder.registerCustomEditor(Calendar.class, new CustomCalendarEditor(new SimpleDateFormat(format), true));
			binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat(format), true));			
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
	 * @return the registerDefaultEditors
	 */
	public boolean isRegisterDefaultEditors() {
		return registerDefaultEditors;
	}

	/**
	 * @param registerDefaultEditors the registerDefaultEditors to set
	 */
	public void setRegisterDefaultEditors(boolean registerDefaultEditors) {
		this.registerDefaultEditors = registerDefaultEditors;
	}

	/**
	 * @return the customEditorsForType
	 */
	public Map<Class<?>, PropertyEditorSupport> getCustomEditorsForType() {
		if (this.customEditorsForType == null) {
			setCustomEditorsForType(new LinkedHashMap<Class<?>, PropertyEditorSupport>(1));
		}
		return this.customEditorsForType;
	}

	/**
	 * @param customEditorsForType the customEditorsForType to set
	 */
	public void setCustomEditorsForType(
			Map<Class<?>, PropertyEditorSupport> customEditorsForType) {
		this.customEditorsForType = customEditorsForType;
	}

	/**
	 * @return the dateTimeFormat
	 */
	public DateTimeLocaleConstants getDateTimeFormat() {
		return dateTimeFormat;
	}

	/**
	 * @param dateTimeFormat the dateTimeFormat to set
	 */
	public void setDateTimeFormat(DateTimeLocaleConstants dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
	}


}
