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

import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
import com.octo.captcha.service.image.ImageCaptchaService;


/**
 * This class is copied from the JCaptcha example
 * @see http://forge.octo.com/jcaptcha/confluence/display/general/5+minutes+application+integration+tutorial
 **/
public class CaptchaServiceSingleton {
    
    private static ImageCaptchaService instance = new DefaultManageableImageCaptchaService();
    
    public static ImageCaptchaService getInstance(){
        return instance;
    }
}