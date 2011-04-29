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

package org.shept.org.springframework.web.servlet.mvc.support;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/** 
 * @version $$Id: ConfigurableLocaleDependentFormatResolver.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class ConfigurableLocaleDependentFormatResolver  implements InitializingBean {
	
	/** Logger that is available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());
	
	private Locale defaultLocale = Locale.getDefault();
	
	private Map<Locale, Map<DateTimeLocaleConstants, String>> dateTimeLocales;
	
	@SuppressWarnings("unchecked")
	protected Map<DateTimeLocaleConstants, String> getDateTimeLocaleConstants(Locale loc) {
		// search for exact match first
		Map <DateTimeLocaleConstants, String> localeConstants = dateTimeLocales.get(loc);
		if (localeConstants == null) {
			// if not found we search for country
			if (localeConstants == null && loc.getCountry() != null) {
				localeConstants = dateTimeLocales.get(new Locale(loc.getCountry()));
			}
			// if not found exact match we search for language
			if (localeConstants == null && loc.getLanguage() != null) {
				localeConstants = dateTimeLocales.get(new Locale(loc.getLanguage()));
			}
			// if not found we provide the default (as provided by the servers java settings)
			if (localeConstants == null) {
				if (logger.isErrorEnabled()) {
					logger.error("Locale constants definition not found for locale " + loc + ". Using default locale instead" );
				}
				localeConstants = dateTimeLocales.get(this.defaultLocale);				
			}
		}
		return localeConstants;
	}
	
	public String resolveProperty(Locale loc, DateTimeLocaleConstants property) {
		String value = getDateTimeLocaleConstants(loc).get(property);
		if (value == null) {
			if (logger.isErrorEnabled()) {
				logger.error("Property " + property + " not found using locale " + loc +". Check your configuration for dateTimeLocales. Trying default locale instead");
			}
			value = getDateTimeLocaleConstants(this.defaultLocale).get(property);
			if (value == null) {
				throw new IllegalStateException("Property " + property + " undefined in locale " + loc );
			}
		}
		return value;
	}
	
	/**
	 * @param dateTimeLocales the dateTimeLocales to set
	 */
	public void setDateTimeLocales(Map<Locale, Map<DateTimeLocaleConstants, String>> locales) {
		this.dateTimeLocales = locales;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notEmpty(this.dateTimeLocales, "The dateTimeLocales definitions have not yet been initialized");
		Assert.state(this.dateTimeLocales.containsKey(this.defaultLocale), "The default locale '" + this.defaultLocale +"' is not properly configured");
	}

	/**
	 * @param defaultLocale the defaultLocale to set
	 */
	public void setDefaultLocale(Locale defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

}
