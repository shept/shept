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

package org.shept.org.springframework.orm.hibernate3;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.beans.support.SortDefinition;
import org.springframework.util.StringUtils;

/** 
 * @version $$Id: IgnoreCaseCriteriaBuilder.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class IgnoreCaseCriteriaBuilder implements CriteriaBuilder {

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.orm.hibernate3.CriteriaBuilder#buildCriteria(java.lang.String, java.lang.Object, org.springframework.beans.support.SortDefinition, org.hibernate.Session)
	 */
	public Criteria buildCriteria(String entityName, Object exampleEntity, SortDefinition sortDef, Session session) {

		Criteria executableCriteria = (entityName != null ?
				session.createCriteria(entityName) : session.createCriteria(exampleEntity.getClass()));
		executableCriteria.add(Example.create(exampleEntity).ignoreCase());
		
		String sortPropRoot = "";

		if (sortDef != null & StringUtils.hasText(sortDef.getProperty())) {
			// alias is needed to support sorting by associated entites properties e.g. assEntity.name
			// maybe we need to generate a surrogate rootProperty alias name (instead of reusing the given property name) ???
			int rootIdx = sortDef.getProperty().indexOf(".");
			if (rootIdx > 0) {
				sortPropRoot = sortDef.getProperty().substring(0, rootIdx);
				executableCriteria.createAlias(sortPropRoot, sortPropRoot);							
			}
			executableCriteria.addOrder(sortDef.isAscending() ? 
					Order.asc(sortDef.getProperty()) : Order.desc(sortDef.getProperty()));
		}

		ClassMetadata meta = session.getSessionFactory().getClassMetadata(exampleEntity.getClass());
		// add subcriteria 
		//		List results = session.createCriteria(Parent.class)
		//			.add( Example.create(parent).ignoreCase() )
		//			.createCriteria("child")
		//			.add( Example.create( parent.getChild() ) )
		//			.list();
		String[] propNames = meta.getPropertyNames();
		for (String propName : propNames) {
			if (meta.getPropertyType(propName).isEntityType()) {
				Object propVal = meta.getPropertyValue(exampleEntity, propName, EntityMode.POJO);
				// Unfortunately the Hibernate criteria implementation isn't perfect, so if we have
				// both a sort subcriteria and a filter subcriteria then we get errors from hibernate
				// "org.hibernate.QueryException: duplicate association path"
				// workaround here is to exclude sorts from the filter [ && !propName.equals(sortPropRoot) ] 
				// which may give unexpected results
				if ( propVal != null && !propName.equals(sortPropRoot)) {
					executableCriteria.createCriteria(propName)
						.add(Example.create(propVal).ignoreCase());
				}
			}
		}
		return executableCriteria;
	}

}
