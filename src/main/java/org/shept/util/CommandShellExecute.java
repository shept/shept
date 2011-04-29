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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * This class executes windows command-line jobs and swallows all
 * error and screen I/O that may occur
 * 
 * This source is from the internet and slightly modified to use Logging
 * and a local 'StreamGobbler' which swallows command line error and info output
 * @see http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=3
 * 
 * @author Andreas Hahn
 *
 */

public class CommandShellExecute implements Runnable {
	
	protected String cmdString;
	
	protected String[] envp = null;					// environment variables for Runtime.or null
	
	protected File workingDirectory = null;		// workingDirectory path or null
	
	private boolean isRunning = false;
	
	private Integer exitVal = -1;		// value indicates 'not executed'

	private static final Log log = LogFactory.getLog(CommandShellExecute.class);
	
	/**
	 * Merge the existing environment with additional parameters
	 * If the additional parameters are null then simply return the environment strings
	 * @param addEnv
	 * @return
	 */
	public static String[] getEnvironment(String[] addEnv) {
		List<String> list = new ArrayList<String>();
		Map<String, String> sysEnv = System.getenv();
		for (Entry<String, String> entry : sysEnv.entrySet()) {
			list.add(entry.getKey() + "=" + entry.getValue());
		}
		if (addEnv != null) {
			list.addAll(Arrays.asList(addEnv));
		}
		// to avoid ClassCastException when casting Object[] to String[]
		String[] res = new String[list.size()];
		for (int i = 0; i < res.length; i++) {res[i] = list.get(i);}
		return res;
	}
	
	public CommandShellExecute() {
	}

	public CommandShellExecute(String cmdString) {
		this.cmdString = cmdString;
	}

	/**
	 * This gobbler swallows any streamed output from the operation
	 * and writes it into the log
	 * 
	 * @author Andreas Hahn
	 *
	 */
	class StreamGobbler extends Thread
	{
		InputStream is;
		Boolean err;

		StreamGobbler(InputStream is, Boolean error)
		{
			this.is = is;
			this.err = error;
		}

		public void run()
		{
			try
			{
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line=null;
				while ( (line = br.readLine()) != null)
					if (err) {
						log.error(line);
					} else {
						log.info(line);
					}
			} catch (IOException ioe)
			{
				log.error("Error", ioe);
			}
		}
	}

	/**
	 * Run the command shell
	 */
	public void run()
	{
		String[] cmd = new String[3];

		Assert.notNull(cmdString);

		try
		{            
			isRunning = true;
			exitVal = -1;		// noit yet started
			String osName = System.getProperty("os.name" );
			if( osName.equals( "Windows 95" ) )
			{
				cmd[0] = "command.com" ;
				cmd[1] = "/C" ;
				cmd[2] = cmdString;
			}
			else if( osName.startsWith("Windows" ))
			{
				cmd[0] = "cmd.exe" ;
				cmd[1] = "/C" ;
				cmd[2] = cmdString;
			}
			else if( osName.startsWith("Linux" ))
			{
				cmd[0] = "/bin/bash" ;
				cmd[1] = "-c" ;
				cmd[2] = cmdString;
			}
			else {
				log.error("Unsupported operating system " + osName + " for command processing:  " + cmdString);
			}

			Runtime rt = Runtime.getRuntime();
			log.info("Executing " + cmd[0] + " " + cmd[1] + " " + cmd[2]);

			Process proc = rt.exec(cmd, envp, workingDirectory);

			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), true);            

			// any output?
			StreamGobbler outputGobbler = new 	StreamGobbler(proc.getInputStream(), false);

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			exitVal = proc.waitFor();
			log.info("Result: " + exitVal.toString()); 

		} catch (Throwable t)
		{
			log.error("Execution Error: " + cmd[0] + " " + cmd[1] + " " + cmd[2], t);
		} finally {
			isRunning = false;
		}
		return;
	}
	
	/**
	 * @return the exitVal
	 */
	public Integer getExitVal() {
		return exitVal;
	}

	/**
	 * @param workingDirectory the workingDirectory to set
	 */
	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	/**
	 * @param envp the envp to set
	 */
	public void setEnvp(String[] envp) {
		this.envp = envp;
	}
	
	/**
	 * set additional environment parameters to the ones inherited by the shell
	 * @param cmdString
	 */
	public void setEnvpAdd(String[] envp) {
		setEnvp(CommandShellExecute.getEnvironment(envp));
	}

	public void setCmdString(String cmdString) {
		this.cmdString = cmdString;
	}

	public boolean isRunning() {
		return isRunning;
	}
}
