package vstools;

import com.jme3.math.Vector3f;

public class MPDPolygon extends Data {

    public MPDPolygon(ByteArray data) {
	super(data);
    }

    public void read(boolean quad) {
	this.quad = quad;

	// two bytes per axis
	p1x = s16();
	p1y = s16();
	p1z = s16();

	// p2, p3, p4 are stored as offset vectors from p1
	// one byte per axis
	p2x = s8();
	p2y = s8();
	p2z = s8();

	p3x = s8();
	p3y = s8();
	p3z = s8();

	r1 = u8();
	g1 = u8();
	b1 = u8();

	// type
	// 52, 54 triangles
	// 60, 62 quads
	type = u8();

	r2 = u8();
	g2 = u8();
	b2 = u8();

	u1 = u8();

	r3 = u8();
	g3 = u8();
	b3 = u8();

	v1 = u8();
	v2 = u8();
	v3 = u8();

	texelMask = s16();

	u2 = u8();
	u3 = u8();

	idTexture = s16();

	if (quad) {
	    p4x = s8();
	    p4y = s8();
	    p4z = s8();

	    u4 = u8();

	    r4 = s8();
	    g4 = s8();
	    b4 = s8();

	    v4 = u8();
	} else {
	    
	}

	// log("  polygon");
	log("    texelMask: " + texelMask);
	log("    idTexture: " + idTexture);
	// log("    type: " + type + " / " + quad);
	// log("    rgb1: " + r1 + " " + g1 + " " + b1);
	// log("    rgb2: " + r2 + " " + g2 + " " + b2);
	// log("    rgb3: " + r3 + " " + g3 + " " + b3);

    }

    public boolean quad;

    // triangle
    public int p1x;
    public int p1y;
    public int p1z;
    public int p2x;
    public int p2y;
    public int p2z;
    public int p3x;
    public int p3y;
    public int p3z;
    public int r1;
    public int g1;
    public int b1;
    public int type;
    public int r2;
    public int g2;
    public int b2;
    public int u1;
    public int r3;
    public int g3;
    public int b3;
    public int v1;
    public int v2;
    public int v3;
    public int texelMask;
    public int u2;
    public int u3;
    public int idTexture;

    // quad
    public int p4x;
    public int p4y;
    public int p4z;
    public int u4;
    public int r4;
    public int g4;
    public int b4;
    public int v4;

    Vector3f normal;
}
