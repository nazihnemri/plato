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
/**
 * 
 */
package eu.scape_project.planning.plato.wf;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;

import eu.scape_project.planning.exception.PlanningException;
import eu.scape_project.planning.manager.ByteStreamManager;
import eu.scape_project.planning.model.Plan;
import eu.scape_project.planning.model.PlanState;
import eu.scape_project.planning.model.tree.Leaf;
import eu.scape_project.planning.validation.ValidationError;

import org.slf4j.Logger;

import pt.gov.dgarq.roda.core.PlanClient;

/**
 * @author Michael Kraxner
 * 
 */
@Stateful
@ConversationScoped
public class ValidatePlan extends AbstractWorkflowStep {
    private static final long serialVersionUID = 7862746302624511130L;

    @Inject
    private Logger log;

    @Inject
    private ByteStreamManager byteStreamManager;

    public ValidatePlan() {
        requiredPlanState = PlanState.PLAN_DEFINED;
        correspondingPlanState = PlanState.PLAN_VALIDATED;
    }

    public void init(Plan p) {
        super.init(p);
        for (Leaf l : plan.getTree().getRoot().getAllLeaves()) {
            l.initTransformer();
        }
    }

    /**
     * Method responsible for approving the current plan.
     */
    public void approvePlan() {
        List<ValidationError> validationErrors = new ArrayList<ValidationError>();
        // proceed planstate to PLAN_VALIDATED
        boolean success = proceed(validationErrors);

        if (success) {
            log.info("Approved plan with id " + plan.getId());
        } else {
            log.warn("Approvement of plan with id " + plan.getId() + " failed");
        }
    }

    /**
     * Method responsible for revising the current approved plan.
     */
    public void revisePlan() {
        // save does the reset of planstate to PLAN_DEFINED for us
        save();
        log.info("Revised plan with id " + plan.getId());
    }

    @Override
    protected void saveStepSpecific() {
        // no custom save operation is needed here
    }

    public void uploadPlanToRODA(String url, String username, String password) throws PlanningException {

        url = url + "/roda-core/";
        PlanClient planClient;
        try {
            planClient = new PlanClient(new URL(url), username, password);
        } catch (MalformedURLException e) {
            log.error("Error creating PlanClient URL '" + url, e);
            throw new PlanningException("Error creating PlanClient URL " + url, e);
        }

        if (!plan.getPreservationActionPlan().isDataExistent()) {
            throw new PlanningException("No Preservation Action Plan found.");
        }

        File planFile = byteStreamManager.getTempFile(plan.getPreservationActionPlan().getPid());
        if (!planFile.canRead()) {
            throw new PlanningException("Preservation Action Plan could not be processed.");
        }

        try {
            planClient.uploadPlan(planFile);
            log.info("Deployed plan {} to {}.", plan.getPlanProperties().getId(), url);
        } catch (RuntimeException e) {
            throw new PlanningException("Error deploying plan.", e);
        }

    }
}
