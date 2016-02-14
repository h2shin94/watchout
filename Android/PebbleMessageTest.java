import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

import com.getpebble.android.kit.util.PebbleDictionary;

// vim command
// :w | :!clear && javac -cp .:junit-4.12.jar PebbleMessageTest.java && java -cp .:junit-4.12.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore PebbleMessageTest

public class PebbleMessageTest {
	@Test
	public void validAlertMessage() {
		Hazard h = new Hazard();
		int distance = 100;
		PebbleDictionary pd = PebbleMessage.createAlert(h, distance);

		assertEquals("TYPE fail in Alert", pd.getInteger(PebbleMessage.Key.TYPE.ordinal()).intValue(), PebbleMessage.Type.ALERT.ordinal());
		assertEquals("HAZARD_ID fail in Alert", pd.getInteger(PebbleMessage.Key.HAZARD_ID.ordinal()).intValue(), h.getId());
		assertEquals("HAZARD_TYPE fail in Alert", pd.getString(PebbleMessage.Key.HAZARD_TYPE.ordinal()).toString(), h.getType());
		assertEquals("HAZARD_DESC fail in Alert", pd.getString(PebbleMessage.Key.HAZARD_DESC.ordinal()).toString(), h.getDescription());
		assertEquals("HAZARD_DIST fail in Alert", pd.getInteger(PebbleMessage.Key.HAZARD_DIST.ordinal()).intValue(), distance);
	}


	@Test
	public void validIgnoreMessage() {
		Hazard h = new Hazard();
		PebbleDictionary pd = PebbleMessage.createIgnore(h);

		assertEquals("TYPE fail in Ignore", pd.getInteger(PebbleMessage.Key.TYPE.ordinal()).intValue(), PebbleMessage.Type.IGNORE.ordinal());
		assertEquals("HAZARD_ID fail in Ignore", pd.getInteger(PebbleMessage.Key.HAZARD_ID.ordinal()).intValue(), h.getId());
	}


	@Test
	public void validUpdateMessage() {
		Hazard h = new Hazard();
		int distance = 100;
		PebbleDictionary pd = PebbleMessage.createUpdate(h, distance);

		assertEquals("TYPE fail in Update", pd.getInteger(PebbleMessage.Key.TYPE.ordinal()).intValue(), PebbleMessage.Type.UPDATE.ordinal());
		assertEquals("HAZARD_ID fail in Update", pd.getInteger(PebbleMessage.Key.HAZARD_ID.ordinal()).intValue(), h.getId());
		assertEquals("HAZARD_DIST fail in Update", pd.getInteger(PebbleMessage.Key.HAZARD_DIST.ordinal()).intValue(), distance);
	}
	
	
	@Test
	public void invalidMessage() {
		Hazard h = new Hazard();
		int distance = 100;
		PebbleDictionary pd = PebbleMessage.createUpdate(h, distance);

		assertFalse("TYPE fail in NegativeUpdate", pd.getInteger(PebbleMessage.Key.TYPE.ordinal()).intValue() == PebbleMessage.Type.IGNORE.ordinal());
		assertEquals("HAZARD_ID fail in NegativeUpdate", pd.getInteger(PebbleMessage.Key.HAZARD_ID.ordinal()).intValue(), h.getId());
		assertEquals("HAZARD_DIST fail in NegativeUpdate", pd.getInteger(PebbleMessage.Key.HAZARD_DIST.ordinal()).intValue(), distance);
	}
	
	
	@Test (expected = NullPointerException.class) // Do we want a better way to handle at this level?
	public void nullHazard() {
		Hazard h = null;
		PebbleDictionary pd = PebbleMessage.createIgnore(h);

		assertEquals("TYPE fail in Ignore", pd.getInteger(PebbleMessage.Key.TYPE.ordinal()).intValue(), PebbleMessage.Type.IGNORE.ordinal());
		assertEquals("HAZARD_ID fail in Ignore", pd.getInteger(PebbleMessage.Key.HAZARD_ID.ordinal()).intValue(), h.getId());
	}
	
	
	@Test
	public void negativeDistance() { // Do we need to consider negative distance and whether this should pass or fail?
		Hazard h = new Hazard();
		int distance = -100;
		PebbleDictionary pd = PebbleMessage.createAlert(h, distance);

		assertEquals("TYPE fail in Alert", pd.getInteger(PebbleMessage.Key.TYPE.ordinal()).intValue(), PebbleMessage.Type.ALERT.ordinal());
		assertEquals("HAZARD_ID fail in Alert", pd.getInteger(PebbleMessage.Key.HAZARD_ID.ordinal()).intValue(), h.getId());
		assertEquals("HAZARD_TYPE fail in Alert", pd.getString(PebbleMessage.Key.HAZARD_TYPE.ordinal()).toString(), h.getType());
		assertEquals("HAZARD_DESC fail in Alert", pd.getString(PebbleMessage.Key.HAZARD_DESC.ordinal()).toString(), h.getDescription());
		assertEquals("HAZARD_DIST fail in Alert", pd.getInteger(PebbleMessage.Key.HAZARD_DIST.ordinal()).intValue(), distance);
	}


}

// test 