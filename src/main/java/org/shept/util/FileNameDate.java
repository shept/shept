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

package org.shept.util;


/** 
 * 
 * This class is for comparing files for modification dates.
 * This class can be used as a key holding files or ftpFiles in a map.
 * For equality both name an date must be same.
 * 
 * @version $$Id: FileNameDate.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class FileNameDate  implements Comparable<FileNameDate> {

		private String name;
		private Long modificationDate;
		private boolean ignoreCase = true;
		
		FileNameDate(String name, Long modificationDate ){
			super();
			this.name = name;
			this.modificationDate = modificationDate;
		}
		
		FileNameDate(String name, Long modificationDate, boolean ignoreCase){
			super();
			this.name = name;
			this.modificationDate = modificationDate;
			this.ignoreCase = ignoreCase;
		}
		
		public String getName() {
			return name;
		}

		public Long getModificationDate() {
			return modificationDate;
		}
		
		protected boolean getIgnoreCase() {
			return ignoreCase;
		}
		
		/**
		 * Default sorting behavior first by fileName and then by modificationDate
		 */
		public int compareTo(FileNameDate key) {
			try {
					int rv = 0;
					if (ignoreCase) {
						rv = name.compareToIgnoreCase(key.getName());
					} else {
						rv = name.compareTo(key.getName());
					}
					if (rv != 0) return rv;
					return modificationDate.compareTo(key.getModificationDate());
			} catch (Exception ex) {
				return 1;
			}
		}
	}

