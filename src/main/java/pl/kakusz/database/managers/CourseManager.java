package pl.kakusz.database.managers;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import pl.kakusz.database.objects.Course;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class CourseManager {
    private final List<Course> courseList;
    private final SessionFactory sessionFactory;

    public CourseManager(SessionFactory sessionFactory) {
        this.courseList = new ArrayList<>();
        this.sessionFactory = sessionFactory;
        loadCourses();
    }

    public void loadCourses() {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            Query<Course> query = session.createQuery("from Course", Course.class);
            List<Course> courses = query.getResultList();
            courseList.addAll(courses);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }


}
