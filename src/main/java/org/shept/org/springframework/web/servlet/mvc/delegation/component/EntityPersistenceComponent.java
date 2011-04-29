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

package org.shept.org.springframework.web.servlet.mvc.delegation.component;

import javax.persistence.Entity;
import javax.servlet.http.HttpServletRequest;

import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken;
import org.shept.org.springframework.web.servlet.mvc.support.ModelUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.web.servlet.ModelAndView;

/** 
 * @version $$Id: EntityPersistenceComponent.java 88 2010-11-30 13:46:17Z aha $$
 *
 * @author Andi
 *
 */
public class EntityPersistenceComponent extends AbstractEditingComponent{

	@Override
	protected ModelAndView doCancel(HttpServletRequest request, ComponentToken token) {
		if (this.reloadOnCancel) {
			// TODO make persistence work for single entities
			// component.refresh(true);
		} else {
			// component.copyBackup()
		}
		return modelRedirectClip(request, token);
	}

	@Override
	protected ModelAndView doSave(final HttpServletRequest request, ComponentToken token) throws Exception {
		final Object model = ModelUtils.unwrapIfNecessary(token.getComponent());
		doInTransactionIfAvailable(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus status) {
				doSaveModel(model);
				return null;
			}
		});
		return modelRedirectClip(request, token);
	}

	protected ModelAndView doDelete(final HttpServletRequest request, ComponentToken token) throws Exception {
		final Object model = ModelUtils.unwrapIfNecessary(token.getComponent());
		doInTransactionIfAvailable(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus status) {
				doDeleteModel(model);
				return null;
			}
		});
		return modelRedirectClip(request, token);
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.component.ComponentHandler#supports(java.lang.Class)
	 */
	public boolean supports(Object commandObject) {
		Object model = ModelUtils.unwrapIfNecessary(commandObject);
		Entity ann = AnnotationUtils.findAnnotation(model.getClass(), Entity.class);
		return (ann != null);
		}
	}

