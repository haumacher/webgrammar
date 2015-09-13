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

import java.io.OutputStreamWriter;

import de.haumacher.webgrammar.model.Grammar;

public class Transformer {

	private final OutputStreamWriter _out;

	public Transformer(OutputStreamWriter out) {
		_out = out;
	}

	public void transform(Grammar grammar) {
		new RemoveEpsilon().transform(grammar);
		new PullOptional().visit(grammar);
		new FlattenConcat().visit(grammar);
		new InlineOptionals().visit(grammar);
		new EliminateEndRecursion().visit(grammar);

		new InlineLists().visit(grammar);
		new JoinOptionalsLists().visit(grammar);
		new InlineDelegates().visit(grammar);
		
		new ExpandIterations().visit(grammar);
		new FlattenConcat().visit(grammar);
		
		new InlineLists().visit(grammar);
		new JoinOptionalsLists().visit(grammar);
		new InlineDelegates().visit(grammar);
		
		new FlattenConcat().visit(grammar);
		new ExtractNonTerminals().visit(grammar);
		
		new InlineDelegates().visit(grammar);
		
		new ConsolidateEnumProductions().visit(grammar);

//		new DeclareStringLiterals().transform(grammar);
		new JavaCCGenerator(_out).visit(grammar);
		new WebgrammarPrinter(_out).visit(grammar);
	}

}
