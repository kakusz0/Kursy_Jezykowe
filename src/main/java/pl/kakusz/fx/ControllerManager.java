package pl.kakusz.fx;

import lombok.Getter;
import pl.kakusz.fx.controllers.*;

@Getter
public class ControllerManager {

    private final LoginController loginController;
    private final DashBoardController dashBoardController;

    private final RegisterController registerController;

    private final ResetPasswordController resetPasswordController;
    private final PrivacyPolicyController privacyPolicyController;
    private static ControllerManager instance;

    public ControllerManager() {
        this.loginController = new LoginController();
        this.registerController = new RegisterController();
        this.resetPasswordController = new ResetPasswordController();
        this.dashBoardController = new DashBoardController();
        this.privacyPolicyController = new PrivacyPolicyController();
    }

    public static ControllerManager getInstance() {
        if (instance == null) {
            instance = new ControllerManager();
        }
        return instance;
    }



}
