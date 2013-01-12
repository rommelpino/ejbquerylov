package src.service;

import java.util.List;

import java.util.Map;

import javax.ejb.Local;

import src.model.Department;
import src.model.Employee;
import src.model.Job;

@Local
public interface HRFacadeLocal {
    Object queryByRange(String jpqlStmt, List<Object[]> hints, int firstResult, int maxResults);

    Job findJobById(String jobId);

    void removeJob(Job job);

    List<Job> findAllJobs();

    Job persistJob(Job job);

    Job mergeJob(Job job);

    List<Employee> findEmployeesByCriteria(String jpqlStmt, int firstResult,
                                           int maxResults);
    List<Employee> findEmployeesByCriteria(String jpqlStmt, List<Object[]> hints, int firstResult,
                                                  int maxResults);

    Employee findEmployeeById(Long employeeId);

    Employee mergeEmployee(Employee employee);

    List<Job> findJobsByCriteria(String jpqlStmt, int firstResult,
                                 int maxResults);

    List<Department> findDepartmentsByCriteria(String jpqlStmt,
                                               int firstResult,
                                               int maxResults);

    Object findById(Class clazz, Object id);

    Long getResultCount(String jpqlStmt);
}
