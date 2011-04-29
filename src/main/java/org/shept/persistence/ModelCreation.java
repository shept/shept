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
 * @version $$Id: ModelCreation.java 71 2010-11-05 17:03:47Z aha $$
 *
 * @author Andi
 *
 */
public interface ModelCreation {
	
	/**
	 * The editedObject will be compared with the template object to decide whether the object may be
	 * created in the database. Comparison should tolerate changes to the edited object which result
	 * from DataBinding
	 * 
	 * @param editedObject 
	 * @return
	 */
	public abstract boolean isCreationAllowed (Object editedObject);
	
	public abstract boolean isTransient();

}
