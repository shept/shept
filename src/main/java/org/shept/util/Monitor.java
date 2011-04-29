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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Andreas Hahn
 * @version %2
 * @date ${date}
 * 
 */
public class Monitor {

	private static final Log log = LogFactory.getLog(Monitor.class);

	private boolean isMonitoring = false;
	private Date timestamp = new Date();
	private Integer monitorPort = 9876;
	private Integer hostPort = 9876;
	private String hostname = "localhost";
	private String sendMsg = "Ping";
	private String rcvMsg = "";
	private Integer lock = 0; // lock variable for sync accessing rcvMsg
	private Integer msecsOfflineLatency = 10000; // latency (10s) in msecs
	// until offline
	private Integer msecsSendTimeout = 2000; // send Timeout = 2s
	private Integer msecsRcvTimeout = 10000; // infinite Rcv Timeout
	private MonitorTask monitor = new MonitorTask();

	private class MonitorTask extends Thread {

		/**
		 * Poll for Incoming messages in an inifinite loop if configured as
		 * server
		 */
		public void run() {
			DatagramSocket sock = null;
			byte[] data = new byte[4096];
			try {
				while (isMonitoring) {
					if (sock == null) {
						sock = new DatagramSocket(monitorPort);
					}
					DatagramPacket pack = new DatagramPacket(data, data.length);
					sock.setSoTimeout(msecsRcvTimeout);
					try {
						sock.receive(pack);
						String rv = new String(data, 0, pack.getLength());
						synchronized (lock) {
							timestamp = new Date();
							rcvMsg = rv;
						}
					} catch (SocketTimeoutException timeEx) {
						// simply ignore timeouts
						// but allow for external changing of 'isMonitor'
						rcvMsg = "";
					} finally {
						// do nothing
					}
				}
			} catch (SocketException ex) {
				log.error("Monitoring Socket could not be bound to port "
						+ monitorPort);
			} catch (Exception e) {
				log.error("Exception while monitoring port " + monitorPort, e);
			} finally {
				if (sock != null) sock.close();
				sock = null;
				isMonitoring = false;
			}
		}
	}

	/**
	 * Send a simple 'Keep Alive' Datagram to the receiver
	 */
	public Boolean keepAlive() {
		try {
			InetAddress adr = InetAddress.getByName(hostname);
			byte[] data = sendMsg.getBytes();
			DatagramSocket socket = new DatagramSocket();
			DatagramPacket pack = new DatagramPacket(data, data.length, adr,
					hostPort);
			socket.setSoTimeout(msecsSendTimeout);
			socket.send(pack);
			socket.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Check for a simple Keep Alive message from a message sender
	 * 
	 * @return
	 */
	public String getMessage() {
		synchronized (lock) {
			return rcvMsg;
		}
	}

	public Date getTimestamp() {
		synchronized (lock) {
			return timestamp;
		}
	}

	public void setMonitoring(boolean isMonitoring) {
		if (isMonitoring && !monitor.isAlive()) {
			monitor = new MonitorTask();
			monitor.start();
		}
		this.isMonitoring = isMonitoring;
	}

	public void setMonitorPort(Integer port) {
		this.monitorPort = port;
	}

	public void setHostPort(Integer port) {
		this.hostPort = port;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setSendMsg(String sendMsg) {
		this.sendMsg = sendMsg;
	}

	/**
	 * answer true if the remote host under control still alive
	 * 
	 * @return
	 */
	public Boolean isAlive() {
		Long diff = (new Date()).getTime() - timestamp.getTime();
		return diff < msecsOfflineLatency;
	}

	/**
	 * answer true if the system is monitoring
	 */
	public Boolean isMonitoring() {
		return isMonitoring;
	}

	/**
	 * @param msecsOfflineLatency
	 *            the msecsOfflineLatency to set
	 */
	public void setMsecsOfflineLatency(Integer msecsOfflineLatency) {
		this.msecsOfflineLatency = msecsOfflineLatency;
	}

	/**
	 * @param msecsSendTimeout
	 *            the msecsSendTimeout to set
	 */
	public void setMsecsSendTimeout(Integer msecsSendTimeout) {
		this.msecsSendTimeout = msecsSendTimeout;
	}

	/**
	 * Shut down the monitoring service
	 */
	public void close() {
		isMonitoring = false;
	}
}
