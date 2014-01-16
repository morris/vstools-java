package vstools;

public class WEPJoint extends Data {

	public WEPJoint(ByteArray data) {
		super(data);

	}

	public void read() {
		length = -s16(); // negative
		skip(2); // always 0xFF, no effect on joint size or model
		parentJointId = s8();
		x = s8();
		y = s8();
		z = s8();

		mode = s8();
		// 0 - 2 normal ?
		// 3 - 6 normal + roll 90 degrees
		// 7 - 255 absolute, different angles

		skip(1); // unknown
		skip(6); // always 0
	}

	public int length;
	public int parentJointId;
	public int x;
	public int y;
	public int z;
	public int mode;

	public WEPJoint parentJoint;
	public String name;
}
