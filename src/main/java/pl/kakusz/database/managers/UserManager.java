package pl.kakusz.database.managers;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.mindrot.jbcrypt.BCrypt;
import pl.kakusz.database.objects.User;

public class UserManager {
    private final SessionFactory sessionFactory;
    @Setter
    @Getter
    private User currentUser = null;

    public UserManager(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
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

    public void updateUserBalance(User user) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            // Aktualizacja danych użytkownika
            session.update(user);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Nie udało się zaktualizować balansu użytkownika", e);
        }
    }

    public User getCurrentUserWithCourses(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT u FROM User u LEFT JOIN FETCH u.courses WHERE u.id = :userId";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("userId", userId);
            return query.uniqueResult(); // Zwraca użytkownika z kolekcją courses załadowaną
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
