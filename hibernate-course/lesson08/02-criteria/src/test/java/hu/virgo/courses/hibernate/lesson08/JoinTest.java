package hu.virgo.courses.hibernate.lesson07;

import eu.drus.jpa.unit.api.JpaUnit;
import hu.virgo.courses.hibernate.lesson07.model.Department_;
import hu.virgo.courses.hibernate.lesson07.model.Employee;
import hu.virgo.courses.hibernate.lesson07.model.Employee_;
import hu.virgo.courses.hibernate.lesson07.model.Project;
import hu.virgo.courses.hibernate.lesson07.model.Project_;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import java.awt.print.Book;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(JpaUnit.class)
public class JoinTest {

	@PersistenceContext(unitName = "course_test")
	private EntityManager em;

	@Test
	public void joinTest() {
		Map<String, Object> filters = new HashMap<>();
//		filters.put("fName", "zga");
//		filters.put("fProjectName", "tás");


		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
		Root<Employee> emp = c.from(Employee.class);

		c.select(emp);
		c.distinct(true);
		Join<Employee, Project> project = emp.join(Employee_.projects, JoinType.LEFT);

		List<Predicate> criteria = new ArrayList<>();
		{
			Object filter;
			if ((filter = filters.get("fName")) != null) {
				criteria.add(cb.like(emp.get(Employee_.name), "%" + filter + "%"));
			}
		}
		{
			Object filter;
			if ((filter = filters.get("fLocation")) != null) {
				criteria.add(cb.equal(emp.get(Employee_.department).get(Department_.location), filter));
			}
		}
		{
			Object filter;
			if ((filter = filters.get("fProjectName")) != null) {
				criteria.add(cb.like(project.get(Project_.name), "%" + filter + "%"));
			}
		}

		if (criteria.size() == 1) {
			c.where(criteria.get(0));
		} else {
			c.where(cb.and(criteria.toArray(new Predicate[0])));
		}
		List<Employee> employees = em.createQuery(c).getResultList();
		System.out.println(employees);
	}

	@Test
	public void subQueryTest() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
		Root<Employee> root = cq.from(Employee.class);

		Subquery<Long> sub = cq.subquery(Long.class);
		Root<Project> subRoot = sub.from(Project.class);
		Join<Project, Employee> subAuthors = subRoot.join(Project_.employees);
		sub.select(cb.count(subRoot.get(Project_.id)));
		sub.where(cb.equal(root.get(Employee_.id), subAuthors.get(Employee_.id)));

		cq.where(cb.greaterThanOrEqualTo(sub, 1L));

		TypedQuery query = em.createQuery(cq);
		List<Employee> employees = query.getResultList();
		System.out.println(employees);
	}
}
