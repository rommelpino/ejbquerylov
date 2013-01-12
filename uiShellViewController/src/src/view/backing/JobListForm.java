package src.view.backing;

import java.util.HashMap;
import java.util.Map;

import javax.faces.event.ActionEvent;
import static src.constants.Constants.STRING_EMPTY_OBJECT_ID;
import src.model.Job;


public class JobListForm extends BaseForm {

    private static final String JOB_DETAILS_TASK_FLOW = "/WEB-INF/flows/job-details-task-flow.xml#job-details-task-flow";

    public void view(ActionEvent actionEvent) {
        launchJobDetails(false);
    }
    
     public void edit(ActionEvent actionEvent) {
      launchJobDetails(true);
    }

    public void create(ActionEvent event) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("jobId", STRING_EMPTY_OBJECT_ID);
        String title = "Job: *";
        launchActivity(title, JOB_DETAILS_TASK_FLOW, parameterMap, true);
    }
    
    private void launchJobDetails(boolean editMode){
      Job job = (Job)getCurrentRowDataProvider("findAllJobsIterator");
      Map<String, Object> parameterMap = new HashMap<String, Object>();
      parameterMap.put("jobId", job.getJobId());
      parameterMap.put(IDENTIFIER, job.getJobId());
      if (editMode){
        parameterMap.put(EDIT_MODE, editMode);
      }
      String title = "Job: " + job.getJobId();
      launchActivity(title, JOB_DETAILS_TASK_FLOW, parameterMap, true);
    }  
}
