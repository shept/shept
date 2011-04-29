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

package org.shept.services.jcaptcha;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.ImageCaptchaService;

/**
 * This class is copied and modified from the JCaptcha example
 * 
 * @see http://forge.octo.com/jcaptcha/confluence/display/general/5+minutes+application+integration+tutorial
 */
public class ImageCaptchaServlet extends HttpServlet implements
		ApplicationContextAware {

	private ApplicationContext ctx;

	private static final long serialVersionUID = 1L;

	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
	}

	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		this.ctx = ctx;
	}

	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException,
			IOException {
		BufferedImage challenge = null;
		try {
			// get the session id that will identify the generated captcha.
			// the same id must be used to validate the response, the session id
			// is a good candidate!
			String captchaId = httpServletRequest.getSession().getId();

			// If we have an explicit configuration for an ImageService we use this
			// else we use the predefined default
			ImageCaptchaService captchaService = CaptchaServiceSingleton.getInstance();
			Map services = ctx.getBeansOfType(ImageCaptchaService.class);
			// there must be exactly on service configured
			if (services.size() == 1) {
				for (Iterator iterator = services.values().iterator(); iterator
						.hasNext();) {
					captchaService = (ImageCaptchaService) iterator.next();
				}
			}
			
			// call the ImageCaptchaService getChallenge method
			challenge = captchaService.getImageChallengeForID(
					captchaId, httpServletRequest.getLocale());

		} catch (IllegalArgumentException e) {
			httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} catch (CaptchaServiceException e) {
			httpServletResponse
					.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		// flush it in the response
		httpServletResponse.setHeader("Cache-Control", "no-store");
		httpServletResponse.setHeader("Pragma", "no-cache");
		httpServletResponse.setDateHeader("Expires", 0);
		httpServletResponse.setContentType("image/jpeg");
		ServletOutputStream responseOutputStream = httpServletResponse
				.getOutputStream();
		ImageIO.write(challenge, "jpeg", responseOutputStream);
		responseOutputStream.flush();
		responseOutputStream.close();
	}

}