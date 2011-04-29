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

import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.shept.beans.support.FilterDefinition;
import org.shept.org.springframework.beans.support.PageableList;
import org.shept.org.springframework.beans.support.Refreshable;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken;
import org.shept.org.springframework.web.servlet.mvc.support.RequestValueUtils;
import org.shept.persistence.ModelCreation;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/** 
 * @version $$Id: ListPersistenceComponent.java 110 2011-02-21 09:16:15Z aha $$
 *
 * @author Andi
 *
 */
public class ListPersistenceComponent extends AbstractEditingComponent {

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.delegation.component.AbstractPersistenceComponent#doCancel(javax.servlet.http.HttpServletRequest, org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	@Override
	protected ModelAndView doCancel(HttpServletRequest request, ComponentToken token) throws Exception {
		boolean reload = isReloadOnCancel();
		PageableList<?> pagedList = (PageableList<?>) token.getComponent();
		Refreshable filter = null;
		if (token.getComponent() instanceof Refreshable) {
			filter = (Refreshable) token.getComponent();	
		} else {
			reload = false;
		}
		
		if (reload) {
			// set the load size again to ensure that we will see the same elements after screen updates
			Integer loadSize = Math.max(
				filter.getSourceProvider().getLoadSize(), pagedList.getLastElementOnPage() + 1);
			filter.getSourceProvider().setLoadSize(loadSize);
			filter.refresh();
		} else {
			if (logger.isWarnEnabled()) {
				logger.warn("When we can't ask the provider (filter) to deliver the collection again " +
						"then we need to keep a deepCopy of the collection before making any changes." +
						"This is quite an expensive operation and can't be recommended as we " +
						"would have to do it just in case it might be needed later. " +
						"So we go on with the modified collection here");
			}
		}
		return modelRedirectClip(request, token);
		
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.delegation.component.AbstractPersistenceComponent#doDelete(javax.servlet.http.HttpServletRequest, org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	@Override
	protected ModelAndView doSave(HttpServletRequest request, ComponentToken token) throws Exception {
		PageableList<?> pagedList = (PageableList<?>) token.getComponent();
		if (pagedList.getFirstElementOnPage() < 0 ||
				pagedList.getFirstElementOnPage() >= pagedList.getNrOfElements()) {
			return modelRedirectClip(request, token);			
		}

		String saveTransaction = getCustomSaveTransaction(request, token);
		if (StringUtils.hasText(saveTransaction)) {
			doCustomTransaction(saveTransaction, pagedList, null);
			return modelRedirectClip(request, token);	
		}

		// saving all models in the visible part of the listHolder
		// this is the default behaviour

		// this didn't work for empty list !?
		// if (pagedList.getLastElementOnPage() - pagedList.getFirstElementOnPage() >= 0 ) {
		doSaveList(request, pagedList);
		return modelRedirectClip(request, token);
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.delegation.component.AbstractPersistenceComponent#doSave(javax.servlet.http.HttpServletRequest, org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	@Override
	protected ModelAndView doDelete(HttpServletRequest request, ComponentToken token) throws Exception {
		PageableList<?> pagedList = (PageableList<?>) token.getComponent();
		int idx = RequestValueUtils.getIndexParameter(token.getToken().getValue());
		if (!pagedList.isVisible(idx)) {
			if (logger.isErrorEnabled()) {
				logger.error("Invalid or Missing Integer Row-number argument value for action " + token.toString());
			}
	 		return modelRedirectClip(request, token);
		}
		
		String deleteTransaction = getCustomDeleteTransaction(request, token);
		if (StringUtils.hasText(deleteTransaction)) {
			doCustomTransaction(deleteTransaction, pagedList, idx);
			return modelRedirectClip(request, token);	
		}

		// delete the model which has been selected
		doDelete(request, pagedList, idx );
		return modelRedirectClip(request, token);
	}

	/**
	 * Execute a custom transaction
	 * First it will be checked if there is a transaction for the PageableList available
	 * Then the dao is checked for a transaction with a List argument, which will contain only
	 * the visible part of the page to be commited.
	 * 
	 * @param transaction	The method name of the transaction
	 * @param pagedList		PageableList argument
	 * @param idx			optional index of the row to be executed (if only one row shall be exceuted)
	 * @throws Exception
	 */
	protected void doCustomTransaction(String transaction, PageableList<?> pagedList, Integer idx) throws Exception {
		
		// keep this for compatibility as ReflectionUtils.findMethod() will only find exact matches !
		Method mth = ReflectionUtils.findMethod(getDao().getClass(), transaction, pagedList.getClass());
		if (mth != null) {
			executeTransaction(mth, pagedList, idx);
			return;			
		} 
		// this is the recommended implementation
		mth = ReflectionUtils.findMethod(getDao().getClass(), transaction, PageableList.class);
		if (mth != null) {
			executeTransaction(mth, pagedList, idx);
			return;			
		} 
		// this is simple way to just handle the visual items on the current page
		mth = ReflectionUtils.findMethod(getDao().getClass(), transaction, List.class);
		if (mth != null) {
			executeTransaction(mth, pagedList.getPageList(), idx);
			return;
		}
		if (logger.isErrorEnabled()) {
			logger.error("The transaction " + transaction + " with a '" + pagedList.getClass() 
				+ "'-parameter could not be found in the class " + getDao().getClass());
		}
	}

	/**
	 * 
	 * @param
	 * @return
	 *
	 * @param mth
	 * @param pageList
	 * @throws Exception
	 */
	private void executeTransaction(final Method mth, final Object target, final Integer idx) throws Exception {
		doInTransactionIfAvailable(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus status) {
				if (idx == null) {
					ReflectionUtils.invokeMethod(mth, getDao(), target);					
				} else {
					ReflectionUtils.invokeMethod(mth, getDao(), target, idx);
				}
				return null;
			}
		});
	}

	/**
	 * @param
	 * @return
	 *
	 * @param request
	 * @param listHolder
	 * @throws Exception 
	 */
	protected void doSaveList(final HttpServletRequest request, final PageableList<?> listHolder) throws Exception {
		doInTransactionIfAvailable(new TransactionCallback<Object>() {
			final ModelCreation mc = getModelCreation(listHolder);
			public Object doInTransaction(TransactionStatus status) {
				for (int i = listHolder.getFirstElementOnPage(); i <= listHolder.getLastElementOnPage(); i++) {
					doSaveModel(listHolder.getSource().get(i), mc);
				}
				return null;
			}
		});
	}

	/**
	 * @param
	 * @return
	 *
	 * @param request
	 * @param listHolder
	 * @throws Exception 
	 */
	protected void doDelete(final HttpServletRequest request, final PageableList<?> listHolder, final int idx) throws Exception {
		doInTransactionIfAvailable(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus status) {
				boolean delFlag = doDeleteModel(listHolder.getSource().get(idx));
				if (delFlag) {
					listHolder.getSource().remove(idx);
				}
				return null;
			}
		});
	}
	
	protected ModelCreation getModelCreation(PageableList<?> listHolder) {
		ModelCreation model = null;
		if (listHolder instanceof Refreshable) {
			FilterDefinition filter = ((Refreshable) listHolder).getFilter();
			model = filter.getNewModelTemplate();
		} 
		if  (model == null && listHolder.getNewModelTemplate() instanceof ModelCreation) {
			model = (ModelCreation) listHolder.getNewModelTemplate();
		}
		return model;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.component.ComponentHandler#supports(java.lang.Class)
	 */
	public boolean supports(Object commandObject) {
		return commandObject instanceof PageableList;
	}


}
