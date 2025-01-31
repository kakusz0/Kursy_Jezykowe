package pl.kakusz.database.managers;

import lombok.Getter;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import pl.kakusz.database.objects.Course;
import pl.kakusz.database.objects.User;

public class HibernateManager {

    @Getter
    private static final SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new Configuration().configure("hibernate.cfg.xml")
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(Course.class)

                    .buildSessionFactory();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("Problem podczas tworzenia fabryki sesji: " + e.getMessage());
        }
    }

    public static void shutdown() {
        getSessionFactory().close();
        System.out.println("session closed");
    }
}
