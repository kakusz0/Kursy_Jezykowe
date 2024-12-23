package pl.kakusz.database.managers;

import lombok.Getter;
import lombok.Setter;
import pl.kakusz.database.objects.User;


@Getter
public class DatabaseManager {
    private final UserManager userManager;

    private static DatabaseManager instance;

    @Setter
    private User currentUser = null;

    public DatabaseManager() {
        this.userManager = new UserManager();

    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
}
