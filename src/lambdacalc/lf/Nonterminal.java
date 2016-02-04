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

import java.util.List;
import java.util.SortedMap;
import java.util.Vector;
import javax.swing.JOptionPane;
import lambdacalc.gui.TrainingWindow;
import lambdacalc.logic.Expr;
import lambdacalc.logic.Type;
import lambdacalc.logic.TypeEvaluationException;

public class Nonterminal extends LFNode {
    
    private Vector children = new Vector();
    
    private CompositionRule compositor;
    private Vector userProvidedMeaningSimplification; //of Expr objects
    
    protected Type type = null;
    protected Expr meaning = null;
    
  /**
   *
   * @return
   */
  @Override
    public List getChildren() {
        return children;
    }
    
    public int size() {
        return children.size();
    }
    
    public boolean isBranching() {
        return children.size() >= 2;
    }
    public LFNode getChild(int index) {
        return (LFNode)children.get(index);
    }
    
    public void setChild(int index, LFNode node) {
        children.set(index, node);
    }
    
    public LFNode getLeftChild() {
        return (LFNode) children.get(0);
    }
    
    public LFNode getRightChild() {
        return (LFNode) children.get(children.size()-1);
    }
    
    public void addChild(LFNode node) {
        children.add(node);
        changes.firePropertyChange("children", null, null);
    }
    
    public CompositionRule getCompositionRule() {
        return compositor;
    }
    
    public boolean knowsCompositionRule() {
        return compositor != null;
    }
    
    public void setCompositionRule(CompositionRule rule) {
//        CompositionRule oldRule = compositor;
        compositor = rule;
//        changes.firePropertyChange("compositionRule", oldRule, compositor);
    }
    
    //returns vector of Expr objects
    public Vector getUserMeaningSimplification() {
        return userProvidedMeaningSimplification;
    }
    
    //simplificationSteps: vector of Expr objects
    public void setUserMeaningSimplification(Vector simplificationSteps) {
        userProvidedMeaningSimplification = simplificationSteps;
    }
    
    @Override
    public String getDisplayName() {
        return "Nonterminal";
    }
    
    @Override
    public boolean isMeaningful() {
        return true;
    }
    
    @Override
    public Expr getMeaning(AssignmentFunction g)
        throws MeaningEvaluationException {
        
        if (this.meaning != null) {
            return this.meaning;
        }
        if (lambdacalc.Main.GOD_MODE) {
            // Guess a composition rule, and if we don't find any, tell the user none seem to apply.
            if (compositor == null || !compositor.isApplicableTo(this))
                guessCompositionRule(TrainingWindow.getCurrentExFile().getRules());
            if (compositor == null) {
                throw new NonterminalLacksCompositionRuleException(this,
                        "I do not know how to combine the children of the " + getLabel() + " node." +
                        " For instance, function application does not apply because neither child's " +
                        "denotation is a function whose domain is the type of the denotation of the other child.");
            } 
        } else {
            if (compositor == null) { 
                if (NonBranchingRule.INSTANCE.isApplicableTo(this))
                    // We are always allowed to guess the non-branching rule, even when not in
                    // God mode.
                    compositor = NonBranchingRule.INSTANCE;
                else
                    throw new NonterminalLacksCompositionRuleException(this,
                        "Select a composition rule for the nonterminal "
                        + toShortString() + " before you try" +
                        " to combine the children of this node.");
            }
        }
        
        Expr m = compositor.applyTo(this, g, true);
        setMeaning(m);
        try {
            setType(m.getType());
        } catch (TypeEvaluationException ex) {
            //ex.printStackTrace();
        }
        return m;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public void setMeaning(Expr meaning) {
//        Expr oldMeaning = this.meaning;
        this.meaning = meaning;
//        changes.firePropertyChange("meaning", oldMeaning, this.meaning);
    }
    
    public boolean hasMeaning() {
        return meaning != null;
    }
    
    /**
     * Returns a map of properties. Keys are Strings and values are Objects.
     * Each entry represents a property-value pair. Properties include orthographic
     * strings, meanings, types, etc.
     *
     * @return a sorted map of properties
     */
    @Override
    public SortedMap getProperties() {
        SortedMap m = super.getProperties();
        m.put("Rule", this.getCompositionRule());
        m.put("Meaning", this.meaning);
        return m;
    }
    
    /**
     * Calls itself recursively on the children nodes, then
     * sets the composition rule of this nonterminal if it hasn't been
     * set yet and if it's uniquely determined.
     *
     * @param rules the rules
     * @param nonBranchingOnly
     */
    @Override
    public void guessRules(RuleList rules, boolean nonBranchingOnly) {
        for (int i = 0; i < children.size(); i++)
            getChild(i).guessRules(rules, nonBranchingOnly);
        
        if (compositor != null)
            return;
        
        if (nonBranchingOnly && this.isBranching()) return;
        
        guessCompositionRule(rules);
    }
    
    /**
     * Does nothing and calls itself recursively on the children nodes.
     *
     * @param lexicon the lexicon
     */
    @Override
    public void guessLexicalEntries(Lexicon lexicon) {
        for (int i = 0; i < children.size(); i++)
            getChild(i).guessLexicalEntries(lexicon);
    }
    
    
    
    private void guessCompositionRule(RuleList rules) {
      int compositorHits = 0;

      for (Object rule1 : rules) {
        CompositionRule rule = (CompositionRule) rule1;
        if (rule.isApplicableTo(this)) {
          if (compositorHits == 0) {
            // The first time we hit a compatible composition rule,
            // assign it to ourself.
            compositor = rule;
          } else {
            if (!lambdacalc.Main.GOD_MODE) {
              // But on the next time we hit a compatible rule, clear
              // out what we set and return. We thus don't actually set
              // compositor unless there is a uniquely applicable rule.
              compositor = null;
              return;
            } else {
              // With polymorphic types, it's possible for there to be
              // two legitimate applicable composition rules. We have
              // to ask God which one to use.
              // TODO: make this less horrible.
              TrainingWindow singleton = TrainingWindow.getSingleton();
              Object[] options = {compositor.toString(), rule.toString()};
              String optionMessage = this + " can be combined in multiple ways."
                 + "\n Which composition rule would you like?";
              int n = JOptionPane.showOptionDialog(singleton,
                                                   optionMessage,
                                                   "Compositor Choice",
                                                   JOptionPane.YES_NO_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE,
                                                   null, //no custom Icon
                                                   options, //button titles
                                                   options[0]); //default title
              switch(n) {
                case 1:
                  compositor = rule;
                  break;
                default:
                  break;
              }
            }
          }
          compositorHits += 1;
        }
      }
    }
    
    @Override
    public String toString() {
        String ret = "[";
        if (getLabel() != null) {
            ret += "." + getLabel();
            if (this.hasIndex())
                ret += String.valueOf(LFNode.INDEX_SEPARATOR) + this.getIndex();
            ret += " ";
        }
        for (int i = 0; i < children.size(); i++) {
            if (i > 0) ret += " ";
            ret += children.get(i).toString();
        }
        ret += "]";
        return ret;
    }

//    public String toLatexString() {
//        String ret = "[.{";
//        if (getLabel() != null) {
//            ret += getLabel();
//        }
//        ret += "} ";
//        for (int i = 0; i < children.size(); i++) {
//            if (i > 0) ret += " ";
//            ret += ((LFNode) children.get(i)).toLatexString();
//        }
//        ret += " ]";
//        if (this.hasIndex()) {
//            ret +="_{" + this.getIndex() + "}";
//        }
//        return ret;
//    }
    
}
