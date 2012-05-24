/*******************************************************************************
 * Copyright 2012 Vienna University of Technology
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This work originates from the Planets project, co-funded by the European Union under the Sixth Framework Programme.
 ******************************************************************************/
package eu.scape_project.planning.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

@Entity
@Inheritance
@DiscriminatorColumn(name = "optype")
public class ObjectProperty implements Serializable, ITouchable, Cloneable, Comparable<ObjectProperty> {

    private static final long serialVersionUID = 2518694956337947010L;    
    
    @Id @GeneratedValue
    private long id;

    private String propertyId;

    private String name;

    /**
     * Hibernate note: standard length for a string column is 255
     * validation is broken because we use facelet templates (issue resolved in  Seam 2.0)
     * therefore allow "long" entries
     */
    @Lob
    private String description;

    private String unit;

    private String type;

    @OneToOne(cascade=CascadeType.ALL)
    private ChangeLog changeLog = new ChangeLog();
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof ObjectProperty) {
            return propertyId.equals(((ObjectProperty)o).getPropertyId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return propertyId.hashCode();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ChangeLog getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(ChangeLog value) {
        changeLog = value;
    }

    public boolean isChanged(){
        return changeLog.isAltered();
    }
    
    public void touch() {
        changeLog.touch();
    }

    /**
     * @see ITouchable#handleChanges(IChangesHandler)
     */
    public void handleChanges(IChangesHandler h) {
        h.visit(this);
    }
    
    /**
     * returns a clone of self.
     * Implemented for storing and inserting fragments.
     * Subclasses obtain a shallow copy by invoking this method, then 
     * modifying the fields required to obtain a deep copy of this object.
     * the id is not copied
     */
    @Override
    public ObjectProperty clone() {
        try {
            // create shallow copy:
            ObjectProperty clone = (ObjectProperty)super.clone();
            // created-timestamp is automatically set to now
            clone.setChangeLog(new ChangeLog(this.getChangeLog().getChangedBy()));
            
            clone.id = 0;
            return clone;
        } catch (CloneNotSupportedException e) {
            // never thrown
            return null;
        }
    }

    /**
     * compares NAMES only
     */
    public int compareTo(ObjectProperty o) {
        if (o == null || o.getName() == null) {
            return 1;
        }
        if (name == null) {
            return -1;
        }
        return name.compareTo(o.getName());
    }

}

