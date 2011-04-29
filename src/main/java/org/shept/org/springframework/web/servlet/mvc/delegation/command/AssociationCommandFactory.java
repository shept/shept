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

package org.shept.org.springframework.web.servlet.mvc.delegation.command;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shept.beans.support.ReloadableAssociation;
import org.shept.org.springframework.beans.support.Refreshable;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.ChainConfigurationException;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.TargetConfiguration;
import org.shept.org.springframework.web.servlet.mvc.support.ModelUtils;
import org.shept.persistence.ModelCreation;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Andreas Hahn
 *
 */
public class AssociationCommandFactory extends AbstractCommandFactory implements CommandFactory {

	/** Logger that is available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());
	
	private String relation;

	/**
	 * 
	 * @see org.shept.org.springframework.web.servlet.mvc.delegation.command.CommandFactory#getCommand(org.shept.org.springframework.web.servlet.mvc.delegation.ComponentConfiguration, java.lang.Object, java.lang.String)
	 * 
	 * @param model
	 * @param jp
	 * @param mth
	 * @return
	 */
	@Override
	public Object getCommand(TargetConfiguration config, Object model) {
		
		if (model == null) {
			throw new ChainConfigurationException("Configuration error in '" 
					+ config.getChainNameDisplay() + "'. <Null> model is not allowed for associations");
		}

		// param contains the subForm name that will be overriden if there is an relation defined
		String assoc = relation == null ? config.getTo().getBeanName() : relation;
		if (assoc != null && assoc.startsWith("get")) {
			assoc = relation.substring("get".length());
		}
		String methodGetter = "get" + StringUtils.capitalize(assoc);
		Method mth = ReflectionUtils.findMethod(model.getClass(), methodGetter);
		if (mth == null) {
			return null;
		}
		Class<?> rt = Object.class;
		if (null != mth) {
			rt = 	mth.getReturnType();
		}
		if (List.class.isAssignableFrom(rt)) {
			return createWrappedList(config, mth, model);
		} 
		// TODO better checks for the different result conditions ...
		else if (Collection.class.isAssignableFrom(rt)){
			throw new ChainConfigurationException("Collection types other than 'List' are currently not supported. " +
				"Type " + rt.getClass().toString() + " is not supported. Use List instead");
		}
		// load a single entity object
		else {
			return createWrappedEntity(config, mth, model);			
		}
	}

	/**
	 * @param mth
	 * @param model
	 * @param
	 * @return
	 */
	protected Object createWrappedEntity(TargetConfiguration config, Method mth, Object model) {
		Object target = ReflectionUtils.invokeMethod(mth, model);

		// this code resolves Hibernate proxies into the real objects
		// but its an additional dependency on hibernate and shouldn't be necessary

//		if (target instanceof HibernateProxy) {
//			target = ((HibernateProxy)target).getHibernateLazyInitializer().getImplementation();
//		}

		Object command = ModelUtils.wrapIfNecessary(target);
		return command;
	}

	/**
	 * 
	 * @param cw
	 * @param mth
	 * @param model
	 * @param
	 * @return
	 */
	protected Object createWrappedList(TargetConfiguration config, Method mth, Object model) {
		ReloadableAssociation ass = createFilter(config, mth, model);
		Refreshable chainedPageHolder = getPageHolderFactory().getObject();
		chainedPageHolder.setFilter(ass);
		return chainedPageHolder;
	}

	/**
	 * @param cc
	 * @param mth
	 * @param model
	 * @param newTemplate
	 * @return
	 */
	protected ReloadableAssociation createFilter(TargetConfiguration cc, Method mth, Object model) {
		ReloadableAssociation ass = new ReloadableAssociation();
		ass.setAssociationMethod(mth);
		ass.setSourceModel(model);
		if (!cc.getDisabledActions().isCreate()) {
			ModelCreation mod = getNewModelTemplate(cc.getEntityClass(), model);
			ass.setNewModelTemplate(mod);			
		}
		return ass;
	}
	
	/**
	 * 
	 * @return the newModelTemplate if any
	 */
	public ModelCreation getNewModelTemplate(Class<?> clazz, Object model ) {
		//TODO improve this to read an optional initialization message code from configuration
		String initString = "???";
		return ModelUtils.getNewModelTemplate(clazz, model, initString);
	}

	/**
	 * @param relation the relation to set
	 */
	public void setRelation(String relation) {
		this.relation = relation;
	}


}
