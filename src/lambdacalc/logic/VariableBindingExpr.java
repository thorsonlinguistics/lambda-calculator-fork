/*
 * Copyright (C) 2007-2014 Dylan Bumford, Lucas Champollion, Maribel Romero
 * and Joshua Tauberer
 * 
 * This file is part of The Lambda Calculator.
 * 
 * The Lambda Calculator is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Lambda Calculator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with The Lambda Calculator.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * VariableBindingExpr.java
 *
 * Created on March 21, 2008, 7:01 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.logic;

import java.util.Set;

/**
 * An interface implemented by Expr's that bind variables.
 */
public interface VariableBindingExpr {
    
    /**
     * Returns whether any variables in the set vars are bound by this binder.
     */
    boolean bindsAny(Set vars);
    
}
