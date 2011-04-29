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

/**
 * 
 */


import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken;
import org.shept.org.springframework.web.servlet.mvc.support.ModelUtils;
import org.shept.persistence.ModelCreation;
import org.shept.persistence.ModelDeletion;
import org.shept.persistence.UnsupportedModelTransformation;
import org.shept.persistence.provider.DaoUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.support.DaoSupport;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;


/** 
 * @version $$Id: AbstractPersistenceComponent.java 90 2010-11-30 14:31:06Z aha $$
 *
 * @author Andi
 *
 */
public abstract class AbstractPersistenceComponent extends AbstractComponent implements InitializingBean, WebComponent {
	
	public static final String OPTIMISTIC_LOCKING_ERROR = "dataAccess.optimisticLockingError";
	public static final String DATA_INTEGRITY_ERROR = "dataAccess.integrityError";
	public static final String DATA_ACCESS_ERROR = "dataAccess.error";
	
	protected boolean enableSave = true;
	
	protected boolean enableDelete = true;
	
	// set reloadOnCancel to true to make sure that a prevously bound form will be refreshed with the content of the database
	protected boolean reloadOnCancel = true;

	// dao transactions are methods in dao objects that perform a save or delete operation (on a list)
	protected boolean enableDaoTransactions = true;
	
	protected PlatformTransactionManager transactionManager = null;

	protected DaoSupport dao;

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.web.servlet.mvc.component.ComponentHandler#excecuteAction(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.shept.org.springframework.web.servlet.mvc.component.ComponentToken)
	 */
	public ModelAndView excecuteAction(HttpServletRequest request,
			HttpServletResponse response, ComponentToken token)
			throws Exception {
		
		WebRequestDataBinder binder = token.getBinder();
		ModelAndView mav = new ModelAndView (new RedirectView());
		
		try {
			mav = doActionInternal( request, token);
		}
			catch (ObjectOptimisticLockingFailureException ex) {
				binder.getBindingResult().reject(OPTIMISTIC_LOCKING_ERROR,
					"Concurrency error: Record already changed by another user. Abort operation and try again");
				if (logger.isInfoEnabled()) {
					logger.info("Concurrency Error in persistence operation", ex);
				}
				return modelWithErrors(binder);
			} 
			catch (DataIntegrityViolationException ex) {
				binder.getBindingResult().reject(DATA_INTEGRITY_ERROR,
					"Data Integrity Violation: Dataset contains forbidden NULLs or duplicate records or deletion fails due to orphaned references");
				if (logger.isErrorEnabled()) {
					logger.error("Data Integrity violation error", ex);
				}
				return modelWithErrors(binder);
			}
			catch (DataAccessException ex) {
				binder.getBindingResult().reject(DATA_ACCESS_ERROR,
					"Unrecoverable data access layer error. See service log file for details");
				if (logger.isErrorEnabled()) {
					logger.error("Data access layer error", ex);
				}
				return modelWithErrors(binder);
			}
			// this should not happen here !?
			// i would have expected all exception checked as DataAccesExceptions ...
			catch (NestedRuntimeException ue) {
				if (logger.isErrorEnabled()) {
					logger.error("Nested Runtime Exception ", ue);
				}				
			}
			return mav;
	}

	protected abstract ModelAndView doActionInternal(HttpServletRequest request,
			ComponentToken token) throws Exception;


	protected void doInTransactionIfAvailable(TransactionCallback<Object> transactionCallback) throws Exception {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		// explicitly setting the transaction name is something that can only be done programmatically
		def.setName(AbstractPersistenceComponent.class.getName());
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		if (transactionManager != null) {
			TransactionStatus status = transactionManager.getTransaction(def);
			try {
				transactionCallback.doInTransaction(status);				
			} catch (Exception ex) {
				transactionManager.rollback(status);
				throw ex;
			}
			transactionManager.commit(status);
		} 
		else {
			transactionCallback.doInTransaction(null);
			doFlushSession();
		}
	}

	protected void doSaveModel(Object modelObject)  {
		doSaveModel(modelObject, null);
	}

