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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shept.org.springframework.beans.support.CommandWrapper;
import org.shept.org.springframework.beans.support.DefaultCommandObject;
import org.shept.org.springframework.web.bind.support.ComponentBindingInitializer;
import org.shept.org.springframework.web.bind.support.ComponentDataBinder;
import org.shept.org.springframework.web.bind.support.ComponentPostprocessor;
import org.shept.org.springframework.web.servlet.mvc.delegation.component.AbstractComponent;
import org.shept.org.springframework.web.servlet.mvc.delegation.component.WebComponent;
import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.SegmentConfiguration;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.LastModified;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
import org.springframework.web.servlet.view.RedirectView;

/**
 * {@link org.springframework.web.servlet.mvc.Controller Controller}
 * implementation that allows multiple request types to be handled by the same
 * class. 
 * 
 * The design is inspired by the {@link org.springframework.web.servlet.mvc.delegating.DelegatingController} but there are
 * some important differences. The DelegatingController is specifically designed to support repetitive elements on a WebPage
 * such as Tables, Schedules, Filtering and searching or any combination of these elements and your own custom requirements.
 * 
 * <p>This is accomplished by delegating to predefined handlers aka {@link WebComponent}
 * A number of handlers for filters and tables are already predefined and ready to use. By subclassing {@link AbstractComponent} 
 * and implementing the {@link WebComponent} you can extend the controller with custom Components. </p>
 * 
 * <p>A component is associated with some part of your command object. To resolve the association between components and
 * your command object your command object needs to implement the {@link SubCommandProvider} Interface which will
 * inform the controller about the components it supports. Basically the {@link SubCommandProvider} Interface specifies a map of 
 * bean pathnames and the (sub-)command objects at the nodes of each pathName. See  {@link DefaultCommandObject} for an example. </p>
 *  
 *  <p>This design is very flexible and allows for infinite extension as elements may repeat an infinte number of times requiring
 *  just a single handler to take care of them. A good example is a WebPage where you search for customers and their associated
 *  data, e.g. addresses, contracts, history information e.t.c. Its easily possible to expand the searchpage towards the end and so
 *  you can group a lot of editable information without writing much code.</p>
 *  
 *  <p>Now we need to match a form submission with the component handler in duty. By convention each form submission for a component
 *  other than root needs a prefix of the same bean pathname which is returned the {@link SubCommandProvider} interface. 
 *  Two resolvers provide this solution: The {@link ComponentNameResolver} will match the the pathname of the associated handler and the
 *  form submission. In a second step the {@link WebActionResolver} will supply the paramters of the form submission with a 
 *  {@link WebActionToken} gathering all the information from the form submission wrapping it with command object and component
 *  in a {@link ComponentToken} for further processing by the target handler. </p>
 *  
 *  <p>The DelegatingController does not bind request parameters to the command object this has to be done by the invoked handler.
 *  This allows for full control over the entire  binder setup and usage, including the invocation of {@link Validator Validators}
 * and the subsequent evaluation of binding/validation errors. All error handling is in the responsibility of the invoked handler</p>
 * 
 * <p>Return values can be a map containing model attributes or ModelAndView or RedirectView objects. If there is no 
 * model name or redirect url specified then default values will be provided. The configured 
 *  {@link org.springframework.web.servlet.RequestToViewNameTranslator} will be  used to determine the view name.
 *  In case of a RedirectView it will use the redirectViewName field which is be default the controllers name which should be
 *  by convention servlet pathName so redirected views implement by default the get-after-post strategy. </p
 *     
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @author Rob Harrop
 * @author Sam Brannen
 * @author Andreas Hahn
 *
 * @see WebActionResolver
 * @see ComponentNameResolver
 * @see SubmitActionResolver
 * @see org.springframework.web.servlet.mvc.LastModified#getLastModified
 * @see org.springframework.web.bind.ServletRequestDataBinder
 */
public class DelegatingController extends AbstractController implements LastModified, BeanNameAware, InitializingBean {

	/** Suffix for last-modified methods */
	public static final String LAST_MODIFIED_METHOD_SUFFIX = "LastModified";

	/** Default command name used for binding command objects: "command" */
	public static final String DEFAULT_COMMAND_NAME = "command";

	/**
	 * Log category to use when no mapped handler is found for a request.
	 * @see #pageNotFoundLogger
	 */
	public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";


