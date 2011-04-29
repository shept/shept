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

import java.util.LinkedHashMap;
import java.util.Map;

import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentUtils;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentValidator;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.SegmentConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.request.ServletWebRequest;

/** 
 * 
 * Leider gibt's ein Binding-Problem mit dem DataBinder (Version 3 RC2)
 * Attribute validator samt setter / getter scheint nicht im .jar zu sein ...
 * (obwohl es im Source der .src-jar ist)
 * 
 * @version $$Id: ComponentDataBinder.java 70 2010-11-04 17:28:46Z aha $$
 *
 * @author Andi
 *
 */
public class ComponentDataBinder extends WebRequestDataBinder {

	@Deprecated
	private Map<String, ComponentValidator> validatorsForPath ;

	public ComponentDataBinder(Object target) {
		super(target);
	}

	/**
	 * @param target
	 * @param objectName
	 */
	public ComponentDataBinder(Object target, String objectName) {
		super(target, objectName);
	}

	public void bindAndValidate(ServletWebRequest request, ComponentToken token) {
		bind(request);
		String componentPath = ComponentUtils.lookupComponentPath(token);
		BindingResult errors = getBindingResult();
		errors.pushNestedPath(componentPath);
		
		// TODO using a validator from a binding initializer should be dropped in favor of the
		// validators from the configuration
		ComponentValidator val = (ComponentValidator) getValidator(componentPath);
		if (val != null) {
			val.validate(getTarget(), getBindingResult(), componentPath);							
		}
		
		SegmentConfiguration config = ComponentUtils.getConfiguration(request.getRequest(), token);
		if (config != null && config.getValidators() != null) {
			for (ComponentValidator validator : config.getValidators()) {
				validator.validate(getTarget(), getBindingResult(), componentPath);		
			}			
		}
		
		errors.popNestedPath();
	}
	
	/**
	 * Invoke the specified Validator, if any.
	 * @see #setValidator(Validator)
	 * @see #getBindingResult()
	 */
	public void validate() {
		throw new UnsupportedOperationException("The component binder does not support a global validation. Use a component based validation instead");
	}

	/**
	 * @return the validator
	 */
	public Validator getValidator() {
		throw new UnsupportedOperationException("The component binder does not support a global validator. Use a component based validatior instead");
	}

	public void setValidator(String path, ComponentValidator validator) {
		getValidatorsForPath().put(path, validator);
	}
	
	@Deprecated
	public ComponentValidator getValidator(String path) {
		return getValidatorsForPath().get(path);
	}

	
	/**
	 * @return the validatorsForPath
	 */
	@Deprecated
	public Map<String, ComponentValidator> getValidatorsForPath() {
		if (this.validatorsForPath == null) {
			setValidatorsForPath(new LinkedHashMap<String, ComponentValidator>(1));
		}
		return this.validatorsForPath;
	}

	/**
	 * @param validatorsForPath the validatorsForPath to set
	 */
	@Deprecated
	public void setValidatorsForPath(Map<String, ComponentValidator> validatorsForPath) {
		this.validatorsForPath = validatorsForPath;
	}
}
