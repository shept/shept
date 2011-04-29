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

package org.shept.persistence.provider.hibernate;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.springframework.beans.BeanUtils;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/** 
 * @version $$Id: HibernateUtils_old.java 70 2010-11-04 17:28:46Z aha $$
 *
 * @author Andi
 *
 */
public class HibernateUtils_old {
	
	private static ClassMetadata getClassMetadata(HibernateDaoSupport dao, Object model) {
		SessionFactory hsf = dao.getHibernateTemplate().getSessionFactory();
		if (model == null) return null;
		return hsf.getClassMetadata(model.getClass());
	}
	
	public static String getIdentifierPropertyName(HibernateDaoSupport dao, Object model) {
		ClassMetadata meta = getClassMetadata(dao, model);
		return meta.getIdentifierPropertyName();
	}
	
	public static Serializable getIdValue(HibernateDaoSupport dao, Object model) {
		String idStr = getIdentifierPropertyName(dao, model);
		// TODO use BeanWrapper instead ?
		Method idMth = ReflectionUtils.findMethod(model.getClass(), "get" + StringUtils.capitalize(idStr));
		Serializable idxObj = (Serializable) ReflectionUtils.invokeMethod(idMth, model);
		return idxObj;
	}
	
	/**
	 * Checking if it is a new model
	 * If the index is a compound index we must check all components if they are all non null
	 * @param index
	 * @return
	 */
	public static boolean isNewModel(HibernateDaoSupport dao, Object model) {
		final Object index = getIdValue(dao, model);
		final List<Field> nulls = new ArrayList<Field>();
		if (index == null) return true;

		ReflectionUtils.doWithFields(index.getClass(), new ReflectionUtils.FieldCallback() {
			public void doWith(Field field) {
				try {
					Method idMth = ReflectionUtils.findMethod(index.getClass(), "get" + StringUtils.capitalize(field.getName()));
					Object value = ReflectionUtils.invokeMethod(idMth, index);
					if (value == null) {
						nulls.add(field);
					}
				} catch (Exception ex) {
					// ignore all Exception here as they are quit frequent
					// e.g. serialVersionUid e.t.c. or do better filtering
					// TODO better eliminate error cases
				}
			}
		});
		return nulls.size() > 0;
	}
	
	public static Object copyTemplate (HibernateDaoSupport dao, Object entityModelTemplate) {
		if (entityModelTemplate != null) {
// hier besser die Metadaten von Hibernate fragen
			if (null != getClassMetadata(dao, entityModelTemplate)) {
//			if (null != AnnotationUtils.findAnnotation(entityModelTemplate.getClass(), Entity.class)) {
				String idName = HibernateUtils_old.getIdentifierPropertyName(dao, entityModelTemplate);
				Object newModel = BeanUtils.instantiateClass(entityModelTemplate.getClass());
				BeanUtils.copyProperties(entityModelTemplate, newModel, new String[] { idName });
				
				Serializable idx = getIdValue(dao, entityModelTemplate);

				ClassMetadata meta = getClassMetadata(dao, idx );
				Type type = meta.getIdentifierType();
				
				if (meta != null && type.isComponentType()) {
// alternaitv					if (id != null && (null != AnnotationUtils.findAnnotation(id.getClass(), Embeddable.class))) {
					Serializable copyId = BeanUtils.instantiate(idx.getClass());
					BeanUtils.copyProperties(idx, copyId);
					Method idMth = ReflectionUtils.findMethod(entityModelTemplate.getClass(), "set" + StringUtils.capitalize(idName), new Class[]{});
					if (idMth != null) {
						ReflectionUtils.invokeMethod(idMth, newModel, copyId);
					}					
				}
				return newModel;
			}
		}
		return null;
	}

}
