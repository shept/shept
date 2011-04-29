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

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import org.shept.util.HelperUtils;
import org.springframework.util.StringUtils;

/** 
 * @version $$Id: LastModifiedSessionFormCache.java 44 2010-09-30 14:32:31Z aha $$
 *
 * @author Andi
 *
 */
public class LastModifiedSessionFormCache extends SimpleSessionFormCache {
	
	public class WrappedObject {
		
		private Object formObject;
		
		private long lastModified = -1L;
		
		private String checksum = "";

		/**
		 * @return the formObject
		 */
		public Object getFormObject() {
			return formObject;
		}

		/**
		 * @param formObject the formObject to set
		 */
		public void setFormObject(Object formObject) {
			this.formObject = formObject;
			String checksum = calculateChecksum(formObject);
			setChecksum(checksum);
			touchLastModified();
		}

		/**
		 * @return the lastModified
		 */
		public long getLastModified() {
			return lastModified;
		}

		/**
		 * @param lastModified the lastModified to set
		 */
		public void setLastModified(long lastModified) {
			this.lastModified = lastModified;
		}

		/**
		 * @return the md5
		 */
		public String getChecksum() {
			return checksum;
		}

		/**
		 * @param checksum to set
		 */
		public void setChecksum(String checksum) {
			this.checksum = checksum;
		}
		
		public void touchLastModified() {
	 		Calendar cal = Calendar.getInstance();
	 		cal.set(Calendar.MILLISECOND, 0);
	 		this. lastModified = cal.getTime().getTime();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.SessionFormCache#saveForm(javax.servlet.http.HttpServletRequest, java.lang.String, java.lang.Object)
	 */
	public void saveForm(HttpServletRequest request,
			String formAttrName, Object command)  {
		WrappedObject wrapped = new WrappedObject();
		wrapped.setFormObject(command);
		super.saveForm(request, formAttrName, wrapped);
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.SessionFormCache#getForm(javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public Object getForm(HttpServletRequest request,
			String formAttrName)  {
		WrappedObject wrapped = (WrappedObject) super.getForm(request, formAttrName);
		return (wrapped == null ? null : wrapped.getFormObject());
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.formcache.AbstractSessionFormCache#getLastModified(javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public long getLastModified(HttpServletRequest request, String formAttrName) {
		try {
			WrappedObject wrapped = (WrappedObject) super.getForm(request, formAttrName);
			if (wrapped == null) {
				logger.warn("Checksum not calculated, no wrapped command object found");
				return -1L;
			}
			String checksum = calculateChecksum(wrapped.getFormObject());
			if (logger.isDebugEnabled()) {
				logger.debug("Calculated checksum is " + checksum);
			}
			if (! StringUtils.hasText(checksum)) {
				return -1L;
			}
			if (checksum.equals(wrapped.getChecksum())) {
				if (logger.isDebugEnabled()) {
					logger.debug("Calculated checksum equals to old checksum");
				}
				return (wrapped.getLastModified());
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Checksum and LastModified set to new value");
			}
	 		wrapped.setChecksum(checksum);
	 		wrapped.touchLastModified();
			return wrapped.getLastModified();

		} catch (Exception ex) {
			return -1L;
		}
	}
	
	protected String calculateChecksum(Object object)  {
		byte[] stream;
		try {
			stream = HelperUtils.serialize(object);
//			return Md5HashUtils.getHashString(stream);
			return String.valueOf(stream.hashCode());
		} catch (Exception ex) {
	 			logger.error("Error while serializing the command object " + object.toString(), ex);
			return "";
		}
	}

	
}
