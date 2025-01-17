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

import io.github.sparqlanything.engine.TriplifierRegister;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.annotations.Example;
import io.github.sparqlanything.model.annotations.Examples;
import io.github.sparqlanything.model.annotations.Format;
import io.github.sparqlanything.model.annotations.Option;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.diff.StringsComparator;
import org.apache.jena.query.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

public class FormatSection {

	private final String name, description, resourceExample;

	private final List<TriplifierSection> triplifiers;

	private final List<String> extensions, mimeTypes;

	private final List<OptionSection> optionSections;

	private final Format format;

	public FormatSection(Package p, Set<Class<?>> classes) {
		this.format = p.getAnnotation(Format.class);
		this.name = format.name();
		this.description = format.description();
		this.resourceExample = format.resourceExample();
		this.triplifiers = new ArrayList<>();
		this.extensions = new ArrayList<>();
		this.mimeTypes = new ArrayList<>();
		this.optionSections = new ArrayList<>();
		init(classes);
	}

	private void init(Set<Class<?>> classes) {
		TriplifierRegister triplifierRegister = TriplifierRegister.getInstance();
		for (Class<?> triplifierClass : classes) {
			TriplifierSection triplifierSection = new TriplifierSection(triplifierClass.getName(), triplifierClass);
			this.extensions.addAll(triplifierRegister.getTriplifierExtensions(triplifierClass.getName()));
			this.mimeTypes.addAll(triplifierRegister.getTriplifierMimeType(triplifierClass.getName()));
			triplifiers.add(triplifierSection);
		}
		Collections.sort(extensions);
		Collections.sort(mimeTypes);
		triplifiers.sort(new Comparator<TriplifierSection>() {
			@Override
			public int compare(TriplifierSection o1, TriplifierSection o2) {
				return o1.getClassName().compareTo(o2.getClassName());
			}
		});

		for (Class<?> triplifier : classes) {
			for (Field field : triplifier.getFields()) {
				if (field.getType().equals(IRIArgument.class)) {
					Option o = field.getAnnotation(Option.class);
					Examples examples = field.getAnnotation(Examples.class);
					Example example = field.getAnnotation(Example.class);
					if (o != null && examples != null) {
						optionSections.add(new OptionSection(field, o, examples.value()));
					} else if (o != null && example != null) {
						optionSections.add(new OptionSection(field, o, new Example[]{example}));
					} else if (o != null) {
						optionSections.add(new OptionSection(field, o, new Example[]{}));
					}
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return Utils.addLinkToIssues(description);
	}

	public List<TriplifierSection> getTriplifiers() {
		return triplifiers;
	}

	public List<String> getExtensions() {
		return extensions;
	}

	public List<String> getMediaTypes() {
		return mimeTypes;
	}

	public String getFacadeXRdf() {
		return Utils.getFacadeXRdf(QueryFactory.create(getDefaultTransformationQuery()));
	}

	public String getDefaultTransformationQuery() {
		if (format.query().isEmpty())
			return QueryFactory.create(String.format("CONSTRUCT {?s ?p ?o} WHERE {SERVICE<x-sparql-anything:location=%s> {GRAPH ?g { ?s ?p ?o}}}", getResourceExample())).toString(Syntax.syntaxSPARQL_11);
		return QueryFactory.create(format.query()).toString(Syntax.syntaxSPARQL_11);
	}

	public String getResourceExample() {
		return resourceExample;
	}

	public String getResourceExamplePreview() throws IOException {
		return Utils.readResourceToString(resourceExample);
	}

	public List<OptionSection> getOptionSections() {
		return optionSections;
	}

	public boolean getBinary(){
		return format.binary();
	}
}
