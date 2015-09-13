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
package de.haumacher.webgrammar.model.visit;

import de.haumacher.webgrammar.model.Annotation;
import de.haumacher.webgrammar.model.ContextFreeRule;
import de.haumacher.webgrammar.model.Grammar;
import de.haumacher.webgrammar.model.Name;
import de.haumacher.webgrammar.model.QName;
import de.haumacher.webgrammar.model.TokenRule;

public interface GrammarVisitor<R, A> extends ExpressionVisitor<R, A>, RegexpVisitor<R, A> {

	R visit(Grammar expr, A arg);
	R visit(ContextFreeRule expr, A arg);
	R visit(TokenRule expr, A arg);
	R visit(Annotation expr, A arg);
	R visit(Name expr, A arg);
	R visit(QName expr, A arg);

}
