/*
 * Copyright (c) 2021 Enrico Daga @ http://www.enridaga.net
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.spiceh2020.sparql.anything.engine.functions;

import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.jena.sparql.util.FmtUtils;

public class Next extends FunctionBase1 implements FXFunction {

	@Override
	public NodeValue exec(NodeValue nodeValue) {
		if(isContainerMembershipProperty(nodeValue)){
			int num = getInt(nodeValue);
			int prev = num + 1;
			return asContainerMembershipProperty(prev);
		}
		throw new ExprEvalException("Not a container membership property: " + FmtUtils.stringForNode(nodeValue.asNode())) ;
	}
}
