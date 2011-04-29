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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/** 
 * @version $$Id: HelperUtils.java 34 2010-08-20 16:46:49Z aha $$
 *
 * @author Andi
 *
 */
public class HelperUtils {

	
	public static byte[] serialize(Object obj)  {
	 	byte[] byteArray = null;
	 	ByteArrayOutputStream baos = null;
	 	ObjectOutputStream out = null;
	 	try {
	 		baos = new ByteArrayOutputStream();
	 		out = new ObjectOutputStream(baos);
	 		out.writeObject(obj);
	 		byteArray = baos.toByteArray();
	 	} catch (IOException ex) {
	 		throw new IllegalStateException("Could not serialize object" + obj.toString(), ex);
		}
	 		finally {
	 			if (out != null) {
	 				try {
						out.close();
					} catch (IOException e) {
					}
	 			}
	 		}
	 	return byteArray;
	 }


}
