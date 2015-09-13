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

import static de.haumacher.webgrammar.transform.TransformUtil.*;

import java.util.List;

import de.haumacher.webgrammar.model.Concat;
import de.haumacher.webgrammar.model.Expression;
import de.haumacher.webgrammar.model.F;
import de.haumacher.webgrammar.model.Iteration;

public class ExpandIterations extends ExpressionTransformer<Void> {

	/**
	 * A -> B  C D ( E F  C D )*
	 * =>
	 * A -> B ( C D , E F )*
	 * 
	 * A -> B  C D  ( C D )*
	 * =>
	 * A -> B ( C D )+
	 * 
	 * A -> B  C D  E F  ( C D , E F )+
	 * =>
	 * A -> B ( C D , E F )[2..]
	 */
	@Override
	public Expression visit(Concat expr, Void arg) {
		super.visit(expr, arg);
		
		List<Expression> expressions = expr.getExpressions();
		for (int n = 0, cnt = expressions.size(); n < cnt; n++) {
			Expression expression = expressions.get(n);
			if (expression instanceof Iteration) {
				Iteration iteration = (Iteration) expression;
				List<Expression> separatorContent = asList(F.getSeparator(iteration));
				
				boolean hasSeparator = !separatorContent.isEmpty();
				if (hasSeparator && iteration.getMin() == 0) {
					// Cannot be extended, since the production can produce a dangling separator.
					break;
				}

				List<Expression> iterationContent = concat(asList(iteration.getExpression()), separatorContent);
				int iterationSize = iterationContent.size();
				while (true) {
					int matchSize = 0;
					for (; matchSize < iterationSize; matchSize++) {
						int contextPos = n - 1 - matchSize;
						if (contextPos < 0) {
							break;
						}
						int iterationPos = iterationSize - 1 - matchSize;
						if (!expressions.get(contextPos).equals(iterationContent.get(iterationPos))) {
							break;
						}
					}
					
					if (matchSize < iterationSize) {
						if (matchSize == 0) {
							break;
						}
						
						if (hasSeparator) {
							break;
						}
												
						// Partial match, try to find separator expression.
						int seperatorSize = iterationSize - matchSize;
						
						Expression separator = toExpression(iterationContent.subList(0, seperatorSize));
						List<Expression> iterationKernel = asModifiableList(iteration.getExpression());
						remove(iterationKernel, 0, seperatorSize);
						iteration.setExpression(toExpression(iterationKernel));

						iteration.setSeparator(separator);
						hasSeparator = true;
					}
					
					remove(expressions, n - matchSize , matchSize);
					n -= matchSize;
					cnt -= matchSize;
					
					iteration.setMin(iteration.getMin() + 1);
				}
			}
		}
		
		return expr;
	}
	
}
