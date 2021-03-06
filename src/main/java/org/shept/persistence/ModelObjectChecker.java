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


/**
 * @author Andreas Hahn
 *
 */
public class ModelObjectChecker {
	
	private Object object;

	public void setObject(Object anObject) {
		this.object = anObject;
	}
	
	/**
	 * NOTE ! As of Java7 these accessors may NOT be named as reserved words 
	 * (i.e. 'isTransient' is NOT a valid name that can be references by expression language) 
	 */
	public boolean isDel() {
		try {
			return ((ModelDeletion) object).isDeleted();
		} catch (Exception ex) {
			return false;
		}
	}
	
	/**
	 * NOTE ! As of Java7 these accessors may NOT be named as reserved words 
	 * (i.e. 'isTransient' is NOT a valid name that can be references by expression language) 
	 */
	public boolean isTrans() {
		try {
			return ((ModelCreation) object).isTransient();
		} catch (Exception ex) {
			return false;
		}		
	}
	
}
