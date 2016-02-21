package firmware.networking;

/**
 *
 * @author stoqn
 */
public class MessagesHandler {
    public static void handleReceivedMessage(String receivedCommand) {
        //  here i should decide whether to make the quad perform some maneuver
        //  or change the configuration
    }
    
    public static String prepareMessageForSending(String message) {
        return message; // i should came up with some network protocol and format the string
    }
}
