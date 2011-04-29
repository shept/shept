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

package org.shept.org.springframework.web.servlet.mvc.support;

import org.shept.util.StringUtilsExtended;
import org.springframework.util.StringUtils;

/** 
 * @version $$Id: RequestValueUtils.java 43 2010-09-29 15:00:49Z aha $$
 *
 * @author Andi
 *
 */
public class RequestValueUtils {
	
	public static final String PARAMETER_VALUE_SEPARATOR = "::";
	
	public static final String SAVE_PREFIX = "save";
	
	public static final String DELETE_PREFIX = "delete";
	
	/**
	 * 
	 * @version $$Id: RequestValueUtils.java 43 2010-09-29 15:00:49Z aha $$
	 *
	 * @author Andi
	 *
	 */
	public static class ChainParameter{

		public ChainParameter(String name) {
			this.name=name;
		}

		public ChainParameter(String name, String param) {
			this.name=name;
			if (getIndexParameter(param) != -1) {
				this.index = getIndexParameter(param);
			} else {
				this.param=param;
			}
		}

		public ChainParameter(String name, String param, int index) {
			this.name=name;
			this.index=index;
			this.param=param;
		}
		
		private String name;
		
		private int index = -1;
		
		private String param;

		/**
		 * @return the index
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * @return the index
		 */
		public boolean isIndexed() {
			return index > - 1;
		}

		/**
		 * @return the param
		 */
		public String getSelectorParam() {
			return param;
		}
		
		/**
		 * 
		 */
		public String getSelector() {
			return StringUtilsExtended.getReadAccessor(param);
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * @return the subFormName
		 */
		public String getSubFormName() {
			return name;
		}
		
	}
		
	public static class KeyValueParameter {
		
		private String key;
		
		private String value;

		/**
		 * @return the key
		 */
		public String getKey() {
			return key;
		}

		/**
		 * @param key the key to set
		 */
		public void setKey(String key) {
			this.key = key;
		}

		/**
		 * @return the value
		 */
		public String getValue() {
			return value;
		}

		/**
		 * @param value the value to set
		 */
		public void setValue(String value) {
			this.value = value;
		}
	}

	public static int getIndexParameter(String param) {
		if (!StringUtils.hasText(param)) {
			return -1;
		};
		try {
			return Integer.valueOf(param);
		} catch (NumberFormatException ex) {
			return -1;
		}
	}
	
	/**
	 * Support for join parameters style
	 * example: ...submitchain_users::getLoginLogs::0
	 * 
	 * @param
	 * @return
	 *
	 * @param param
	 * @return
	 */
	public static ChainParameter getChainParameter(String param)  {
		String[] parms = StringUtils.delimitedListToStringArray(param, PARAMETER_VALUE_SEPARATOR);
		ChainParameter cp;
		if (parms.length ==1) {
			// parameter contains only subform name
			cp = new ChainParameter(parms[0]);
		} else if (parms.length == 2) {
			// subform name and additional parameter
			cp = new ChainParameter(parms[0], parms[1]);			
		} else {
			// subform name, parameter and selection index
			cp = new ChainParameter(parms[0], parms[1],getIndexParameter(parms[2]));
		}
		return cp; 
	}
	
	public static KeyValueParameter getKeyValueParameter(String param) {
		String[] parms = StringUtils.delimitedListToStringArray(param, PARAMETER_VALUE_SEPARATOR);
		KeyValueParameter kvp = new KeyValueParameter();
		if (parms.length > 0) {
			kvp.key = parms[0];
		}
		if (parms.length > 1) {
			kvp.value = parms[1];
		}
		return kvp;
	}

}
 