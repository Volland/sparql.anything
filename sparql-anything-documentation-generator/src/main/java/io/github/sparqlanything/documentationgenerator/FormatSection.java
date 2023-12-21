package io.github.sparqlanything.documentationgenerator;

import io.github.sparqlanything.engine.TriplifierRegister;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.annotations.Example;
import io.github.sparqlanything.model.annotations.Examples;
import io.github.sparqlanything.model.annotations.Format;
import io.github.sparqlanything.model.annotations.Option;
import org.apache.jena.query.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FormatSection {

	private final String name, description, resourceExample;

	private final List<TriplifierSection> triplifiers;

	private final List<String> extensions, mimeTypes;

	private final List<OptionSection> optionSections;

	public FormatSection(Package p, Set<Class<?>> classes) {
		Format format = p.getAnnotation(Format.class);
		this.name = format.name();
		this.description = format.description();
		this.resourceExample = format.resourceExample();
		this.triplifiers = new ArrayList<>();
		this.extensions = new ArrayList<>();
		this.mimeTypes = new ArrayList<>();

		TriplifierRegister triplifierRegister = TriplifierRegister.getInstance();
		for (Class<?> triplifierClass : classes) {
			TriplifierSection triplifierSection = new TriplifierSection(triplifierClass.getName(), triplifierClass);
			this.extensions.addAll(triplifierRegister.getTriplifierExtensions(triplifierClass.getName()));
			this.mimeTypes.addAll(triplifierRegister.getTriplifierMimeType(triplifierClass.getName()));
			triplifiers.add(triplifierSection);
		}

		optionSections = new ArrayList<>();
		for (Class<?> triplifier : classes) {
			for (Field field : triplifier.getFields()) {
				if (field.getType().equals(IRIArgument.class)) {
					Option o = field.getAnnotation(Option.class);
					Examples examples = field.getAnnotation(Examples.class);
					Example example = field.getAnnotation(Example.class);
					if (o != null && examples != null) {
						optionSections.add(new OptionSection(o, examples.value()));
					} else if (o !=null && example!=null) {
						optionSections.add(new OptionSection(o, new Example[]{example}));
					}
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
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

	public String getDefaultTransformationQuery() {
		//return QueryFactory.create(String.format("CONSTRUCT {GRAPH ?g {?s ?p ?o}} WHERE {SERVICE<x-sparql-anything:location=%s> { GRAPH ?g { ?s ?p ?o}}}", f.getResourceExample()));
		return QueryFactory.create(String.format("CONSTRUCT {?s ?p ?o} WHERE {SERVICE<x-sparql-anything:location=%s> {GRAPH ?g { ?s ?p ?o}}}", getResourceExample())).toString(Syntax.syntaxSPARQL_11);
	}

	public String getResourceExample() {
		return resourceExample;
	}

	public String getFacadeXRdf() {
		return Utils.getFacadeXRdf(QueryFactory.create(getDefaultTransformationQuery()));
	}

	public List<OptionSection> getOptionSections(){
		return optionSections;
	}
}
