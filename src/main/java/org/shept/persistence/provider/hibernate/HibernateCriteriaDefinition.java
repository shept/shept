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

import org.hibernate.criterion.DetachedCriteria;
import org.shept.beans.support.FilterDefinition;
import org.springframework.beans.support.SortDefinition;

/** 
 * The HibernateCriteriaDefinition interface is very flexible way to create extensible FilterDefinitions.
 * 
 *  Its typical usage is to support FilterDefinition in a search filter user interface.
 *  
 *  This is an example implementation to search for User Logins:
 *  <p>
 *  
 *  <code>
 *	public DetachedCriteria getCriteria(SortDefinition sortDefinition) {</br>
 *		DetachedCriteria crit = DetachedCriteria.forClass(LoginLog.class);</br>
 *		</br>
 *		if (StringUtils.hasText(getUserName())) {</br>
 *			crit.add(Restrictions.eq("userName", getUserName()));</br>
 *		}</br>
 *		</br>
 *		if (StringUtils.hasText(getRemoteAddr())) {</br>
 *			crit.add(Restrictions.eq("remoteAddr", getRemoteAddr()));</br>
 *		}</br>
 *		</br>
 *		if (getBsuccess() != null) {</br>
 *			crit.add(Restrictions.eq("bsuccess", getBsuccess()));</br>
 *		}</br>
 *		</br>
 *		if (getBlogoutMissing()) {</br>
 *			crit.add(Restrictions.isNull("dateLogout"));</br>
 *		}</br>
 *		</br>
 *		if (getDateFrom() != null) {</br>
 *			crit.add(Restrictions.gt("dateLogin", getDateFrom()));</br>
 *		}</br>
 *		</br>
 *		if (getDateTill() != null) {</br>
 *			Calendar till = Calendar.getInstance();</br>
 *			till.setTime(getDateTill().getTime());</br>
 *		    till.add(Calendar.DAY_OF_YEAR, 1);</br>
 *			crit.add(Restrictions.lt("dateLogin", getDateTill()));</br>
 *		}</br>
 *		</br>
 *		if (getUserAgentId() != null) {</br>
 *			crit.add(Restrictions.eq("agentId", getUserAgentId()));</br>
 *		}</br>
 *		</br>
 *		// set the default sorting if no sorting is specified</br>
 *		if (sortDefinition != null &&  ! StringUtils.hasText(sortDefinition.getProperty())) {</br>
 *			BeanUtils.copyProperties(getDefaultSort(), sortDefinition);</br>
 *		}</br>
 *		</br>
 *		// set sort criteria from FormFilter</br>
 *		if (null != sortDefinition && StringUtils.hasText(sortDefinition.getProperty())) {</br>
 *			if (sortDefinition.isAscending())</br>
 *				crit.addOrder(Order.asc(sortDefinition.getProperty()));</br>
 *			else</br>
 *				crit.addOrder(Order.desc(sortDefinition.getProperty()));</br>
 *		}</br>
 *</br>
 *		return crit;</br>
 *	}</br>
 *  </code>
 *  </p>
 *  
 *  
 * @version $Rev: 34 $
 * @author Andi
 * 
 */
public interface HibernateCriteriaDefinition extends FilterDefinition {
	
	public DetachedCriteria getCriteria(SortDefinition sortDefinition);
	
}
