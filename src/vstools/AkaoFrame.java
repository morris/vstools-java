package vstools;

public class AkaoFrame extends Data {
    
    public AkaoFrame(ByteArray data) {
	super(data);
    }
    
    public void read() {
	skip(4); // AKAO, 4
	id = u16(); // 6
	length = u16(); // 8
	skip(8); // unknown, 16
	skip(length); // unknown
    }

    public int id;
    public int length;
}
