package vstools;

public class WEPGroup extends Data {

    public WEPGroup(ByteArray data) {
	super(data);
    }

    public void read() {
	joint = s16();
	lastVertex = u16();
    }
    
    public int joint;
    public int lastVertex;
    
    public WEPJoint jointObject;
}
