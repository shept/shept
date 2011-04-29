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
	 * Copyright 2002-2008 the original author or authors.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.SortDefinition;
import org.springframework.util.StringUtils;

	/**
	 * PropertyComparator performs a comparison of two beans,
	 * evaluating the specified bean property via a BeanWrapper.
	 *
	 *
	 * This is a modified version of the original PropertyComparator as deliverd by Spring.
	 * For some reason the original Comparator won't sort if the models objects in the list are
	 * Array objects. This is the case if Hibernate returns Tuples (i.e. Arrays where each index represents a model object).
	 * It would be nice if the BeanWrapperImpl would properly handle these cases but this seems to be a limit of the java
	 * bean spec.
	 *  
	 *  So I modified the {@link #getPropertyValue(Object)} method to support Tuples also.
	 *
	 * @author Juergen Hoeller
	 * @author Jean-Pierre Pawlak
	 * @since 19.05.2003
	 * @see org.springframework.beans.BeanWrapper
	 */
	public class PropertyComparator implements Comparator {

		protected final Log logger = LogFactory.getLog(getClass());

		private final SortDefinition sortDefinition;

		private final BeanWrapperImpl beanWrapper = new BeanWrapperImpl(false);


		/**
		 * Create a new PropertyComparator for the given SortDefinition.
		 * @see MutableSortDefinition
		 */
		public PropertyComparator(SortDefinition sortDefinition) {
			this.sortDefinition = sortDefinition;
		}

		/**
		 * Create a PropertyComparator for the given settings.
		 * @param property the property to compare
		 * @param ignoreCase whether upper and lower case in String values should be ignored
		 * @param ascending whether to sort ascending (true) or descending (false)
		 */
		public PropertyComparator(String property, boolean ignoreCase, boolean ascending) {
			this.sortDefinition = new MutableSortDefinition(property, ignoreCase, ascending);
		}

		/**
		 * Return the SortDefinition that this comparator uses.
		 */
		public final SortDefinition getSortDefinition() {
			return this.sortDefinition;
		}


		public int compare(Object o1, Object o2) {
			Object v1 = getPropertyValue(o1);
			Object v2 = getPropertyValue(o2);
			if (this.sortDefinition.isIgnoreCase() && (v1 instanceof String) && (v2 instanceof String)) {
				v1 = ((String) v1).toLowerCase();
				v2 = ((String) v2).toLowerCase();
			}

			int result;
			
			// Put an object with null property at the end of the sort result.
			try {
				if (v1 != null) {
					result = (v2 != null ? ((Comparable) v1).compareTo(v2) : -1);
				}
				else {
					result = (v2 != null ? 1 : 0);
				}
			}
			catch (RuntimeException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Could not sort objects [" + o1 + "] and [" + o2 + "]", ex);
				}
				return 0;
			}

			return (this.sortDefinition.isAscending() ? result : -result);
		}

		/**
		 * Get the SortDefinition's property value for the given object.
		 * @param obj the object to get the property value for
		 * @return the property value
		 */
		private Object getPropertyValue(Object obj) {
			// If a nested property cannot be read, simply return null
			// (similar to JSTL EL). If the property doesn't exist in the
			// first place, let the exception through.
			try {
				String propertyName = this.sortDefinition.getProperty();
				if (obj.getClass().isArray() || obj instanceof Map || obj instanceof List || obj instanceof Set) {

					// special case which will not work directly with specifications like [0].name, [1].date
					// for tuples / maps  ...  as possible result objects returned by (Hibernate) queries.
					// Wrapping these special cases ensures proper handling by the BeanWrapperImpl.

					WrappedProperty o = new WrappedProperty();
					o.setProperty(obj);
					propertyName = "property" + propertyName;
					this.beanWrapper.setWrappedInstance(o);					
				} else {
					this.beanWrapper.setWrappedInstance(obj);
				}
				return this.beanWrapper.getPropertyValue(propertyName);
			}
			catch (BeansException ex) {
				logger.info("PropertyComparator could not access property - treating as null for sorting", ex);
				return null;
			}
		}


		/**
		 * Sort the given List according to the given sort definition.
		 * <p>Note: Contained objects have to provide the given property
		 * in the form of a bean property, i.e. a getXXX method.
		 * @param source the input List
		 * @param sortDefinition the parameters to sort by
		 * @throws java.lang.IllegalArgumentException in case of a missing propertyName
		 */
		public static void sort(List source, SortDefinition sortDefinition) throws BeansException {
			if (StringUtils.hasText(sortDefinition.getProperty())) {
				Collections.sort(source, new PropertyComparator(sortDefinition));
			}
		}

		/**
		 * Sort the given source according to the given sort definition.
		 * <p>Note: Contained objects have to provide the given property
		 * in the form of a bean property, i.e. a getXXX method.
		 * @param source input source
		 * @param sortDefinition the parameters to sort by
		 * @throws java.lang.IllegalArgumentException in case of a missing propertyName
		 */
		public static void sort(Object[] source, SortDefinition sortDefinition) throws BeansException {
			if (StringUtils.hasText(sortDefinition.getProperty())) {
				Arrays.sort(source, new PropertyComparator(sortDefinition));
			}
		}
		
		

		//---------------------------------------------------------------------
		// Inner class for internal use
		//---------------------------------------------------------------------

		private static class WrappedProperty {

			public Object property;

			/**
			 * @return the property
			 */
			public Object getProperty() {
				return property;
			}

			/**
			 * @param property the property to set
			 */
			public void setProperty(Object property) {
				this.property = property;
			}

		}


}
