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
 * IdentifierTyper.java
 *
 * Created on May 29, 2006, 3:45 PM
 */

package lambdacalc.logic;

import java.util.*;
import lambdacalc.exercises.Exercise;
import lambdacalc.exercises.ExerciseFile;
import lambdacalc.exercises.ExerciseFileFormatException;
import lambdacalc.exercises.ExerciseFileVersionException;

/**
 * Says whether an Identifier is a variable or a constant
 * and provides the semantic type of the Identifier. This class encapsulates
 * the conventions about which letters stand for which types of things.
 */
public class IdentifierTyper {
    private class Entry {
        public String start, end;
        public boolean var;
        public Type type;
        public String descr;
        
        public Entry(String s, String e, boolean v, Type t, String descr) {
            start = s;
            end = e;
            var = v;
            type = t;
            this.descr = descr;
        }
    }
    
    private ArrayList entries = new ArrayList();
    
    /**
     * Creates a new IdentifierTyper with no type mappings.
     */
    // TODO make private?
    public IdentifierTyper() {
    }
    
    /**
     * Creates an IdentifierTyper with the usual defaults.
     * The defaults are as follows:
     *    a-e : constants of type e
     *    P-Q : constants of type et (one place predicates)
     *    R-S : constants of type <e*e,t> (two place predicates: from a vector of two e's to a t)
     *    u-z : variables of type e
     *    U-Z : variables of type et
     */
    public static IdentifierTyper createDefault() {
        IdentifierTyper typer = new IdentifierTyper();
        typer.addEntry("a", "e", false, Type.E);
        typer.addEntry("P", "Q", false, Type.ET, "one-place predicate");
        typer.addEntry("R", "S", false, Type.ExET, "two-place predicate");
        typer.addEntry("u", "z", true, Type.E);
        typer.addEntry("U", "Z", true, Type.ET, "one-place predicate");
        return typer;
    }
    
    /**
     * Clears the mappings.
     */
    public void clear() {
        entries.clear();
    }
    
    /**
     * Sets the type of identifiers starting with the given character,
     * overriding previous settings.
     */
    public void addEntry(String lex, boolean isVariable, Type type) {
        addEntry(lex, lex, isVariable, type, null);
    }
    
    /**
     * Sets the type of identifiers starting with a character in the given range,
     * overriding previous settings.
     * @param descr A description of the type to display to users, or null
     */
    public void addEntry(String start, String end, boolean isVariable, Type type) {
        addEntry(start, end, isVariable, type, null);
    }
    
    /**
     * Sets the type of identifiers starting with a character in the given range,
     * overriding previous settings.
     * @param descr A description of the type to display to users, or null
     */
    public void addEntry(String start, String end, boolean isVariable, Type type, String description) {
        if (start == null || end == null || start.length() == 0 || end.length() == 0)
            throw new IllegalArgumentException("start or end is null, or a zero-length string.");
        if (!Character.isLetter(start.charAt(0)) || !Character.isLetter(end.charAt(0)))
            throw new IllegalArgumentException("Identifiers must start with letters.");
        if (Character.isLowerCase(start.charAt(0)) != Character.isLowerCase(end.charAt(0)))
            throw new IllegalArgumentException("In a range, the start and end of the range must be both uppercase or both lowercase.");
            
        entries.add(new Entry(start, end, isVariable, type, description));
    }
    
    private Entry findEntry(String identifier) throws IdentifierTypeUnknownException {
        // For single-letter trivial ranges, like x-x, we have to only look at the
        // first letter of identifier, because if identifier has primes and such,
        // we want that to still count as x.

	boolean isSingleLetter = identifier.length() == 1
            || !Character.isLetterOrDigit(identifier.charAt(1))
            || ExpressionParser.isPrime(identifier.charAt(1));

        for (int i = entries.size() - 1; i >= 0; i--) {
            Entry e = (Entry)entries.get(i);

            if (!isSingleLetter) {
                if (e.start.equals(identifier))
                    return e;
                continue;
            }
            
            if (e.start.length() > 1 || e.end.length() > 1) continue;

            boolean startOK, endOK;

            if (e.start.length() == 1)
                startOK = identifier.charAt(0) >= e.start.charAt(0);
            else
                startOK = identifier.compareTo(e.start) <= 0;

            if (e.end.length() == 1)
                endOK = identifier.charAt(0) <= e.end.charAt(0);
            else
                endOK = identifier.compareTo(e.end) <= 0;

            if (startOK && endOK)
                return e;
        }
        throw new IdentifierTypeUnknownException(identifier);
    }
    
    /**
     * Gets whether the identifier is a variable.
     * @throws IdentifierTypeUnknownException if the identifier cannot be
     * typed because it starts with a character not mapped
     */
    public boolean isVariable(String identifier) throws IdentifierTypeUnknownException {
        return findEntry(identifier).var;
    }
    
    /**
     * Gets the semantic type of the identifier.
     * @throws IdentifierTypeUnknownException if the identifier cannot be
     * typed because it starts with a character not mapped
     */
    public Type getType(String identifier) throws IdentifierTypeUnknownException {
        return findEntry(identifier).type;
    }

    /**
     * Gets a variable of a given type according to this IdentifierTyper.
     * Returns null if the IdentifierTyper doesn't contain this type or only
     * contains it as a type of constants.
     */
    public Var getVarForType(Type type, boolean markAsExplicitlyTyped) {
        return getVarForType(type, markAsExplicitlyTyped, "z");
    }
    public Var getVarForType(Type type, boolean markAsExplicitlyTyped, String defaultVar) {
        String varSymbol = defaultVar;
        if (!markAsExplicitlyTyped) {
            for (int j = 0; j < entries.size(); j++) {
                Entry e = (Entry)entries.get(j);
                if (e.type.equals(type) && e.var == true) {
                    varSymbol = e.start;
                    return new Var(varSymbol, type, false);
                }
            }
        }
        return new Var(varSymbol, type, true);
        // didn't find anything
//        return null;
    }
    
