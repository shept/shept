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

import org.springframework.beans.support.SortDefinition;

/** 
 * @version $$Id: HibernateExampleListProviderImpl.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class HibernateExampleListProviderImpl extends AbstractHibernateListProvider {
	
	private Object example;
	
	private SortDefinition sortDef;

	/* (non-Javadoc)
	 * @see org.shept.provider.AbstractScrollingListProvider#loadListFirst()
	 */
	@Override
	public List<?> loadListFirst() {
		loaded = 0;
		eol = false;
		List<?> l = Collections.EMPTY_LIST;

		// The criteria MUST be reloaded on loadListFirst else we would miss 
		// changes of the underlying filter
		example = getFilterDefinition();
		sortDef = getSortDefinition();
		if (loadSize > 0 ) {
			l = getHibernateTemplate().findByExample(example, sortDef, loaded, loadSize);
		} else {
			l = getHibernateTemplate().findByExample(example, sortDef);
			eol = true;
		}
		incrementLoadSizeAfterFetch(l.size());
		return l;
	}

	/* (non-Javadoc)
	 * @see org.shept.provider.AbstractScrollingListProvider#loadListNext()
	 */
	@Override
	public List<?> loadListNext() {
		List<?> l = Collections.EMPTY_LIST;
		if (!eol) {
			l = getHibernateTemplate().findByExample(example, sortDef, loaded, loadSize);
			incrementLoadSizeAfterFetch(l.size());
		}
		return l;
	}

}
