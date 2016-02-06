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
 * @since 04/02/2016
 */
public class PebbleSender {
    /**
     * KEY for TYPE field - required for all messages.
     * Value options: {@code DICT_TYPE_ALERT}, {@code DICT_TYPE_NEW}, {@code DICT_TYPE_ACTION} or {@code DICT_TYPE_IGNORE}.
     */
    public final static int DICT_TYPE_KEY = 0;

    /**
     * Value for TYPE field indicating an ALERT message.
     * ALERT messages require TYPE, HAZARD_ID, HAZARD_TYPE and HAZARD_DESC fields.
     */
    public final static int DICT_TYPE_ALERT = 0;

    /**
     * Value for TYPE field indicating a NEW message.
     * NEW messages require TYPE and HAZARD_TYPE fields.
     */
    public final static int DICT_TYPE_NEW = 1;

    /**
     * Value for TYPE field indicating an ACTION message.
     * ACTION messages require TYPE, HAZARD_ID and ACTION fields.
     */
    public final static int DICT_TYPE_ACTION = 2;

    /**
     * Value for TYPE field indicating an IGNORE message.
     * IGNORE messages require TYPE, and HAZARD_ID fields.
     */
    public final static int DICT_TYPE_IGNORE = 3;

    /**
     * KEY for HAZARD_ID field - required for ALERT, ACTION and IGNORE messages.
     * Value options: Hazard ID numbers.
     */
    public final static int DICT_HAZARD_ID_KEY = 1;

    /**
     * KEY for HAZARD_TYPE field - required for ALERT and NEW messages.
     * Value options: String giving brief description of hazard.
     */
    public final static int DICT_HAZARD_TYPE_KEY = 2;

    /**
     * KEY for HAZARD_DESC field - required for ALERT messages.
     * Value options: String giving more detailed description of hazard.
     */
    public final static int DICT_HAZARD_DESC_KEY = 3;

    /**
     * KEY for ACTION field - required for ACTION messages.
     * Value options: {@code DICT_ACTION_ACK}, {@code DICT_ACTION_DIS} or {@code DICT_ACTION_NACK}.
     */
    public final static int DICT_ACTION_KEY = 4;

    /**
     * Value for ACTION field indicating an acknowledgement of some alert.
     */
    public final static int DICT_ACTION_ACK = 0;

    /**
     * Value for ACTION field indicating the refutation of some alert.
     */
    public final static int DICT_ACTION_DIS = 1;

    /**
     * Value for ACTION field indicating the ignoring of an alert.
     */
    public final static int DICT_ACTION_NACK = 2;

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