    /**
     * Clones this instance.
     */
    public IdentifierTyper cloneTyper() {
        IdentifierTyper ret = new IdentifierTyper();
        for (int j = 0; j < entries.size(); j++) {
            Entry e = (Entry)entries.get(j);
            ret.addEntry(e.start, e.end, e.var, e.type, e.descr);
        }
        return ret;
    }
    
    public String toString() {
        String ret = "";
        TypeMapping[] m = getMapping();
        for (int i = 0; i < m.length; i++) {
        	if (m[i].ranges.length == 0)
        		continue;
        	
            if (ret != "") ret += "\n";
            for (int k = 0; k < m[i].ranges.length; k++) {
                if (k > 0) ret += " ";
                if (m[i].ranges[k].start == m[i].ranges[k].end)
                    ret += m[i].ranges[k].start;
                else
                    ret += m[i].ranges[k].start + "-" + m[i].ranges[k].end;
            }
            ret += " : ";
            if (m[i].var)
                ret += "variables";
            else
                ret += "constants";
            ret += " of type ";
            if (m[i].descr != null)
                ret += m[i].descr;
            else
                ret += m[i].type.toString();
        }
        return ret;
    }
    
    public TypeMapping[] getMapping() {
        // Get a unique list of the types involved
        Set types = new HashSet();
        for (int i = 0; i < entries.size(); i++) {
            Entry e = (Entry)entries.get(i);
            types.add(new TypeMapping(e.type, e.var, e.descr));
        }
        
        // Create the type mapping array and put the types into a natural order
        TypeMapping[] ret = (TypeMapping[])types.toArray(new TypeMapping[0]);
        Arrays.sort(ret);
        
        // Build the return value; loop through distinct types
        for (int i = 0; i < ret.length; i++) {
            // Mark off which letters are covered by this type, in the ASCII range
            boolean[] letters = new boolean[256];
            
            for (int j = 0; j < entries.size(); j++) {
                Entry e = (Entry)entries.get(j);
                if (!e.type.equals(ret[i].type) || e.var != ret[i].var || e.type.containsVar() || ret[i].type.containsVar()) continue;
                if (e.start.length() > 1) continue;
                
                for (int k = e.start.charAt(0) + (e.start.length() == 1 ? 0 : 1); k <= e.end.charAt(0); k++)
                    if (k >= 0 && k < letters.length)
                        letters[k] = true;
            }
            
            // Collapse consecutive letters into ranges
            ArrayList ranges = new ArrayList();
            CharRange lastRange = null;
            for (int k = 0; k < letters.length; k++) {
                if (!letters[k]) continue;
                
                // If this is the first letter we've seen, or if this letter
                // does not extend the previous range we added, put a new
                // singleton range into the list.
                if (lastRange == null || k != lastRange.end + 1) {
                    lastRange = new CharRange((char)k, (char)k);
                    ranges.add(lastRange);
                    
                // Otherwise, this letter extends the previous range, so
                // just extend it.  (Since lastRange is an object, we're modifying
                // the object we last put into the list.
                } else {
                    lastRange.end = (char)k;
                }
            }
            
            ret[i].ranges = (CharRange[])ranges.toArray(new CharRange[0]);
            Arrays.sort(ret[i].ranges); // is already be sorted, actually
        }

        return ret;
    }
            
    public class TypeMapping implements Comparable {
        public boolean var;
        public Type type;
        public CharRange[] ranges;
        public String descr;
        
        TypeMapping(Type t, boolean v, String d) {
            type = t; var = v; descr = d;
        }
        
        public int hashCode() { return type.hashCode(); }
        
        public boolean equals(Object other) {
            return compareTo(other) == 0;
        }

        public int compareTo(Object other) {
            TypeMapping m = (TypeMapping)other;
            int c = type.compareTo(m.type);
            if (c != 0) return c;
            if (var != m.var) return (var ? 1 : -1);
            return 0;
        }
    }
    
    public class CharRange implements Comparable {
        public char start, end;
        CharRange(char start, char end) {
            this.start = start; this.end = end;
        }
        public int compareTo(Object other) {
            CharRange r = (CharRange)other;
            if (start < r.start) return -1;
            if (start > r.start) return 1;
            if (end < r.end) return -1;
            if (end > r.end) return 1;
            return 0;
        }
    }

    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeShort(2); // format version marker
        output.writeShort(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            Entry e = (Entry)entries.get(i);
            output.writeUTF(e.start);
            output.writeUTF(e.end);
            output.writeBoolean(e.var);
            e.type.writeToStream(output);
            output.writeBoolean(e.descr != null);
            if (e.descr != null) output.writeUTF(e.descr);
        }
    }
    public void readFromStream(java.io.DataInputStream input, int fileFormatVersion) throws java.io.IOException, ExerciseFileFormatException {
        
        if (input.readShort() != 2) throw new ExerciseFileVersionException();
        
        int nEntries = input.readShort();
        for (int i = 0; i < nEntries; i++) {
            String start = input.readUTF();
            String end = input.readUTF();
            boolean var = input.readBoolean();
            Type type = Type.readFromStream(input);
            String descr = null;
            if (input.readBoolean())
                descr = input.readUTF();
            
            Entry e = new Entry(start, end, var, type, descr);
            entries.add(e);
        }
    }
}
