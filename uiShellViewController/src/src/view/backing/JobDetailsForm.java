package src.view.backing;

import javax.faces.event.ActionEvent;

import oracle.binding.OperationBinding;

import static src.constants.Constants.STRING_EMPTY_OBJECT_ID;

import src.model.Job;

public class JobDetailsForm extends BaseForm {

    private static final String JOB_LIST_TASK_FLOW =
        "/WEB-INF/flows/job-list-task-flow.xml#job-list-task-flow";

    public void cancel(ActionEvent actionEvent) {
        setCurrentTabClean();
        closeCurrentTab();
        //tries to activate the Overview tab if present
        activateTab(JOB_LIST_TASK_FLOW);
    }

    public void saveAndClose(ActionEvent actionEvent) {
        save();
        setCurrentTabClean();
        closeCurrentTab();
        activateTab(JOB_LIST_TASK_FLOW);
    }

    private void save() {
        OperationBinding oper = null;
        if (isNewJob()) {
            oper = getOperationBinding("persistJob");
        } else {
            oper = getOperationBinding("mergeJob");
        }
        oper.execute();
    }

    private boolean isNewJob() {
        return STRING_EMPTY_OBJECT_ID.equals(getPageFlowScope().get("jobId"));
    }

    public void initialize() {
        Job job =
            (Job)getBindings().getOperationBinding("findJobById").execute();
        //do some initialization
        job.setJobTitle("Default Job Title");
        //ensures that form is displayed in edit-mode
        getPageFlowScope().put(EDIT_MODE, true);
    }
}
