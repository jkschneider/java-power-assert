/*
 * Copyright 2016 the original author or authors.
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

package org.powerassert;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("*")
public class PowerAssertProcessor extends AbstractProcessor {
	private JavacPowerAssertGenerator javacGenerator = new JavacPowerAssertGenerator();

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		if(!isInitialized()) {
			super.init(processingEnv);
		}
		javacGenerator.init(processingEnv);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if(!roundEnv.processingOver()) {
			for(Element element: roundEnv.getRootElements()) {
				javacGenerator.scan(element);
			}
		}

		return false;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		// the latest version of whatever JDK we run on
		return SourceVersion.values()[SourceVersion.values().length - 1];
	}
}