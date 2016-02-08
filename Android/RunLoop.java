import java.lang.InterruptedException;
import java.lang.Runnable;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

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

	private Location lastCachedLocation;
	private Date lastCachedTime;
	private LinkedHashSet<Hazard> pendingHazards; // all hazards in our cache radius begin here
	private LinkedHashSet<Hazard> activeHazards; // nearby hazards
	private LinkedHashMap<Hazard,Date> inactiveHazards; // recently warned hazards

	public RunLoop() {
		this.runState = RunState.INACTIVE;
		Location currentLocation = GPS.getCurrentLocation();
		HazardManager.renewCache(ServerConnection.getHazards(currentLocation));
		this.lastCachedLocation = currentLocation;
		this.lastCachedTime = new Date();
		this.pendingHazards = HazardManager.getHazardCache();
		this.activeHazards = new LinkedHashSet<Hazard>();
		this.inactiveHazards = new LinkedHashMap<Hazard,Date>();
	}

	@Override
	public void run() {
		while (true) {
			boolean renewPending = false;
			Location currentLocation = GPS.getCurrentLocation();
			Date currentTime = new Date();

			// update the cache if the distance from the last update has equalled or exceeded CACHE_RADIUS or the time from the last update is at least CACHE_TIMEOUT
			if (GPS.calculateDistance(this.lastCachedLocation, currentLocation) >= CACHE_RADIUS
               || currentTime.getTime() - this.lastCachedTime.getTime() >= CACHE_TIMEOUT) {
				HazardManager.renewCache(ServerConnection.getHazards(currentLocation));
				this.pendingHazards = HazardManager.getHazardCache();
				renewPending = true;
			}
			if (renewPending || HazardManager.getNewHazardFlag()) {
				this.pendingHazards.addAll(HazardManager.getNewHazards());
				renewPending = false;
			}

			// remove hazards from the set of recently warned hazards if at least WARN_DELAY have passed
			for (Iterator<Date> it = this.inactiveHazards.values().iterator(); it.hasNext(); ) {
				Date nextDate = it.next();
				if (currentTime.getTime() - nextDate.getTime() >= WARN_DELAY)
					it.remove();
			}

			// move hazards from pendingHazards to activeHazards if we are at most WARN_DISTANCE away and the hazard is not in inactiveHazards
			for (Hazard h : this.pendingHazards) {
				if (GPS.calculateDistance(h.getLocation(), currentLocation) <= WARN_DISTANCE && !this.inactiveHazards.keySet().contains(h))
					PebbleDataSender.sendNewHazard(h);
			}

			// (I) update distance for active hazards or (II) move the hazard to inactiveHazards if we are more than WARN_DISTANCE away
			for (Iterator<Hazard> it = this.activeHazards.iterator(); it.hasNext(); ) {
				Hazard h = it.next();
				double distanceFromH = GPS.calculateDistance(h.getLocation(), currentLocation);
				if (distanceFromH <= WARN_DISTANCE) // (I)
					PebbleDataSender.updateActiveHazard(h, distanceFromH);
				else { // (II)
					this.inactiveHazards.put(h, currentTime);
					it.remove();
				}
			}

			try {
				if (runState == RunState.ACTIVE)
					Thread.sleep(LOOP_DELAY_ACTIVE);
				else Thread.sleep(LOOP_DELAY_INACTIVE);
			} catch (InterruptedException ie) {
				break;
			}
		}
	}
}

/* test stuff */

class Location {
	private double latitude;
	private double longitude;

	public double getLatitude() { return latitude; }
	public double getLongitude() { return longitude; }

	Location(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
}

class GPS {
	public static Location getCurrentLocation() { return new Location(10,20); }
	public static double calculateDistance(Location n, Location m) { return 1000; }
}

class ServerConnection {
	public static byte[] getHazards(Location l) { return new byte[1]; }
}

class HazardManager {
	public static void renewCache(byte[] locations) { }
	public static LinkedHashSet<Hazard> getHazardCache() { return new LinkedHashSet<Hazard>(); }
	public static LinkedHashSet<Hazard> getNewHazards() { return new LinkedHashSet<Hazard>(); }
	public static boolean getNewHazardFlag() { return true; }
}

class PebbleDataSender {
	public static void sendNewHazard(Hazard h) {}
	public static void updateActiveHazard(Hazard h, double distanceFromHazard) {}
}

class Hazard {
	private Location loc;
	public Location getLocation() { return loc; }
}
