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
package eu.scape_project.planning.plato.wfview.full;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;

import eu.scape_project.planning.exception.PlanningException;
import eu.scape_project.planning.model.Alternative;
import eu.scape_project.planning.model.Plan;
import eu.scape_project.planning.model.PlanState;
import eu.scape_project.planning.model.PlatoException;
import eu.scape_project.planning.model.SampleObject;
import eu.scape_project.planning.plato.bean.IServiceLoader;
import eu.scape_project.planning.plato.bean.ServiceInfoDataModel;
import eu.scape_project.planning.plato.bean.TavernaServices;
import eu.scape_project.planning.plato.wf.AbstractWorkflowStep;
import eu.scape_project.planning.plato.wf.DefineAlternatives;
import eu.scape_project.planning.plato.wfview.AbstractView;
import eu.scape_project.planning.services.PlanningServiceException;
import eu.scape_project.planning.services.action.ActionInfoFactory;
import eu.scape_project.planning.services.action.IActionInfo;
import eu.scape_project.planning.services.pa.PreservationActionRegistryDefinition;
import eu.scape_project.planning.utils.FacesMessages;
import eu.scape_project.planning.validation.ValidationError;

/**
 * Class used as backing-bean for the view definealternatives.xhtml.
 * 
 * @author Markus Hamm
 */
@Named("defineAlternatives")
@ConversationScoped
public class DefineAlternativesView extends AbstractView {
    private static final long serialVersionUID = 1L;

    @Inject
    private Logger log;

    @Inject
    private FacesMessages facesMessages;

    @Inject
    private DefineAlternatives defineAlternatives;

    /**
     * Alternative that is currently being edited.
     */
    private IActionInfo editableAlternativeActionInfo;

    /**
     * Name of the editable alternative which cannot be set directly in the
     * editableAlternative because alternative renaming is a complex process.
     */
    @NotNull
    @Length(min = 1, max = 30)
    private String editableAlternativeName;

    /**
     * 
     */
    private Alternative editableAlternative;

    /**
     * Alternative that is newly created.
     */
    private Alternative customAlternative;

    /**
     * Cache for myExperiment service details.
     */
    @Inject
    private TavernaServices tavernaServices;

    /**
     * List of all currently defined preservation service registries.
     */
    private List<PreservationActionRegistryDefinition> availableRegistries;

    /**
     * List of actions of the currently selected registry.
     */
    private List<IActionInfo> availableActions;

    /**
     * Data model for services.
     */
    private ServiceInfoDataModel serviceInfoData;

    /**
     * Service identifiers and their loaders.
     */
    private Map<String, IServiceLoader> serviceLoaders;

    /**
     * Show custom alternatives.
     */
    private Boolean showCustomAlternatives = false;

    /**
     * Creates a new view object.
     */
    public DefineAlternativesView() {
        currentPlanState = PlanState.TREE_DEFINED;
        name = "Define Alternatives";
        viewUrl = "/plan/definealternatives.jsf";
        group = "menu.evaluateAlternatives";

        editableAlternative = null;

        availableRegistries = new ArrayList<PreservationActionRegistryDefinition>();
        availableActions = new ArrayList<IActionInfo>();
        serviceLoaders = new HashMap<String, IServiceLoader>();
    }

    /**
     * Initialises the view with plan data.
     * 
     * @param plan
     *            plan used to initialize
     */
    public void init(Plan plan) {
        super.init(plan);

        availableRegistries.clear();
        availableActions.clear();
        try {
            availableRegistries.addAll(defineAlternatives.getPreservationActionRegistries());
        } catch (PlanningServiceException e) {
            log.error("Failed to retrieve registries", e);
            facesMessages.addError("Could not find any preservation action registries.");
        }

        serviceLoaders.put("myExperiment", tavernaServices);
        tavernaServices.clear();

        showCustomAlternatives();
    }

    /**
     * Removes the provided alternative from the plan.
     * 
     * @param alternative
     *            alternative to delete
     */
    public void removeAlternative(Alternative alternative) {
        if (plan.isGivenAlternativeTheCurrentRecommendation(alternative)) {
            facesMessages.addInfo("You have removed the action which was chosen as the recommended alternative.");
        }

        plan.removeAlternative(alternative);

        if (alternative == editableAlternative) {
            editableAlternative = null;
        }
    }

    /**
     * Method responsible for preparing and showing the edit alternatives
     * window.
     * 
     * @param alternative
     *            the alternative to show
     */
    public void showEditAlternative(Alternative alternative) {
        editableAlternative = alternative;
        editableAlternativeName = alternative.getName();
        
        if (alternative.getAction() != null) {
            editableAlternativeActionInfo = ActionInfoFactory.createActionInfo(alternative.getAction());
            IServiceLoader serviceLoader = serviceLoaders.get(alternative.getAction().getActionIdentifier());
            if (serviceLoader != null) {
                serviceLoader.load(editableAlternativeActionInfo);
            }
        }
    }

