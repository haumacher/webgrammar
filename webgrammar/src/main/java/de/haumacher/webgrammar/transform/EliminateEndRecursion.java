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

import java.util.List;

import de.haumacher.webgrammar.model.Concat;
import de.haumacher.webgrammar.model.ContextFreeRule;
import de.haumacher.webgrammar.model.Expression;
import de.haumacher.webgrammar.model.F;
import de.haumacher.webgrammar.model.Iteration;
import de.haumacher.webgrammar.model.NonTerminal;
import de.haumacher.webgrammar.model.Optional;

public class EliminateEndRecursion extends ExpressionTransformer<Void> {
	
	/**
	 * A -> B ( C A )?
	 * =>
	 * A -> B ( C B )*
	 */
	@Override
	public void visit(ContextFreeRule rule, Void arg) {
		Expression last = last(rule.getExpression());
		if (last instanceof Optional) {
			Optional optional = (Optional) last;
			Expression lastOptional = last(optional.getExpression());
			if (lastOptional instanceof NonTerminal) {
				NonTerminal nonTerminal = (NonTerminal) lastOptional;
				if (nonTerminal.getName().equals(rule.getName())) {
					// End-recursive rule.
					Expression expression = removeLast(rule.getExpression());
					Expression optionalExpression = removeLast(optional.getExpression());
					
					if (optionalExpression == null) {
						Iteration iterated = F.oneOrMore();
						iterated.setExpression(expression);
						rule.setExpression(iterated);
					} else {
						Expression x = concat(optionalExpression, expression);
						Iteration iterated = F.zeroOrMore();
						iterated.setExpression(x);
						rule.setExpression(concat(expression, iterated));
					}
				}
			}
		}
	}

	private Expression last(Expression expression) {
		if (expression instanceof Concat) {
			List<Expression> expressions = ((Concat) expression).getExpressions();
			return last(expressions.get(expressions.size() - 1));
		} else {
			return expression;
		}
	}

	private Expression removeLast(Expression expression) {
		if (expression instanceof Concat) {
			List<Expression> expressions = ((Concat) expression).getExpressions();
			int lastIndex = expressions.size() - 1;
			Expression last = removeLast(expressions.get(lastIndex));
			if (last == null) {
				expressions.remove(lastIndex);
				if (expressions.isEmpty()) {
					return null;
				}
			} else {
				expressions.set(lastIndex, last);
			}
			return expression;
		} else {
			return null;
		}
	}

	private Expression concat(Expression e1, Expression e2) {
		if (e1 instanceof Concat) {
			((Concat) e1).getExpressions().add(e2);
			return e1;
		} else if (e2 instanceof Concat) {
			((Concat) e2).getExpressions().add(0, e1);
			return e2;
		} else {
			Concat result = F.concat();
			result.getExpressions().add(e1);
			result.getExpressions().add(e2);
			return result;
		}
	}

}
