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

/**
 * Marker interface to indicate a valid FilterProvider.
 * The @see ScrollingListProviderFactory will use this definition to initiantiate appropriate
 * Scrolling list objects.
 * 
 * This interface is typically used on entity objects to define instances as examples to the underlying filter.
 * A simple implementation in a user object would just return the uninitialized object meaning no filtering
 * is performed and the ListProvider will just return all available instances from the database.
 * 
 * public ModelCreation getNewModelTemplate() {
 * 		// initialize properties here as required
 * 		return new User();
 * }
 * 
 * Note that you will usually have to annotate {@link #getNewModelTemplate()} with a @Transient for entity models.
 *  
 * @version $Rev: 61 $
 * @author Andi
 *
 */
public interface ExampleDefinition extends FilterDefinition {

}
