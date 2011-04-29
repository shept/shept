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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Date;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;

/** 
 * @version $$Id: FtpFileCopy.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class FtpFileCopy {
	
	private static final Log logger = LogFactory.getLog(FtpFileCopy.class);

	/**
	 * @param
	 * @return
	 *
	 * @param config
	 * @param ftpC
	 * @throws SocketException
	 * @throws IOException
	 */
	private boolean configSetup(FtpConfig config, FTPClient ftpC)
			throws IOException {
		boolean rc;
		ftpC.connect(config.getHostAddress(), config.getPort());
		rc = ftpC.login(config.getUserName(), config.getPassword());
		if (!rc) {
			logger.error("Ftp could not log to remote server with " + config.getUserName());
			return false;
		}
		if (StringUtils.hasText(config.getServerPath())) {
			int cwdCode = ftpC.cwd(config.getServerPath());
			if ( ! FTPReply.isPositiveCompletion(cwdCode)) {
				logger.error("Ftp could not change working dir to " + config.getServerPath() + " - Server returned Code " + cwdCode);
				return false;
			}
		}
		rc = ftpC.setFileType(config.getFileType());
		if ( !rc) {
			logger.error("Ftp could not change FileType for transmission");
			return false;
		}
		return true;
	}

	public int ftpSyncLocal(FtpConfig config, String localPath, String filePattern) {
		boolean rc = false;
		FTPClient ftpC = new FTPClient();
		int num = -1;
		try {
			rc = configSetup(config, ftpC);
			if (!rc) {
				return 0;
			}
			num = syncPull(ftpC, localPath, filePattern);
		} catch( Exception ex) {
			logger.error("Ftp preparing fileCopy did not succeed: " + config.toString(), ex);
		}
		try {
			ftpC.disconnect();
		} catch (Exception ex) {};
		return num;
}

	public int ftpSyncRemote(FtpConfig config, String remotePath, String filePattern) {
		boolean rc = false;
		FTPClient ftpC = new FTPClient();
		int num = -1;
		try {
			rc = configSetup(config, ftpC);
			if (!rc) {
				return 0;
			}
			num = syncPush(remotePath, ftpC, filePattern);
		} catch( Exception ex) {
			logger.error("Ftp preparing fileCopy did not succeed: " + config.toString(), ex);
		}
		try {
			ftpC.disconnect();
		} catch (Exception ex) {};
		return num;
}


/**
 * Compare the source path and the destination path for filenames with the filePattern
 * e.g. *.bak, *tmp and copy these files to the destination dir if they are not present at the
 * destination or if they have changed (by modifactionDate).
 * Copying is done through a retrieve operation.
 * 
 * @param ftpSource
 * @param localDestPat
 * @param filePattern
 * @return the number of files being copied
 * @throws IOException 
 */
public Integer syncPull(FTPClient ftpSource, String localDestPat, String filePattern) throws IOException {
	// check for new files since the last check which need to be copied

	Integer number = 0;
	SortedMap<FileNameDate, File> destMap = FileUtils.fileMapByNameAndDate(localDestPat, filePattern);
	SortedMap<FileNameDate, FTPFile> sourceMap = fileMapByNameAndDate(ftpSource, filePattern);
	// identify the list of source files different from their destinations
	for (FileNameDate fk : destMap.keySet()) {
		sourceMap.remove(fk);
	}

	// copy the list of files from source to destination
	for (FTPFile file : sourceMap.values()) {
		logger.debug("Copying file " + file.getName() + ": " + file.getTimestamp());
		File copy = new File(localDestPat, file.getName());
		try {
			if (! copy.exists() ) {
				// copy to tmp file first to avoid clashes during lengthy copy action
				File tmp = File.createTempFile("vrs", ".tmp", copy.getParentFile());
				FileOutputStream writeStream = new FileOutputStream(tmp);
				boolean rc = ftpSource.retrieveFile(file.getName(), writeStream);
				writeStream.close();
				if (rc) {
					rc = tmp.renameTo(copy);
					number ++;
				}
				if (!rc) {
					tmp.delete();	// cleanup if we fail
				}
			}
		} catch (IOException ex ) {
			logger.error("Ftp FileCopy did not succeed (using " + file.getName() + ")" +
					" FTP reported error  " + ftpSource.getReplyString(), ex );
		}
	}
	return number;
}


