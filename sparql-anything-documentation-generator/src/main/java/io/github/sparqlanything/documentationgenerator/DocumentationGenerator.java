/*
 * Copyright (c) 2024 SPARQL Anything Contributors @ http://github.com/sparql-anything
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

package io.github.sparqlanything.documentationgenerator;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.github.sparqlanything.engine.FacadeX;
import io.github.sparqlanything.engine.TriplifierRegister;
import io.github.sparqlanything.model.annotations.Format;
import io.github.sparqlanything.model.annotations.Triplifier;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.engine.main.QC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DocumentationGenerator {

	private static final Logger logger = LoggerFactory.getLogger(DocumentationGenerator.class);

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String formatFolder = args[0];
		final String targetPackage;
		if (args.length > 1) {
			targetPackage = args[1];
		} else {
			targetPackage = null;
		}
		QC.setFactory(ARQ.getContext(), FacadeX.ExecutorFactory);
		Map<Package, Set<Class<?>>> packageToClasses = getFormatPackages();
		Configuration freemarkerCfg = getConfiguration();
		Template temp = freemarkerCfg.getTemplate("format.ftlh");

		packageToClasses.forEach((p, classes) -> {
			try {
				if (targetPackage == null || p.getName().matches(targetPackage)) {
					generateTemplateForFormat(temp, p, classes, formatFolder);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static Map<Package, Set<Class<?>>> getFormatPackages() throws ClassNotFoundException {
		Map<Package, Set<Class<?>>> packageToClass = new HashMap<>();
		for (String triplifier : TriplifierRegister.getInstance().getTriplifiers()) {
			Class<?> triplifierClass = Class.forName(triplifier);
			if (triplifierClass.getAnnotation(Triplifier.class) != null) {
				Package p = triplifierClass.getPackage();
				Format f = p.getAnnotation(Format.class);
				if (f != null) {
					Set<Class<?>> classes = packageToClass.get(p);
					if (classes == null) {
						classes = new HashSet<>();
					}
					classes.add(triplifierClass);
					packageToClass.put(p, classes);
				}
			}
		}
		return packageToClass;
//		return Arrays.stream(Package.getPackages()).filter(aPackage -> aPackage.getAnnotation(Format.class)!=null).collect(Collectors.toList());
	}

	private static Configuration getConfiguration() {
		// Create your Configuration instance, and specify if up to what FreeMarker
		// version (here 2.3.27) do you want to apply the fixes that are not 100%
		// backward-compatible. See the Configuration JavaDoc for details.
		Configuration freemarkerCfg = new Configuration(Configuration.VERSION_2_3_28);

		// Specify the source where the template files come from. Here I set a
		// plain directory for it, but non-file-system sources are possible too:
		freemarkerCfg.setClassLoaderForTemplateLoading(DocumentationGenerator.class.getClassLoader(), ".");
		freemarkerCfg.setNumberFormat("computer");

		// Set the preferred charset template files are stored in. UTF-8 is
		// a good choice in most applications:
		freemarkerCfg.setDefaultEncoding("UTF-8");

		// Sets how errors will appear.
		// During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is
		// better.
		freemarkerCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		// Don't log exceptions inside FreeMarker that it will thrown at you anyway:
		freemarkerCfg.setLogTemplateExceptions(true);

		// Wrap unchecked exceptions thrown during template processing into
		// TemplateException-s.
		freemarkerCfg.setWrapUncheckedExceptions(true);

		freemarkerCfg.setClassForTemplateLoading(DocumentationGenerator.class, "");
		return freemarkerCfg;
	}

	private static void generateTemplateForFormat(Template temp, Package p, Set<Class<?>> classes, String formatFolder) throws IOException {
		Map<String, Object> var = new HashMap<>();

		FormatSection formatSection = new FormatSection(p, classes);
		var.put("format", formatSection);

		String filename = formatFolder + "/" + formatSection.getName() + ".md";
		FileWriter fileWriter = new FileWriter(filename);
		try {
			temp.process(var, fileWriter);
		} catch (TemplateException | IOException e) {
			e.printStackTrace();
		}

		logger.info("{} generated!", filename);


	}


}
