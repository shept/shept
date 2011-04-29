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

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.shept.beans.support.QueryDefinition;
import org.springframework.beans.support.SortDefinition;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;


public class HibernateTemplateExtended extends HibernateTemplate {
	
	private CriteriaBuilder criteriaBuilder = new IgnoreCaseCriteriaBuilder();

	public HibernateTemplateExtended() {
		super();
	}

	public HibernateTemplateExtended(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public HibernateTemplateExtended(SessionFactory sessionFactory,
			boolean allowCreate) {
		super(sessionFactory, allowCreate);
	}

	public boolean isDirty() {
		Boolean executeWithNativeSession = executeWithNativeSession((new HibernateCallback<Boolean>() {
			public Boolean doInHibernate(final Session session) throws HibernateException {
				return session.isDirty();
			}
		}));
		return executeWithNativeSession.booleanValue();
	}
	
	//-------------------------------------------------------------------------
	// Convenience finder methods for named queries
	// returning a single object or null if not found
	//-------------------------------------------------------------------------


	public Object findObjectByNamedParam(String queryString, String paramName, Object value)
	throws DataAccessException {

		return findObjectByNamedParam(queryString, new String[] {paramName}, new Object[] {value});
	}

	public Object findObjectByNamedParam(final String queryString, final String[] paramNames, final Object[] values)
	throws DataAccessException {

		if (paramNames.length != values.length) {
			throw new IllegalArgumentException("Length of paramNames array must match length of values array");
		}
		@SuppressWarnings("rawtypes")
		List res =  executeWithNativeSession(new HibernateCallback<List>() {
			public List doInHibernate(Session session) throws HibernateException {
				Query queryObject = session.createQuery(queryString);
				prepareQuery(queryObject);
				queryObject.setMaxResults(1);
				queryObject.setFetchSize(1);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						applyNamedParameterToQuery(queryObject, paramNames[i], values[i]);
					}
				}
				return queryObject.list();
			}
		});
		if (CollectionUtils.isEmpty(res)) return null;
		return res.get(0);

	}

	public Object findObjectByNamedQuery(String queryName) throws DataAccessException {
		return findObjectByNamedQuery(queryName, (Object[]) null);
	}

	public Object findObjectByNamedQuery(String queryName, Object value) throws DataAccessException {
		return findObjectByNamedQuery(queryName, new Object[] {value});
	}

	public Object findObjectByNamedQuery(final String queryName, final Object[] values) throws DataAccessException {
		@SuppressWarnings("rawtypes")
		List res = (List) executeWithNativeSession(new HibernateCallback<List>() {
			public List doInHibernate(Session session) throws HibernateException {
				Query queryObject = session.getNamedQuery(queryName);
				prepareQuery(queryObject);
				queryObject.setMaxResults(1);
				queryObject.setFetchSize(1);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						queryObject.setParameter(i, values[i]);
					}
				}
				return queryObject.list();
			}
		});
		if (CollectionUtils.isEmpty(res)) return null;
		return res.get(0);
	}

	public Object findObjectByNamedQueryAndNamedParam(String queryName, String paramName, Object value)
			throws DataAccessException {

		return findObjectByNamedQueryAndNamedParam(queryName, new String[] {paramName}, new Object[] {value});
	}

	public Object findObjectByNamedQueryAndNamedParam(
			final String queryName, final String[] paramNames, final Object[] values)
			throws DataAccessException {

		if (paramNames != null && values != null && paramNames.length != values.length) {
			throw new IllegalArgumentException("Length of paramNames array must match length of values array");
		}

		@SuppressWarnings("rawtypes")
		List res = executeWithNativeSession(new HibernateCallback<List>() {
			public List doInHibernate(Session session) throws HibernateException {
				Query queryObject = session.getNamedQuery(queryName);
				prepareQuery(queryObject);
				queryObject.setMaxResults(1);
				queryObject.setFetchSize(1);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						applyNamedParameterToQuery(queryObject, paramNames[i], values[i]);
					}
				}
				return queryObject.list();
			}
		});
		
		if (CollectionUtils.isEmpty(res)) return null;
		return res.get(0);

	}
	
	public List<?> find (final QueryDefinition queryDef, final int firstResult, final int maxResults) {
		return executeWithNativeSession(new HibernateCallback<List<?>>() {
			public List<?> doInHibernate(Session session) throws HibernateException {
				Query queryObject;
				if (isNamedQuery(queryDef.getQuery())) {
					queryObject = session.getNamedQuery(queryDef.getQuery());
				} else {
					queryObject = session.createQuery(queryDef.getQuery());
				}
				prepareQuery(queryObject);
				if (firstResult >= 0) {
					queryObject.setFirstResult(firstResult);
				}
				if (maxResults > 0) {
					queryObject.setFetchSize(maxResults);	// aha added, shouldn't it be everywhere (DetachedCriteria, e.t.c. ? )
					queryObject.setMaxResults(maxResults);
				}
				if (ObjectUtils.isEmpty(queryDef.getParamNames())) {
					// query with '?' as paramter placeholder
					if (queryDef.getValues() != null) {
						for (int i = 0; i < queryDef.getValues().length; i++) {
							queryObject.setParameter(i, queryDef.getValues()[i]);
						}
					}
				} else if (queryDef.getValues() != null ){
					// query with namedParamters[]
					for (int i = 0; i < queryDef.getValues().length; i++) {
						applyNamedParameterToQuery(queryObject, 
								queryDef.getParamNames()[i], queryDef.getValues()[i]);
					}
				}
				return queryObject.list();
			}
		});
	}

	/**
	 * Extended all find by examples to support a sort order
	 */
	
	@SuppressWarnings("rawtypes" )
	public List findByExample(Object exampleEntity, final SortDefinition sort) throws DataAccessException {
		return findByExample(null, exampleEntity, sort, -1, -1);
	}

	@SuppressWarnings("rawtypes")
	public List findByExample(String entityName, Object exampleEntity, final SortDefinition sort) throws DataAccessException {
		return findByExample(entityName, exampleEntity, sort, -1, -1);
	}

	@SuppressWarnings("rawtypes")
	public List findByExample(Object exampleEntity, final SortDefinition sort, int firstResult, int maxResults) throws DataAccessException {
		return findByExample(null, exampleEntity, sort, firstResult, maxResults);
	}

	@SuppressWarnings("rawtypes")
	public List findByExample(
			final String entityName, final Object exampleEntity, final SortDefinition sort, final int firstResult, final int maxResults)
			throws DataAccessException {

		Assert.notNull(exampleEntity, "Example entity must not be null");
		return executeWithNativeSession(new HibernateCallback<List>() {
			public List doInHibernate(Session session) throws HibernateException {

				Criteria executableCriteria = criteriaBuilder.buildCriteria(entityName, exampleEntity, sort, session);

				prepareCriteria(executableCriteria);
				if (firstResult >= 0) {
					executableCriteria.setFirstResult(firstResult);
				}
				if (maxResults > 0) {
					executableCriteria.setMaxResults(maxResults);
				}
				return executableCriteria.list();
			}
		});
	}

	@SuppressWarnings("rawtypes")
	public Object findObjectByExample(Object exampleEntity) throws DataAccessException {
		List res = findByExample(exampleEntity);
		if (CollectionUtils.isEmpty(res)) return null;
		return res.get(0);
	}

	/**
	 * @return the criteriaBuilder
	 */
	public CriteriaBuilder getCriteriaBuilder() {
		return criteriaBuilder;
	}

	/**
	 * @param criteriaBuilder the criteriaBuilder to set
	 */
	public void setCriteriaBuilder(CriteriaBuilder criteriaBuilder) {
		this.criteriaBuilder = criteriaBuilder;
	}

	private boolean isNamedQuery(String query) {
		if (! StringUtils.hasText(query)) {
			return false;
		} else {	// trying to find out if it is a named query
			String[] check = new String[] {"select ", "from "};		// startstrings that indicate that it is a select statement
			String q = StringUtils.trimLeadingWhitespace(query);
			for (int i = 0; i < check.length; i++) {
				if (q.regionMatches(true, 0, check[i], 0, check[i].length())) return false;
			}
			return true;
		}
	}


}
