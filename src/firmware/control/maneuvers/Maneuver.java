/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package firmware.control.maneuvers;

/**
 *
 * @author stoqn
 */
public class Maneuver {

    public ManeuverDirection direction;
    public byte value;

    public Maneuver(ManeuverDirection direction, byte value) {
        this.direction = direction;
        this.value = value;
    }
}
