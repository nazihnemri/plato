/*******************************************************************************
 * Copyright 2006 - 2012 Vienna University of Technology,  
 * Department of Software Technology and Interactive Systems, IFS
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
package eu.scape_project.planning.model.values;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.validator.constraints.Length;

import eu.scape_project.planning.model.ChangeLog;
import eu.scape_project.planning.model.IChangesHandler;
import eu.scape_project.planning.model.ITouchable;
import eu.scape_project.planning.model.scales.Scale;


@Entity
@Inheritance
@DiscriminatorColumn(name = "type")
public abstract class Value implements Serializable, Cloneable,ITouchable {

    private static final long serialVersionUID = -5691552398563804873L;
    
    
    @OneToOne(cascade = CascadeType.ALL)
    private ChangeLog changeLog = new ChangeLog();
    
    @Id
    @GeneratedValue
    private int id;

    public ChangeLog getChangeLog() {
        return changeLog;
    }

    @ManyToOne
    private Scale scale;


    /**
     * Hibernate note: standard length for a string column is 255
     * validation is broken because we use facelet templates (issue resolved in  Seam 2.0)
     * therefore allow "long" entries
     *
     * Please note that when chaning something here don't forget to update the
     * length restriction in xhtml!
     */
    @Length(max = 2000000)
    @Column(length = 200000)
    private String comment = "";
    
    //private String comment;

    public Scale getScale() {
        return scale;
    }

    public void setScale(Scale scale) {
        this.scale = scale;
    }

    public void setChangeLog(ChangeLog value) {
        changeLog = value;
    }

    /**
     * Evaluates this Value Object and returns if everything is ok. Used in
     * proceed of EvaluateAction so that you can only proceed if every value in
     * a Leaf is completely evaluated.
     *
     * @return evaluation status of this Value Object (default = false)
     */
    public boolean isChanged() {
        return this.changeLog.isAltered();
    }

    public void touch() {
        this.changeLog.touch();
    }

    /**
     * @see ITouchable#handleChanges(IChangesHandler)
     */
    public void handleChanges(IChangesHandler h) {
        // let the changeshandler handle this instance
        h.visit(this);
    }
    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isEvaluated() {
        return scale.isEvaluated(this);
    }

    /**
     * Returns an object with the same values.
     * Important: Does not copy id and link to scale!
     * 
     *  Derived classes have to override this.
     */
    public Value clone() {
        try {
            Value clone = (Value)super.clone();
            clone.id = 0;
            clone.scale = null;
            // created-timestamp is automatically set to now
            clone.setChangeLog(new ChangeLog(this.getChangeLog().getChangedBy()));
            return clone;
        } catch (CloneNotSupportedException e) {
            // never thrown
            return null;
        }
    }
    
    
    public String getFormattedValue(){
        return "abstract value";
    }
    
    /**
     * parses a String value and sets the value appropriately.
     * At the moment there are no guarantees about error checking or behaviour, 
     * this needs to be used carefully or experimentally ;)
     * @param text String to parse
     */
    public abstract void parse(String text);

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
