/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package firmware;

import firmware.control.QuadCopterController;

/**
 *
 * @author stoqn
 */
public class Main {

    public static void main(String[] args) {
        QuadCopterController quadController = new QuadCopterController();
        quadController.hover();
        quadController.calibrateHoverOnePercentUp();
        quadController.hover();
        quadController.stopAllEngines();
    }
}
