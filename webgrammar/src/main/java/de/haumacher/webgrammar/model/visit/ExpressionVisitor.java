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

import de.haumacher.webgrammar.model.Alternative;
import de.haumacher.webgrammar.model.Concat;
import de.haumacher.webgrammar.model.Constant;
import de.haumacher.webgrammar.model.Epsilon;
import de.haumacher.webgrammar.model.Expression;
import de.haumacher.webgrammar.model.Iteration;
import de.haumacher.webgrammar.model.NonTerminal;
import de.haumacher.webgrammar.model.Optional;

/**
 * Visitor interface for the {@link Expression} hierarchy.
 * 
 * @author <a href="http://haumacher.de">Bernhard Haumacher</a>
 * @version $Revision: $ $Author: $ $Date: $
 */
public interface ExpressionVisitor<R,A> {

	R visit(Concat expr, A arg);
	R visit(Alternative expr, A arg);
	R visit(Constant expr, A arg);
	R visit(Epsilon expr, A arg);
	R visit(NonTerminal expr, A arg);
	R visit(Optional expr, A arg);
	R visit(Iteration expr, A arg);
}