	protected void doSaveModel(Object rowObject, ModelCreation modelCreation)  {
		Object saveModel = getWrappedModel(rowObject);
		if (logger.isDebugEnabled()) {
			logger.debug("Saving model " + saveModel.toString());
		}
		if (this.dao instanceof HibernateDaoSupport) {
			HibernateDaoSupport hibernateDao = (HibernateDaoSupport) this.dao;
			boolean newModel = DaoUtils.isNewModel(hibernateDao, saveModel);
			if (newModel) {
				if (modelCreation == null || modelCreation.isCreationAllowed(saveModel)) {
					hibernateDao.getHibernateTemplate().save(saveModel);											
				}
			} else {
				// this MUST be a saveOrUpdate because we also may have user supplied keys, so update is not sufficient in these cases
				hibernateDao.getHibernateTemplate().saveOrUpdate(saveModel);				
			}
		} 
		else if (this.dao instanceof JpaDaoSupport) {
			JpaDaoSupport jpaDao = (JpaDaoSupport) this.dao;
			jpaDao.getJpaTemplate().persist(saveModel);
		}
	}

	protected void doFlushSession() {
		if (this.dao instanceof HibernateDaoSupport) {
			HibernateDaoSupport hibernateDao = (HibernateDaoSupport) this.dao;
			hibernateDao.getHibernateTemplate().flush();
		} 
		else if (this.dao instanceof JpaDaoSupport) {
			JpaDaoSupport jpaDao = (JpaDaoSupport) this.dao;
			jpaDao.getJpaTemplate().flush();
		}
	}

	/**
	 * 
	 * @param
	 * @return true if the model is physically removed
	 *
	 * @param rowObject
	 * @return
	 */
	protected boolean doDeleteModel(Object rowObject)  {
		Object model = getWrappedModel(rowObject);
		// don't delete the model if there is a special case deletion handling implemented
		if (model instanceof ModelDeletion) {
			boolean delFlag = ((ModelDeletion) model).setDeleted(true);
			if (delFlag) {
				if (logger.isDebugEnabled()) {
					logger.debug("Deleting model by special case deletion handling (set deletion flag)" + this.toString());
				}
				doSaveModel(model, null);
				return false;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Deleting model " + model.toString());
		}
		if (this.dao instanceof HibernateDaoSupport) {
			HibernateDaoSupport hibernateDao = (HibernateDaoSupport) this.dao;
			hibernateDao.getHibernateTemplate().delete(model);
		} 
		else if (this.dao instanceof JpaDaoSupport) {
			JpaDaoSupport jpaDao = (JpaDaoSupport) this.dao;
			jpaDao.getJpaTemplate().remove(model);
		}
		return true;
	}

	protected Object getWrappedModel(Object model) 
		throws UnsupportedModelTransformation {
		Object rv = ModelUtils.unwrapIfNecessary(model);
		if (rv.getClass().isArray() || rv instanceof Map || rv instanceof List || rv instanceof Set) {
			throw new UnsupportedModelTransformation(model); 
		}
		return rv;
	}

	/**
	 * @return the dao
	 */
	public DaoSupport getDao() {
		return dao;
	}

	/**
	 * @param dao the dao to set
	 */
	@Resource
	public void setDao(DaoSupport dao) {
		this.dao = dao;
	}

	/**
	 * @param enableUpdate the enableUpdate to set
	 */
	public void setEnableSave(boolean enableSave) {
		this.enableSave = enableSave;
	}

	/**
	 * @param enableDelete the enableDelete to set
	 */
	public void setEnableDelete(boolean enableDelete) {
		this.enableDelete = enableDelete;
	}

	/**
	 * @param transactionManager the transactionManager to set
	 */
	@Resource
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.dao, "A Hibernate or Jpa persistence wrapper must be specified");
		Assert.isTrue(this.dao instanceof HibernateDaoSupport || this.dao instanceof JpaDaoSupport,
				"Only Jpa or Hibernate persistence wrappers are supported, " + this.dao.getClass().toString() + " is unsupported.");
	}

	/**
	 * @param reloadOnCancel the reloadOnCancel to set
	 */
	public void setReloadOnCancel(boolean reloadOnCancel) {
		this.reloadOnCancel = reloadOnCancel;
	}

	/**
	 * @return the enableSave
	 */
	public boolean isEnableSave() {
		return enableSave;
	}

	/**
	 * @return the enableDelete
	 */
	public boolean isEnableDelete() {
		return enableDelete;
	}

	/**
	 * @return the reloadOnCancel
	 */
	public boolean isReloadOnCancel() {
		return reloadOnCancel;
	}

	/**
	 * @return the enableDaoTransactions
	 */
	public boolean isEnableDaoTransactions() {
		return enableDaoTransactions;
	}

	/**
	 * @param enableDaoTransactions the enableDaoTransactions to set
	 */
	public void setEnableDaoTransactions(boolean enableDaoTransactions) {
		this.enableDaoTransactions = enableDaoTransactions;
	}

	
}

