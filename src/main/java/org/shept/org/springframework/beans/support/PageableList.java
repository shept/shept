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

import java.util.Date;
import java.util.List;

import org.springframework.beans.support.SortDefinition;

public interface PageableList<E> {

	public static final String LIST_BINDING_NAME="source";	// name MUST be the same as list variable 'source'
	
	public static final int DEFAULT_PAGE_SIZE = 10;

	public static final int DEFAULT_NEW_MODEL_SIZE = 1;

	/**
	 * Return the element index of the first element on the current page.
	 * Element numbering starts with 0.
	 */
	public abstract int getFirstElementOnPage();

	/**
	 * Return the first page to which create a link around the current page.
	 */
	public abstract int getFirstLinkedPage();

	/**
	 * Return the element index of the last element on the current page.
	 * Element numbering starts with 0.
	 */
	public abstract int getLastElementOnPage();

	/**
	 * Return the last page to which create a link around the current page.
	 */
	public abstract int getLastLinkedPage();

	/**
	 * Return the maximum number of page links to a few pages around the current one.
	 */
	public abstract int getMaxLinkedPages();

	/**
	 * Return the total number of elements in the source list.
	 */
	public abstract int getNrOfElements();

	/**
	 * Return the current page number.
	 * Page numbering starts with 0.
	 */
	public abstract int getPage();

	/**
	 * Return the number of pages for the current source list.
	 */
	public abstract int getPageCount();

	/**
	 * Return a sub-list representing the current page.
	 */
	public abstract List<E> getPageList();

	/**
	 * Return the current page size.
	 */
	public abstract int getPageSize();

	/**
	 * Return the last time the list has been fetched from the source provider.
	 */
	public abstract Date getRefreshDate();

	/**
	 * Return the sort definition for this holder.
	 */
	public abstract SortDefinition getSort();

	public abstract SortDefinition getSortUsed();

	/**
	 * Return the source list for this holder.
	 */
	public abstract List<E> getSource();

	/**
	 * Return if the underlying resultset is completely read
	 * (always true if the resultset is not based on cursors)
	 */
	public abstract boolean isEol();

	/**
	 * Return if the current page is the first one.
	 */
	public abstract boolean isFirstPage();

	/**
	 * Return if the current page is the last one.
	 */
	public abstract boolean isLastPage();

	/**
	 * Return if the given element index is within the range of the currently visible Page
	 */
	public abstract boolean isVisible(Integer idx);

	/**
	 * Switch to next page.
	 * Will stay on last page if already on last page.
	 */
	public abstract void nextPage();

	/**
	 * Added to the original implementation so we can check for changes
	 * and refresh the view conditionally..
	 */
	public abstract boolean isSortNeeded();

	/**
	 * Switch to previous page.
	 * Will stay on first page if already on first page.
	 */
	public abstract void previousPage();

	/**
	 * Resort the list if necessary, i.e. if the current <code>sort</code> instance
	 * isn't equal to the backed-up <code>sortUsed</code> instance.
	 * <p>Calls <code>doSort</code> to trigger actual sorting.
	 * @see #doSort
	 */
	public abstract void resort();

	/**
	 * Set the current page number.
	 * Page numbering starts with 0.
	 */
	public abstract void setPage(int page);

	/**
	 * Set the current page size.
	 * Resets the current page number if changed.
	 * <p>Default value is 10.
	 */
	public abstract void setPageSize(int pageSize);

	/**
	 * Set the sort definition for this holder.
	 * Typically an instance of MutableSortDefinition.
	 * @see org.springframework.beans.support.MutableSortDefinition
	 */
	public abstract void setSort(SortDefinition sort);

	/**
	 * Set the source list for this holder.
	 */
	public abstract void setSource(List<E> source);

	/**
	 * return the binding name of the list
	 */
	public abstract String getListBindingName();
	
	/**
	 * get the new model template (for creating new objects)
	 */
	public abstract Object getNewModelTemplate();

	/**
	 * Set the new model template (for creating new objects)
	 */
	public abstract void setNewModelTemplate(Object object);

	/**
	 * get the new model template (for creating new objects)
	 */
	public abstract int getNewModelSize();

	/**
	 * Set the new model template (for creating new objects)
	 */
	public abstract void setNewModelSize(int newModelSize);

	
	
	
}