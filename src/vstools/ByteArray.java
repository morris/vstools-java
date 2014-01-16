package vstools;

/**
 * Encapsulation of a raw byte array. Allows for easy access to different data
 * types. Everything is Little Endian unless suffixed with "big". Compared to C,
 * Java is awkward at these kinds of operations. The main drawback is that
 * everything has to be casted to int, otherwise development would involve too
 * much casting.
 * 
 * @author morris
 * 
 */
public class ByteArray {

	public ByteArray(byte[] data) {
		this.data = data;
		length = data.length;
		pos = 0;
	}

	public void seek(int i) {
		pos = i;
	}

	public void skip(int i) {
		pos += i;
	}

	public byte byte_() {
		pos += 1;
		return data[pos - 1];
	}

	public int s8() {
		pos += 1;
		return (int) data[pos - 1];
	}

	public int u8() {
		return s8() & 0x000000ff;
	}

	public int s16() {
		return u8() | s8() << 8;
	}

	public int s16big() {
		return s8() << 8 | u8();
	}

	public int u16() {
		return s16() & 0x0000ffff;
	}

	public int s32() {
		return u8() | u8() << 8 | u8() << 16 | u8() << 24;
	}

	public int u32() {
		// TODO only works if u32 are really all smaller than 0x7fffffff
		// should return long here, but this would break a lot
		return s32();
	}

	public int[] buf(int len) {
		int[] buffer = new int[len];
		for (int i = 0; i < len; ++i) {
			buffer[i] = u8();
		}
		return buffer;
	}

	public static void main(String args[]) {
		ByteArray t = new ByteArray(null);
		t.test();
	}

	public void test() {
		// TODO this test should be more exhaustive. i'm worried there might
		// still be a few bugs here...
		byte[] t = { (byte) 0x01, (byte) 0xff };
		data = t;

		System.out.println(u8());
		System.out.println(s8());
		pos = 0;
		System.out.println(s16());
		pos = 0;
		System.out.println(s16big());
		System.out.println(Util.hex(data[pos - 1]));
	}

	public byte[] data;
	public int pos;
	public final int length;
}
