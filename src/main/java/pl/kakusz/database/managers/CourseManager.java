package pl.kakusz.database.managers;

import javafx.scene.control.Alert;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import pl.kakusz.database.objects.Course;
import pl.kakusz.database.objects.User;
import pl.kakusz.fx.AlertHelper;
import pl.kakusz.fx.controllers.DashBoardController;

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

    public void deleteCourse(String courseName) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            Course course = getCourseByName(courseName);
            session.delete(course);
            courseList.remove(course);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się, sproboj ponownie " + e.getMessage());

        } finally {
            session.close();
        }
    }
    public Course getCourseByName(String courseName) {
        try (Session session = sessionFactory.openSession()) {
            String fieldName = "name";
            return (Course) session.createQuery(
                            "FROM Course u WHERE u." + fieldName + " = :value")
                    .setParameter("value", courseName)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się, sproboj ponownie " + e.getMessage());

            return null;
        }
    }


//    public Course getCourseByName(String courseName) {
//        Session session = sessionFactory.openSession();
//        Course course = null;
//
//        try {
//            course = session.get(Course.class, courseName);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            session.close();
//        }
//        return course;
//    }
    public Course getCourseById(Long courseId) {
        Session session = sessionFactory.openSession();
        Course course = null;

        try {
            course = session.get(Course.class, courseId);
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się, sproboj ponownie " + e.getMessage());

        } finally {
            session.close();
        }
        return course;
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
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się, sproboj ponownie " + e.getMessage());

        } finally {
            session.close();
        }
    }

    public List<Course> loadCoursesList() {
        List<Course> courses = new ArrayList<>();
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            Query<Course> query = session.createQuery("from Course", Course.class);
            courses = query.getResultList();
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się, sproboj ponownie " + e.getMessage());
            e.printStackTrace();
        } finally {
            session.close();
        }
        return courses;
    }


    public void addCourse(Course course) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            session.save(course);
            courseList.add(course);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się, sproboj ponownie " + e.getMessage());

        } finally {
            session.close();
        }
    }
}
