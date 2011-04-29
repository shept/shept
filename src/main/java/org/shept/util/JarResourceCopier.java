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

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

/**
 * @author Andi
 *
 */
public class JarResourceCopier implements ResourceInitializer {

	/** Logger that is available to subclasses */
	protected final static Log logger = LogFactory.getLog(JarResourceCopier.class);

	private String sourcePath;
	
	private String targetPath;
	
	private String[] files;
	
	public void initializeResources(ServletContext context) {
		String destPath = StringUtils.cleanPath(context.getRealPath(getTargetPath()));
		if (files != null && files.length > 0) {
			for (int i = 0; i < files.length; i++) {
				String source = StringUtils.applyRelativePath(getSourcePath(), files[i]);
				// surprise surprise you can't use StringUtils.applyRelativePath here this will cut off the last part of destPath
				JarUtils.copyResourcesOnce(new ClassPathResource(source), destPath, destPath + "/" + files[i]);				
			}
		} else {
			JarUtils.copyResourcesOnce(new ClassPathResource(getSourcePath()), destPath);							
		}
	}

	/**
	 * @return the sourcePath
	 */
	public String getSourcePath() {
		return sourcePath;
	}

	/**
	 * @param sourcePath the sourcePath to set
	 */
	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	/**
	 * @return the targetPath
	 */
	public String getTargetPath() {
		return targetPath;
	}

	/**
	 * @param targetPath the targetPath to set
	 */
	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	/**
	 * @return the files
	 */
	public String[] getFiles() {
		return files;
	}

	/**
	 * @param files the files to set
	 */
	public void setFiles(String[] files) {
		this.files = files;
	}


}
