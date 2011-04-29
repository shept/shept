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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.shept.org.springframework.beans.support.PageableList;
import org.shept.org.springframework.web.servlet.mvc.delegation.ComponentToken;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

/** 
 * @version $$Id: PagedListComponent.java 94 2010-12-22 15:12:29Z aha $$
 *
 * @author Andi
 *
 */
public class PagedListComponent extends AbstractComponent implements WebComponent {
	
	public ModelAndView excecuteAction(HttpServletRequest request,
			HttpServletResponse response, ComponentToken token) throws Exception {
		
		return doActionInternal( request, token);
	}

	protected ModelAndView doActionInternal(HttpServletRequest request,  ComponentToken token) throws Exception {
		
		String method = token.getToken().getMethod();
		PageableList<?> pagedList = (PageableList<?>) token.getComponent();
		
		if (method.equals("onGotoFirst")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Jump to first page: " + token.toString());
			}
			pagedList.setPage(0);
			return modelRedirect(request, token);
			
		} else if (method.equals("onGotoNext")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Jump to next page: " + token.toString());
			}
			pagedList.setPage(Math.min(pagedList.getPage() + 1, pagedList
					.getPageCount() - 1));
			return modelRedirect(request, token);
	
		} else if (method.equals("onGotoPrevious")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Jump to previous page: " + token.toString());
			}
			pagedList.setPage(Math.max(pagedList.getPage() - 1, 0));
			return modelRedirect(request, token);
	
		} else if (method.equals("onGotoLast")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Jump big step towards last page: " + token.toString());
			}
			pagedList.setPage(pagedList.getPageCount() - 1);
			return modelRedirect(request, token);
	
		} else if (method.equals("onGotoPage")) {
			int idx = 0;
			try {
				idx = Integer.parseInt(token.getToken().getValue());
			} catch (Exception e) { } // do nothing }
			if (logger.isDebugEnabled()) {
				logger.debug("Jump to page " +idx + ": " + token.toString());
			}
			pagedList.setPage(idx);
			return modelRedirect(request, token);
	
		} else if (method.equals("onPageResize")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Resize page: " + token.toString());
			}
			// the other option is binding, so we don't need ServletRequestUtils
			// the disadvantage is that all other fields are also bound, there may be other errors then ....
			int newSize = ServletRequestUtils.getIntParameter(request, token.getToken().getPathName() + "pageSize", 0);
			if (newSize > 0) {
				pagedList.setPageSize(newSize);
			}
			return modelRedirect(request, token); 	// just show the page again

		} else if (method.equals("onSortAsc")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Sort ascending: " + token.toString());
			}
			resort(pagedList, token.getToken().getValue(), true);
			return modelRedirect(request, token); 	// just show the page again

		} else if (method.equals("onSortDesc")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Sort descending: " + token.toString());
			}
			resort(pagedList, token.getToken().getValue(), false);
			return modelRedirect(request, token); 	// just show the page again
		}
		
		return modelUnhandled(request, token);
	}

	private void resort(PageableList<?> pagedList, String columnName, boolean asc) {
		MutableSortDefinition msd = new MutableSortDefinition();
		msd.setProperty(columnName);
		msd.setAscending(asc);
		pagedList.setSort(msd);
		pagedList.resort();
	}
	
	public Map<String, String> getDefaultMappings() {
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.put("pageFirst", "onGotoFirst");					// jump to the first page
		mappings.put("pageNext", "onGotoNext");				// jump to the next (=subsequent) page
		mappings.put("pagePrevious", "onGotoPrevious");	// jump to the page before
		mappings.put("pageLast", "onGotoLast");					// jump far towards the end of all pages but not necessarily the last page
		mappings.put("pageSelect", "onGotoPage");				// jump to the page number select by page attribute
		mappings.put("pageSortAsc", "onSortAsc");				// sort ascending depending on strategy. Can force reload
		mappings.put("pageSortDesc", "onSortDesc");			// sort descending depending on strategy. Can force reload
		mappings.put("pageResize", "onPageResize");			// resize the page by setting the pageSize attribute
		return mappings;
	}

	public boolean supports(Object commandObject) {
		return commandObject instanceof PageableList;
	}

}
