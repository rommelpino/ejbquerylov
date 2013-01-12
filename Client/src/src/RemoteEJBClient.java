package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import java.util.List;

import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.persistence.QueryHint;

import org.eclipse.persistence.config.QueryHints;

import org.eclipse.persistence.queries.FetchGroup;

import src.model.Employee;

import src.service.HRFacade;

public class RemoteEJBClient {
    private HRFacade service;
    public RemoteEJBClient() {
        try {
            final Context context = getInitialContext();
            service =
                    (HRFacade)context.lookup("UIShell-uiShellModel-HRFacade#src.service.HRFacade");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
        

    private static Context getInitialContext() throws NamingException {
        Hashtable env = new Hashtable();
        // WebLogic Server 10.x connection details
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "weblogic.jndi.WLInitialContextFactory");
        env.put(Context.PROVIDER_URL, "t3://localhost:7101");
        return new InitialContext(env);
    }
    
    
    public static void main(String[] args) {
        RemoteEJBClient client = new RemoteEJBClient();
        client.testJoinFetchStatments();
    }
    public void testJoinFetchStatments(){
        String jpql = "Select o  from Employee o";
        List<Object[]> hints = new ArrayList<Object[]>();
        hints.add(new Object []{QueryHints.LEFT_FETCH, "o.manager"});
        hints.add(new Object[]{QueryHints.LEFT_FETCH, "o.department"});
        FetchGroup group = new FetchGroup();
        group.addAttribute("firstName");
        group.addAttribute("lastName");
        group.addAttribute("manager.email");
        group.addAttribute("department.departmentName");
        hints.add(new Object[]{QueryHints.FETCH_GROUP, group});
        List<Employee> resultList = service.queryByRange(jpql, hints, 0, 0);
        for (Employee emp: resultList){
            System.out.println(emp.getEmployeeId());
            System.out.println(emp.getLastName());
            if (emp.getManager()!= null){
                System.out.println(">>>>>>>>>>>>" + emp.getManager().getEmail());
            }
            if (emp.getDepartment() != null){
                System.out.println(emp.getDepartment().getDepartmentName());
            }
            System.out.println(emp.getFirstName());
        }
    }

}
