package services;

import services.AuthService;
import utils.DatabaseManager;

public class ApplicationBootstrapService {

    private final AuthService authService = new AuthService();

    public void initializeBackend() {
        DatabaseManager.initializeDatabase();
        authService.seedAdminIfMissing();
    }
}
