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

package org.shept.org.springframework.beans;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import org.springframework.util.StringUtils;

/** 
 * @version $$Id: CustomCalendarEditor.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class CustomCalendarEditor extends PropertyEditorSupport {
	
	private DateFormat dateFormat;
	private boolean allowEmpty;

	public CustomCalendarEditor(DateFormat dateFormat, boolean allowEmpty) {
		this.dateFormat = dateFormat;
		this.allowEmpty = allowEmpty;
	}

	@Override
	public String getAsText() {
		Calendar value = (Calendar) getValue();
		return (null != value ? this.dateFormat.format(value.getTime()) : "");
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
	    if (this.allowEmpty && !StringUtils.hasText(text)) {
	      setValue(null);
	    } else {
	      try {
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(this.dateFormat.parse(text));
	        setValue(cal);
	    } catch (ParseException ex) {
	      throw new IllegalArgumentException();
	    }
	  }
	}	    
}	
