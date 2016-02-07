package uk.ac.cam.wvs22.watchoutpebbletools;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The {@code PebbleSender} class handles connections to some connected Pebble (if one is connected).
 * No objects of this class can be created. The static functions are shared over the App as there should
 * only be a single channel of data being sent to the Pebble from an App.
 *
 * @author Will Simmons
 * @since 07/02/2016
 */
public class PebbleSender {

    /**
     * Enumeration of the possible message types for communications with the Pebble.
     * ALERT will provide a new alert for an approaching hazard - requires TYPE, HAZARD_ID, HAZARD_TYPE, HAZARD_DESC and HAZARD_DIST fields.
     * NEW is sent from the Pebble to the phone upon the user reporting a new hazard - requires TYPE and HAZARD_TYPE fields.
     * ACTION is sent from the Pebble when the user responds to an alert - requires TYPE, HAZARD_ID and ACTION fields.
     * IGNORE is sent to the Pebble when an alert is no longer relevant and it should no longer be displayed - requires TYPE and HAZARD_ID fields.
     * UPDATE is sent to the Pebble to update the approximate distance to a hazard for its alert - requires TYPE, HAZARD_ID and HAZARD_DIST fields.
     */
    public enum PebbleMessageType {
        ALERT, NEW, ACTION, IGNORE, UPDATE
    }

    /**
     * Enumeration of the possible keys for all messages with the Pebble.
     * TYPE indicates the purpose of the message - value is an ordinal of some PebbleMessageType.
     * HAZARD_ID is the id code for the hazard under concern - value is an integer.
     * HAZARD_TYPE is the title of the hazard - value is a string (max length 15 characters).
     * HAZARD_DESC is the long description of the hazard - value is a string (max length 80 characters).
     * HAZARD_DIST is the approximate distance to the hazard (in metres) - value is an int.
     * ACTION is the action taken by the user on some alert - value is an ordinal of some PebbleActionType.
     */
    public enum PebbleMessageKey {
        TYPE, HAZARD_ID, HAZARD_TYPE, HAZARD_DESC, HAZARD_DIST, ACTION
    }

    /**
     * Enumeration of the possible active actions taken by the user on some alert.
     * ACK indicates that the user observed and acknowledged the hazard.
     * DIS indicates that the user could not see the hazard and reported its absence.
     * NACK indicates that the user cleared the alert list from their Pebble without acknowledgement or dismissal.
     */
    public enum PebbleActionType {
        ACK, DIS, NACK
    }

    
    private static PebbleKit.PebbleNackReceiver mNackReceiver;
    private static PebbleKit.PebbleAckReceiver mAckReceiver;
    private static Map<Integer, PebbleDictionary> messages;
    private static int currentSendMessageId = 0;
    private static int currentQueueMessageId = 0;
    private static AppCompatActivity parent;
    public final static UUID PEBBLE_APP_UUID =
            UUID.fromString("172e12e2-bba3-469c-bef5-4af9b88bfd96");

    private PebbleSender() {}

    /**
     * This method should be called upon booting up the app to initialise the sender with the
     * main Activity (as required for acquiring the context for sending to the Pebble). Automatically
     * sets up a {@code PebbleKit.PebbleAckReceiver} and a {@code PebbleKit.PebbleNackReceiver}, so
     * these should not be registered elsewhere in the App.
     *
     * @param mainAct Main Activity of the App.
     */
    public static void startSender(AppCompatActivity mainAct) {
        if (parent != null) {
            parent = mainAct; return;
        }
        parent = mainAct;

        messages = new HashMap<Integer, PebbleDictionary>();

        mNackReceiver = new PebbleKit.PebbleNackReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveNack(Context context, int transactionId) {
                Log.i("DataSender", transactionId + " was nacked");
                if (transactionId == currentSendMessageId)
                    PebbleKit.sendDataToPebbleWithTransactionId(parent.getApplicationContext(),
                            PEBBLE_APP_UUID,
                            messages.get(currentSendMessageId),
                            currentSendMessageId);
            }
        };

        mAckReceiver = new PebbleKit.PebbleAckReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveAck(Context context, int transactionId) {
                if (transactionId == currentSendMessageId) {
                    Log.i("DataSender", "Sent " + currentSendMessageId);
                    currentSendMessageId = (currentSendMessageId + 1) % 256;
                    if (currentSendMessageId != currentQueueMessageId) {
                        try {
                            Thread.sleep(1000, 0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        PebbleKit.sendDataToPebbleWithTransactionId(parent.getApplicationContext(),
                                PEBBLE_APP_UUID,
                                messages.get(currentSendMessageId),
                                currentSendMessageId);
                    }
                }
            }
        };

        PebbleKit.registerReceivedNackHandler(parent.getApplicationContext(), mNackReceiver);
        PebbleKit.registerReceivedAckHandler(parent.getApplicationContext(), mAckReceiver);
    }

    /**
     * <h2>send</h2>
     * Send a prepared PebbleDictionary message (either ALERT or IGNORE) to the Pebble.
     * Handles queueing of these and resending to cope with dropped messages.
     *
     * @param data The PebbleDictionary ALERT or IGNORE message to be sent to the Pebble.
     */
    public static void send(PebbleDictionary data) {
        messages.put(currentQueueMessageId, data);
        Log.i("DataSender", "Attempting to send " + currentQueueMessageId);
        if (currentSendMessageId == currentQueueMessageId) {
            PebbleKit.sendDataToPebbleWithTransactionId(parent.getApplicationContext(),
                    PEBBLE_APP_UUID, data,
                    currentSendMessageId);
        }
        currentQueueMessageId = (currentQueueMessageId + 1) % 256;
    }
}
