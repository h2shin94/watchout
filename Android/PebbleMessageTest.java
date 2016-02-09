import static org.junit.Assert.assertEquals;
import org.junit.Test;

import com.getpebble.android.kit.util.PebbleDictionary;

// vim command
// :w | :!clear && javac -cp .:junit-4.12.jar PebbleMessageTest.java && java -cp .:junit-4.12.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore PebbleMessageTest

public class PebbleMessageTest {
	@Test
	public void validAlertMessage() {
		Hazard h = new Hazard();
		int distance = 100;
		PebbleDictionary pd = PebbleMessage.createAlert(h,distance);

		assertEquals("TYPE fail in Alert", pd.getInteger(PebbleMessage.Key.TYPE.ordinal()).intValue(), PebbleMessage.Type.ALERT.ordinal());
		assertEquals("HAZARD_ID fail in Alert", pd.getInteger(PebbleMessage.Key.HAZARD_ID.ordinal()).intValue(), h.getId());
		assertEquals("HAZARD_TYPE fail in Alert", pd.getString(PebbleMessage.Key.HAZARD_TYPE.ordinal()).toString(), h.getType());
		assertEquals("HAZARD_DESC fail in Alert", pd.getString(PebbleMessage.Key.HAZARD_DESC.ordinal()).toString(), h.getDescription());
		assertEquals("HAZARD_DIST fail in Alert", pd.getInteger(PebbleMessage.Key.HAZARD_DIST.ordinal()).intValue(), distance);
	}
}
