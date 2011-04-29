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

import java.net.InetAddress;

import org.apache.commons.net.ftp.FTPClient;

/** 
 * @version $$Id: FtpConfig.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class FtpConfig {

	private int fileType = FTPClient.BINARY_FILE_TYPE;
	
	private int port = 21;
	
	private InetAddress hostAddress;
	
	private String	userName = "anonymous";
	
	private String password = "";
	
	private String serverPath = "/";

	/**
	 * @return the fileType
	 */
	public int getFileType() {
		return fileType;
	}

	/**
	 * @param fileType the fileType to set
	 */
	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the hostAddress
	 */
	public InetAddress getHostAddress() {
		return hostAddress;
	}

	/**
	 * @param hostAddress the hostAddress to set
	 */
	public void setHostAddress(InetAddress hostAddress) {
		this.hostAddress = hostAddress;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the serverPath
	 */
	public String getServerPath() {
		return serverPath;
	}

	/**
	 * @param serverPath the serverPath to set
	 */
	public void setServerPath(String serverPath) {
		this.serverPath = serverPath;
	}


}
