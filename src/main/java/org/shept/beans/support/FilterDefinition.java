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

package org.shept.beans.support;

import org.shept.persistence.ModelCreation;
import org.shept.persistence.provider.ScrollingListProviderFactory;


/**
 * Abstract interface for FilterDefinitions.
 * FilterDefinitions are are used to provide a filtered collection of objects of an entity.</br></br>
 * 
 * ScrollingListProviderFactory  will use this definition to instantiate appropriate
 * Scrolling list objects. </br>
 * 
 * By default all filters also support creation of new objects which obey the filter criteria.
 * This is done via #getNewModelTemplate implementation. 
 *  
 * @see ScrollingListProviderFactory
 *  
 * @version $Rev: 61 $
 * @author Andi
 *
 */
public abstract interface FilterDefinition {

	/**
	 * 
	 * @return an default instance of the model object which may depend on the filtered template or
	 * null, if creation of new model objects of your entity is not supported.
	 * 
	 */
	public abstract ModelCreation getNewModelTemplate();


}
