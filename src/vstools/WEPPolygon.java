package vstools;

public class WEPPolygon extends Data {


    public WEPPolygon(ByteArray data) {
	super(data);
    }

    public void read() {
	type = byte_();
	if (type == 0x2C) {
	    // quad
	} else if (type == 0x24) {
	    // triangle
	} else {
	    log("unknown poly: " + hex(type));
	}
	size = byte_();
	info = byte_();
	skip(1); // always 0

	vertex1 = (short) (u16() / 4);
	vertex2 = (short) (u16()  / 4);
	vertex3 = (short) (u16()  / 4);
	if (type == 0x2C) {
	    vertex4 = (short) (u16() / 4);
	}
	u1 = u8();
	v1 = u8();
	u2 = u8();
	v2 = u8();
	u3 = u8();
	v3 = u8();
	if (type == 0x2C) {
	    u4 = u8();
	    v4 = u8();

	    //log("  quad(" + vertex1 + "," + vertex2 + "," + vertex3 + "," + vertex4 + ")");
	    //log("  quad.uv(" + u1 + "," + v1 + "/" + u2 + "," + v2 + "/" + u3 + "," + v3 + "/" + u4 + "," + v4 + ")");
	} else {
	    //log("  triangle(" + vertex1 + "," + vertex2 + "," + vertex3 + ")");
	    //log("  triangle.uv(" + u1 + "," + v1 + "/" + u2 + "," + v2 + "/" + u3 + "," + v3 + ")");
	}
    }

    public boolean quad() {
	return type == 0x2C;
    }
    
    public boolean doubl() {
	return info == 5;
    }

    public byte type;
    public byte size;
    public byte info;
    public int vertex1;
    public int vertex2;
    public int vertex3;
    public int vertex4;
    public int u1;
    public int v1;
    public int u2;
    public int v2;
    public int u3;
    public int v3;
    public int u4;
    public int v4;

}
