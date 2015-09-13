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
package de.haumacher.webgrammar.model;

import java.util.ArrayList;
import java.util.List;

import de.haumacher.values.Value;
import de.haumacher.values.ValueFactory;

public class F {

	public static Grammar grammar() {
		return ValueFactory.newInstance(Grammar.class);
	}

	public static ContextFreeRule nonTerminalRule() {
		return ValueFactory.newInstance(ContextFreeRule.class);
	}

	public static TokenRule regexpRule() {
		return ValueFactory.newInstance(TokenRule.class);
	}

	public static RegexpAlternative regexpAlternative() {
		return ValueFactory.newInstance(RegexpAlternative.class);
	}

	public static RegexpSequence regexpSequence() {
		return ValueFactory.newInstance(RegexpSequence.class);
	}

	public static RegexpAlphabet regexpAlphabet() {
		return ValueFactory.newInstance(RegexpAlphabet.class);
	}

	public static RegexpOptional regexpOptional() {
		return ValueFactory.newInstance(RegexpOptional.class);
	}

	public static RegexpZeroOrMore regexpZeroOrMore() {
		return ValueFactory.newInstance(RegexpZeroOrMore.class);
	}

	public static RegexpOneOrMore regexpOneOrMore() {
		return ValueFactory.newInstance(RegexpOneOrMore.class);
	}

	public static RegexpString regexpString() {
		return ValueFactory.newInstance(RegexpString.class);
	}

	public static RegexpIdentifier regexpIdentifier() {
		return ValueFactory.newInstance(RegexpIdentifier.class);
	}

	public static CharRange charRange() {
		return ValueFactory.newInstance(CharRange.class);
	}

	public static Epsilon emptyExpression() {
		return ValueFactory.newInstance(Epsilon.class);
	}

	public static Concat concat() {
		return ValueFactory.newInstance(Concat.class);
	}

	public static Optional optional() {
		return ValueFactory.newInstance(Optional.class);
	}

	public static Iteration zeroOrMore() {
		return iteration();
	}

	public static Iteration oneOrMore() {
		Iteration result = iteration();
		result.setMin(1);
		return result;
	}

	public static Iteration iteration() {
		return ValueFactory.newInstance(Iteration.class);
	}

	public static Constant constant() {
		return ValueFactory.newInstance(Constant.class);
	}

	public static NonTerminal nonTerminal() {
		return ValueFactory.newInstance(NonTerminal.class);
	}

	public static Alternative alternative() {
		return ValueFactory.newInstance(Alternative.class);
	}

	// TODO: Workaround for but that Value contents cannot be null.
	public static Expression getSeparator(Iteration iteration) {
		Expression result = iteration.getSeparator();
		if (result != null && ((Value) result).descriptor().getValueInterface() == Expression.class) {
			return null;
		}
		return result;
	}

	public static Annotation annotation() {
		return ValueFactory.newInstance(Annotation.class);
	}

	public static QName qname() {
		return ValueFactory.newInstance(QName.class);
	}

	public static Name name(String name) {
		Name result = ValueFactory.newInstance(Name.class);
		result.setValue(name);
		return result;
	}

	public static List<Annotation> annotations() {
		return new ArrayList<>();
	}

}