	/**
	 * Additional logger to use when no mapped handler is found for a request.
	 * @see #PAGE_NOT_FOUND_LOG_CATEGORY
	 */
	protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);
	
	/** Delegate that knows how to determine method names and submission values from incoming requests */
	private WebActionResolver actionResolver = new SubmitActionResolver();

	/** Optional strategy for pre-initializing data binding */
	private WebBindingInitializer webBindingInitializer;

	/** components handlers */
	private WebComponent[] delegates;
	
	/** Redirection url - by default this should be the name of the bean (set via BeanNameAware) */
	private String redirectUrl;

	/** formView - this is the fileName  */
	private String formView;

	/** Command class specification */
	private Class<Object> commandClass;
	
	/**
	 * @param actionResolver the actionResolver to set
	 */
	public void setActionResolver(WebActionResolver actionResolver) {
		this.actionResolver = actionResolver;
	}

	/**
	 * @param delegates the delegates to set
	 */
	public void setDelegates(WebComponent[] delegates) {
		this.delegates = delegates;
	}

	/**
	 * @param commandClass the commandClass to set
	 */
	public void setCommandClass(Class<Object> commandClass) {
		this.commandClass = commandClass;
	}

	/**
	 * Specify a WebBindingInitializer which will apply pre-configured
	 * configuration to every DataBinder that this controller uses.
	 * <p>Allows for factoring out the entire binder configuration
	 * to separate objects, as an alternative to {@link #initBinder}.
	 */
	public final void setWebBindingInitializer(WebBindingInitializer webBindingInitializer) {
		this.webBindingInitializer = webBindingInitializer;
	}

	/**
	 * Return the WebBindingInitializer (if any) which will apply pre-configured
	 * configuration to every DataBinder that this controller uses.
	 */
	public final WebBindingInitializer getWebBindingInitializer() {
		return this.webBindingInitializer;
	}


	//---------------------------------------------------------------------
	// Implementation of LastModified
	//---------------------------------------------------------------------

	/**
	 * Try to find an XXXXLastModified method, where XXXX is the name of a handler.
	 * Return -1 if there's no such handler, indicating that content must be updated.
	 * @see org.springframework.web.servlet.mvc.LastModified#getLastModified(HttpServletRequest)
	 */
	public long getLastModified(HttpServletRequest request) {
//		try {
//			String handlerMethodName = "" ; // this.methodNameResolver.getHandlerMethodName(request);
//			Method lastModifiedMethod = this.lastModifiedMethodMap.get(handlerMethodName);
//			if (lastModifiedMethod != null) {
//				try {
//					// Invoke the last-modified method...
//					Long wrappedLong = (Long) lastModifiedMethod.invoke(this.delegate, request);
//					return (wrappedLong != null ? wrappedLong : -1);
//				}
//				catch (Exception ex) {
//					// We encountered an error invoking the last-modified method.
//					// We can't do anything useful except log this, as we can't throw an exception.
//					logger.error("Failed to invoke last-modified method", ex);
//				}
//			}
//		}
//		catch (NoSuchRequestHandlingMethodException ex) {
			// No handler method for this request. This shouldn't happen, as this
			// method shouldn't be called unless a previous invocation of this class
			// has generated content. Do nothing, that's OK: We'll return default.
//		}
		return -1L;
	}


	//---------------------------------------------------------------------
	// Implementation of AbstractController
	//---------------------------------------------------------------------

	/**
	 * Determine a handler method and invoke it.
	 * @see MethodNameResolver#getHandlerMethodName
	 * @see #invokeNamedMethod
	 * @see #handleNoSuchRequestHandlingMethod
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		try {
			// TODO improve error checks
			Object command = getCommandObject(request, commandClass);
			
			Map<String, Object> components = new HashMap<String, Object>();
			
			// check the command objects for components
			if (command instanceof SubCommandProvider) {
				SubCommandProvider cp = (SubCommandProvider) command;
				components = cp.getSubCommands();
			}
			
			// add the 'default' component, this is the 'root' command object at the root path
			components.put("", command);
			components = prepareComponentPathForLookup(components);
				
			// build a map of containing pathNames and their handlers
			Map<String, List<WebComponent>> handlersForPath = new HashMap<String, List<WebComponent>>();
			for (Entry<String, Object> component : components.entrySet()) {
				for (WebComponent handler : this.delegates) {
					if (component.getValue() != null
							&& handler.supports(component.getValue())) {
						List<WebComponent> handlers = handlersForPath.get(component.getKey());
						if (handlers == null) {
							handlers = new ArrayList<WebComponent>();
							handlersForPath.put(component.getKey(), handlers);
						}
						handlers.add(handler);
					}
				}
			}

			WebActionToken token = actionResolver.getAction(request, components.keySet(), handlersForPath);

			ComponentDataBinder binder = createBinder(request, command);
			ModelAndView mav = new ModelAndView(getFormView(), binder.getBindingResult().getModel());

			if (token != null) {		// we found a command in one of the components and delegate action to handler
				ComponentToken comToken = new ComponentToken();
				comToken.setComponent(components.get(token.getPathName()));
				comToken.setToken(token);
				comToken.setBinder(binder);
				logger.info("Processing input " + comToken);
				Object rv = token.getHandler().excecuteAction(request, response, comToken);
				mav = massageReturnValueIfNecessary(rv);
				if (mav != null && !mav.hasView()) {
					mav.setViewName(getFormView());
				}
			}	
			postProcessModel(request, mav);
			return mav;
		}
		
		catch (NoSuchRequestHandlingMethodException ex) {
			return handleNoSuchRequestHandlingMethod(ex, request, response);
		}
	}


	/**
	 * 
	 * put a colon '.' at the end of non empty pathNames
	 * @param
	 * @return
	 *
	 * @param componentPathName
	 * @param pathNames
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object>  prepareComponentPathForLookup(Map<String, Object> components) {
		Map<String, Object> copy = BeanUtils.instantiate(components.getClass());
		for (String pathName : components.keySet()) {
			Object object = components.get(pathName);
			pathName = ComponentUtils.getPropertyPathPrefix(pathName);
			copy.put(pathName, object);
		}
		return copy;
	}



	/**
	 * Handle the case where no request handler method was found.
	 * <p>The default implementation logs a warning and sends an HTTP 404 error.
	 * Alternatively, a fallback view could be chosen, or the
	 * NoSuchRequestHandlingMethodException could be rethrown as-is.
	 * @param ex the NoSuchRequestHandlingMethodException to be handled
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render, or <code>null</code> if handled directly
	 * @throws Exception an Exception that should be thrown as result of the servlet request
	 */
	protected ModelAndView handleNoSuchRequestHandlingMethod(
			NoSuchRequestHandlingMethodException ex, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		pageNotFoundLogger.warn(ex.getMessage());
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
		return null;
	}

	/**
	 * Processes the return value of a handler method to ensure that it either returns
	 * <code>null</code> or an instance of {@link ModelAndView}. When returning a {@link Map},
	 * the {@link Map} instance is wrapped in a new {@link ModelAndView} instance.
	 */
	@SuppressWarnings("unchecked")
	protected ModelAndView massageReturnValueIfNecessary(Object returnValue) {
		if (returnValue instanceof ModelAndView) {
			ModelAndView mav = (ModelAndView) returnValue;
			if (mav.getView() instanceof RedirectView) {
				RedirectView view = (RedirectView) mav.getView();
				String fragment ="";
				String url = view.getUrl();
				if (StringUtils.hasText(url) && url.startsWith("#")) {	// check if url begins with jump to anchor
					fragment = url;
					url = "";
				}
				if (! StringUtils.hasText(url)) {
					url = getRedirectUrl();
					if (StringUtils.hasText(fragment)) {
						url = url + fragment;
					}
					// NOTE: do NOT expose model attributes as part of the url for internal redirection
					mav.setView(new RedirectView(url, true, true, false));
				}
				return mav;
			} else {
				if (!mav.hasView()) {
					mav.setViewName(getFormView());
				}
				return mav;								
			}
		}
		else if (returnValue instanceof Map) {
			return new ModelAndView(getFormView(), (Map) returnValue);
		}
		else if (returnValue instanceof String) {
			return new ModelAndView((String) returnValue);
		}
		else {
			// Either returned null or was 'void' return.
			// We'll assume that the handle method already wrote the response.
			return null;
		}
	}


	/**
	 * Create a new command object of the given class.
	 * <p>This implementation uses <code>BeanUtils.instantiateClass</code>,
	 * so commands need to have public no-arg constructors.
	 * Subclasses can override this implementation if desired.
	 * @throws Exception if the command object could not be instantiated
	 * @see org.springframework.beans.BeanUtils#instantiateClass(Class)
	 */
	protected Object getCommandObject(HttpServletRequest request, Class clazz) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new command of class [" + clazz.getName() + "]");
		}
		return BeanUtils.instantiateClass(clazz);
	}

	/**
	 * Create a new binder instance for the given command and request.
	 * <p>Called by <code>bind</code>. Can be overridden to plug in custom
	 * ServletRequestDataBinder subclasses.
	 * <p>The default implementation creates a standard ServletRequestDataBinder,
	 * and invokes <code>initBinder</code>. Note that <code>initBinder</code>
	 * will not be invoked if you override this method!
	 * @param request current HTTP request
	 * @param command the command to bind onto
	 * @return the new binder instance
	 * @throws Exception in case of invalid state or arguments
	 * @see #bind
	 * @see #initBinder
	 */
	protected ComponentDataBinder createBinder(HttpServletRequest request, Object command) throws Exception {
		ComponentDataBinder binder = new ComponentDataBinder(command, getCommandName(command));
		initBinder(request, binder);
		return binder;
	}

	/**
	 * Return the command name to use for the given command object.
	 * <p>Default is "command".
	 * @param command the command object
	 * @return the command name to use
	 * @see #DEFAULT_COMMAND_NAME
	 */
	protected String getCommandName(Object command) {
		return DEFAULT_COMMAND_NAME;
	}

	/**
	 * Initialize the given binder instance, for example with custom editors.
	 * Called by <code>createBinder</code>.
	 * <p>This method allows you to register custom editors for certain fields of your
	 * command class. For instance, you will be able to transform Date objects into a
	 * String pattern and back, in order to allow your JavaBeans to have Date properties
	 * and still be able to set and display them in an HTML interface.
	 * <p>The default implementation is empty.
	 * <p>Note: the command object is not directly passed to this method, but it's available
	 * via {@link org.springframework.validation.DataBinder#getTarget()}
	 * @param request current HTTP request
	 * @param binder new binder instance
	 * @throws Exception in case of invalid state or arguments
	 * @see #createBinder
	 * @see org.springframework.validation.DataBinder#registerCustomEditor
	 * @see org.springframework.beans.propertyeditors.CustomDateEditor
	 */
	protected void initBinder(HttpServletRequest request, ComponentDataBinder binder) throws Exception {
		if (this.webBindingInitializer != null) {
			this.webBindingInitializer.initBinder(binder, new ServletWebRequest(request));
		}
		if (binder.getTarget() instanceof SubCommandProvider) {
			Map<String, CommandWrapper> pathMap = ComponentUtils.getComponentPathMap((SubCommandProvider)binder.getTarget());			
			for (Entry<String, CommandWrapper> entry : pathMap.entrySet()) {
				String name = entry.getValue().getTagName();
				SegmentConfiguration config = ComponentUtils.getConfiguration(getWebApplicationContext(), name);
				if (config != null && config.getComponentBindingInitializers() != null) {
					for (ComponentBindingInitializer bindInitializer : config.getComponentBindingInitializers()) {
						bindInitializer.initBinder(new ServletWebRequest(request), binder, entry.getKey());
					}
				}					
			}
		}
	}


	protected void postProcessModel (HttpServletRequest request, ModelAndView modelAndView) throws Exception {
		SubCommandProvider command = ComponentUtils.getCommand(modelAndView);
		if (command != null) {
			Map<String, CommandWrapper> pathMap = ComponentUtils.getComponentPathMap((SubCommandProvider) command);			
			for (Entry<String, CommandWrapper> entry : pathMap.entrySet()) {
				String name = entry.getValue().getTagName();
				SegmentConfiguration config = ComponentUtils.getConfiguration(getWebApplicationContext(), name);
				if (config != null && config.getComponentPostprocessors() != null) {
					for (ComponentPostprocessor processor : config.getComponentPostprocessors()) {
						processor.postHandle(new ServletWebRequest(request), modelAndView, entry.getKey());
					}
				}					
			}
		}
	}

	public void setBeanName(String name) {
		if (this.redirectUrl == null) {
			setRedirectUrl(name);
		}
	}

	/**
	 * @return the viewName
	 */
	public String getRedirectUrl() {
		return redirectUrl;
	}

	/**
	 * @param viewName the viewName to set
	 */
	public void setRedirectUrl(String viewName) {
		this.redirectUrl = viewName;
	}

	public void afterPropertiesSet() throws Exception {
		// TODO cleanup this seems to be nonsense from some earlier version
		if (this instanceof WebComponent) {
			if (this.delegates == null) {
				this.setDelegates(new WebComponent[] {(WebComponent)this});
			} else {
				List<WebComponent> list = new ArrayList<WebComponent>();
				list.addAll(Arrays.asList(delegates));
				list.add((WebComponent)this);
				this.setDelegates((WebComponent[]) list.toArray(new WebComponent[0]));
			}
		}
	}

	/**
	 * @return the formView
	 */
	public String getFormView() {
		return formView;
	}

	/**
	 * @param formView the formView to set
	 */
	public void setFormView(String formView) {
		this.formView = formView;
	}

}
