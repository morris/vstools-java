package vstools;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public class MPDPolygon extends Data {

	public MPDPolygon(MPDGroup group, ByteArray data) {
		super(data);
		this.group = group;
	}

	public void readTriangle() {
		read(false);
	}

	public void readQuad() {
		read(true);
	}

	private void read(boolean quad) {
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
		u2 = u8();
		v2 = u8();

		clutId = u16();

		u3 = u8();
		v3 = u8();

		textureId = s16();

		p1 = new Vector3f(p1x, p1y, p1z);
		p2 = new Vector3f(p2x * group.scale + p1x, p2y * group.scale + p1y, p2z
				* group.scale + p1z);
		p3 = new Vector3f(p3x * group.scale + p1x, p3y * group.scale + p1y, p3z
				* group.scale + p1z);

		uv1 = Util.abs2uv(u1, v1, 256, 256);
		uv2 = Util.abs2uv(u2, v2, 256, 256);
		uv3 = Util.abs2uv(u3, v3, 256, 256);

		if (quad) {
			p4x = s8();
			p4y = s8();
			p4z = s8();

			u4 = u8();

			r4 = u8();
			g4 = u8();
			b4 = u8();

			v4 = u8();

			p4 = new Vector3f(p4x * group.scale + p1x, p4y * group.scale + p1y,
					p4z * group.scale + p1z);

			uv4 = Util.abs2uv(u4, v4, 256, 256);
		} else {

		}

		// log("  polygon");
		// log(textureId + " -> " + clutId);
		// assert t2 == 56;
		// log("textureId: " + ;
		// log("    type: " + type + " / " + quad);
		// log("    rgb1: " + r1 + " " + g1 + " " + b1);
		// log("    rgb2: " + r2 + " " + g2 + " " + b2);
		// log("    rgb3: " + r3 + " " + g3 + " " + b3);

	}

	public String toString() {
		String s = p1 + " " + uv1 + "\n" + p2 + " " + uv2 + "\n" + p3 + " "
				+ uv3;
		if (quad) {
			s += "\n" + p4 + " " + uv4;
		}
		return s;
	}

	public boolean isQuad() {
		return quad;
	}

	public MPDGroup group;

	private boolean quad;

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

	public int clutId;
	public int t2;

	public int u2;
	public int u3;

	public int textureId;

	// quad
	public int p4x;
	public int p4y;
	public int p4z;

	public int u4;

	public int r4;
	public int g4;
	public int b4;

	public int v4;

	Vector3f p1;
	Vector3f p2;
	Vector3f p3;
	Vector3f p4;

	Vector2f uv1;
	Vector2f uv2;
	Vector2f uv3;
	Vector2f uv4;
}
