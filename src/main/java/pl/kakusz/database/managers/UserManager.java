package pl.kakusz.database.managers;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;
import pl.kakusz.database.objects.User;

public class UserManager {

    private final SessionFactory sessionFactory;

    public UserManager() {
        this.sessionFactory = HibernateManager.getSessionFactory();
    }

    public void saveUser(User user) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    private boolean existsByField(String fieldName, String value) {
        try (Session session = sessionFactory.openSession()) {
            Long count = (Long) session.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u." + fieldName + " = :value")
                    .setParameter("value", value)
                    .uniqueResult();
            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean userExists(String username) {
        return existsByField("username", username);
    }

    public boolean emailExists(String email) {
        return existsByField("email", email);
    }

    private User getUserByField(String fieldName, String value) {
        try (Session session = sessionFactory.openSession()) {
            return (User) session.createQuery(
                            "FROM User u WHERE u." + fieldName + " = :value")
                    .setParameter("value", value)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public User getUserByUsername(String username) {
        return getUserByField("username", username);
    }

    public User getUserByEmail(String email) {
        return getUserByField("email", email);
    }

    public boolean updatePassword(String email, String oldPassword, String newPassword) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            User user = (User) session.createQuery(
                            "FROM User u WHERE u.email = :email")
                    .setParameter("email", email)
                    .uniqueResult();

            if (user != null && BCrypt.checkpw(oldPassword, user.getPassword())) {
                transaction = session.beginTransaction();
                user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
                session.update(user);
                transaction.commit();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }
}
