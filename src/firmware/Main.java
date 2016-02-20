/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package firmware;

import firmware.control.QuadCopterController;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author stoqn
 */
public class Main {

    public static void main(String[] args) {
        try {
            QuadCopterController quadController = new QuadCopterController();
            quadController.hover();
            Thread.sleep(1000);
            quadController.calibrateHoverOnePercentUp();
            quadController.hover();
            Thread.sleep(1000);
            quadController.calibrateHoverOnePercentDown();
            quadController.hover();
            Thread.sleep(1000);
            quadController.stopAllEngines();
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
