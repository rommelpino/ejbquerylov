package src.view.backing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import oracle.jbo.AttributeDef;
import oracle.jbo.uicli.binding.JUCtrlListBinding;

import soadev.component.PaginationComponent;

import soadev.view.utils.JSFUtils;

import src.model.Employee;

public class EmployeeListForm extends BaseForm {
  private static final String EMPLOYEE_DETAILS_TASK_FLOW = "/WEB-INF/flows/employee-details-task-flow.xml#employee-details-task-flow";

  public void view(ActionEvent actionEvent) {
      launchEmployeeDetails(false);
  }
  public void edit(ActionEvent actionEvent) {
   launchEmployeeDetails(true);
  }
  private void launchEmployeeDetails(boolean editMode){
    Employee employee = (Employee)getCurrentRowDataProvider("findEmployeesByCriteriaIterator");
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put("employeeId", employee.getEmployeeId());
    parameterMap.put(IDENTIFIER, employee.getEmployeeId());
    if (editMode){
      parameterMap.put(EDIT_MODE, editMode);
    }
    String title = "Employee: " + employee.getEmployeeId();
    launchActivity(title, EMPLOYEE_DETAILS_TASK_FLOW, parameterMap, true);
  } 
  
  public void test(ActionEvent e){
      Object object = JSFUtils.resolveExpression("#{bindings.Department}");
      JUCtrlListBinding control = (JUCtrlListBinding) object;
      Object selectedValue = control.getSelectedValue();
      System.out.println("getSelectedValue" + selectedValue);
      AttributeDef[] attrDefs = control.getAttributeDefs();
      for (AttributeDef attrDef: attrDefs){
          System.out.println(attrDef.getName());
      }
      String[] labels = control.getLabelSet();
      for (String s: labels){
          System.out.println(s);
      }
      String[] attributeNames = control.getAttributeNames();
      System.out.println("getAttributeNames");
      for (String s: attributeNames){
          System.out.println(s);
      }
      String[] displayAttrs= control.getListDisplayAttrNames();
      System.out.println("getListDisplayAttrNames");
      for (String s: displayAttrs){
          System.out.println(s);
      }
      List result = control.getDisplayData();
      System.out.println("getDisplayData");
      for (Object o: result){
          System.out.println(o);
      }
  }
}
