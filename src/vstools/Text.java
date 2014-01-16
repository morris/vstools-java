package vstools;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides VS text conversions.
 * http://datacrystal.romhacking.net/wiki/Vagrant_Story:TBL
 * 
 * @author morris
 * 
 */
public class Text {

	public static String convert(int[] i) {
		return convert(i, i.length);
	}

	public static String convert(int[] i, int len) {
		return convert(i, len, false);
	}

	public static String convert(int[] i, int len, boolean end) {
		String s = "";
		int j = 0;
		while (j < len) {
			// control code 0xFA
			if (i[j] == 0xFA) {

				if (i[j + 1] == 0x06) { // space
					s += ' ';
					j += 2;
				} else { // TODO unknown
					// s += "{0xfa" + hex(i[j + 1]) + "}";
					j += 2;
				}
			}
			// control code 0xFA
			else if (i[j] == 0xF8) {
				// unknown, skip
				j += 2;
			}
			// control code 0xFC
			else if (i[j] == 0xFC) {
				// unknown, skip
				j += 2;
			}
			// control code 0xFB
			else if (i[j] == 0xFB) {
				// unknown, skip
				j += 2;
			}
			// end of string
			else if (i[j] == 0xE7 && end) {
				return s;
			} else {
				s += convert(i[j]);
				++j;
			}
		}
		return s;
	}

	public static String convert(int i) {
		setup();
		String c = map.get(i);
		if (c != null) {
			return c;
		} else {
			return "{" + Util.hex(i, 2) + "}";
		}
	}

	private static void setup() {
		if (map == null) {
			map = new HashMap<Integer, String>();

			// build table

			// 0 - 9
			for (int i = 0; i <= 0x09; ++i) {
				put(i, Character.toChars(i + 0x30));
			}
			// A - Z
			for (int i = 0x0A; i <= 0x23; ++i) {
				put(i, Character.toChars(i + 0x41 - 0x0A));
			}
			// a - z
			for (int i = 0x24; i <= 0x3D; ++i) {
				put(i, Character.toChars(i + 0x61 - 0x24));
			}

			put(0x40, "_");
			put(0x41, '�');
			put(0x42, '�');
			put(0x43, '�');
			put(0x44, '�');
			put(0x45, '�');
			put(0x46, '�');
			put(0x47, '�');
			put(0x48, '�');
			put(0x49, "_");
			put(0x4A, '�');
			put(0x4B, "_");
			put(0x4C, '�');
			put(0x4D, '�');
			put(0x4E, '�');
			put(0x4F, '�');
			put(0x50, '�');
			put(0x51, '�');
			put(0x52, '�');
			put(0x53, '�');
			put(0x54, '�');
			put(0x55, '�');
			put(0x56, '�');
			put(0x57, '�');
			put(0x58, '�');
			put(0x59, '�');
			put(0x5A, '�');
			put(0x5B, '�');
			put(0x5C, '�');
			put(0x5D, '�');
			put(0x5E, '�');
			put(0x5F, '�');
			put(0x60, '�');
			put(0x61, '�');
			put(0x62, '�');
			put(0x63, '�');
			put(0x64, '�');
			put(0x65, '�');
			put(0x66, '�');
			put(0x67, '�');
			put(0x68, '�');
			put(0x69, '�');
			put(0x6A, '�');

			put(0x8f, ' ');

			// long dash
			put(0x8d, "--");

			put(0x90, '!');
			put(0x91, '"');

			put(0x94, '%');

			put(0x96, '\'');
			put(0x97, '(');
			put(0x98, ')');

			put(0x9B, '[');
			put(0x9C, ']');
			put(0x9D, ';');
			put(0x9E, ':');
			put(0x9F, ',');
			put(0xA0, '.');
			put(0xA1, '/');
			put(0xA2, '\\');
			put(0xA3, '<');
			put(0xA4, '>');
			put(0xA5, '?');

			put(0xA7, '-');
			put(0xA8, '+');

			put(0xB6, "Lv."); // TODO what's this?

			put(0xE8, '\n');
		}

	}

	private static void put(int i, char c) {
		map.put(i, String.valueOf(c));
	}

	private static void put(int i, char[] c) {
		map.put(i, String.valueOf(c));
	}

	private static void put(int i, String s) {
		map.put(i, s);
	}

	public static void maptest() {
		int n[] = { 0, 1, 2, 3 };
		int AZ[] = { 0x0A, 0x0B, 0x23 };
		int az[] = { 0x24, 0x25, 0x3D };

		log(convert(n));
		log(convert(AZ));
		log(convert(az));
	}

	public static void log(Object t) {
		System.out.println(t);
	}

	private static Map<Integer, String> map;
}
