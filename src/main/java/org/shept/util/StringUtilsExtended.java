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

package org.shept.util;

import org.springframework.util.StringUtils;

/** 
 * @version $$Id: StringUtilsExtended.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public abstract class StringUtilsExtended extends StringUtils {
	
	public static boolean equalsIgnoreNulls (String s1, String s2) {
		if (hasText(s1) && hasText(s2)) {
			return s1.equals(s2);
		}
		return (! (hasText(s1) || hasText(s2)));
	}

	public static boolean equalsIgnoreNullsAndCase (String s1, String s2) {
		if (hasText(s1) && hasText(s2)) {
			return s1.equalsIgnoreCase(s2);
		}
		return (! (hasText(s1) || hasText(s2)));
	}
	
	public static String getReadAccessor(String name) {
		if (hasText(name)) {
			return "get" + StringUtils.capitalize(name);
		}
		return name;
	}

}
