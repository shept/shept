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

/*
 * Copyright 2002-2005 the original author or authors.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.shept.beans.support.PropertyComparator;
import org.shept.org.springframework.web.servlet.mvc.support.ModelUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.SortDefinition;
import org.springframework.util.CollectionUtils;

/**
 * 
 * This is an extended version of the original PagedListHolder as delivered by
 * the spring framework.
 * The original version has implemeted a fix sorting behavior which is done in
 * memory and cannot (easily ?) be delegated to the dataSource to be performed by 
 * a database query.
 * 
 * <p> The only thing we have changed is that we made most of the local
 * variables protected instead of private so that this behaviour can be
 * redefined in subclasses.
 * 
 *<p>added <b>getSortUsed()</b> getterMethod 
 *<br/>added <b>implements PageableList();</b>
 * 
 * <p>--- The following comment is unchanged from the original version ----
 * 
 * <br/>PagedListHolder is a simple state holder for handling lists of objects,
 * separating them into pages. Page numbering starts with 0.
 *
 * <p>This is mainly targetted at usage in web UIs. Typically, an instance will be
 * instantiated with a list of beans, put into the session, and exported as model.
 * The properties can all be set/get programmatically, but the most common way will
 * be data binding, i.e. populating the bean from request parameters. The getters
 * will mainly be used by the view.
 *
 * <p>Supports sorting the underlying list via a SortDefinition implementation,
 * available as property "sort". By default, a MutableSortDefinition instance
 * that toggles the ascending value on setting the same property again is used.
 *
 * <p>The data binding names have to be called "pageSize" and "sort.ascending",
 * as expected by BeanWrapper. Note that the names and the nesting syntax match
 * the respective JSTL EL expressions, like "myModelAttr.pageSize" and
 * "myModelAttr.sort.ascending".
 *
 * <p>This class just provides support for an unmodifiable List of beans.
 * If you need on-demand refresh because of Locale or filter changes,
 * consider RefreshablePagedListHolder.
 *
 * @author Juergen Hoeller
 * @since 19.05.2003
 * @see #getPageList
 * @see org.springframework.beans.support.RefreshablePagedListHolder
 * @see org.springframework.beans.support.MutableSortDefinition
 * 
 * TODO LIST_BINDING_NAME und getListBindingName() remove ?
 */
public class PagedListHolder<E> implements Serializable, PageableList<E> {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_MAX_LINKED_PAGES = 10;

	private List<E> source;

	// if this is not transient, then a compare (or serialization) of listHolders is different before and after view rendering
	protected transient Date refreshDate;

	// specify sorting defaults which may be overridden by criteria and filter processing
	protected SortDefinition sort = new MutableSortDefinition();	 

	protected SortDefinition sortUsed ; 
	
	protected int pageSize = DEFAULT_PAGE_SIZE;

	protected int page = 0;

	// remember, if newPage is set. Will be reset during view rendering
	// if this is not transient, then a compare (or serialization) of listHolders would be different before and after view rendering
	protected transient boolean newPageSet;			

	protected int maxLinkedPages = DEFAULT_MAX_LINKED_PAGES;
	
	protected Object newModelTemplate;
	
	protected int newModelSize = DEFAULT_NEW_MODEL_SIZE;
	
	/**
	 * Create a new holder instance.
	 * You'll need to set a source list to be able to use the holder.
	 * @see #setSource
	 */
	public PagedListHolder() {
		this(new ArrayList<E>(0));
	}

	/**
	 * Create a new holder instance with the given source list, starting with
	 * a default sort definition (with "toggleAscendingOnProperty" activated).
	 * @param source the source List
	 * @see MutableSortDefinition#setToggleAscendingOnProperty
	 */
	public PagedListHolder(List<E> source) {
		setSource(source);
		setSort(new MutableSortDefinition(true));
	}

	/**
	 * Create a new holder instance with the given source list.
	 * @param source the source List
	 * @param sort the SortDefinition to start with
	 */
	public PagedListHolder(List<E> source, SortDefinition sort, Object newModelTemplate) {
		setNewModelTemplate(newModelTemplate);
		setSource(source);
		setSort(sort);
	}


