/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package firmware.configuration;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author stoqn
 */
public class ConfigurationHandler {

    private Configuration config;

    //**************************************************************************
    // Globally accesible constants
    //**************************************************************************
    public static final double EXPECTED_HOVER_THROTTLE_VALUE = 30;
    public static final double MIN_BOUND_OFFSET_FROM_HOVER = 10; // in percents

    // these two should sum up to 2
    public static final double MAX_RANGE_FACTOR_BETWEEN_MIN_HOVER_THROTTLE_DURING_MANEUVER = 0.5;
    public static final double MAX_RANGE_FACTOR_BETWEEN_MAX_HOVER_THROTTLE_DURING_MANEUVER = 1.5;

    // if value is high there is a risk of over steering and crashing
    public static final double MAX_RANGE_OFFSET_BETWEEN_MIN_MAX_THROTTLE_DURING_MANEUVER = 15;

    // the place in file system where the quad will store its settings
    // make sure you have write permission
    public static final String CONFIGURATION_FILE = "config.bin";

    //**************************************************************************
    // Singleton's sharedInstance
    //**************************************************************************
    private static ConfigurationHandler instance = null;

    // Lazy Initialization (If required then only)
    public static ConfigurationHandler sharedInstance() {
        // Thread Safe. Might be costly operation in some case
        synchronized (ConfigurationHandler.class) {
            if (instance == null) {
                instance = new ConfigurationHandler();
            }
        }
        return instance;
    }

    //**************************************************************************
    // Storing and loading configuration to disk
    //**************************************************************************
    public Configuration getStoredConfiguration() {
        if (this.config != null) {
            return this.config;
        }
        Configuration storedConfig = null;
        try {
            FileInputStream fileIn = new FileInputStream(CONFIGURATION_FILE);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            storedConfig = (Configuration) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            // the default config case
            this.config = new Configuration();
            this.config.hoverThrottlePercentage = EXPECTED_HOVER_THROTTLE_VALUE;
            this.storeConfiguration(this.config);
            return this.config;
        } catch (ClassNotFoundException c) {
        }
        this.config = storedConfig;
        return this.config;
    }

    public void storeConfiguration(Configuration config) {
        this.config = config;
        try {
            FileOutputStream fileOut = new FileOutputStream(CONFIGURATION_FILE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(config);
            out.close();
            fileOut.close();
            System.out.printf("Configuration is saved in: %f", CONFIGURATION_FILE);
        } catch (IOException i) {
            System.err.printf("[WARNING]Configuration save failed,"
                    + " and will not persist during relaunching of the program."
                    + "\nMake sure you have write permission in %f", CONFIGURATION_FILE);
        }
    }
}
