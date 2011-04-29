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

package org.shept.persistence.provider;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;

import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.support.DaoSupport;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author $Author: aha $
 * @version $Revision: 72 $
 * 
 */
public class DaoUtils {

	private static ClassMetadata getClassMetadata(DaoSupport dao, Object model) {
		checkProvider(dao);
		SessionFactory hsf = ((HibernateDaoSupport) dao).getHibernateTemplate()
				.getSessionFactory();
		if (model == null) {
			return null;
		}
		return hsf.getClassMetadata(model.getClass());
	}

	/**
	 * 
	 * @param dao
	 * @param entityModelTemplate
	 * @return true if the requested entityModelTemplate Object is managed by the persistent layer
	 */
	public static boolean isEntity(DaoSupport dao, Object entityModelTemplate) {
		if (dao == null) {
			return false;
		}
		ClassMetadata modelMeta = getClassMetadata(dao, entityModelTemplate);
		return (modelMeta != null);
	}
	
	public static Object getIdValue(DaoSupport dao, Object entityModelTemplate) {
		ClassMetadata modelMeta = getClassMetadata(dao, entityModelTemplate);
		if (null == modelMeta) {
			return null;
		}
		return modelMeta.getIdentifier(entityModelTemplate, EntityMode.POJO);
	}

	/**
	 * Checking if it is a new model (identifier == null) If the index is a
	 * compound index we must check all components if just one of them is null
	 * 
	 * @param index
	 * @return
	 */
	public static boolean isNewModel(DaoSupport dao, Object model) {
		ClassMetadata modelMeta = getClassMetadata(dao, model);
		if (null == modelMeta) {
			return false;
		}

		Object idValue = modelMeta.getIdentifier(model, EntityMode.POJO);

		if (idValue == null) {
			return true;
		}
		Type type = modelMeta.getIdentifierType();
		if (!(type instanceof ComponentType)) {
			return false;
		}

		// didn't manage to get the individual objects of a compound index out of
		// the Hibernate metaModel API although that should be possible ...
		PropertyDescriptor[] desc = BeanUtils.getPropertyDescriptors(idValue.getClass());
		for (int i = 0; i < desc.length; i++) {
			Method mth = desc[i].getReadMethod();
			Object val = ReflectionUtils.invokeMethod(mth, idValue);
			if (null == val) {
				return true;
			}
		}

		return false;
	}

	/**
	 * This is a simplified modelObject copy method. It will copy all properties
	 * of the template model but ignore the id. In case the id is a compound id
	 * the the resulting copy will get a deepCopy of the id.
	 * 
	 * NOTE that the method will only create shallow copies. This means that
	 * collections, components, associations will NOT be deep copied
	 * 
	 * A better version of this method would iterate across all property types
	 * inspecting them an doing deepCopies in case of collections, components or
	 * associations. It still remains a threat to limit the depth of deep copies
	 * to prevent recursive dependencies
	 * 
	 * @param dao
	 * @param entityModelTemplate
	 * @return a copy of the entityModelTemplate
	 */
	/**
	 * This deepCopy will copy all properties of the template model but ignore
	 * the id. In case the id is a compound id the the resulting copy will get a
	 * deepCopy of the id if the composite id is only partially filled
	 * 
	 * NOTE that the method will only create shallow copies except for component
	 * properties which will be deep copied This means that collections and
	 * associations will NOT be deep copied
	 * 
	 * @param dao
	 * @param entityModelTemplate
	 * @return a copy of the entityModelTemplate
	 */
	public static Object deepCopyModel(DaoSupport dao,
			Object entityModelTemplate) {
		ClassMetadata modelMeta = getClassMetadata(dao, entityModelTemplate);
		if (null == modelMeta) {
			return null;
		}
		Object modelCopy = shallowCopyModel(dao, entityModelTemplate, false);

		// Ids of new models are either null or composite ids and will be copied
		// if new
		boolean isCopyId = isNewModel(dao, entityModelTemplate);
		Object idValue = modelMeta.getIdentifier(entityModelTemplate, EntityMode.POJO);
		if (null != idValue && isCopyId) {
			String idName = modelMeta.getIdentifierPropertyName();
			Object idCopy = BeanUtils.instantiateClass(idValue.getClass());
			BeanUtils.copyProperties(idValue, idCopy, new String[] { idName });
			modelMeta.setIdentifier(modelCopy, (Serializable) idCopy, EntityMode.POJO);
		}

		String[] names = modelMeta.getPropertyNames();
		Type[] types = modelMeta.getPropertyTypes();
		for (int i = 0; i < modelMeta.getPropertyNames().length; i++) {
			if (types[i].isComponentType()) {
				String propName = names[i];
				Object propValue = modelMeta.getPropertyValue(
						entityModelTemplate, propName, EntityMode.POJO);
				Object propCopy = shallowCopyModel(dao, propValue, true);
				modelMeta.setPropertyValue(modelCopy, propName, propCopy,
						EntityMode.POJO);
			}
		}
		return modelCopy;
	}

