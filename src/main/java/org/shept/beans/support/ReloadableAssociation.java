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

import java.io.Serializable;
import java.lang.reflect.Method;

import org.shept.persistence.ModelCreation;

/** 
 * Implementation of the FilterDefiition Interface to make Associations,
 * e.g. JPA or HibernateAssociations reloadable, e.g let them support 
 * associated collections
 * 
 * @version $Rev: 70 $
 * @author Andi
 */
public class ReloadableAssociation implements Serializable, FilterDefinition {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Object sourceModel;
	
	private transient Method associationMethod;
	
	private ModelCreation newModelTemplate;
	
	/**
	 * @return the souceModel
	 */
	public Object getSourceModel() {
		return sourceModel;
	}

	/**
	 * @param souceModel the souceModel to set
	 */
	public void setSourceModel(Object souceModel) {
		this.sourceModel = souceModel;
	}

	/**
	 * @return the associationMethod
	 */
	public Method getAssociationMethod() {
		return associationMethod;
	}

	/**
	 * @param associationMethod the associationMethod to set
	 */
	public void setAssociationMethod(Method associationMethod) {
		this.associationMethod = associationMethod;
	}

	/* (non-Javadoc)
	 * @see org.shept.beans.support.FilterDefinition#getNewModelTemplate()
	 */
	public ModelCreation getNewModelTemplate() {
		return newModelTemplate;
	}

	/**
	 * @param newModelTemplate the newModelTemplate to set
	 */
	public void setNewModelTemplate(ModelCreation newModelTemplate) {
		this.newModelTemplate = newModelTemplate;
	}

}
