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

package org.shept.org.springframework.web.servlet.mvc.delegation.configuration;

import org.shept.org.springframework.web.servlet.mvc.delegation.command.AssociationCommandFactory;
import org.shept.org.springframework.web.servlet.mvc.delegation.command.CommandFactory;
import org.shept.org.springframework.web.servlet.mvc.delegation.command.EditItemCommandFactory;
import org.shept.org.springframework.web.servlet.mvc.delegation.command.RefreshableListCommandFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Andreas Hahn
 *
 */
public class ChainConfiguration extends TargetConfiguration
	implements InitializingBean, BeanNameAware {
	
	private SegmentConfiguration from;
	
	private String relation;
	

	/**
	 * 
	 */
	protected CommandFactory createCommandFactory() {
		AssociationCommandFactory acf = new AssociationCommandFactory();
		if (relation != null) {
			acf.setRelation(relation);
			return acf;			
		}
		if (getFilterClass() != null) {
			return new RefreshableListCommandFactory();
		}
		if (getTo() instanceof DataGridConfiguration) {
			return acf;
		} else {
			return new EditItemCommandFactory();
		}
	}

	/**
	 * 
	 */
	protected CommandFactory createCommandFactory_old() {
		if (getTo() instanceof DataGridConfiguration) {
			if (relation != null || getFilterClass() == null ) {
				AssociationCommandFactory acf = new AssociationCommandFactory();
				acf.setRelation(relation);
				return acf;
			} else {
				return new RefreshableListCommandFactory();
			}			
		} else {
			return new EditItemCommandFactory();
		}
	}

	/**
	 * @return the from
	 */
	public SegmentConfiguration getFrom() {
		return from;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(SegmentConfiguration from) {
		this.from = from;
	}

	public String getChainNameDisplay() {
		String name = "Chain" ;
		if (getBeanName() != null) {
			name = name + " " + getBeanName();
		}
		name = name + " ( from " + from.getBeanName() + " to " + getTo().getBeanName() + ")";
		return name;
	}
	
	/**
	 * @return the association
	 */
	public String getRelation() {
		return relation;
	}

	/**
	 * @param association the association to set
	 */
	public void setRelation(String relation) {
		this.relation = relation;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		Assert.notNull(from);
		if (relation != null) {
			if (! relation.startsWith("get")) {
				relation = "get" + StringUtils.capitalize(relation);
			}
			Assert.notNull(from.getEntityClass(), "Chain configuration for '" + getChainNameDisplay() + "' does not specify a source entity");
			Assert.notNull(ReflectionUtils.findMethod(from.getEntityClass(), relation), 
				"Chain configuration for '" + getChainNameDisplay() + "' specifies an invalid relation ('"
				+ relation + "')");
		}
	}

}
