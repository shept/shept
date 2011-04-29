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

package org.shept.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/** 
 * @version $$Id: BeanUtilsExtended.java 70 2010-11-04 17:28:46Z aha $$
 *
 * @author Andi
 *
 */
public class BeanUtilsExtended extends BeanUtils {
	
	/**
	 * Merge the property values of the given source bean into the target bean.
	 * <p>Note: Only not-null values are merged into the given target bean.
	 * Note: The source and target classes do not have to match or even be derived
	 * from each other, as long as the properties match. Any bean properties that the
	 * source bean exposes but the target bean does not will silently be ignored.
	 * <p>This is just a convenience method. For more complex transfer needs,
	 * consider using a full BeanWrapper.
	 * @param source the source bean
	 * @param target the target bean
	 * @throws BeansException if the copying failed
	 * @see BeanWrapper
	 */
	public static void mergeProperties(Object source, Object target) throws BeansException {
		mergeProperties(source, target, null, null);
	}

	/**
	 * Merge the property values of the given source bean into the given target bean,
	 * only setting properties defined in the given "editable" class (or interface).
	 * <p>Note: Only not-null values are merged into the given target bean.
	 * Note: The source and target classes do not have to match or even be derived
	 * from each other, as long as the properties match. Any bean properties that the
	 * source bean exposes but the target bean does not will silently be ignored.
	 * <p>This is just a convenience method. For more complex transfer needs,
	 * consider using a full BeanWrapper.
	 * @param source the source bean
	 * @param target the target bean
	 * @param editable the class (or interface) to restrict property setting to
	 * @throws BeansException if the copying failed
	 * @see BeanWrapper
	 */
	public static void mergeProperties(Object source, Object target, Class<?> editable)
			throws BeansException {

		mergeProperties(source, target, editable, null);
	}

	/**
	 * Merge the property values of the given source bean into the given target bean.
	 * <p>Note: Only not-null values are merged into the given target bean.
	 * Note: The source and target classes do not have to match or even be derived
	 * from each other, as long as the properties match. Any bean properties that the
	 * source bean exposes but the target bean does not will silently be ignored.
	 * @param source the source bean
	 * @param target the target bean
	 * @param editable the class (or interface) to restrict property setting to
	 * @param ignoreProperties array of property names to ignore
	 * @throws BeansException if the copying failed
	 * @see BeanWrapper
	 */
	private static void mergeProperties(Object source, Object target, Class<?> editable, String[] ignoreProperties)
			throws BeansException {

		Assert.notNull(source, "Source must not be null");
		Assert.notNull(target, "Target must not be null");

		Class<?> actualEditable = target.getClass();
		if (editable != null) {
			if (!editable.isInstance(target)) {
				throw new IllegalArgumentException("Target class [" + target.getClass().getName() +
						"] not assignable to Editable class [" + editable.getName() + "]");
			}
			actualEditable = editable;
		}
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		List<String> ignoreList = (ignoreProperties != null) ? Arrays.asList(ignoreProperties) : null;

		for (PropertyDescriptor targetPd : targetPds) {
			if (targetPd.getWriteMethod() != null &&
					(ignoreProperties == null || (!ignoreList.contains(targetPd.getName())))) {
				PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
				if (sourcePd != null && sourcePd.getReadMethod() != null) {
					try {
						Method readMethod = sourcePd.getReadMethod();
						if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
							readMethod.setAccessible(true);
						}
						Object value = readMethod.invoke(source);
						if (value != null) {
							Method writeMethod = targetPd.getWriteMethod();
							if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
								writeMethod.setAccessible(true);
							}
							writeMethod.invoke(target, value);							
						}
					}
					catch (Throwable ex) {
						throw new FatalBeanException("Could not copy properties from source to target", ex);
					}
				}
			}
		}
	}

	
	public static Map<String, ?> findNestedPaths(Object targetObject, Class<?> targetClass) throws Exception {
		return findNestedPaths(targetObject, targetClass, new LinkedList<String>());
	}

	public static Map<String, ?> findNestedPaths(Object targetObject, Class<?> targetClass,  List<String> ignoreList) throws Exception {
		Set<Object> visited = new HashSet<Object>();
		return findNestedPaths(targetObject, targetClass,  null, ignoreList, visited , 10);	// limit recursion depth
	}

	
	/**
	 * Find all occurrences of targetClass in targetObject.
	 * Be careful. This stuff may be recursive !
	 * Should be improved to prevent endless recursive loops.
	 * 
	 * @param
	 * @return
	 *
	 * @param targetObject
	 * @param targetClass
	 * @param nestedPath
	 * @return
	 * @throws Exception 
	 */
	public static Map<String,?> findNestedPaths(Object targetObject, Class<?> targetClass, String nestedPath, 
			List<String> ignoreList,  Set<Object> visited, int maxDepth ) throws Exception {

		Assert.notNull(targetObject);
		Assert.notNull(targetClass);
		Assert.notNull(ignoreList);
		HashMap<String, Object> nestedPaths = new HashMap<String, Object>();
		if (maxDepth <= 0) {
			return nestedPaths;
		}
		
		nestedPath = (nestedPath == null ? "" : nestedPath);
		BeanWrapper bw =  PropertyAccessorFactory.forBeanPropertyAccess(targetObject);
		PropertyDescriptor[] props = bw.getPropertyDescriptors();
		
		for (PropertyDescriptor pd : props) {
			Class<?> clazz = pd.getPropertyType();
			if (clazz != null && !clazz.equals(Class.class)
					&& !clazz.isAnnotation() && !clazz.isPrimitive()) {
				Object value = null;
				String pathName = pd.getName();
				if (!ignoreList.contains(pathName)) {
					try {
						value = bw.getPropertyValue(pathName);
					} catch (Exception e) {
					} // ignore any exceptions here
					if (StringUtils.hasText(nestedPath)) {
						pathName = nestedPath
								+ PropertyAccessor.NESTED_PROPERTY_SEPARATOR
								+ pathName;
					}
					// TODO break up this stuff into checking and excecution a la ReflectionUtils
					if (targetClass.isAssignableFrom(clazz)) {
						nestedPaths.put(pathName, value);
					} 
					// exclude objects already visited from further inspection to prevent circular references
					// unfortunately this stuff isn't fool proof as there are ConcurrentModificationExceptions
					// when adding objects to the visited list
					if (value != null && !isInstanceVisited(visited, value)) {
						nestedPaths.putAll(findNestedPaths(value, targetClass,
								pathName, ignoreList,visited,  maxDepth - 1));
					}
				}
			}
		}
		return nestedPaths;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static boolean isInstanceVisited(Collection coll, Object value) throws Exception {
		for (Iterator<?> it = coll.iterator(); it.hasNext() ;) {
			Object v = it.next();
			if (v.equals(value)) { 
				return true; 
			}
		}
		// sometimes ConcurrentModificationExceptions here
		coll.add(value);
		return false;
	}
		
}
