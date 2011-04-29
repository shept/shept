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

package org.shept.org.springframework.web.servlet.mvc.formcache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.support.WebApplicationObjectSupport;

/** 
 * @version $$Id: SimpleSessionFormCache.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class SimpleSessionFormCache extends WebApplicationObjectSupport implements SessionFormCache {
	
	public static String SESSION_FORM_CACHE_ATTIRBUTE = "sessionFormCacheAttribute";
	
	public static int MAX_CACHE_SIZE = 5;		// max number of forms to be held in the cache
	
	private int maxCacheSize = MAX_CACHE_SIZE;		// the number of forms which can be held max in the session
	
	@SuppressWarnings("hiding")
	private class SizeLimitedCache<String, Object> extends LinkedHashMap<String, Object> {

		private static final long serialVersionUID = 1L;

		/* (non-Javadoc)
		 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
		 */
		@Override
		protected boolean removeEldestEntry(Entry<String, Object> eldest) {
			if (size() > maxCacheSize) {
				if (logger.isDebugEnabled()) {
					logger.debug("Removing eldest cached form from session [" + eldest.getKey() + "]" ) ;
				}
				return true;			
			}
			return false;
		}
	}

	public void clearForm(HttpServletRequest request, String formAttrName)
			throws Exception {
		// Session-form mode: retrieve form object from HTTP session attribute.
		
		Map<String, Object> cache = getFormCache(request, false, false);
		if (cache == null) return;

		if (logger.isDebugEnabled()) {
				logger.debug("Removing form form from session cache [" + formAttrName + "]");
		}
		cache.remove(formAttrName);
		return;
	}
	
	public void clearOtherForms(HttpServletRequest request, String formAttrName)
			throws Exception {
		// Session-form mode: retrieve form object from HTTP session attribute.
		
		Map<String, Object> cache = getFormCache(request, false, false);
		if (cache == null) return;

		if (logger.isDebugEnabled()) {
				logger.debug("Removing all forms from session cache except [" + formAttrName + "]");
		}
		
		for (String key : cache.keySet()) {
			if (!key.equals(formAttrName) )
				cache.remove(key);
		}
		return;
	}
	
	public void clearCache(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Removing the cache from the session");
		}
			session.removeAttribute(SESSION_FORM_CACHE_ATTIRBUTE);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String,Object> getFormCache(HttpServletRequest request, boolean required, boolean create)  {
		// Session-form mode: retrieve form object from HTTP session attribute.
		HttpSession session = request.getSession(create);
		if (session == null) {
			if (! required) {
				return null;
			} else {
				throw new SessionFormCacheException("Must have session when trying to bind (in session-form mode)");
			}
		}
		Map<String,Object> cache = (Map<String, Object>) session.getAttribute(SESSION_FORM_CACHE_ATTIRBUTE);
		if (cache == null ) {
			cache = new SizeLimitedCache<String,Object>();
			session.setAttribute(SESSION_FORM_CACHE_ATTIRBUTE, cache);
		}
		return cache;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.SessionFormCache#getLastModified(javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public long getLastModified(HttpServletRequest request, String formAttrName) {
		return -1L;
	}

	public void saveForm(HttpServletRequest request, String formAttrName, Object command) {
				if (logger.isDebugEnabled()) {
					logger.debug("Caching form in session [" + formAttrName + "] to: " + command);
				}
				Map<String, Object> cache = getFormCache(request, true, true);
				if ( command != null) {
					cache.put(formAttrName, command);
				} else {
					cache.remove(formAttrName);
				}
			}

	public Object getForm(HttpServletRequest request, String formAttrName)  {
	
		Map<String, Object> cache = getFormCache(request, false, false);
		if (cache == null) {
			return null;
		}
		Object sessionFormObject = cache.get(formAttrName);
		if (sessionFormObject == null) {
			return null;
		}
		
		return sessionFormObject;
	}

	/**
	 * @return the maxCacheSize
	 */
	public int getMaxCacheSize() {
		return maxCacheSize;
	}

	/**
	 * @param maxCacheSize the maxCacheSize to set
	 */
	public void setMaxCacheSize(int maxCacheSize) {
		this.maxCacheSize = maxCacheSize;
	}

}
