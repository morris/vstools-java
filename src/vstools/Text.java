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
			put(0x41, 'Â');
			put(0x42, 'Ä');
			put(0x43, 'Ç');
			put(0x44, 'È');
			put(0x45, 'É');
			put(0x46, 'Ê');
			put(0x47, 'Ë');
			put(0x48, 'Ì');
			put(0x49, "_");
			put(0x4A, 'Î');
			put(0x4B, "_");
			put(0x4C, 'Ò');
			put(0x4D, 'Ó');
			put(0x4E, 'Ô');
			put(0x4F, 'Ö');
			put(0x50, 'Ù');
			put(0x51, 'Ú');
			put(0x52, 'Û');
			put(0x53, 'Ü');
			put(0x54, 'ß');
			put(0x55, 'æ');
			put(0x56, 'à');
			put(0x57, 'á');
			put(0x58, 'â');
			put(0x59, 'ä');
			put(0x5A, 'ç');
			put(0x5B, 'è');
			put(0x5C, 'é');
			put(0x5D, 'ê');
			put(0x5E, 'ë');
			put(0x5F, 'ì');
			put(0x60, 'í');
			put(0x61, 'î');
			put(0x62, 'ï');
			put(0x63, 'ò');
			put(0x64, 'ó');
			put(0x65, 'ô');
			put(0x66, 'ö');
			put(0x67, 'ù');
			put(0x68, 'ú');
			put(0x69, 'û');
			put(0x6A, 'ü');

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
