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

import de.haumacher.webgrammar.model.ContextFreeRule;
import de.haumacher.webgrammar.model.Expression;
import de.haumacher.webgrammar.model.NonTerminal;
import de.haumacher.webgrammar.model.Rule;

public abstract class InlineTransformation extends ExpressionTransformer<Void> {

	@Override
	public Expression visit(NonTerminal expr, Void arg) {
		ContextFreeRule definition = getDefinition(expr);
		if (definition != null && shouldInline(definition)) {
			return copy(definition.getExpression());
		}
		
		return super.visit(expr, arg);
	}

	protected abstract boolean shouldInline(ContextFreeRule definition);
	
	protected ContextFreeRule getDefinition(NonTerminal expr) {
		Rule rule = getRule(expr.getName());
		if (rule instanceof ContextFreeRule) {
			return (ContextFreeRule) rule;
		}
		return null;
	}

}
