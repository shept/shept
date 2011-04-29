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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;



/** 
 * @version $$Id: JarUtils.java 94 2010-12-22 15:12:29Z aha $$
 *
 * @author Andi
 *
 */
public abstract class JarUtils {
	
	
	private static Set<? super String> alreadyCopied = new HashSet<String>();
	
	/** Logger that is available to subclasses */
	protected final static Log logger = LogFactory.getLog(JarUtils.class);

	/**
	 * Init this controllers ressources by copying them into the web container
	 * (subpath of WEB-INF). This is useful for resources as pictures, sounds for access by plain HTML
	 * This would not work if they stay just in the jar. 
	 * (Well actually it would work with Mozilla browsers because they support url naming convention 
	 * specifying resources inside a .jar-file. This isn't true for Internet Explorer).</p>
	 * <p>Resource copying is done only if there are no resources in the destination path so you can
	 * easily provide other than the default resources.</p>
	 * Note that changing the names for destination directory in the servlet context requires that you
	 * need to specify an imagePath for the taglibs. Alternatively you can also copy the taglibs into your
	 * WEB-INF/tags directory and modify for a different behavior or look and feel. In this case you need
	 * to specify your own (shept).tld file (copy implicit.tld from shept META-INF).
	 */
	
	public static void copyResourcesOnce(ClassPathResource cpr, String destPath, String resName) {
		if (alreadyCopied.contains(resName)) return;
		try {
			JarUtils.copyResources(cpr, destPath);
		} catch (IOException ex) {
			logger.error("Could not copy required resources from " + cpr.getPath() + " to " + destPath, ex);
		} catch (URISyntaxException e) {
			logger.error("Could not copy required resources from " + cpr.getPath() + " to " + destPath, e);
		}
		alreadyCopied.add(resName);
	}

	public static void copyResourcesOnce(ClassPathResource cpr, String destPath) {
		copyResourcesOnce(cpr, destPath, destPath);
	}
	
	/**
	 * Copy resources from a classPath, typically within a jar file 
	 * to a specified destination, typically a resource directory in
	 * the projects webApp directory (images, sounds, e.t.c. )
	 * 
	 * Copies resources only if the destination file does not exist and
	 * the specified resource is available.
	 * 
	 * The ClassPathResource will be scanned for all resources in the path specified by the resource.
	 * For example  a path like:
	 * 	new ClassPathResource("resource/images/pager/", SheptBaseController.class);
	 * takes all the resources in the path 'resource/images/pager' (but not in sub-path)
	 * from the specified clazz 'SheptBaseController'
	 * 
	 * @param cpr ClassPathResource specifying the source location on the classPath (maybe within a jar)
	 * @param webAppDestPath Full path String to the fileSystem destination directory   
	 * @throws IOException when copying on copy error
	 * @throws URISyntaxException 
	 */
	public static void copyResources(ClassPathResource cpr, String webAppDestPath) 
		throws IOException, URISyntaxException {
		String dstPath = webAppDestPath; //  + "/" + jarPathInternal(cpr.getURL());
		File dir = new File(dstPath);  
		dir.mkdirs();				

		URL url = cpr.getURL();
		// jarUrl is the URL of the containing lib, e.g. shept.org in this case
		URL jarUrl = ResourceUtils.extractJarFileURL(url);
		String urlFile = url.getFile();
		String resPath = "";
		int separatorIndex = urlFile.indexOf(ResourceUtils.JAR_URL_SEPARATOR);
		if (separatorIndex != -1) {
			// just copy the the location path inside the jar without leading separators !/
			resPath = urlFile.substring(separatorIndex + ResourceUtils.JAR_URL_SEPARATOR.length());
		} else {
			return;	// no resource within jar to copy
		}
		
		File f = new File(ResourceUtils.toURI(jarUrl));
		JarFile jf = new JarFile(f); 

		Enumeration<JarEntry> entries = jf.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry =  entries.nextElement();
			String path = entry.getName();
			if (path.startsWith(resPath) && entry.getSize() > 0) {
				String fileName = path.substring(path.lastIndexOf("/"));
				File dstFile = new File(dstPath, fileName); 	//  (StringUtils.applyRelativePath(dstPath, fileName));
				Resource fileRes = cpr.createRelative(fileName);
				if (!dstFile.exists() && fileRes.exists()) {
					FileOutputStream fos = new FileOutputStream(dstFile);
					FileCopyUtils.copy(fileRes.getInputStream(), fos);
					logger.info("Successfully copied file " + fileName + " from " + cpr.getPath() + " to " + dstFile.getPath());
				}
			}
		}
		
		if (jf != null) {
			jf.close();
		}
		
	}

	/**
	 * Copy resources from a classPath, typically within a jar file 
	 * to a specified destination, typically a resource directory in
	 * the projects webApp directory (images, sounds, e.t.c. )
	 * 
	 * Copies resources only if the destination file does not exist and
	 * the specified resource is available.
	 * 
	 * @param resources Array of String resources (filenames) to be copied
	 * @param cpr ClassPathResource specifying the source location on the classPath (maybe within a jar)
	 * @param webAppDestPath Full path String to the fileSystem destination directory   
	 * @throws IOException when copying on copy error
	 */
	public static void copyResources(String[] resources, ClassPathResource cpr, String webAppDestPath) 
		throws IOException {
		String dstPath = webAppDestPath; //  + "/" + jarPathInternal(cpr.getURL());
		File dir = new File(dstPath);  
		dir.mkdirs();				
		for (int i = 0; i < resources.length; i++) {
			File dstFile = new File(dstPath, resources[i]); //    (StringUtils.applyRelativePath(dstPath, images[i]));
			Resource fileRes = cpr.createRelative(resources[i]);
			if (!dstFile.exists() && fileRes.exists()) {
				FileOutputStream fos = new FileOutputStream(dstFile);
				FileCopyUtils.copy(fileRes.getInputStream(), fos);
				logger.info("Successfully copied file " +  fileRes.getFilename() + " from " + cpr.getPath() + " to " + dstFile.getPath());
			}
		}
	}

	/**
	 * Extract the URL-String for the internal resource path from the given URL
	 * (which points to a resource in a jar file).
	 * This methods complements {@link ResourceUtils#extractJarFileURL(URL)}
	 * 
	 * @return empty String if no such internal jar path is found
	 * @param jarPathUrl
	 */
	public static String jarPathInternal(URL jarPathUrl) {
		String urlFile = jarPathUrl.getFile();
		int separatorIndex = urlFile.indexOf(ResourceUtils.JAR_URL_SEPARATOR);
		if (separatorIndex != -1) {
			separatorIndex = separatorIndex + ResourceUtils.JAR_URL_SEPARATOR.length();
			return urlFile.substring(separatorIndex, urlFile.length());
		}
		return "";
	}


	/**
	 * @return the alreadyCopied
	 */
	public static Set<? super String> getAlreadyCopied() {
		return alreadyCopied;
	}

}
