package services;

import utils.DatabaseManager;

public class ApplicationBootstrapService {

    public void initializeBackend() {
        DatabaseManager.initializeDatabase();
    }
}
