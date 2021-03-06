/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package firmware.control;

import firmware.control.maneuvers.*;
import firmware.configuration.ConfigurationManager;
import firmware.configuration.Configuration;

/**
 *
 * @author stoqn
 */
public class QuadCopterController {

    private final QuadCopter quad;
    
    public QuadCopterController () {
        this.quad = new QuadCopter();
    }
    
    //**************************************************************************
    // Public interface
    //**************************************************************************
    public void performManeuver(Maneuver maneuver) {

        switch (maneuver.direction) {

            case DIRECTION_GO_UP_DOWN: {
                this.sendQuadUpWithOffsetValue(maneuver.value);
            }
            case DIRECTION_GO_LEFT_RIGHT: {
                this.sendQuadLeftWithValue(maneuver.value);
            }
            case DIRECTION_GO_FORWARD_BACKWARD: {
                this.sendQuadForwardWithValue(maneuver.value);
            }
            case DIRECTION_ROTATE_CLOCKWISE_COUNTERCLOCKWISE: {
                this.rotateQuadClockwiseWithValue(maneuver.value);
            }
        }
    }

    public void hover() {
        Configuration storedConfig = ConfigurationManager.sharedInstance().getStoredConfiguration();
        this.quad.setAllEnginesOnThrottle(storedConfig.hoverThrottlePercentage);
    }

    public void calibrateHoverOnePercentUp() {
        Configuration storedConfig = ConfigurationManager.sharedInstance().getStoredConfiguration();
        storedConfig.hoverThrottlePercentage++;
        System.out.printf("[WARNING]Hover percentage changed to: %.2f\n", storedConfig.hoverThrottlePercentage);
        ConfigurationManager.sharedInstance().storeConfiguration(storedConfig);
    }

    public void calibrateHoverOnePercentDown() {
        Configuration storedConfig = ConfigurationManager.sharedInstance().getStoredConfiguration();
        storedConfig.hoverThrottlePercentage--;
        System.out.printf("[WARNING]Hover percentage changed to: %.2f\n", storedConfig.hoverThrottlePercentage);
        ConfigurationManager.sharedInstance().storeConfiguration(storedConfig);
    }

    public void stopAllEngines() {
        System.out.println("[WARNING]Stopping all engines");
        this.quad.setAllEnginesOnThrottle(0);
    }

    //**************************************************************************
    // Private Methods
    //**************************************************************************
    // Quad Maneuvers ----------------------------------------------------------

    private void sendQuadUpWithOffsetValue(byte offsetValue) {
        this.hover();
        double throttlePercentageInHover = this.quad.getBiggestThrottleValue();
        double minPercentageBoundDuringDescent = throttlePercentageInHover - ConfigurationManager.MIN_BOUND_OFFSET_FROM_HOVER;
        double throttlePercentageRangeFromHoverToMax = 100 - throttlePercentageInHover;
        double throttlePercentageRangeFromMinBoundToHover = throttlePercentageInHover - minPercentageBoundDuringDescent;

        // i am using different divisors because i want the rate of the ascent
        // to be different than the rate of descent
        // if you want to change the descent rate change
        // the constant - QuadCopterController.MIN_BOUND_OFFSET_FROM_HOVER
        double divisor;

        // gaining height
        if (offsetValue > 0) {
            divisor = Byte.MAX_VALUE / throttlePercentageRangeFromHoverToMax;
        } // loosing height
        else if (offsetValue < 0) {
            divisor = Byte.MAX_VALUE / throttlePercentageRangeFromMinBoundToHover;
        } // staying idle
        else {
            this.hover();
            return;
        }

        offsetValue /= divisor;
        this.quad.setAllEnginesOnThrottle(throttlePercentageInHover + offsetValue);
    }

