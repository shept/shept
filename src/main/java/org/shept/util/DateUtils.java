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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {

	
	/**
	 * Merge two calendars, one with the day date component and the other
	 * with a time component. The resutling calendar is of the date's
	 * locale and timezone. If the time argument is null the method returns
	 * only the date's date with 00:00:00 time. 
	 * If the date is null current date is used
	 * 
	 * @param date
	 * @param time
	 * @return
	 */
	public static Calendar merge(Calendar date, Calendar time) {
		Calendar resCal = Calendar.getInstance();
		if (date == null && time == null) return resCal;
		if (date != null) resCal = (Calendar) date.clone();
		resCal.clear();
		if (null == time) {
			resCal.set(
					date.get(Calendar.YEAR),
					date.get(Calendar.MONTH),
					date.get(Calendar.DAY_OF_MONTH));
			return resCal;
		}
		resCal.set(
				date.get(Calendar.YEAR),
				date.get(Calendar.MONTH),
				date.get(Calendar.DAY_OF_MONTH),
				time.get(Calendar.HOUR_OF_DAY),
				time.get(Calendar.MINUTE),
				time.get(Calendar.SECOND));
		return resCal;
	}
	
	/**
	 * Merge a date's date part with another dates time part
	 * @param date
	 * @param time
	 * @return
	 * @deprecated
	 */
	public static Date merge(Date date, Date time) {
		Calendar dateCal = Calendar.getInstance();
		dateCal.setTime(date);
		if (time == null ) return merge(dateCal, null).getTime();
		Calendar timeCal = Calendar.getInstance();
		timeCal.setTime(time);
		return merge(dateCal, timeCal).getTime();
	}
	
	/*
	 * 'Merge' a date's day part with another days time part
	 */
	@Deprecated
	public static Date merge_Old(Date date, Date time) {
		Calendar resCal = GregorianCalendar.getInstance();
		Calendar dayCal = GregorianCalendar.getInstance();
		Calendar timeCal = GregorianCalendar.getInstance();
		dayCal.setTime(date);
		timeCal.setTime(time);
		resCal.clear();		// clear out any current-time settings
		resCal.set(
				dayCal.get(Calendar.YEAR), 
				dayCal.get(Calendar.MONTH), 
				dayCal.get(Calendar.DAY_OF_MONTH), 
				timeCal.get(Calendar.HOUR_OF_DAY), 
				timeCal.get(Calendar.MINUTE),
				0);
		return (resCal.getTime());
	}
	
	/**
	 * Return a calendars day-part only
	 * @param date
	 * @return
	 */
	public static Calendar datePart(Calendar date){
		return merge(date, null);
	}
	
	/**
	 * Return a date's day part only
	 * @param date
	 * @return
	 */
	public static Date datePart(Date date) {
		return merge(date, null);
	}

	/*
	 * Return a dates day-part only
	 */
	@Deprecated
	public static Date datePart_Old(Date date) {
		Calendar resCal = GregorianCalendar.getInstance();
		Calendar dayCal = GregorianCalendar.getInstance();
		dayCal.setTime(date);
		resCal.clear();			// clear out any existing settings
		resCal.set(
				dayCal.get(Calendar.YEAR), 
				dayCal.get(Calendar.MONTH), 
				dayCal.get(Calendar.DAY_OF_MONTH));
		return (resCal.getTime());
	}
	
	@Deprecated
	public static Date add(Date d, int field, int amount) {
		Calendar resCal = GregorianCalendar.getInstance();
		resCal.setTime(d);
		resCal.add(field, amount);
		return resCal.getTime();
	}
	
	/**
	 * Return true if both calenday date's are of the same day
	 * @param day1
	 * @param day2
	 * @return
	 */
	public static boolean isSameDay(Calendar day1, Calendar day2) {
		return datePart(day1).compareTo(datePart(day2)) == 0;
	}
	

	/** 
	 * Return the calendars minute count since midnight
	 * @param calendar
	 * @return
	 */
	public static Integer minOfDay(Calendar calendar) {
		return calendar.get(Calendar.HOUR_OF_DAY * 60 + Calendar.MINUTE);
	}
	
	@Deprecated
	public static Integer minOfDay(Date d) {
		Calendar c = GregorianCalendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.HOUR_OF_DAY * 60 + Calendar.MINUTE);
	}

}
