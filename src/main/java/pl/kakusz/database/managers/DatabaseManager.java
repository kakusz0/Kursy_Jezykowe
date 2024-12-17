package pl.kakusz.database.managers;

import lombok.Getter;


@Getter
public class DatabaseManager {
    private final UserManager userManager;

    private static DatabaseManager instance;

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
