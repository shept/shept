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

package org.shept.org.springframework.web.servlet.mvc.delegation;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/** 
 * @version $$Id: ComponentValidator.java 70 2010-11-04 17:28:46Z aha $$
 *
 * @author Andi
 *
 */
public interface ComponentValidator {

	/**
	 * Validate the supplied <code>target</code> object, which must be
	 * of a {@link Class} for which the {@link #supports(Class)} method
	 * typically has (or would) return <code>true</code>.
	 * <p>The supplied {@link Errors errors} instance can be used to report
	 * any resulting validation errors.
	 * @param target the object that is to be validated (can be <code>null</code>) 
	 * @param errors contextual state about the validation process (never <code>null</code>) 
	 * @see ValidationUtils
	 */
	void validate(Object target, Errors errors, String componentPath);

}