    private void sendQuadLeftWithValue(byte offsetValue) {
        this.hover();
        double throttleValueInHover = this.quad.getBiggestThrottleValue();

        double divisor = Byte.MAX_VALUE / (100 - throttleValueInHover);
        double offset = Math.abs(offsetValue / divisor);

        double minThrottleValue = throttleValueInHover - ConfigurationManager.MAX_RANGE_FACTOR_BETWEEN_MIN_HOVER_THROTTLE_DURING_MANEUVER * offset;
        double maxThrottleValue = throttleValueInHover + ConfigurationManager.MAX_RANGE_FACTOR_BETWEEN_MAX_HOVER_THROTTLE_DURING_MANEUVER * offset;

        while (maxThrottleValue - minThrottleValue > ConfigurationManager.MAX_RANGE_OFFSET_BETWEEN_MIN_MAX_THROTTLE_DURING_MANEUVER) {
            minThrottleValue += 0.5;
            maxThrottleValue -= 0.5;
        }

        if (offsetValue > 0) {
            this.quad.setEngineFrontLeftThrottle(minThrottleValue);
            this.quad.setEngineRearLeftThrottle(minThrottleValue);
            this.quad.setEngineFrontRightThrottle(maxThrottleValue);
            this.quad.setEngineRearRightThrottle(maxThrottleValue);
        } else if (offsetValue < 0) {
            this.quad.setEngineFrontLeftThrottle(maxThrottleValue);
            this.quad.setEngineRearLeftThrottle(maxThrottleValue);
            this.quad.setEngineFrontRightThrottle(minThrottleValue);
            this.quad.setEngineRearRightThrottle(minThrottleValue);
        } else {
            this.hover();
        }
    }

    private void sendQuadForwardWithValue(byte offsetValue) {
        this.hover();
        double throttleValueInHover = this.quad.getBiggestThrottleValue();

        double divisor = Byte.MAX_VALUE / (100 - throttleValueInHover);
        double offset = Math.abs(offsetValue / divisor);

        double minThrottleValue = throttleValueInHover - ConfigurationManager.MAX_RANGE_FACTOR_BETWEEN_MIN_HOVER_THROTTLE_DURING_MANEUVER * offset;
        double maxThrottleValue = throttleValueInHover + ConfigurationManager.MAX_RANGE_FACTOR_BETWEEN_MAX_HOVER_THROTTLE_DURING_MANEUVER * offset;

        while (maxThrottleValue - minThrottleValue > ConfigurationManager.MAX_RANGE_OFFSET_BETWEEN_MIN_MAX_THROTTLE_DURING_MANEUVER) {
            minThrottleValue += 0.5;
            maxThrottleValue -= 0.5;
        }

        if (offsetValue > 0) {
            this.quad.setEngineFrontLeftThrottle(minThrottleValue);
            this.quad.setEngineFrontRightThrottle(minThrottleValue);
            this.quad.setEngineRearLeftThrottle(maxThrottleValue);
            this.quad.setEngineRearRightThrottle(maxThrottleValue);
        } else if (offsetValue < 0) {
            this.quad.setEngineFrontLeftThrottle(maxThrottleValue);
            this.quad.setEngineFrontRightThrottle(maxThrottleValue);
            this.quad.setEngineRearLeftThrottle(minThrottleValue);
            this.quad.setEngineRearRightThrottle(minThrottleValue);
        } else {
            this.hover();
        }
    }

    private void rotateQuadClockwiseWithValue(byte offsetValue) {
        this.hover();
        double throttleValueInHover = this.quad.getBiggestThrottleValue();

        double divisor = Byte.MAX_VALUE / (100 - throttleValueInHover);
        double offset = Math.abs(offsetValue / divisor);

        double minThrottleValue = throttleValueInHover - ConfigurationManager.MAX_RANGE_FACTOR_BETWEEN_MIN_HOVER_THROTTLE_DURING_MANEUVER * offset;
        double maxThrottleValue = throttleValueInHover + ConfigurationManager.MAX_RANGE_FACTOR_BETWEEN_MAX_HOVER_THROTTLE_DURING_MANEUVER * offset;

        while (maxThrottleValue - minThrottleValue > ConfigurationManager.MAX_RANGE_OFFSET_BETWEEN_MIN_MAX_THROTTLE_DURING_MANEUVER) {
            minThrottleValue += 0.5;
            maxThrottleValue -= 0.5;
        }

        if (offsetValue > 0) {
            this.quad.setEngineFrontLeftThrottle(minThrottleValue);
            this.quad.setEngineRearRightThrottle(minThrottleValue);
            this.quad.setEngineRearLeftThrottle(maxThrottleValue);
            this.quad.setEngineFrontRightThrottle(maxThrottleValue);
        } else if (offsetValue < 0) {
            this.quad.setEngineFrontLeftThrottle(maxThrottleValue);
            this.quad.setEngineRearRightThrottle(maxThrottleValue);
            this.quad.setEngineRearLeftThrottle(minThrottleValue);
            this.quad.setEngineFrontRightThrottle(minThrottleValue);
        } else {
            this.hover();
        }
    }
}
