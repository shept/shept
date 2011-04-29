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

package org.shept.org.springframework.web.servlet.mvc.delegation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.shept.org.springframework.web.servlet.mvc.delegation.component.WebComponent;

/**
 * Interface that parameterizes the MultiActionController class.
 * In contrast to the {@link org.springframework.web.servlet.mvc.multiaction.MethodNameResolver}
 * this interface does not only provide a method name but also a value if available
 * and a component for handling this value
 */
public interface WebActionResolver {

	WebActionToken getAction(HttpServletRequest request, Set<String> componentPathName, Map<String, List<WebComponent>> handlersForPath );

}
