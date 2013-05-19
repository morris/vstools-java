package vstools;

import java.util.ArrayList;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class MPDMesh {

    public MPDMesh(MPDGroup group, int textureId, int clutId) {
	this.group = group;
	this.textureId = textureId;
	this.clutId = clutId;
	polygons = new ArrayList<MPDPolygon>();
    }

    public void build() {
	int nv = 0;
	int ni = 0;

	for (int i = 0; i < polygons.size(); ++i) {
	    MPDPolygon p = polygons.get(i);

	    if (p.isQuad()) {
		nv += 4;
		ni += 6;
	    } else {
		ni += 3;
		nv += 3;
	    }
	}

	vertices3 = new Vector3f[nv];
	indices = new int[ni];
	normals = new Vector3f[nv];
	uv = new Vector2f[nv];
	colors = new float[nv * 4];

	int iv = 0;
	int ii = 0;
	int colorsIndex = 0;

	for (int i = 0; i < polygons.size(); ++i) {
	    MPDPolygon p = polygons.get(i);

	    // compute normal
	    Vector3f n = new Vector3f(p.p2x, p.p2y, p.p2z);
	    n.crossLocal(p.p3x, p.p3y, p.p3z);
	    n.normalizeLocal();
	    n.negateLocal();

	    if (p.isQuad()) {
		vertices3[iv + 0] = p.p1;
		vertices3[iv + 1] = p.p2;
		vertices3[iv + 2] = p.p3;
		vertices3[iv + 3] = p.p4;

		// 321
		indices[ii + 0] = iv + 2;
		indices[ii + 1] = iv + 1;
		indices[ii + 2] = iv + 0;
		// 234
		indices[ii + 3] = iv + 1;
		indices[ii + 4] = iv + 2;
		indices[ii + 5] = iv + 3;

		normals[iv + 0] = n;
		normals[iv + 1] = n;
		normals[iv + 2] = n;
		normals[iv + 3] = n;

		// CORRECT
		uv[iv + 0] = p.uv2;
		uv[iv + 1] = p.uv3;
		uv[iv + 2] = p.uv1;
		uv[iv + 3] = p.uv4;
		
		colors[colorsIndex++] = p.r1 / 255.0f;
		colors[colorsIndex++] = p.g1 / 255.0f;
		colors[colorsIndex++] = p.b1 / 255.0f;
		colors[colorsIndex++] = 1.0f;
		
		colors[colorsIndex++] = p.r2 / 255.0f;
		colors[colorsIndex++] = p.g2 / 255.0f;
		colors[colorsIndex++] = p.b2 / 255.0f;
		colors[colorsIndex++] = 1.0f;
		
		colors[colorsIndex++] = p.r3 / 255.0f;
		colors[colorsIndex++] = p.g3 / 255.0f;
		colors[colorsIndex++] = p.b3 / 255.0f;
		colors[colorsIndex++] = 1.0f;
		
		colors[colorsIndex++] = p.r4 / 255.0f;
		colors[colorsIndex++] = p.g4 / 255.0f;
		colors[colorsIndex++] = p.b4 / 255.0f;
		colors[colorsIndex++] = 1.0f;
		

		iv += 4;
		ii += 6;
	    } else {
		vertices3[iv + 0] = p.p1;
		vertices3[iv + 1] = p.p2;
		vertices3[iv + 2] = p.p3;

		indices[ii + 0] = iv + 2;
		indices[ii + 1] = iv + 1;
		indices[ii + 2] = iv;

		normals[iv + 0] = n;
		normals[iv + 1] = n;
		normals[iv + 2] = n;

		uv[iv + 0] = p.uv2;
		uv[iv + 1] = p.uv3;
		uv[iv + 2] = p.uv1;
		
		colors[colorsIndex++] = p.r1 / 255.0f;
		colors[colorsIndex++] = p.g1 / 255.0f;
		colors[colorsIndex++] = p.b1 / 255.0f;
		colors[colorsIndex++] = 1.0f;
		
		colors[colorsIndex++] = p.r2 / 255.0f;
		colors[colorsIndex++] = p.g2 / 255.0f;
		colors[colorsIndex++] = p.b2 / 255.0f;
		colors[colorsIndex++] = 1.0f;
		
		colors[colorsIndex++] = p.r3 / 255.0f;
		colors[colorsIndex++] = p.g3 / 255.0f;
		colors[colorsIndex++] = p.b3 / 255.0f;
		colors[colorsIndex++] = 1.0f;

		ii += 3;
		iv += 3;
	    }
	}

	mesh = new Mesh();
	mesh.setBuffer(Type.Position, 3,
		BufferUtils.createFloatBuffer(vertices3));
	mesh.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indices));
	mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
	mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(uv));
	mesh.setBuffer(Type.Color, 4, BufferUtils.createFloatBuffer(colors));

	mesh.updateBound();
	mesh.updateCounts();

	geom = new Geometry("MPDMesh", mesh);
	geom.scale(0.1f);
	geom.rotate((float) Math.PI, 0, 0);

	if (group != null && group.mpd != null && group.mpd.znd != null) {
	    geom.setMaterial(group.mpd.znd.getMaterial(textureId, clutId));
	}
    }

    public MPDGroup group;
    public int textureId;
    public int clutId;

    public ArrayList<MPDPolygon> polygons;

    public Vector3f[] vertices3;
    public int[] indices;
    public Vector3f[] normals;
    public Vector2f[] uv;
    public float[] colors;

    public Mesh mesh;
    public Geometry geom;

    // final by psx GPU
    public static final int textureWidth = 256;
    public static final int textureHeight = 256;
}
