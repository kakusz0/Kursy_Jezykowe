package pl.kakusz.database.managers;

import lombok.Getter;
import org.hibernate.SessionFactory;


@Getter
public class DatabaseManager {
    private final UserManager userManager;
    private final CourseManager courseManager;

    private static DatabaseManager instance;
    private final SessionFactory sessionFactory;


    public DatabaseManager() {
        this.sessionFactory = HibernateManager.getSessionFactory();
        this.userManager = new UserManager(this.sessionFactory);
        this.courseManager = new CourseManager(this.sessionFactory);
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
}
