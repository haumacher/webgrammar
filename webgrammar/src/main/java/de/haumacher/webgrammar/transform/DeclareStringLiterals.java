/*
 * Copyright (c) 2015, Bernhard Haumacher. 
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package de.haumacher.webgrammar.transform;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.haumacher.webgrammar.model.Constant;
import de.haumacher.webgrammar.model.Expression;
import de.haumacher.webgrammar.model.F;
import de.haumacher.webgrammar.model.Grammar;
import de.haumacher.webgrammar.model.NonTerminal;
import de.haumacher.webgrammar.model.RegexpString;
import de.haumacher.webgrammar.model.Rule;
import de.haumacher.webgrammar.model.TokenRule;

public class DeclareStringLiterals extends ExpressionTransformer<Void> {

	private final Set<String> _existingNames = new HashSet<String>();
	private final Map<String, String> _namesForLiterals = new LinkedHashMap<String, String>();
	
	private int _id = 1;
	
	@Override
	public void visit(Grammar grammar) {
		for (Rule rule : grammar.getRules()) {
			_existingNames.add(JavaCCGenerator.ruleName(rule));
		}
		
		super.visit(grammar);
		
		int n = 0;
		for (Entry<String, String> entry : _namesForLiterals.entrySet()) {
			TokenRule rule = F.regexpRule();
			rule.setName(entry.getValue());
			RegexpString literal = F.regexpString();
			literal.setContent(entry.getKey());
			rule.setExpression(literal);
			
			grammar.getRules().add(n++, rule);
		}
	}

	@Override
	public Expression visit(Constant expr, Void arg) {
		String content = expr.getContent();
		String name = _namesForLiterals.get(content);
		if (name == null) {
			name = content.replaceAll("[^a-zA-Z]+", "").toUpperCase();
			
			String origName = name;
			boolean empty = name.isEmpty();
			if (empty || _existingNames.contains(name)) {
				name = "T" + (_id++) + (empty ? "" : "_" + origName);
			}
			_namesForLiterals.put(content, name);
		}
		NonTerminal result = F.nonTerminal();
		result.setName(name);
		return result;
	}

}
