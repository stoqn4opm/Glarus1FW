/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package firmware.control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

enum Engines {

    ENGINE_FRONT_LEFT(17, "F_LEFT"),
    ENGINE_FRONT_RIGHT(18, "F_RIGHT"),
    ENGINE_REAR_LEFT(21, "R_LEFT"),
    ENGINE_REAR_RIGHT(22, "R_RIGHT");

    public int piGpioPin;
    public String name;

    Engines(int pin, String name) {
        this.piGpioPin = pin;
        this.name = name;
    }
}

/**
 *
 * @author stoqn
 */
public class QuadCopter {

    //private static final String PI_BLASTER_FIFO = "/dev/pi-blaster";
    private static final String PI_BLASTER_FIFO = "pi-blaster.test";
    private static final double ENGINE_PWM_TIMING_LOW = 0.05; // here the engine is at min throttle
    private static final double ENGINE_PWM_TIMING_HIGH = 0.1; // here the engine is at full throttle

    private BufferedWriter commandWriter;

    private double engineFrontLeftThrottle;
    private double engineFrontRightThrottle;
    private double engineRearLeftThrottle;
    private double engineRearRightThrottle;

    //**************************************************************************
    // Constructors
    //**************************************************************************
    
    public QuadCopter() {
        File piBlasterFile = new File(QuadCopter.PI_BLASTER_FIFO);
        try {
            if (!piBlasterFile.exists()) {
                piBlasterFile.createNewFile();
            }
            FileWriter fw = new FileWriter(piBlasterFile.getAbsoluteFile());
            this.commandWriter = new BufferedWriter(fw);
        } catch (IOException e) {
            System.err.println("[ERROR]Glarus1 can't init itself");
            System.exit(-1);
        }
        this.setAllEnginesOnThrottle(0);
    }

    //**************************************************************************
    // Engines interaction
    //**************************************************************************
    public double getEngineFrontLeftThrottle() {
        return engineFrontLeftThrottle;
    }

    public void setEngineFrontLeftThrottle(double engineFrontLeftThrottle) {
        if (engineFrontLeftThrottle > 100) {
            engineFrontLeftThrottle = 100;
        } else if (engineFrontLeftThrottle < 0) {
            engineFrontLeftThrottle = 0;
        }
        this.engineFrontLeftThrottle = engineFrontLeftThrottle;
        this.trySendCommandToESCs(Engines.ENGINE_FRONT_LEFT, engineFrontLeftThrottle);
    }

    public double getEngineFrontRightThrottle() {
        return engineFrontRightThrottle;
    }

    public void setEngineFrontRightThrottle(double engineFrontRightThrottle) {
        if (engineFrontRightThrottle > 100) {
            engineFrontRightThrottle = 100;
        } else if (engineFrontRightThrottle < 0) {
            engineFrontRightThrottle = 0;
        }
        this.engineFrontRightThrottle = engineFrontRightThrottle;
        this.trySendCommandToESCs(Engines.ENGINE_FRONT_RIGHT, engineFrontRightThrottle);
    }

    public double getEngineRearLeftThrottle() {
        return engineRearLeftThrottle;
    }

    public void setEngineRearLeftThrottle(double engineRearLeftThrottle) {
        if (engineRearLeftThrottle > 100) {
            engineRearLeftThrottle = 100;
        } else if (engineRearLeftThrottle < 0) {
            engineRearLeftThrottle = 0;
        }
        this.engineRearLeftThrottle = engineRearLeftThrottle;
        this.trySendCommandToESCs(Engines.ENGINE_REAR_LEFT, engineRearLeftThrottle);
    }

    public double getEngineRearRightThrottle() {
        return engineRearRightThrottle;
    }

    public void setEngineRearRightThrottle(double engineRearRightThrottle) {
        if (engineRearRightThrottle > 100) {
            engineRearRightThrottle = 100;
        } else if (engineRearRightThrottle < 0) {
            engineRearRightThrottle = 0;
        }
        this.engineRearRightThrottle = engineRearRightThrottle;
        this.trySendCommandToESCs(Engines.ENGINE_REAR_RIGHT, engineRearRightThrottle);
    }

    public final void setAllEnginesOnThrottle(double throttlePercentage) {
        this.setEngineFrontLeftThrottle(throttlePercentage);
        this.setEngineFrontRightThrottle(throttlePercentage);
        this.setEngineRearLeftThrottle(throttlePercentage);
        this.setEngineRearRightThrottle(throttlePercentage);
    }

    public double getBiggestThrottleValue() {
        double frontLeft = this.getEngineFrontLeftThrottle();
        double frontRight = this.getEngineFrontRightThrottle();
        double rearLeft = this.getEngineRearLeftThrottle();
        double rearRight = this.getEngineRearRightThrottle();

        double maxAppliedThrottle = 0;
        double enginesThrottle[] = {frontLeft, frontRight, rearLeft, rearRight};
        for (int i = 0; i < enginesThrottle.length; i++) {
            if (enginesThrottle[i] > maxAppliedThrottle) {
                maxAppliedThrottle = enginesThrottle[i];
            }
        }
        return maxAppliedThrottle;
    }
    //**************************************************************************
    // Private Methods
    //**************************************************************************

    private boolean trySendCommandToESCs(Engines engine, double throttleInPercentage) {
        boolean success = true;
        try {
            String command = String.format("%d=%f%n", engine.piGpioPin, this.timeForPercentage(throttleInPercentage));
            this.commandWriter.write(command);
            this.commandWriter.flush();

            System.out.printf("[INFO]Command send to ESC's - %s:\t%.2f\n", engine.name, throttleInPercentage);
        } catch (IOException e) {
            // very very bad case, dont want to happen during flight
            success = false;
        }
        return success;
    }

    private double timeForPercentage(double percentage) {
        double wholeThrottleRange = QuadCopter.ENGINE_PWM_TIMING_HIGH - QuadCopter.ENGINE_PWM_TIMING_LOW;
        double timeForSinglePercentOfWholeThrottleRange = wholeThrottleRange / 100.0;
        return QuadCopter.ENGINE_PWM_TIMING_LOW + timeForSinglePercentOfWholeThrottleRange * percentage;
    }
}
