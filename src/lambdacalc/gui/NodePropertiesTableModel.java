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
 * NodePropertiesTableModel.java
 *
 * Created on June 13, 2007, 10:28 AM
 *
 */

package lambdacalc.gui;

import java.util.*;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author champoll
 */
public class NodePropertiesTableModel extends AbstractTableModel {
    
    private String[] columnNames = { "Property", "Value" };
    
    private List data;
    
    /**
     * Creates a new instance of NodePropertiesTableModel from an iterator
     * over Map.Entry objects, each of which must map a String to an Object. 
     * This constructor can be used to specify a certain
     * order for the table.
     */
    public NodePropertiesTableModel(Iterator order) {
        data = new ArrayList();
        while (order.hasNext()) {
            data.add(order.next());
        }
    }
    
    /** 
     * Creates a new instance of NodePropertiesTableModel from a Map. The
     * Map must map strings to objects. The order of the rows in the table will
     * be the order of the iterator of the map.
     */
    public NodePropertiesTableModel(Map properties) {
        data = new ArrayList(properties.entrySet());
    }
    
    public int getColumnCount() {
        return 2;
    }
    
    public int getRowCount() {
        return data.size();
    }
    
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    private Map.Entry getEntry(int row) {
        return (Map.Entry) data.get(row);
    }
    
    private String getKey(int row) {
        return (String) getEntry(row).getKey();
    }
    
    private Object getValue(int row) {
        return getEntry(row).getValue();
    }
    
    public Object getValueAt(int row, int col) {
        if (col == 0) return getKey(row);
        if (col == 1) return getValue(row);
        throw new IllegalArgumentException();
    }
    
    public Class getColumnClass(int c) {
        if (c == 0) return String.class;
        if (c == 1) return Object.class;
        throw new IllegalArgumentException();
    }
    
    //public boolean isCellEditable(int row, int col) {
      //  return col == 1;
    //}
    
    //public void setValueAt(Object value, int row, int col) {
      //TODO implement editable cells   
    //}
}
