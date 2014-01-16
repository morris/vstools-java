package vstools;

public class ARMRoomIndices extends Data {

	public ARMRoomIndices(ByteArray data) {
		super(data);
	}

	public void read() {
		p1 = u8();
		p2 = u8();
		p3 = u8();
		p4 = u8();
	}

	public String toString() {
		return p1 + " " + p2 + " " + p3 + " " + p4;
	}

	int p1;
	int p2;
	int p3;
	int p4;
}
