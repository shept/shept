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

import java.util.Collections;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;

/** 
 * @version $$Id: HibernateCriteriaListProviderImpl.java 54 2010-10-13 15:39:43Z aha $$
 *
 * @author Andi
 *
 */
public class HibernateCriteriaListProviderImpl extends
		AbstractHibernateListProvider {

	protected DetachedCriteria crit;
	
	public List<?> loadListFirst() {
		loaded = 0;
		eol = false;
		List<?> l = Collections.EMPTY_LIST;

		// The criteria MUST be reloaded on loadListFirst else we would miss 
		// changes of the underlying filter
		crit = ((HibernateCriteriaDefinition) getFilterDefinition()).getCriteria(getSortDefinition());
		if (crit == null) {
			logger.error("Null criteria for filter '" + getFilterDefinition() + "' detected");
			eol = true;
			return l;
		}
		if (loadSize > 0 ) {
			l = getHibernateTemplate().findByCriteria(crit, loaded, loadSize);
		} else {
			l = getHibernateTemplate().findByCriteria(crit);
			eol = true;
		}
		incrementLoadSizeAfterFetch(l.size());
		return l;
	}

	public List<?> loadListNext() {
		List<?> l = Collections.EMPTY_LIST;
		if (!eol) {
			l = getHibernateTemplate().findByCriteria(crit, loaded, loadSize);
			incrementLoadSizeAfterFetch(l.size());
		}
		return l;
	}

}
