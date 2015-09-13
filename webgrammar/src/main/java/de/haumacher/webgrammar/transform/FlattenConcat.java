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

import java.util.ListIterator;

import de.haumacher.webgrammar.model.Concat;
import de.haumacher.webgrammar.model.Expression;

public class FlattenConcat extends ExpressionTransformer<Void> {

	@Override
	public Expression visit(Concat expr, Void arg) {
		Expression result = super.visit(expr, arg);
		inlineConcats(expr);
		
		switch (expr.getExpressions().size()) {
		case 0: return null;
		case 1: return expr.getExpressions().get(0);
		default: return result;
		}
	}
	
	private void inlineConcats(Concat expr) {
		for (ListIterator<Expression> it = expr.getExpressions().listIterator(); it.hasNext(); ) {
			Expression sub = it.next();
			if (sub instanceof Concat) {
				it.remove();
				for (Expression part : ((Concat) sub).getExpressions()) {
					it.add(part);
				}
			}
		}
	}
	
}
