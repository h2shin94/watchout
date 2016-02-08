package uk.ac.cam.wvs22.watchoutpebbletools;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

/**
 * The {@code PebbleReceiver} class handles connections from some connected Pebble (if one is connected).
 * No objects of this class can be created. The static functions are shared over the App as there should
 * only be a single channel of data being sent from the Pebble to an App.
 *
 * @author Will Simmons
 * @since 04/02/2016
 */
public class PebbleReceiver {
    private static PebbleKit.PebbleDataReceiver mDataReceiver;
    private static AppCompatActivity parent;

    private PebbleReceiver() {}

    /**
     * This method should be called upon booting up the app to initialise the receiver with the
     * main Activity (as required for acquiring the context for registering the receiver). Automatically
     * sets up a {@code PebbleKit.PebbleDataReceiver}, so none should be registered elsewhere in the App.
     *
     * @param mainAct Main Activity of the App.
     */
    public static void startReceiver(AppCompatActivity mainAct) {
        parent = mainAct;
        mDataReceiver = new PebbleKit.PebbleDataReceiver(PebbleSender.PEBBLE_APP_UUID) {
            @Override
            public void receiveData(Context context, int transactionId, PebbleDictionary dict) {
                PebbleKit.sendAckToPebble(context, transactionId);
                switch (PebbleSender.PebbleMessageType.values()[dict.getInteger(PebbleSender.PebbleMessageKey.TYPE.ordinal()).intValue()]) {
                    case NEW:
                        // TODO:: Handle new hazard
                        // Invoke HazardManager.newHazard(dict);
                        Log.i("DataReceiver", "Received New Hazard");
                        break;
                    case ACTION:
                        // TODO:: Handle action
                        switch (PebbleSender.PebbleActionType.values()[dict.getInteger(PebbleSender.PebbleMessageKey.ACTION.ordinal()).intValue()]) {
                            case ACK:
                                // User acknowledged the hazard
                                // Find hazard in alert list
                                // Add hazard to acknowledged list
                                // Remove hazard from alert list
                                Log.i("DataReceiver", "Received Ack");
                                break;
                            case DIS:
                                // User did not see the hazard
                                // Find hazard in alert list
                                // Add hazard to refuted list
                                // Remove hazard from alert list
                                Log.i("DataReceiver", "Received Dismissal");
                                break;
                            case NACK:
                                // User dismissed the alert
                                // Find hazard in alert list
                                // Remove hazard from alert list
                                Log.i("DataReceiver", "Received Nack");
                                break;
                        }
                        break;
                    default:

                        break;
                }
            }
        };
        PebbleKit.registerReceivedDataHandler(parent.getApplicationContext(), mDataReceiver);
    }

}
