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
 * @version $$Id: ModelDeletion.java 90 2010-11-30 14:31:06Z aha $$
 *
 * @author Andi
 *
 */
public interface ModelDeletion {

	/**
	 * set deletion mark and return the success state
	 * @param
	 * @return
	 *
	 * @return
	 */
	public abstract boolean setDeleted (boolean delete);
	
	/**
	 * return true if model object is deleted
	 * @param
	 * @return
	 *
	 * @return
	 */
	public abstract boolean isDeleted();
	
}
