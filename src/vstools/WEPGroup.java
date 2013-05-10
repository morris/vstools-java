package vstools;

public class WEPGroup extends Data {

    public WEPGroup(ByteArray data) {
	super(data);
    }

    public void read() {
	jointId = s16();
	lastVertex = u16();
    }
    
    public int jointId;
    public int lastVertex;
    
    public WEPJoint joint;
}
