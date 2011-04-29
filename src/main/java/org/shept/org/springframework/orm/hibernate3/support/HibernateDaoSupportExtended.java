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

package org.shept.org.springframework.orm.hibernate3.support;

import java.io.ObjectStreamException;

import org.hibernate.SessionFactory;
import org.shept.org.springframework.orm.hibernate3.HibernateTemplateExtended;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public abstract class HibernateDaoSupportExtended extends HibernateDaoSupport {

	protected HibernateTemplateExtended createHibernateTemplate(SessionFactory sessionFactory) {
		return new HibernateTemplateExtended (sessionFactory);
	}
	
	public HibernateTemplateExtended getHibernateTemplateExtended() {
		  return (HibernateTemplateExtended) getHibernateTemplate();
		}

	/**
	 * do not serialize / deserialize when parent objects require serialization
	 * 
	 * @throws ObjectStreamException
	 */
	protected Object writeReplace() throws ObjectStreamException{
		return null;
	}
	 
	/**
	 * do not serialize / deserialize when parent objects require serialization
	 * 
	 * @throws ObjectStreamException
	 */
	protected Object readResolve() throws ObjectStreamException {
		return null;
	}

}
