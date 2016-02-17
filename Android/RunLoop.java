package uk.ac.cam.grpproj.lima2016.watchout;

import com.google.android.gms.maps.model.LatLng;
import com.getpebble.android.kit.util.PebbleDictionary;
import org.json.JSONObject;
import java.lang.InterruptedException;
import java.lang.Runnable;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import org.json.JSONException;

/**
 * @author Suraj Patel <suraj-patel-95@outlook.com>
 */

class RunLoop implements Runnable {
	/**
	 * Defines whether or not the user has initiated a run.
	 * <p>
	 * The frequency of GPS requests and cache updates is modified appropriately.
	 */
	public enum RunState { ACTIVE, INACTIVE }
	private RunState runState;
	/**
	 * Sets the runState of the loop thread.
	 *
	 * @param rs The state to which the runState is set.
	 */
	public void setRunState(RunState rs) { this.runState = rs; }

	private static final int CACHE_TIMEOUT = 1000; //TODO: set appropriate value (milliseconds)
	private static final double CACHE_RADIUS = 1000; //TODO: set appropriate value
	private static final int LOOP_DELAY_ACTIVE = 1000; //TODO: set appropriate value
	private static final int LOOP_DELAY_INACTIVE = 2000; //TODO: set appropriate value
	private static final double WARN_DISTANCE = 1000; //TODO: set appropriate value
	private static final int WARN_DELAY = 1000; //TODO: set appropriate value

	private LatLng lastCachedLocation;
	private Date lastCachedTime;
	private Set<Hazard> activeHazards; // nearby hazards
	private LinkedHashMap<Hazard,Date> inactiveHazards; // recently warned hazards

	public RunLoop() {
		this.runState = RunState.INACTIVE;
		LatLng currentLocation = GPS.getCurrentLocation();
		try {
			HazardManager.populateHazardSet(ServerInterface.getHazards(currentLocation));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (JSONException jsone) {
			jsone.printStackTrace();
		}
		this.lastCachedLocation = currentLocation;
		this.lastCachedTime = new Date();
		this.activeHazards = Collections.synchronizedSet(new LinkedHashSet<Hazard>());
		this.inactiveHazards = new LinkedHashMap<Hazard,Date>();
	}

	/**
	 * Allows removal of hazards from the set of active hazards, for the case where the user actively dismisses the hazard.
	 */
	public void removeActiveHazard(int hazardID) {
		synchronized(this.activeHazards) {
			for (Iterator<Hazard> it = activeHazards.iterator(); it.hasNext(); ) {
				Hazard h = it.next();
				if (h.getId() == hazardID) {
					it.remove();
					break;
				}
			}
		}
	}

	@Override
	public void run() {
		while (true) {
			LatLng currentLocation = GPS.getCurrentLocation();
			Date currentTime = new Date();

			// update the cache if the distance from the last update has equalled or exceeded CACHE_RADIUS or the time from the last update is at least CACHE_TIMEOUT
			if (GPS.calculateDistance(this.lastCachedLocation, currentLocation) >= CACHE_RADIUS
					|| currentTime.getTime() - this.lastCachedTime.getTime() >= CACHE_TIMEOUT) {
				try {
					HazardManager.populateHazardSet(ServerInterface.getHazards(currentLocation));
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} catch (JSONException jsone) {
					jsone.printStackTrace();
				}
			}

			// remove hazards from the set of recently warned hazards if at least WARN_DELAY have passed
			for (Iterator<Date> it = this.inactiveHazards.values().iterator(); it.hasNext(); ) {
				Date nextDate = it.next();
				if (currentTime.getTime() - nextDate.getTime() >= WARN_DELAY)
					it.remove();
			}

			// (I) update distance for active hazards or (II) move the hazard to inactiveHazards if we are more than WARN_DISTANCE away
			synchronized(this.activeHazards) {
				for (Iterator<Hazard> it = this.activeHazards.iterator(); it.hasNext(); ) {
					Hazard h = it.next();
					double distanceFromH = GPS.calculateDistance(h.getLatLong(), currentLocation);
					if (distanceFromH <= WARN_DISTANCE) // (I)
						PebbleSender.send(PebbleMessage.createUpdate(h, (int) distanceFromH));
					else { // (II)
						PebbleSender.send(PebbleMessage.createIgnore(h));
						this.inactiveHazards.put(h, currentTime);
						it.remove();
					}
				}
			}

			// copy hazards to activeHazards if we are at most WARN_DISTANCE away and the hazard is not in inactiveHazards
			for (Hazard h : HazardManager.getHazardSet()) {
				double distanceFromH = GPS.calculateDistance(h.getLatLong(), currentLocation);
				if (distanceFromH <= WARN_DISTANCE && !this.inactiveHazards.keySet().contains(h)) {
					PebbleSender.send(PebbleMessage.createAlert(h, (int) distanceFromH));
					this.activeHazards.add(h);
				}
			}

			try {
				if (runState == RunState.ACTIVE)
					Thread.sleep(LOOP_DELAY_ACTIVE);
				else
					Thread.sleep(LOOP_DELAY_INACTIVE);
			} catch (InterruptedException ie) {
				break;
			}
		}
	}
}

/* test stuff
   TODO: remove */

class GPS {
	public static LatLng getCurrentLocation() { return new LatLng(10, 20); }
	public static double calculateDistance(LatLng n, LatLng m) { return 1000; }
}

/*class HazardManager {
	public static void populateHazardSet(JSONObject locations) { }
	public static Set<Hazard> getHazardSet() { return new LinkedHashSet<Hazard>(); }
	public static LinkedHashSet<Hazard> getNewHazards() { return new LinkedHashSet<Hazard>(); }
	public static boolean getNewHazardFlag() { return true; }
}*/

/*class PebbleSender {
	public static void send(PebbleDictionary pd) {}
}*/
