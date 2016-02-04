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

package lambdacalc.lf;

import java.util.HashMap;
import java.util.Map;
import lambdacalc.gui.TrainingWindow;
import lambdacalc.logic.CompositeType;
import lambdacalc.logic.Expr;
import lambdacalc.logic.FunApp;
import lambdacalc.logic.IdentifierTyper;
import lambdacalc.logic.Lambda;
import lambdacalc.logic.Type;
import lambdacalc.logic.TypeEvaluationException;
import lambdacalc.logic.Var;

public class FunctionCompositionRule extends CompositionRule {
  public static final FunctionCompositionRule INSTANCE
    = new FunctionCompositionRule();

  private FunctionCompositionRule() {
    super("Function Composition");
  }

  public boolean isApplicableTo(Nonterminal node) {
    if (node.size() != 2) return false;

    LFNode left = node.getChild(0);
    LFNode right = node.getChild(1);

    try {
      Expr leftMeaning = left.getMeaning();
      Expr rightMeaning = right.getMeaning();

      if (canComposeWith(leftMeaning, rightMeaning))
        return true;
      if (canComposeWith(rightMeaning, leftMeaning))
        return true;
    } catch (Exception e) {
    }

    return false;
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
    if (node.size() != 2)
      throw new MeaningEvaluationException("Function composition is not " +
          "applicable on a nonterminal that does not have exactly " +
          "two children.");

    LFNode left = node.getChild(0);
    LFNode right = node.getChild(1);

    if (onlyIfApplicable && left instanceof BareIndex)
      throw new MeaningEvaluationException("The left child of this node is" +
          " an index for lambda abstraction. Function composition is " +
          "undefined on such a node.");
    if (onlyIfApplicable && right instanceof BareIndex)
      throw new MeaningEvaluationException("The right child of this node is" +
          " an index for lambda abstraction. Function composition is " +
          "undefined on such a node.");

    Expr leftMeaning, rightMeaning;
    HashMap<Type,Type> typeMatches = new HashMap<>();

    CompositeType lt;
    CompositeType rt;

    try {
      leftMeaning = left.getMeaning();
      rightMeaning = right.getMeaning();
    } catch (MeaningEvaluationException mee) {
      throw new MeaningEvaluationException(mee.getMessage());
    }

    if (canComposeWith(leftMeaning, rightMeaning)) {
      try {
        lt = (CompositeType)leftMeaning.getType();
        rt = (CompositeType)rightMeaning.getType();
        typeMatches = Expr.alignTypes(lt.getLeft(), rt.getRight());
      } catch (TypeEvaluationException ex) {
        throw new MeaningEvaluationException(ex.getMessage());
      }
      return compose(left, right, rt.getLeft(), g, typeMatches);
    } else if (canComposeWith(rightMeaning, leftMeaning)) {
      try {
        lt = (CompositeType)leftMeaning.getType();
        rt = (CompositeType)rightMeaning.getType();
        typeMatches = Expr.alignTypes(lt.getRight(), rt.getLeft());
      } catch (TypeEvaluationException ex) {
        throw new MeaningEvaluationException(ex.getMessage());
      }
      return compose(right, left, lt.getLeft(), g, typeMatches);
    }

    if (onlyIfApplicable) {
      throw new MeaningEvaluationException("The children of the nonterminal "
          + (node.getLabel() == null ? node.toString() : node.getLabel()) +
          " are not of compatible types for function application.");
    } else {
      try {
        lt = (CompositeType)leftMeaning.getType();
        rt = (CompositeType)rightMeaning.getType();
      } catch (TypeEvaluationException ex) {
        throw new MeaningEvaluationException(ex.getMessage());
      }
      if (defaultApplyLeftToRight)
        return compose(left, right, rt.getLeft(), g);
      else
        return compose(right, left, lt.getLeft(), g);
    }
  }

  private boolean canComposeWith(Expr left, Expr right) {
    try {
      Type l = left.getType();
      Type r = right.getType();
      if (l instanceof CompositeType && r instanceof CompositeType) {
        CompositeType lt = (CompositeType)l;
        CompositeType rt = (CompositeType)r;
        if (rt.getRight().equals(lt.getLeft()))
            return true;
      }
    } catch (TypeEvaluationException ex) {
    }
    return false;
  }

  private Expr compose(LFNode left, LFNode right, Type inputType, 
      AssignmentFunction g) throws MeaningEvaluationException {
    Expr leftMeaning = left.getMeaning();
    Expr rightMeaning = right.getMeaning();
   
    IdentifierTyper typingConventions = 
      TrainingWindow.getCurrentTypingConventions();
    Var VARIABLE = typingConventions.getVarForType(inputType, false);

    FunApp internalFA = new FunApp(rightMeaning, VARIABLE);
    FunApp externalFA = new FunApp(leftMeaning, internalFA);

    Lambda result = new Lambda(VARIABLE, externalFA, true);

    return result;
  }

  private Expr compose(LFNode left, LFNode right, Type inputType, 
          AssignmentFunction g, HashMap<Type,Type> alignments) 
          throws MeaningEvaluationException {
    Expr leftMeaning = left.getMeaning();
    Expr rightMeaning = right.getMeaning();

    IdentifierTyper typingConventions =
      TrainingWindow.getCurrentTypingConventions();
    Var VARIABLE = typingConventions.getVarForType(inputType, false);

    FunApp internalFA = new FunApp(rightMeaning, VARIABLE);
    FunApp externalFA = new FunApp(leftMeaning, internalFA);

    if (!alignments.isEmpty()) {
      Map updates = new HashMap();

      internalFA = (FunApp) internalFA.createAlphatypicalVariant(
          alignments, internalFA.getAllVars(), updates
      );
      
      Map updates2 = new HashMap();
      externalFA = (FunApp) externalFA.createAlphatypicalVariant(
          alignments, externalFA.getAllVars(), updates
      );
    }

    Lambda result = new Lambda(VARIABLE, externalFA, true);
    
    return result;
  }
}