	/**
	 * This Hibernate specific code will create shallow copies of the model
	 * object
	 * 
	 * @param dao
	 * @param entityModelTemplate
	 * @param includeId
	 *            true if the id is to be included
	 * @return
	 */
	public static Object shallowCopyModel(DaoSupport dao,
			Object entityModelTemplate, boolean includeId) {
		ClassMetadata modelMeta = getClassMetadata(dao, entityModelTemplate);
		if (null == modelMeta) {
			return null;
		}
		Object modelCopy = BeanUtils.instantiateClass(entityModelTemplate
				.getClass());
		if (! includeId) {
			String idName = modelMeta.getIdentifierPropertyName();
			BeanUtils.copyProperties(entityModelTemplate, modelCopy,
					new String[] { idName });
		} else {
			BeanUtils.copyProperties(entityModelTemplate, modelCopy);
		}
		return modelCopy;
	}

	public static void checkProvider(DaoSupport dao) {
		if (!(dao instanceof HibernateDaoSupport)) {
			throw new UnsupportedDataProviderException(
					"This version only supports Hibernate as the dao wrapper.");
		}
	}

	//
	// These are old / experimental versions as a reminder
	//
	//

	private static Serializable getIdValue_old(DaoSupport dao, Object model) {
		checkProvider(dao);
		String idStr = getIdentifierPropertyName_old(dao, model);
		if (!(StringUtils.hasText(idStr))) {
			return null;
		}
		Method idMth = ReflectionUtils.findMethod(model.getClass(), "get"
				+ StringUtils.capitalize(idStr));
		Serializable idxObj = (Serializable) ReflectionUtils.invokeMethod(
				idMth, model);
		return idxObj;
	}

	private static String getIdentifierPropertyName_old(DaoSupport dao,
			Object model) {
		checkProvider(dao);
		ClassMetadata meta = getClassMetadata((HibernateDaoSupport) dao, model);
		if (meta == null) {
			return null;
		}
		return meta.getIdentifierPropertyName();
	}

	private static Object copyTemplate_Experimental(HibernateDaoSupport dao,
			Object entityModelTemplate) {
		ClassMetadata modelMeta = getClassMetadata(dao, entityModelTemplate);
		if (null == modelMeta) {
			return null;
		}
		String idName = modelMeta.getIdentifierPropertyName();
		Object modelCopy = BeanUtils.instantiateClass(entityModelTemplate
				.getClass());
		BeanUtils.copyProperties(entityModelTemplate, modelCopy,
				new String[] { idName });

		Type idType = modelMeta.getIdentifierType();
		if (null == idType || !idType.isComponentType()) {
			return modelCopy;
		}

		Object idValue = modelMeta.getPropertyValue(entityModelTemplate,
				idName, EntityMode.POJO);
		if (null == idValue) {
			return modelCopy;
		}

		Object idCopy = BeanUtils.instantiate(idValue.getClass());
		BeanUtils.copyProperties(idValue, idCopy);

		if (null == idValue || (null != idType)) {
			return modelCopy;
		}

		Method idMth = ReflectionUtils.findMethod(
				entityModelTemplate.getClass(),
				"set" + StringUtils.capitalize(idName), new Class[]{} );
		if (idMth != null) {
			ReflectionUtils.invokeMethod(idMth, modelCopy, idCopy);
		}

		return modelCopy;
	}

}
