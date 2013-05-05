package vstools;

public class WEPVertex extends Data {

    public WEPVertex(ByteArray data) {
	super(data);
    }
    
    public void read() {
	x = s16();
	y = s16();
	z = s16();
	skip(2); // zero padding
	//log("  vertex(" + x + "," + y + "," + z + ")");
    }
    
    public int x;
    public int y;
    public int z;
    public int group;
    public WEPGroup groupObject;
    public int joint;
}
