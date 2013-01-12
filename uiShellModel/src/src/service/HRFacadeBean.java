package src.service;

import com.sun.jndi.ldap.EntryChangeResponseControl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import static src.constants.Constants.*;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import src.model.Department;
import src.model.Employee;
import src.model.Job;
@Stateless(name = "HRFacade", mappedName = "UIShell-uiShellModel-HRFacade")
@Remote
@Local
public class HRFacadeBean implements HRFacade, HRFacadeLocal {
    @PersistenceContext(unitName="uiShellModel")
    private EntityManager em;
    
    public HRFacadeBean() {
    }

    public Object queryByRange(String jpqlStmt, List<Object[]> hints, int firstResult,
                               int maxResults) {
        if(jpqlStmt == null){
            return Collections.emptyList();
        }
        Query query = em.createQuery(jpqlStmt);
        if(hints != null && !hints.isEmpty()){
            for(Object[] hint : hints){
                query.setHint((String)hint[0], hint[1]);
            }
        }
        if (firstResult > 0) {
            query = query.setFirstResult(firstResult);
        }
        if (maxResults > 0) {
            query = query.setMaxResults(maxResults);
        }
        return query.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Long getResultCount(String jpqlStmt){
        if(jpqlStmt == null){
            return 0L;
        }
        jpqlStmt = jpqlStmt.replaceFirst("SELECT o FROM", "SELECT COUNT(o) FROM");
        Query query = em.createQuery(jpqlStmt);
        Long result = (Long)query.getSingleResult();
        return result;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Employee> findEmployeesByCriteria(String jpqlStmt, int firstResult,
                                                  int maxResults){
        return (List<Employee>)queryByRange(jpqlStmt, null, firstResult, maxResults);
                                                    
    }
    
    public List<Employee> findEmployeesByCriteria(String jpqlStmt, List<Object[]> hints, int firstResult,
                                                  int maxResults){
        return (List<Employee>)queryByRange(jpqlStmt, hints, firstResult, maxResults);
                                                    
    }
    
  public List<Department> findDepartmentsByCriteria(String jpqlStmt, int firstResult,
                                                int maxResults){
      return (List<Department>)queryByRange(jpqlStmt, null, firstResult, maxResults);
                                                  
  }
  public List<Job> findJobsByCriteria(String jpqlStmt, int firstResult,
                                                int maxResults){
      return (List<Job>)queryByRange(jpqlStmt, null, firstResult, maxResults);
                                                  
  }
  
  public Object findById(Class clazz, Object id){
      return em.find(clazz, id);
  }
    
    public Employee findEmployeeById(Long employeeId){
      if (employeeId == null){
          throw new IllegalArgumentException();
      }
      if (LONG_EMPTY_OBJECT_ID.equals(employeeId)){
          return new Employee();
      }
      return em.find(Employee.class, employeeId);
    }
    
    

    public Job findJobById(String jobId) {
        if (jobId == null){
            throw new IllegalArgumentException();
        }
        if (STRING_EMPTY_OBJECT_ID.equals(jobId)){
            return new Job();
        }
        return em.find(Job.class, jobId);
    }

    public void removeJob(Job job) {
        job = em.find(Job.class, job.getJobId());
        em.remove(job);
    }

    /** <code>select o from Job o</code> */
    public List<Job> findAllJobs() {
        return em.createNamedQuery("findAllJobs").getResultList();
    }

    public Job persistJob(Job job) {
        em.persist(job);
        return job;
    }

    public Job mergeJob(Job job) {
        return em.merge(job);
    }

    public Employee mergeEmployee(Employee employee) {
        return em.merge(employee);
    }
}
