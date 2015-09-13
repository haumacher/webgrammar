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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.haumacher.webgrammar.model.Annotation;
import de.haumacher.webgrammar.model.Concat;
import de.haumacher.webgrammar.model.ContextFreeRule;
import de.haumacher.webgrammar.model.Expression;
import de.haumacher.webgrammar.model.F;

public class TransformUtil {

	public static void remove(List<Expression> expressions, int pos, int size) {
		int last = pos + size - 1;
		for (int k = 0; k < size; k++) {
			expressions.remove(last - k);
		}
	}

	public static Expression toExpression(List<Expression> list) {
		if (list.size() == 1) {
			return list.get(0);
		}
		Concat concat = F.concat();
		concat.getExpressions().addAll(list);
		return concat;
	}

	public static <T> List<T> concat(List<T> l1, List<T> l2) {
		if (l1.isEmpty()) {
			return l2;
		}
		if (l2.isEmpty()) {
			return l1;
		}
		List<T> result = new ArrayList<>(l1.size() + l2.size());
		result.addAll(l1);
		result.addAll(l2);
		return result;
	}

	public static List<Expression> asList(Expression expression) {
		if (expression == null) {
			return Collections.emptyList();
		}
		
		if (expression instanceof Concat) {
			return Collections.unmodifiableList(((Concat) expression).getExpressions());
		} else {
			return Collections.singletonList(expression);
		}
	}

	public static List<Expression> asModifiableList(Expression expression) {
		ArrayList<Expression> result = new ArrayList<>();
		if (expression instanceof Concat) {
			result.addAll(((Concat) expression).getExpressions());
		} else {
			result.add(expression);
		}
		return result;
	}

	public static void annotateSynthesized(ContextFreeRule nonEmpty, String synthesizedFromRuleName) {
		Annotation annotation = F.annotation();
		annotation.setName("synthesized");
		annotation.getArguments().add(F.name(synthesizedFromRuleName));
		nonEmpty.getAnnotations().add(annotation);
	}

}
