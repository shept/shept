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

package org.shept.org.springframework.beans;

import java.beans.PropertyEditor;

/**
 * Holder for a registered custom editor with property name.
 * Keeps the PropertyEditor itself plus the type it was registered for.
 * NOTE: this is from org.springframework.beans.PropertyEditorRegistrySupport
 */
public class CustomEditorHolder implements PropertyEditorHolder {

	private PropertyEditor propertyEditor;

	private Class<?> registeredType;

	public CustomEditorHolder(PropertyEditor propertyEditor, Class<?> registeredType) {
		this.propertyEditor = propertyEditor;
		this.registeredType = registeredType;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.PropertyEditorHolder#setPropertyEditor(java.beans.PropertyEditor)
	 */
	public void setPropertyEditor(PropertyEditor propertyEditor) {
		this.propertyEditor = propertyEditor;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.PropertyEditorHolder#getPropertyEditor()
	 */
	public PropertyEditor getPropertyEditor() {
		return this.propertyEditor;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.PropertyEditorHolder#setRegisteredType(java.lang.Class)
	 */
	public void setRegisteredType(Class<?> registeredType) {
		this.registeredType = registeredType;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.PropertyEditorHolder#getRegisteredType()
	 */
	public Class<?> getRegisteredType() {
		return this.registeredType;
	}

}
