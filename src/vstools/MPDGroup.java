package vstools;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public class MPDGroup extends Data {

    public MPDGroup(MPD mpd, ByteArray data) {
	super(data);
	this.mpd = mpd;
    }
    
    public void read() {
	header();
	data();
    }

    public void header() {
	header = new int[64];
	for (int i = 0; i < 64; ++i) {
	    header[i] = u8();
	}
	log(hex(header));
	
	// the header is not well understood
	// it seems that the bits in the second byte are flag bits
	// the following fixes the scaling issues in maps 001 and 002
	if((header[1] & 0x08) > 0) {
	    scale = 1;
	} else {
	    scale = 8; // default?
	}
    }

    public void diff(MPDGroup g) {
	for (int i = 0; i < 64; ++i) {
	    if (header[i] != g.header[i]) {
		log("diff at " + i + ": " + header[i] + " != " + g.header[i]);
	    }
	}
    }

    public void data() {
	numPoly3gts = u32();
	numPoly4gts = u32();
	numPoly = numPoly3gts + numPoly4gts;
	log("numPoly: " + numPoly);

	polygons = new MPDPolygon[numPoly];
	for (int i = 0; i < numPoly3gts; ++i) {
	    polygons[i] = new MPDPolygon(data);
	    polygons[i].read(false);
	}
	for (int i = numPoly3gts; i < numPoly; ++i) {
	    polygons[i] = new MPDPolygon(data);
	    polygons[i].read(true);
	}
    }

    public void build() {
	int nv = 0;
	int ni = 0;

	for (int i = 0; i < polygons.length; ++i) {
	    MPDPolygon p = polygons[i];

	    if (p.quad) {
		nv += 4;
		ni += 6;
	    } else {
		ni += 3;
		nv += 3;
	    }
	}

	vertices3 = new Vector3f[nv];
	uv = new Vector2f[nv];
	indices = new int[ni];
	normals = new Vector3f[nv];

	int iv = 0;
	int ii = 0;

	for (int i = 0; i < polygons.length; ++i) {
	    MPDPolygon p = polygons[i];

	    // compute normal
	    Vector3f n = new Vector3f(p.p2x, p.p2y, p.p2z);
	    n.crossLocal(p.p3x, p.p3y, p.p3z);
	    n.normalizeLocal();
	    n.negateLocal();

	    if (p.quad) {
		vertices3[iv] = new Vector3f(p.p1x, p.p1y, p.p1z);

		vertices3[iv + 1] = new Vector3f(p.p2x, p.p2y, p.p2z);
		vertices3[iv + 1].multLocal(scale);
		vertices3[iv + 1].addLocal(vertices3[iv]);

		vertices3[iv + 2] = new Vector3f(p.p3x, p.p3y, p.p3z);
		vertices3[iv + 2].multLocal(scale);
		vertices3[iv + 2].addLocal(vertices3[iv]);

		vertices3[iv + 3] = new Vector3f(p.p4x, p.p4y, p.p4z);
		vertices3[iv + 3].multLocal(scale);
		vertices3[iv + 3].addLocal(vertices3[iv]);

		normals[iv] = n;
		normals[iv + 1] = n;
		normals[iv + 2] = n;
		normals[iv + 3] = n;

		uv[iv] = abs2uv(p.u1, p.v1, textureWidth, textureHeight);
		uv[iv + 1] = abs2uv(p.u2, p.v2, textureWidth, textureHeight);
		uv[iv + 2] = abs2uv(p.u3, p.v3, textureWidth, textureHeight);
		uv[iv + 3] = abs2uv(p.u4, p.v4, textureWidth, textureHeight);

		// 321
		indices[ii] = iv + 2;
		indices[ii + 1] = iv + 1;
		indices[ii + 2] = iv;
		// 234
		indices[ii + 3] = iv + 1;
		indices[ii + 4] = iv + 2;
		indices[ii + 5] = iv + 3;

		iv += 4;
		ii += 6;
	    } else {
		vertices3[iv] = new Vector3f(p.p1x, p.p1y, p.p1z);

		vertices3[iv + 1] = new Vector3f(p.p2x, p.p2y, p.p2z);
		vertices3[iv + 1].multLocal(scale);
		vertices3[iv + 1].addLocal(vertices3[iv]);

		vertices3[iv + 2] = new Vector3f(p.p3x, p.p3y, p.p3z);
		vertices3[iv + 2].multLocal(scale);
		vertices3[iv + 2].addLocal(vertices3[iv]);

		normals[iv] = n;
		normals[iv + 1] = n;
		normals[iv + 2] = n;

		uv[iv] = abs2uv(p.u1, p.v1, textureWidth, textureHeight);
		uv[iv + 1] = abs2uv(p.u2, p.v2, textureWidth, textureHeight);
		uv[iv + 2] = abs2uv(p.u3, p.v3, textureWidth, textureHeight);

		indices[ii] = iv + 2;
		indices[ii + 1] = iv + 1;
		indices[ii + 2] = iv;

		ii += 3;
		iv += 3;
	    }
	}
    }
    
    public MPD mpd;

    public int[] header;

    public int numPoly3gts;
    public int numPoly4gts;
    public int numPoly;
    public int scale = 8; // TODO remove default

    public MPDPolygon[] polygons;

    public Vector3f[] vertices3;
    public Vector3f[] normals;
    public int[] indices;
    public Vector2f[] uv;

    public int textureWidth = 256; // TODO common default, overwrite
    public int textureHeight = 256; // TODO common default, overwrite
}
