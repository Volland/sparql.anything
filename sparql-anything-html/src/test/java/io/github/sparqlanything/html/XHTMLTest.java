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

package io.github.sparqlanything.html;

import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.testutils.AbstractTriplifierTester;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;

public class XHTMLTest extends AbstractTriplifierTester {
	public XHTMLTest() {
		super(new HTMLTriplifier(), new Properties(), "xhtml");
	}

	protected void properties(Properties properties) {
		properties.setProperty(IRIArgument.BLANK_NODES.toString(), "true");
		properties.setProperty(HTMLTriplifier.PROPERTY_PARSER.toString(), "xml");
	}

	@Test
	public void testXHTML() {
		this.assertResultIsIsomorphicWithExpected();
	}

}
