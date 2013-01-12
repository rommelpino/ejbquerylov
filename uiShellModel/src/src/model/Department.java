package src.model;

import java.io.Serializable;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@NamedQueries({
  @NamedQuery(name = "findAllDepartments", query = "select o from Department o")
})
@Table(name = "DEPARTMENTS")
public class Department implements Serializable {
    @Id
    @Column(name="DEPARTMENT_ID", nullable = false)
    private Long departmentId;
    @Column(name="DEPARTMENT_NAME", nullable = false, length = 30)
    private String departmentName;
    @Column(name="LOCATION_ID")
    private Long locationId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "MANAGER_ID")
    private Employee manager;

    public Department() {
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }


    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee employee) {
        this.manager = employee;
    }
  @Override
  public String toString() {
      return this.departmentId.toString();
  }

}