	/**
	 * Create a deep copy of the given sort definition,
	 * for use as state holder to compare a modified sort definition against.
	 * <p>Default implementation creates a MutableSortDefinition instance.
	 * Can be overridden in subclasses, in particular in case of custom
	 * extensions to the SortDefinition interface. Is allowed to return
	 * null, which means that no sort state will be held, triggering
	 * actual sorting for each <code>resort</code> call.
	 * @param sort the current SortDefinition object
	 * @return a deep copy of the SortDefinition object
	 * @see MutableSortDefinition#MutableSortDefinition(SortDefinition)
	 */
	protected SortDefinition copySortDefinition(SortDefinition s) {
		SortDefinition sd = (SortDefinition) BeanUtils.instantiateClass(s.getClass());
		BeanUtils.copyProperties(s,sd);
		return sd;
	}

	/**
	 * Actually perform sorting of the given source list, according to
	 * the given sort definition.
	 * <p>The default implementation uses Spring's PropertyComparator.
	 * Can be overridden in subclasses.
	 * @see PropertyComparator#sort(java.util.List, SortDefinition)
	 */
	protected void doSort(List<E> src, SortDefinition s) {
//		PropertyComparator.sort(src, s);
		PropertyComparator.sort(src, s);
		}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#getFirstElementOnPage()
	 */
	protected int getFirstElementOnPage_orig() {
		return (getPageSize() * getPage());
	}

