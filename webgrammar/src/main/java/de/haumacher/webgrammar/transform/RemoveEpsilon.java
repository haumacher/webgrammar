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

import de.haumacher.webgrammar.model.Alternative;
import de.haumacher.webgrammar.model.Concat;
import de.haumacher.webgrammar.model.Constant;
import de.haumacher.webgrammar.model.ContextFreeRule;
import de.haumacher.webgrammar.model.Epsilon;
import de.haumacher.webgrammar.model.Expression;
import de.haumacher.webgrammar.model.F;
import de.haumacher.webgrammar.model.Grammar;
import de.haumacher.webgrammar.model.Iteration;
import de.haumacher.webgrammar.model.NaryExpression;
import de.haumacher.webgrammar.model.NonTerminal;
import de.haumacher.webgrammar.model.Optional;
import de.haumacher.webgrammar.model.Rule;
import de.haumacher.webgrammar.model.UnaryExpression;
import de.haumacher.webgrammar.model.visit.ExpressionVisitor;
import de.haumacher.webgrammar.model.visit.VisitHelper;

public class RemoveEpsilon implements ExpressionVisitor<Expression, Void> {

	public void transform(Grammar grammar) {
		for (Rule rule : grammar.getRules()) {
			if (rule instanceof ContextFreeRule) {
				ContextFreeRule result = transformUnary((ContextFreeRule) rule);
				if (result == null) {
					throw new UnsupportedOperationException("Grammar has rule that expands to only the empty sequece.");
				}
			}
		}
	}

	@Override
	public Expression visit(Concat expr, Void arg) {
		return transformSequence(expr);
	}

	@Override
	public Expression visit(Alternative expr, Void arg) {
		boolean isOptional = transformNary(expr);
		if (isOptional) {
			Expression inner;
			if (expr.getExpressions().isEmpty()) {
				return null;
			} else if (expr.getExpressions().size() == 1) {
				inner = expr.getExpressions().get(0);
			} else {
				inner = expr;
			}
			Optional optional = F.optional();
			optional.setExpression(inner);
			return optional;
		} else {
			return expr;
		}
	}

	@Override
	public Expression visit(Constant expr, Void arg) {
		return expr;
	}

	@Override
	public Expression visit(Epsilon expr, Void arg) {
		return null;
	}

	@Override
	public Expression visit(NonTerminal expr, Void arg) {
		return expr;
	}

	@Override
	public Expression visit(Optional expr, Void arg) {
		return transformUnary(expr);
	}

	@Override
	public Expression visit(Iteration expr, Void arg) {
		expr.setSeparator(transform(F.getSeparator(expr)));
		
		return transformUnary(expr);
	}

	private Expression transformSequence(Concat expr) {
		if (transformNary(expr)) {
			if (expr.getExpressions().size() == 1) {
				return expr.getExpressions().get(0);
			} else if (expr.getExpressions().isEmpty()) {
				return null;
			}
		}
		return expr;
	}

	private <E extends UnaryExpression> E transformUnary(E expr) {
		Expression sub = expr.getExpression();
		Expression result = transform(sub);
		expr.setExpression(result);

		if (result == null) {
			return null;
		} else {
			return expr;
		}
	}
	
	private boolean transformNary(NaryExpression expr) {
		boolean removed = false;
		
		ListIterator<Expression> it = expr.getExpressions().listIterator();
		while (it.hasNext()) {
			Expression sub = it.next();
			Expression result = transform(sub);
			if (result == null) {
				it.remove();
				removed = true;
			} else {
				it.set(result);
			}
		}
		
		return removed;
	}

	private Expression transform(Expression sub) {
		return VisitHelper.visitExpression(this, sub, null);
	}

}