/**
 * Compare the source path and the destination path for filenames with the filePattern
 * e.g. *.bak, *tmp and copy these files to the destination dir if they are not present at the
 * destination or if they have changed (by modifactionDate).
 * Copying is done from local directory into remote ftp destination directory.
 * 
 * @param destPath
 * @param localSourcePath
 * @param filePattern
 * @return the number of files being copied
 * @throws IOException 
 */
public Integer syncPush(String localSourcePath, FTPClient ftpDest, String filePattern) throws IOException {
	// check for new files since the last check which need to be copied

	Integer number = 0;
	SortedMap<FileNameDate, FTPFile> destMap = fileMapByNameAndDate(ftpDest, filePattern);
	SortedMap<FileNameDate, File> sourceMap = FileUtils.fileMapByNameAndDate(localSourcePath, filePattern);
	// identify the list of source files different from their destinations
	for (FileNameDate fk : destMap.keySet()) {
		sourceMap.remove(fk);
	}
	
	// copy the list of files from source to destination
	for (File file : sourceMap.values()) {
		logger.debug(file.getName() + ": " + new Date(file.lastModified()));

		try {
			// only copy file that don't exist yet
			if (ftpDest.listNames(file.getName()).length == 0 ) {
				FileInputStream fin = new FileInputStream(file);
				String tmpName = "tempFile";
				boolean rc = ftpDest.storeFile(tmpName, fin);
				fin.close();
				if (rc) {
					rc = ftpDest.rename(tmpName, file.getName());
					number ++;
				}
				if (! rc) {
					ftpDest.deleteFile(tmpName);
				}
			}
		} catch (Exception ex) {
				logger.error("Ftp FileCopy did not succeed (using " + file.getName() + ")" +
						" FTP reported error  " + ftpDest.getReplyString() );
		}
	}
	return number;
}


/**
 * List the file directory as specified by the name filter and the path directory
 * The maps keys contain name and modification date for comparison
 * @throws IOException 
 */
public SortedMap<FileNameDate, FTPFile> fileMapByNameAndDate(FTPClient ftp, String filePattern) 
	throws IOException {
	SortedMap<FileNameDate, FTPFile> newFileMap = new TreeMap<FileNameDate, FTPFile>();
	FTPFile[] files = ftp.listFiles();
	for (FTPFile ftpFile : files) {
		if (acceptFile(ftpFile, filePattern)) {
			ftpFile.getTimestamp();
			FileNameDate fk = new FileNameDate(ftpFile.getName(), ftpFile.getTimestamp().getTimeInMillis());
			newFileMap.put(fk, ftpFile);
		}
	}
	return newFileMap;
}


protected boolean acceptFile(FTPFile file, String pattern) {
		if (file.isDirectory()) return false;
		return PatternMatchUtils.simpleMatch(pattern, file.getName());
	}


/**
 * Creates the needed directories if necessary.
 * @param ftpClient A <code>FTPClient</code> being <b>connected</b>.
 * @param basePath The base path. This one <b>has to exist</b> on the ftp server!
 * @param path The path to be created.
 * @throws IOException IN case of an error.
 */
private boolean ensureFtpDirectory(FTPClient ftpClient, String basePath, String path) 
    throws IOException {
    ftpClient.changeWorkingDirectory(basePath);
    StringTokenizer tokenizer = new StringTokenizer(path, "/");
    while (tokenizer.hasMoreTokens()) {
        String folder = tokenizer.nextToken();
        FTPFile[] ftpFile = ftpClient.listFiles(folder);
        if (ftpFile.length == 0) {
            // create the directoy
            if (!ftpClient.makeDirectory(folder)) {
                  logger.error("Ftp Creating the destination directory did not succeed " + ftpClient.getReplyString());
                  return false;
            }
        }
        ftpClient.changeWorkingDirectory(folder);            
    }
    return true;
}


}
