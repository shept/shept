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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;


/**
 * @author Andreas Hahn
 * @version %1
 * @date 11.05.2008
 *
 */
public class FileUtils {
	
	private static final Log log = LogFactory.getLog(FileUtils.class);

	private class Filter implements FileFilter {
		
		String pattern = "*";
		
		String path = "";
		
		public Filter(String dirPath, String filePattern) {
			this.path = dirPath;
			this.pattern = filePattern;
		}
		
		public boolean accept(File file) {
			if (file.isDirectory()) return false;
			if (! StringUtils.pathEquals(file.getParent(), path)) return false;
			return PatternMatchUtils.simpleMatch(this.pattern, file.getName());
		}
	}
	
	/**
	 * List the file directory as specified by the name filter and the path directory
	 * The maps keys contain name and modification date for comparison
	 */
	public static SortedMap<FileNameDate, File> fileMapByNameAndDate(String path, String filePattern) {
		FileUtils fu = new FileUtils();  // needed for inner class
		SortedMap<FileNameDate, File> newFileMap = new TreeMap<FileNameDate, File>();
		File fileDir = new File(path);
		if (null != path && fileDir.isDirectory()) {
			FileFilter filter = fu.new Filter(path,filePattern);
			File[] files = fileDir.listFiles(filter);
			for (int i = 0; i < files.length; i++) {
				FileNameDate fk = new FileNameDate(files[i].getName(), files[i].lastModified());
				newFileMap.put(fk, files[i]);
			}
		}
		return newFileMap;
	}
	
	/**
	 * List the file directory as specified by the name filter and path directory
	 * The maps keys contain modification dat for comparison
	 * @param path
	 * @param filePattern
	 * @return
	 */
	public static SortedMap<Calendar, File> fileMapByDate(String path, String filePattern) {
		FileUtils fu = new FileUtils();  // needed for inner class
		SortedMap<Calendar, File> newFileMap = new TreeMap<Calendar, File>();
		File fileDir = new File(path);
		if (null != path && fileDir.isDirectory()) {
			FileFilter filter = fu.new Filter(path,filePattern);
			File[] files = fileDir.listFiles(filter);
			for (int i = 0; i < files.length; i++) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(files[i].lastModified());
				newFileMap.put(cal, files[i]);
			}
		}
		return newFileMap;
	}
	
	/**
	 * List the file directory as specified by the name filter and path directory
	 * The maps keys contain the parsed date from the filename.
	 * The filename can be something like Backup_db_2008_12_01_1350.xyz
	 * The first part is may begin with any character, the first number found
	 * is treated as year followed by month followed by day.
	 * All characters are separated by "_" (underscores).
	 * The last 4-letter number represents hours & minutes
	 * 
	 * @param path
	 * @param filePattern
	 * @return
	 */
	public static SortedMap<Calendar, File> fileMapByFilenameDate(String path,
			String filePattern) {
		FileUtils fu = new FileUtils(); // needed for inner class
		SortedMap<Calendar, File> newFileMap = new TreeMap<Calendar, File>();
		File fileDir = new File(path);
		if (null != path && fileDir.isDirectory()) {
			FileFilter filter = fu.new Filter(path, filePattern);
			File[] files = fileDir.listFiles(filter);
			for (int i = 0; i < files.length; i++) {
				Calendar cal = Calendar.getInstance();
				String[] split = files[i].getName().split("_");
				int j = 0;
				// looking for the first digit
				while (!Character.isDigit(split[j].charAt(0))) {
					j++;
				}
				try {
					cal.set(Calendar.YEAR, Integer.valueOf(split[j++]));
					cal.set(Calendar.MONTH, Integer.valueOf(split[j++]) -1);
					cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(split[j++]));
					cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(split[j]
							.substring(0, 2)));
					cal.set(Calendar.MINUTE, Integer.valueOf(split[j]
							.substring(2, 4)));
					newFileMap.put(cal, files[i]);
				} catch (Throwable t) {
					// if format doesn't fit ignore the file
				}
			}
		}
		return newFileMap;
	}
	
	
	
	/**
	 * Compare the source path and the destination path for filenames with the filePattern
	 * e.g. *.bak, *tmp and copy these files to the destination dir if they are not present at the
	 * destination or if they have changed (by modifactionDate)
	 * 
	 * @param destPath
	 * @param sourcePath
	 * @param filePattern
	 * @return the number of files being copied
	 */
	public static Integer syncAdd(String sourcePath, String destPath, String filePattern) {
		// check for new files since the last check which need to be copied
		Integer number = 0;
		SortedMap<FileNameDate, File> destMap = fileMapByNameAndDate(destPath, filePattern);
		SortedMap<FileNameDate, File> sourceMap = fileMapByNameAndDate(sourcePath, filePattern);
		// identify the list of source files different from their destinations
		for (FileNameDate fk : destMap.keySet()) {
			sourceMap.remove(fk);
		}
		
		// copy the list of files from source to destination
		for (File file : sourceMap.values()) {
			log.debug(file.getName() + ": " + new Date(file.lastModified()));
			File copy = new File(destPath, file.getName());
			try {
				if (! copy.exists() ) {
					// copy to tmp file first to avoid clashes during lengthy copy action
					File tmp = File.createTempFile("vrs", ".tmp", copy.getParentFile());
					FileCopyUtils.copy(file, tmp);
					if (! tmp.renameTo(copy)) {
						tmp.delete();	// cleanup if we fail
					}
					number ++;
				}
			} catch (IOException ex ) {
				log.error("FileCopy error for file " + file.getName(), ex);
			}
		}
		return number;
	}
	
	/**
	 * 
	 * @param dirPath
	 * @return the ArrayList of file in the directory dirPath matching the filePattern
	 */
	public static List<File> getDirectory(String dirPath, String filePattern) {
		FileUtils fu = new FileUtils();
		File fileDir = new File(dirPath);
		if (null == dirPath || ! fileDir.isDirectory() ) { 
			return new ArrayList<File>();
		}
		FileFilter filter = fu.new Filter(dirPath,filePattern);
		File[] files = fileDir.listFiles(filter);
		return (List<File>) CollectionUtils.arrayToList(files);
	}

	public static List<File> getDirectory(String dirPath) {
		return getDirectory(dirPath, "*");
	}

	public static String getTomcatWebAppPath() {
	      String path = System.getProperty("wtp.deploy");		// use this property while debugging
	      if (!StringUtils.hasText(path)) {
		      path = System.getProperty("catalina.base");
		      Assert.hasLength(path, "Catalina (tomcat) installation base directory not found");
		      path = path + "/webapps";
	      }
	      return path;
	}

}
