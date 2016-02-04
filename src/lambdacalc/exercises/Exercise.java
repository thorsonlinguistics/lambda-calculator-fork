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
 * Exercise.java
 *
 * Created on May 30, 2006, 4:30 PM
 */

package lambdacalc.exercises;

import lambdacalc.logic.*;

/**
 * An Exercise is the abstract base class of all individual exercises.
 * @author tauberer
 */
public abstract class Exercise {
    private int index;
    private boolean done;
    private java.math.BigDecimal points = java.math.BigDecimal.valueOf(1); // because floats might do weird rounding
    private String instructions;
    private boolean notSoFast;
    
    /**
     * Creates an Exercise with the given index in its ExerciseGroup.
     */
    public Exercise(int index) {
        this.index = index;
    }
    
    /**
     * Gets the zero-based index of this Exercise in its ExerciseGroup.
     */
    public int getIndex() {
        return index;
    }
    
    public String toString() {
        return getExerciseText();
    }
    
    /**
     * Gets the number of points associated with the exercise.
     */
    public java.math.BigDecimal getPoints() {
        return this.points;
    }
    
    /**
     * Are multiple reductions allowed at once?
     */
    public boolean getNotSoFast() {
        return this.notSoFast;
    }
    
    public void setNotSoFast(boolean nsf) {
        this.notSoFast = nsf;
    }
    
    /**
     * Sets the number of points associated with the exercise.
     * @param points the number of points awarded for a correct answer on this problem
     */
    public void setPoints(java.math.BigDecimal points) {
        this.points = points;
    }
    
    /**
     * Gets optional instruction text for the exercise.
     * @return instructional text, or null
     */
    public String getInstructions() {
        return instructions;
    }
    
    /**
     * Sets optional instruction text for the exercise.
     * @param instructions instructional text, or null
     */
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    /**
     * Gets the text associated with the exercise.
     */
    public abstract String getExerciseText();
    
    /**
     * Gets a tip to be displayed grayed in the user input text field,
     * like "enter your answer here".
     */
    public abstract String getTipForTextField();
    
    /**
     * Gets a very short directive for the problem, like "Simplify the expression".
     */
    public abstract String getShortDirective();

    /**
     * Gets a very short title for the problem, to be displayed in the tree to the left
     * of the TrainingWindow.
     */
    public abstract String getShortTitle();
    
    /**
     * Checks the status of an answer to the exercise.  Throws a SyntaxException
     * if the answer could not be understood at all.
     */
    public abstract AnswerStatus checkAnswer(String answer) throws SyntaxException;
    
    /**
     * Gets whether this Exercise has been totally completed.
     */
    public boolean isDone() {
        return done;
    }
    
    /**
     * Called by Exercise implementations to indicate that the exercise has
     * been completed.
     */
    public void setDone(boolean done) {
        this.done = done;
    }
    
    /**
     * Returns whether the user has provided a correct answer for this exercise.
     * For one-step exercises, this returns true just when the exercise has been
     * correctly completed. For multi-step exercises, this returns just when the user
     * has given a correct intermediate step.
     */
    public boolean hasBeenStarted() {
        return getLastAnswer() != null;
    }
    
    /**
     * Resets an exercise to its pristine unanswered state.
     */
    public void reset() {
        this.done = false;
    }
    
    /**
     * If the exercise has been started, gets the last correct answer
     * given by the user.  If the exercise is finished, this is the
     * correct final answer.  Otherwise it is the last correct
     * intermediate answer given.  If the exercise hasn't been started
     * yet, returns null.
     */
    public abstract String getLastAnswer();

    /**
     * Serialzies the exercise to a stream.
     */
    public abstract void writeToStream(java.io.DataOutputStream output) throws java.io.IOException;
}
