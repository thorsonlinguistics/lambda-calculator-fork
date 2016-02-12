/*
 * This file is part of The Lambda Calculator.
 *
 * The Lambda Calculator is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The Lambda Calculator is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * The Lambda Calculator. If not, see <http://www.gnu.org/licenses/>.
 */

package lambdacalc.lf;

import lambdacalc.gui.TrainingWindow;
import lambdacalc.logic.CompositeType;
import lambdacalc.logic.Expr;
import lambdacalc.logic.FunApp;
import lambdacalc.logic.IdentifierTyper;
import lambdacalc.logic.Type;
import lambdacalc.logic.Var;

public class IntensionalTraceRule extends CompositionRule {
  public static final IntensionalTraceRule INSTANCE
    = new IntensionalTraceRule();

  private IntensionalTraceRule() {
    super("Intensional Trace");
  }

  public boolean isApplicableTo(Nonterminal node) {
    if (node.size() != 1) return false;

    LFNode trace = node.getChild(0);

    if (trace instanceof Trace) {
      try {
        Expr traceMeaning = trace.getMeaning();
        // We only care about composite types, so we cast to CompositeType
        CompositeType traceType = (CompositeType)traceMeaning.getType();
        Type left = traceType.getLeft();
        if (left == Type.S) {
          return true;
        }
        else {
          return false;
        }
      } catch (Exception e) { // TypeEvaluation, MeaningEvaluation, casting
        return false;
      }
    } else {
      return false;
    }
  }

  public Expr applyTo(Nonterminal node, boolean onlyIfApplicable,
      boolean defaultApplyLeftToRight) throws MeaningEvaluationException {
    return this.applyTo(node, new AssignmentFunction(), onlyIfApplicable,
        defaultApplyLeftToRight);
  }

  public Expr applyTo(Nonterminal node, AssignmentFunction g,
      boolean onlyIfApplicable) throws MeaningEvaluationException {
    return this.applyTo(node, g, onlyIfApplicable, true);
  }

  public Expr applyTo(Nonterminal node, AssignmentFunction g,
      boolean onlyIfApplicable, boolean defaultApplyLeftToRight)
    throws MeaningEvaluationException {

    if (!this.isApplicableTo(node)) {
      throw new MeaningEvaluationException("The intensional trace rule is" +
          " not applicable to the node: " + node.toString());
    }

    Trace trace = (Trace)node.getChild(0);
    Expr traceMeaning = trace.getMeaning();

    IdentifierTyper typingConventions 
      = TrainingWindow.getCurrentTypingConventions();

    // Get a variable to represent the base world
    Var var = typingConventions.getVarForType(Type.S, false, "w");
    FunApp application = new FunApp(traceMeaning, var);

    return application;
  }

}
