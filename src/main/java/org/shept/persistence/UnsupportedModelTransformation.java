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

package org.shept.persistence;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.NestedRuntimeException;

/** 
 * @version $$Id: UnsupportedModelTransformation.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
@SuppressWarnings("serial")
public class UnsupportedModelTransformation extends NestedRuntimeException {

	public UnsupportedModelTransformation(Object rowModel) {
		super(NestedExceptionUtils.buildMessage("The row " + rowModel.toString() +
				" cannot be converted into a persistable model object", null));
	}

}
