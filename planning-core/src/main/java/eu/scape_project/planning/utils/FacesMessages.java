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
 ******************************************************************************/
package eu.scape_project.planning.utils;

import java.io.Serializable;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

/**
 * Helper class to simplify the output of FacesMessages It results in the
 * creation of an additional object, but at the same time it allows us to unit
 * test backing beans.
 * 
 * @author Michael Kraxner, Markus Hamm
 */
public class FacesMessages implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5804425794122264314L;

    /**
     * Add Faces Info-Message.
     * 
     * @param message
     *            Message to publish.
     */
    public void addInfo(final String message) {
        addMessage(FacesMessage.SEVERITY_INFO, null, message, "");
    }
    
    /**
     * Add Faces Info-Message for a specific component.
     * 
     * @param componentId
     *            Id of the component this message belongs to. (Component id is
     *            the id used in xhtml code (e.g. <h:form id="myId">))
     * @param message
     *            Message to publish.
     */
    public void addInfo(final String componentId, final String message) {
        addMessage(FacesMessage.SEVERITY_INFO, componentId, message, "");
    }

    public void addInfo(final String componentId, final String message, final String detailedInfo) {
        addMessage(FacesMessage.SEVERITY_INFO, componentId, message, detailedInfo);
    }
    
    private void addMessage(final FacesMessage.Severity severity, final String componentId, final String message, final String detailedInfo) {
        String clientId = null;
        if (componentId != null) {
            resolveClientId(componentId);
        }
        FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(severity, message, detailedInfo));
    }

    /**
     * Add Faces Error-Message.
     * 
     * @param message
     *            Message to publish.
     */
    public void addError(final String message) {
        addMessage(FacesMessage.SEVERITY_ERROR, null, message, "");
    }

    /**
     * Add Faces Error-Message for a specific component.
     * 
     * @param componentId
     *            Id of the component this message belongs to. (Component id is
     *            the id used in xhtml code (e.g. <h:form id="myId">))
     * @param message
     *            Message to publish.
     */
    public void addError(final String componentId, final String message) {
        addMessage(FacesMessage.SEVERITY_ERROR, componentId, message, "");
    }

    /**
     * Add Faces Error-Message.
     * 
     * @param message
     *            Message to publish.
     */
    public void addWarning(final String message) {
        addMessage(FacesMessage.SEVERITY_WARN, null, message, "");
    }

    /**
     * Add Faces Warning-Message for a specific component.
     * 
     * @param componentId
     *            Id of the component this message belongs to. (Component id is
     *            the id used in xhtml code (e.g. <h:form id="myId">))
     * @param message
     *            Message to publish.
     */
    public void addWarning(final String componentId, final String message) {
        addMessage(FacesMessage.SEVERITY_WARN, componentId, message, "");
    }

    /**
     * Returns the clientId for a given component id. Component id is the id
     * used in xhtml code (e.g. <h:form id="myId">) Client id is the id used at
     * rendering the page to the browser. It is also used internally by JSF for
     * uniquely identifying elements.
     * 
     * @param componentId
     *            Component id.
     * @return Client id.
     */
    private String resolveClientId(final String componentId) {
        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot root = context.getViewRoot();

        UIComponent c = findComponent(root, componentId);
        return c.getClientId(context);
    }

    /**
     * Finds a component with the given id.
     * 
     * @param c
     *            Search root.
     * @param id
     *            Id to search for.
     * @return Wanted component is returned if found. Otherwise null is
     *         returned.
     */
    private UIComponent findComponent(final UIComponent c, final String id) {
        if (id.equals(c.getId())) {
            return c;
        }

        Iterator<UIComponent> kids = c.getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent found = findComponent(kids.next(), id);
            if (found != null) {
                return found;
            }
        }

        return null;
    }
}
