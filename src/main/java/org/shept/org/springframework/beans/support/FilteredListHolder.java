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

package org.shept.org.springframework.beans.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.shept.beans.support.FilterDefinition;
import org.shept.org.springframework.web.servlet.mvc.support.ModelUtils;
import org.shept.persistence.provider.ScrollingListProvider;
import org.shept.persistence.provider.ScrollingListProviderFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.support.SortDefinition;
import org.springframework.dao.support.DaoSupport;

/**
 * FilteredListHolder is a PagedListHolder subclass with reloading capabilities.
 * It automatically re-requests the List from the source provider, in case of Locale or
 * filter or sort changes.
 * 
 * It is very similar as springs RefreshablePagedListHolder (no longer available as of spring 3.x) but in contrast it can read portions of the query
 * from the database so large amount of data are not a problem. It also rereads the database
 * when changes in sorting occur. 
 *
 *
 * @see org.springframework.beans.support.PagedListSourceProvider
 * @see org.springframework.beans.propertyeditors.LocaleEditor
 * 
 * @Author The authors of the springframework
 * 
 * @Version $Version$
 * $Id: FilteredListHolder.java 110 2011-02-21 09:16:15Z aha $
 * 
 */

public class FilteredListHolder<E> extends  ChoiceListHolder<E> implements Refreshable, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// transient to exclude from serialization
	// design question: should we remove the dao ?
	// drawback is that for now we need access to a dao for generic copying of entity objects
	// unless entity objects provide their #clone() implementations themselves
	protected transient DaoSupport dao;	
	
	protected transient ScrollingListProviderFactory listFactory;

	protected transient ScrollingListProvider sourceProvider;

	protected FilterDefinition filter;

	protected FilterDefinition filterUsed;
	
	protected FilterDefinition filterInitial;
	
	// choose sortInMemory=false if the backing filter supports sorting (sortByDatabase))
	protected Boolean sortInMemory = true;	
	
	// choose reloadOnSort=false if resultset is fully loaded
	protected Boolean reloadOnSort = true;
	
	// refresh even if the filter did not change
	// frequent refreshing has the advantage of reflecting other sessions changes at the price of slower throughput
	protected boolean alwaysRefresh = true;

		/**
		 * Create a new list holder.
		 * You'll need to set a source provider to be able to use the holder.
		 * @see #setSourceProvider
		 */
		public FilteredListHolder() {
			super();
		}

		/**
		 * Create a new list holder with the given source provider.
		 */
		public FilteredListHolder(ScrollingListProvider sourceProvider) {
			super();
			this.sourceProvider = sourceProvider;
		}

		@SuppressWarnings("unchecked")
		protected void loadListFirst() {
			
			initNewModelTemplate();
			
			ScrollingListProvider sp = getSourceProvider();
			sp.setSortDefinition(getSort());
			sp.setFilterDefinition(getFilter());
			setSource((List<E>) sp.loadListFirst());
			initOptions(0, getSource().size());

			if (getSortInMemory() ) {
				removeNullObjects();
				super.doSort(getSource(), getSort());
			} 
			this.sortUsed = copySortDefinition(getSort());
		}
		
		protected void initNewModelTemplate() {
			Object newTemplate = getFilter().getNewModelTemplate();
			setNewModelTemplate(newTemplate);
		}

		
		@SuppressWarnings("unchecked")
		protected void loadListNext() {
			// only append if this is not the first fetch (initialization)
			if (getSource().size() > 0) {
				int startIndex = getSource().size();
				getSource().addAll((List<E>) getSourceProvider().loadListNext());
				initOptions(startIndex, getSource().size());
			}
		}
		
		/*
		 * Just reload from database which will by default sort due to sorting conditions
		 * Parameter SortDefinition is just for the sake of parent definition
		 */
		protected void doSort(List<E> source, SortDefinition sort) {
			if (getReloadOnSort()) {
				loadListFirst();
			}
			else if (getSortInMemory())  {
				removeNullObjects();
				super.doSort(source, sort);
			}
		}

		/* (non-Javadoc)
		 * @see org.shept.org.springframework.beans.support.PagedListHolder#copyModel(java.lang.Object)
		 */
		@Override
		protected Object copyModel(Object model) {
			return ModelUtils.copyModel(dao, model);
		}

		/**
		 * Return the filter that the source provider should use for loading the list.
		 * @return the current filter, or <code>null</code>
		 */
		public FilterDefinition getFilter() {
			return filter;
		}
		
		/**
		 * Return the callback class for reloading the List when necessary.
		 */
		public ScrollingListProvider getSourceProvider() {
			return sourceProvider;
		}

		/* (non-Javadoc)
		 * @see util.HibernatePagedListSourceProvider1#isEol()
		 */
		public boolean isEol() {
			if (getSourceProvider() == null) {
				return true;
			} else {
				return getSourceProvider().isEol();
			}
		}
		
		/**
		 * Erase the list of loaded elements from the source provider
		 * Needs to do a refresh for example to populate the list again
		*
		 */
		public void clear() {
			if (getSource() != null) {
				getSource().clear();
			}
		}

		/* (non-Javadoc)
		 * @see org.shept.org.springframework.beans.support.Filter#refresh(boolean)
		 */
		public void refresh() {
			if (alwaysRefresh ||  (this.filter != null && !this.filter.equals(this.filterUsed))) {

				initSourceProvider(this.dao);
				loadListFirst();

				if (this.filter != null && !this.filter.equals(this.filterUsed)) {
					this.setPage(0);
				}
				if (null != this.filter) {
					this.filterUsed = BeanUtils.instantiateClass(this.filter.getClass());
					BeanUtils.copyProperties(this.filter, this.filterUsed);
				}
			}
		}
		
		/* (non-Javadoc)
		 * @see org.shept.org.springframework.beans.support.Filter#refresh(boolean)
		 */
		@Deprecated
		public void refresh(DaoSupport dao) {
			this.dao = dao;
			refresh();
		}
		
		/*
		 * Some wrappers (incl. Hibernate) will return null elements as returned by the database
		 * In-Memory sorting will throw an exception in case of not existing model objects (=null)
		 * So we remove nulls before sorting
		 */
		protected void removeNullObjects() {
			List<Object> al = new ArrayList<Object>();
			al.add(null);
			getSource().removeAll(al);		// remove null elements which may be returned by hibernate
		}
		
		/**
		 * Set the filter object that the source provider should use for loading the list.
		 * This will typically be a bean, for easy data binding.
		 * @param filter the filter object, or <code>null</code>
		 * @throws Exception 
		 */
		public void setFilter(FilterDefinition filter) {
			this.filter = filter;
			if (filter != null && this.filterInitial == null) {
				setFilterInitial(this.filter);
			}
		}

		/* (non-Javadoc)
		 * @see util.HibernatePagedListSourceProvider1#setPage(int)
		 */
		public void setPage(int page) {
			// only append if this is not the first fetch (initialization)
			if (page >= getPageCount() -1 ) {
				loadListNext();
			}
			super.setPage(page);
		}
		
		public void setPageSize(int pageSize) {
			int newLoadSize = pageSize * 3;

			// try to set the page that the same elements as before were shown
			int newPage = getFirstElementOnPage() / pageSize;
			super.setPageSize(pageSize);
			super.setPage(newPage);
			
			// load more data from the database
			if (getSourceProvider() != null) {
				if (newLoadSize != getSourceProvider().getLoadSize()) {
					getSourceProvider().setLoadSize(newLoadSize);
					if (getLastElementOnPage() >= getSource().size()-1 ) {
						loadListNext();
					}
				}
			}
		}
		
		/**
		 * Set the source list for this holder.
		 * In contrast to the original version we do not reset sortUsed
		 */
		@Override
		public void setSource(List<E> source) {
			super.setSource(source);
		}

		/**
		 * Set the callback class for reloading the List when necessary.
		 * If the list is definitely not modifiable, i.e. not locale aware
		 * and no filtering, use PagedListHolder.
		 * @see org.springframework.beans.support.PagedListHolder
		 */
		public void setSourceProvider(ScrollingListProvider sourceProvider) {
			this.sourceProvider = sourceProvider;
		}

		public FilterDefinition getFilterUsed() {
			return filterUsed;
		}

		public void setFilterUsed(FilterDefinition filterUsed) {
			this.filterUsed = filterUsed;
		}

		public Boolean getSortInMemory() {
			return sortInMemory;
		}

		public void setSortInMemory(Boolean sortInMemory) {
			this.sortInMemory = sortInMemory;
		}

		public Boolean getReloadOnSort() {
			return reloadOnSort;
		}

		public void setReloadOnSort(Boolean reloadOnSort) {
			this.reloadOnSort = reloadOnSort;
		}
		
		/**
		 * @return the listFactory
		 */
		public ScrollingListProviderFactory getListFactory(DaoSupport dao) {
			if (listFactory == null) {
				listFactory = new ScrollingListProviderFactory();
				listFactory.setDao(dao);
			}
			return listFactory;
		}

		/**
		 * @param listFactory the listFactory to set
		 */
		public void setListFactory(ScrollingListProviderFactory listFactory) {
			this.listFactory = listFactory;
		}
		
		/**
		 * Initialize the source provider with meaningful default values.
		 * By default the source provider is reused when it is already initialized.
		 * Paging will be requested although the capability still depends on the provider
		 * 
		 * @param force 
		 * @return
		 *
		 * @param force
		 * @throws Exception
		 */
		protected void initSourceProvider(DaoSupport dao)  {
			if (null == getFilter() ) {
				throw new IllegalStateException("The filter provider is not yet initialized");
			}
			ScrollingListProvider sp = getListFactory(dao).getScrollingList(getFilter());
			if (getSourceProvider() == null  ||
					! getSourceProvider().getClass().equals(sp.getClass())) {
				sp.setLoadSize(getDefaultLoadSize());
				if (sp.getDao() == null && dao != null) {
					sp.setDao(dao);
				}
				setSourceProvider(sp);
			}	// else we reuse the existing source provider
		}

		public int getDefaultLoadSize() {
			return getPageSize() * 2 + 1; 
		}

		/**
		 * @return the filterInitial
		 */
		public FilterDefinition getFilterInitial() {
			return filterInitial;
		}

		/**
		 * @param filterInitial the filterInitial to set
		 */
		public void setFilterInitial(FilterDefinition filterInitial) {
			this.filterInitial = copyFilterDefinition(filterInitial);
		}

		protected FilterDefinition copyFilterDefinition(FilterDefinition f) {
			FilterDefinition fd = BeanUtils.instantiateClass(f.getClass());
			BeanUtils.copyProperties(f,fd);
			return fd;
		}

		public void setUseFilter(FilterType type) {
			switch (type) {
			case FILTER_INITIAL:
				if (getFilterInitial() == null) {
					throw new IllegalStateException("The filterInitial is not yet initialized");
				}
				setFilter(copyFilterDefinition(getFilterInitial()));	// need a copy here, else we would overwrite the initial settings !
				break;
			case FILTER_LAST_USED:
				if (getFilterUsed() == null) {
					throw new IllegalStateException("The filterUsed is not yet initialized");
				}
				setFilter(getFilterUsed());	// don't need a copy here because we always provide a copy on refresh
			default:
				break;
			}
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(super.toString());
			if (filter != null) {
				sb.append(" Filter: " + filter.toString());
			}
			if (filterInitial != null) {
				sb.append(" FilterInitial: " + filterInitial.toString());
			}
			if (filterUsed != null) {
				sb.append(" FilterUsed: " + filterUsed.toString());
			}
			sb.append(" reloadOnSort: " + reloadOnSort);
			sb.append(" sortInMemory: " + sortInMemory);
			return sb.toString();
		}

		/**
		 * @return the alwaysRefresh
		 */
		public boolean isAlwaysRefresh() {
			return alwaysRefresh;
		}

		/**
		 * @param alwaysRefresh the alwaysRefresh to set
		 */
		public void setAlwaysRefresh(boolean alwaysRefresh) {
			this.alwaysRefresh = alwaysRefresh;
		}

		public void setDao(DaoSupport dao) {
			this.dao = dao;
		}

}
