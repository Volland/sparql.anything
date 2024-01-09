/*
 * Copyright (c) 2023 SPARQL Anything Contributors @ http://github.com/sparql-anything
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

package io.github.sparqlanything.cli;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assume;
import io.github.sparqlanything.model.HTTPHelper;
public class Issue339Test {
	public static final Logger logger = LoggerFactory.getLogger(Issue339Test.class);

	@Test
	public void test() throws Exception {
		Assume.assumeTrue(HTTPHelper.checkHostIsReachable("https://schema.org"));
		String q = getClass().getClassLoader().getResource("./JsonldTest.sparql").toString();
		String d = getClass().getClassLoader().getResource("./JsonldTest.json").toURI().toString();
		//SPARQLAnything sa = new SPARQLAnything();
		String out = SPARQLAnything.callMain(new String[]{
				"-q", q, "-l", d
		});
		logger.debug("{}", out);
	}
}
