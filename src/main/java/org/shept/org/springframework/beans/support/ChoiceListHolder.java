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


public class ChoiceListHolder<E> extends PagedListHolder<E>  implements Serializable, MultiChoice<E> {
	
	public static final String OPTIONS_BINDING_NAME="options";	// name MUST be the same as list variable 'options'

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected List<Boolean> options = new ArrayList<Boolean>(0);
	
	protected Object selectedAction;
	
	public class ModelWithSelection implements ModelSupplier{
		private Boolean selected;
		private Object model;
		
		public ModelWithSelection(Boolean selected, Object model) {
			this.selected = selected;
			this.model = model;
		}
		
		/**
		 * @return the selection mark
		 */
		public Boolean getSelected() {
			return selected;
		}
		/**
		 * @param selected the selected to set
		 */
		public void setSelected(Boolean selected) {
			this.selected = selected;
		}
		/**
		 * @return the item
		 */
		public Object getModel() {
			return model;
		}
		/**
		 * @param item the item to set
		 */
		public void setModel(Object model) {
			this.model = model;
		}
		
	}
	
	protected void initOptions(int startIndex, int endIndex) {
		if (startIndex == 0) {		// reinitialize
			options = new ArrayList<Boolean>(0);
		} 
		for (int i = startIndex; i < endIndex; i++) {
			options.add(Boolean.FALSE);
		}
	}
	
	public List<Boolean> getOptions() {
		return options;
	}
	
	/* (non-Javadoc)
	 * @see org.shept.org.springframework.beans.support.MultiChoice#getSelectedItems()
	 */
	public List<E> getSelectedItems() {
		List<E> sel = new ArrayList<E>(0);
		List<E> src = getSource();
		for (int i = 0; i < src.size(); i++) {
			if (options.get(i)) sel.add(src.get(i));
		}
		return sel;
	}
	
	public List<ModelWithSelection> getPageListWithSelection() {
		List<ModelWithSelection> itemList = new ArrayList<ModelWithSelection>(getPageCount());
		List<E> src = getSource();
		List<Boolean> opt = getOptions();
		for (int i = getFirstElementOnPage(); i < getLastElementOnPage() +1 ; i++) {
			itemList.add(new ModelWithSelection(opt.get(i), src.get(i)));
		}
		return itemList;
	}
	
	/*
	 * Copied from pageList() in Superclass
	 */
	public List<Boolean> getPageOptions() {
		return getOptions().subList(getFirstElementOnPage(), getLastElementOnPage() +1);
	}

	public Object getSelectedAction() {
		return selectedAction;
	}

	public void setSelectedAction(Object selectedAction) {
		this.selectedAction = selectedAction;
	}
}
