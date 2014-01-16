package vstools;

import java.util.HashMap;

public class LBA {

	private static void init() {
		if (map == null) {
			map = new HashMap<Integer, String>();

			map.put(0x18115, "OBJ/00_COM.SEQ");

			// TODO statically put all LBAs here
		}
	}

	public static String getFilename(int lba) throws Exception {
		init();

		if (map.containsKey(lba)) {
			return map.get(lba);
		} else {
			throw new Exception("Unknown LBA " + Util.hex(lba));
		}
	}

	private static HashMap<Integer, String> map;
}
