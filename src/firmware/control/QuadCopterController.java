/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package firmware.control;

import firmware.control.maneuvers.*;

/**
 *
 * @author stoqn
 */
public class QuadCopterController {

    private double hoverPercentage = 30;

    // all of the following are in percentage
    private static final double MIN_BOUND_OFFSET_FROM_HOVER = 10;

    // these two should sum up to 2
    private static final double MAX_RANGE_FACTOR_BETWEEN_MIN_HOVER_THROTTLE_DURING_MANEUVER = 0.5;
    private static final double MAX_RANGE_FACTOR_BETWEEN_MAX_HOVER_THROTTLE_DURING_MANEUVER = 1.5;

    private static final double MAX_RANGE_OFFSET_BETWEEN_MIN_MAX_THROTTLE_DURING_MANEUVER = 15;

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
        QuadCopter.sharedInstance().setAllEnginesOnThrottle(hoverPercentage);
    }

    public void calibrateHoverOnePercentUp() {
        this.hoverPercentage++;
        System.out.printf("Hover percentage changed to: %d\n", this.hoverPercentage);
    }

    public void calibrateHoverOnePercentDown() {
        this.hoverPercentage--;
        System.out.printf("Hover percentage changed to: %d\n", this.hoverPercentage);
    }

    //**************************************************************************
    // Private Methods
    //**************************************************************************
    private void sendQuadUpWithOffsetValue(byte offsetValue) {
        this.hover();
        double throttlePercentageInHover = QuadCopter.sharedInstance().getBiggestThrottleValue();
        double minPercentageBoundDuringDescent = throttlePercentageInHover - QuadCopterController.MIN_BOUND_OFFSET_FROM_HOVER;
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
        QuadCopter.sharedInstance().setAllEnginesOnThrottle(throttlePercentageInHover + offsetValue);
    }

    private void sendQuadLeftWithValue(byte offsetValue) {
        this.hover();
        double throttleValueInHover = QuadCopter.sharedInstance().getBiggestThrottleValue();

        double divisor = Byte.MAX_VALUE / (100 - throttleValueInHover);
        double offset = Math.abs(offsetValue / divisor);

        double minThrottleValue = throttleValueInHover - QuadCopterController.MAX_RANGE_FACTOR_BETWEEN_MIN_HOVER_THROTTLE_DURING_MANEUVER * offset;
        double maxThrottleValue = throttleValueInHover + QuadCopterController.MAX_RANGE_FACTOR_BETWEEN_MAX_HOVER_THROTTLE_DURING_MANEUVER * offset;

        while (maxThrottleValue - minThrottleValue > QuadCopterController.MAX_RANGE_OFFSET_BETWEEN_MIN_MAX_THROTTLE_DURING_MANEUVER) {
            minThrottleValue += 0.5;
            maxThrottleValue -= 0.5;
        }

        if (offsetValue > 0) {
            QuadCopter.sharedInstance().setEngineFrontLeftThrottle(minThrottleValue);
            QuadCopter.sharedInstance().setEngineRearLeftThrottle(minThrottleValue);
            QuadCopter.sharedInstance().setEngineFrontRightThrottle(maxThrottleValue);
            QuadCopter.sharedInstance().setEngineRearRightThrottle(maxThrottleValue);
        } else if (offsetValue < 0) {
            QuadCopter.sharedInstance().setEngineFrontLeftThrottle(maxThrottleValue);
            QuadCopter.sharedInstance().setEngineRearLeftThrottle(maxThrottleValue);
            QuadCopter.sharedInstance().setEngineFrontRightThrottle(minThrottleValue);
            QuadCopter.sharedInstance().setEngineRearRightThrottle(minThrottleValue);
        } else {
            this.hover();
        }
    }

    private void sendQuadForwardWithValue(byte value) {
    }

    private void rotateQuadClockwiseWithValue(byte value) {
    }

}
