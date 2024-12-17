package pl.kakusz.fx;

import lombok.Getter;
import pl.kakusz.fx.controllers.DashBoardController;
import pl.kakusz.fx.controllers.LoginController;
import pl.kakusz.fx.controllers.RegisterController;
import pl.kakusz.fx.controllers.ResetPasswordController;

@Getter
public class ControllerManager {

    private final LoginController loginController;
    private final DashBoardController dashBoardController;

    private final RegisterController registerController;

    private final ResetPasswordController resetPasswordController;
    private static ControllerManager instance;

    public ControllerManager() {
        this.loginController = new LoginController();
        this.registerController = new RegisterController();
        this.resetPasswordController = new ResetPasswordController();
        this.dashBoardController = new DashBoardController();
    }

    public static ControllerManager getInstance() {
        if (instance == null) {
            instance = new ControllerManager();
        }
        return instance;
    }



}