    /**
     * Method responsible for add/edit a new/existing alternative.
     */
    public void editAlternative() {
        editableAlternative.touch();
        try {
            plan.renameAlternative(editableAlternative, editableAlternativeName);
        } catch (PlanningException e) {
            facesMessages.addError(e.getMessage());
            return;
        }
        editableAlternative = null;
    }

    /**
     * Method responsible for causing the add/edit-alternative window to close.
     */
    public void closeEditArea() {
        editableAlternative = null;
    }

    /**
     * Adds the predefine alternative "do nothing" to the plan.
     */
    public void addDoNothing() {
        try {
            defineAlternatives.addAlternative("Keep status quo", "Keep the objects as they are.");
        } catch (PlanningException e) {
            facesMessages.addError("Could not add the alternative: " + e.getMessage());
        }
    }

    /**
     * Adds the custom alternative to the plan.
     */
    public void addCustomAlternative() {
        try {
            defineAlternatives.addAlternative(customAlternative);
            customAlternative = Alternative.createAlternative();
        } catch (PlanningException e) {
            facesMessages.addError("Could not add the alternative: " + e.getMessage());
        }
    }

    /**
     * Determines if there is at least one sample with format info.
     * 
     * @return true if a sample with format info is available
     */
    public boolean isFormatInfoAvailable() {
        return plan.getSampleRecordsDefinition().getFirstSampleWithFormat() != null;
    }

    /**
     * Returns a sample with attached format info - at the moment this is the
     * first sample with format info found.
     * 
     * @return a sample object with format info
     */
    public SampleObject getSampleWithFormat() {
        return plan.getSampleRecordsDefinition().getFirstSampleWithFormat();
    }

    /**
     * Shows the custom alternative input.
     */
    public void showCustomAlternatives() {
        customAlternative = Alternative.createAlternative();
        showCustomAlternatives = true;
    }

    /**
     * Retrieves the list of services available in the given registry, for the
     * current sample with format info.
     * 
     * @param registry
     *            the registry to query
     */
    public void showPreservationServices(PreservationActionRegistryDefinition registry) {
        showCustomAlternatives = false;
        availableActions.clear();
        tavernaServices.clear();
        try {
            availableActions.addAll(defineAlternatives.queryRegistry(getSampleWithFormat().getFormatInfo(), registry));
            serviceInfoData = new ServiceInfoDataModel(availableActions, serviceLoaders);
        } catch (PlatoException e) {
            facesMessages.addError("Failed to query the registry: " + registry.getShortname() + " - " + e.getMessage());
            log.error("Failed to query the registry: " + registry.getShortname(), e);
        }
    }

    /**
     * Adds a preservation action to the plan, created from the provided action
     * info.
     * 
     * @param actionInfo
     *            the action info
     */
    public void addPreservationAction(IActionInfo actionInfo) {
        try {
            defineAlternatives.addAlternative(actionInfo);
        } catch (PlanningException e) {
            facesMessages.addError("Could not create an alternative from the service you selected.");
        }
    }

    @Override
    protected boolean tryProceed(List<ValidationError> errors) {
        // view-specific validation
        if (editableAlternative != null) {
            errors
                .add(new ValidationError(
                    "You are currently editing an Alternative. Please finish editing first before you proceed to the next step.", editableAlternative));
        }

        // general validation
        boolean result = defineAlternatives.proceed(errors);
        return result && errors.isEmpty();
    }

    // --------------- getter/setter ---------------

    @Override
    protected AbstractWorkflowStep getWfStep() {
        return defineAlternatives;
    }

    public FacesMessages getFacesMessages() {
        return facesMessages;
    }

    public void setFacesMessages(FacesMessages facesMessages) {
        this.facesMessages = facesMessages;
    }

    public Alternative getEditableAlternative() {
        return editableAlternative;
    }

    public void setEditableAlternative(Alternative editableAlternative) {
        this.editableAlternative = editableAlternative;
    }

    public String getEditableAlternativeName() {
        return editableAlternativeName;
    }

    public void setEditableAlternativeName(String editableAlternativeName) {
        this.editableAlternativeName = editableAlternativeName;
    }

    public IActionInfo getEditableAlternativeActionInfo() {
        return editableAlternativeActionInfo;
    }

    public Alternative getCustomAlternative() {
        return customAlternative;
    }

    public void setCustomAlternative(Alternative customAlternative) {
        this.customAlternative = customAlternative;
    }

    public Boolean getShowCustomAlternatives() {
        return showCustomAlternatives;
    }

    public List<PreservationActionRegistryDefinition> getAvailableRegistries() {
        return availableRegistries;
    }

    public List<IActionInfo> getAvailableActions() {
        return availableActions;
    }

    public ServiceInfoDataModel getServiceInfoData() {
        return serviceInfoData;
    }

    public TavernaServices getTavernaServices() {
        return tavernaServices;
    }

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }
}