	/**
	 * Modifications in {@link #getFirstElementOnPage()} and {@link #getLastElementOnPage()}
	 * are a workaround for a problem in jsp implementation that {@link #getLastElementOnPage()}
	 * may not return -1 (numbers below 0) which is neccessary when the list is empty
	 * (because the first element will be 0 then and the last element must be below this value
	 * otherwise an unexisting element might be indexed) 
	 */
	public int getFirstElementOnPage() {
		int firstElement = getFirstElementOnPage_orig();
		if (firstElement < 0 ) {
			return Integer.MAX_VALUE;
		} else {
			return firstElement;
		}
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#getFirstLinkedPage()
	 */
	public int getFirstLinkedPage() {
		return Math.max(0, getPage() - (getMaxLinkedPages() /2));
	}
	
	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#getLastElementOnPage()
	 */
	protected int getLastElementOnPage_orig() {
		int endIndex = getPageSize() * (getPage() + 1);
		return (endIndex > getSource().size() ? getSource().size() : endIndex) -1;
	}

	/**
	 * Modifications in {@link #getFirstElementOnPage()} and {@link #getLastElementOnPage()}
	 * are a workaround for a problem in jsp implementation that {@link #getLastElementOnPage()}
	 * may not return -1 (numbers below 0) which is neccessary when the list is empty
	 * (because the first element will be 0 then and the last element must be below this value
	 * otherwise an unexisting element might be indexed) 
	 */
	public int getLastElementOnPage() {
		int endIndex = getLastElementOnPage_orig();
		if (endIndex < 0 ) {
			return 0;
		} else {
			return endIndex;
		}
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#getLastLinkedPage()
	 */
	public int getLastLinkedPage() {
		return Math.min(getFirstLinkedPage() + getMaxLinkedPages() -1, getPageCount() -1);
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#getMaxLinkedPages()
	 */
	public int getMaxLinkedPages() {
		return maxLinkedPages;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#getNrOfElements()
	 */
	public int getNrOfElements() {
		return getSource().size();
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#getPage()
	 */
	public int getPage() {
		this.newPageSet = false;
		if (this.page >= getPageCount()) {
			this.page = getPageCount() - 1;
		}
		return this.page;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#getPageCount()
	 */
	public int getPageCount() {
		float nrOfPages = (float) getSource().size() / getPageSize();
		return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#getPageList()
	 */
	public List<E> getPageList() {
		return getSource().subList(getFirstElementOnPage(), getLastElementOnPage() +1);
	}


	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#getPageSize()
	 */
	public int getPageSize() {
		return pageSize;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#getRefreshDate()
	 */
	public Date getRefreshDate() {
		return refreshDate;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#getSort()
	 */
	public SortDefinition getSort() {
		return sort;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#getSortUsed()
	 */
	public SortDefinition getSortUsed() {
		return sortUsed;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#getSource()
	 */
	public List<E> getSource() {
		return source;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#isFirstPage()
	 */
	public boolean isFirstPage() {
		return getPage() == 0;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#isLastPage()
	 */
	public boolean isLastPage() {
		return getPage() == getPageCount() -1;
	}
	
	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#isVisible(java.lang.Integer)
	 */
	public boolean isVisible(Integer idx){
		return (idx >= getFirstElementOnPage() && idx <= getLastElementOnPage());
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#nextPage()
	 */
	public void nextPage() {
		if (!isLastPage()) {
			this.page++;
		}
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#isSortNeeded()
	 */
	public boolean isSortNeeded(){
		SortDefinition s = getSort();
		return s != null && !s.equals(getSortUsed());
	}
	
	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#previousPage()
	 */
	public void previousPage() {
		if (!isFirstPage()) {
			this.page--;
		}
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#resort()
	 */
	public void resort() {
		SortDefinition s = getSort();
		if (s != null && !s.equals(this.sortUsed)) {
			this.sortUsed = copySortDefinition(s);
			doSort(getSource(), s);
			setPage(0);
		}
	}

	/**
	 * Set the maximum number of page links to a few pages around the current one.
	 */
	public void setMaxLinkedPages(int maxLinkedPages) {
		this.maxLinkedPages = maxLinkedPages;
	}


	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#setPage(int)
	 */
	public void setPage(int page) {
		this.page = page;
		this.newPageSet = true;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#setPageSize(int)
	 */
	public void setPageSize(int pageSize) {
		if (pageSize != this.pageSize) {
			this.pageSize = pageSize;
			if (!this.newPageSet) {
				this.page = 0;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#setSort(org.springframework.beans.support.SortDefinition)
	 */
	public void setSort(SortDefinition sort) {
		this.sort = sort;
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#setSource(java.util.List)
	 */
	public void setSource(List<E> source) {
		List<E> newModels = getNewModels();
		if (newModels.size() > 0) {
			source.addAll(0, newModels);
		}
		setSourceInternal(source);
	}

	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#setSource(java.util.List)
	 */
	protected void setSourceInternal(List<E> source) {
		this.source = source;
		this.refreshDate = new Date();
		this.sortUsed = null;
	}

	@SuppressWarnings("unchecked")
	protected List<E> getNewModels() {
		List<E> newModels = new ArrayList<E>(getNewModelSize());
		Object template = getNewModelTemplate();
		if (template != null) {
			for (int i = 0; i < getNewModelSize(); i++) {			
				Object newModel = copyModel(template);
				newModels.add((E) newModel);
			}			
		}
		return newModels;
	}

	/**
	 * Use Springs default bean copying implementation to obtain a shallow object copy
	 * @return
	 */
	protected Object copyModel(Object model) {
		return ModelUtils.copyModel(model);
	}
	
	/**
	 * @return the newModelTemplate
	 */
	public Object getNewModelTemplate() {
		return newModelTemplate;
	}

	/**
	 * Set a model object as a template for creating new models.
	 */
	public void setNewModelTemplate(Object newModelTemplate) {
		this.newModelTemplate = copyModel(newModelTemplate);
	}

	/**
	 * @return the newRowSize
	 */
	public int getNewModelSize() {
		return newModelSize;
	}

	/**
	 * @param newRowSize the newRowSize to set
	 */
	public void setNewModelSize(int newModelSize) {
		this.newModelSize = newModelSize;
	}

	/* (non-Javadoc)
	 * Return always true as this list is not extendible
	 */
	public boolean isEol() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.PageableList#getListBindingName()
	 */
	public String getListBindingName() {
		return LIST_BINDING_NAME; 
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().toString() + " on ") ;
		if (CollectionUtils.isEmpty(this.source)) {
			sb.append(" empty source");
		} else {
			sb.append(" on " + source.size() + " objects in list with hash " + String.valueOf(source.hashCode()));
		};
		sb.append(" -- ");
		sb.append("Sort: ");
		if (this.sort != null) {
			sb.append("Property: " + sort.getProperty());
			sb.append(" ascending " + sort.isAscending());
			sb.append(" ignoreCase: " + sort.isIgnoreCase());
		} else {
			sb.append(" null");
		}
		sb.append("SortUsed: ");
		if (this.sortUsed != null) {
			sb.append("Property: " + sortUsed.getProperty());
			sb.append(" ascending " + sortUsed.isAscending());
			sb.append(" ignoreCase: " + sortUsed.isIgnoreCase());
		} else {
			sb.append(" null");
		}
		sb.append(" maxLinkedPages: " + maxLinkedPages);
		sb.append(" newPageSet: " + newPageSet); 
		sb.append(" page: " + page);
		sb.append(" pageSize: " + pageSize);
		sb.append(" refreshDate: " + refreshDate);
		
		return sb.toString();
	}

}
