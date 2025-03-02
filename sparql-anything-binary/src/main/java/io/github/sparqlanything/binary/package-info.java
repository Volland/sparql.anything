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

@Format(name="Binary", description = "A binary file is any file that cannot be interpreted as a text file.\n" +
		"From a structural standpoint, a binary file can be seen as sequence of bytes.\n", resourceExample = "https://raw.githubusercontent.com/ianare/exif-samples/master/jpg/Canon_40D.jpg", binary = true)
package io.github.sparqlanything.binary;

import io.github.sparqlanything.model.annotations.Format;
