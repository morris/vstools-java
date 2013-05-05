package vstools;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

public class ARMRoom extends Data {

    public ARMRoom(ARM arm, ByteArray data) {
	super(data);
	this.arm = arm;
    }
    
    public void read() {
	header();
	graphics();
    }

    public void header() {
	u1 = u32();
	mapLength = u32();
	zoneNumber = u16();
	mapNumber = u16();
	log("room(" + u1 + " " + mapLength + " " + zoneNumber + " " + mapNumber
		+ ")");
    }

    public void graphics() {
	log("graphics");

	numVertices = u32();

	vertices = new Vector3f[numVertices];
	for (int i = 0; i < numVertices; ++i) {
	    vertices[i] = new Vector3f(s16(), s16(), s16());
	    skip(2); // zero padding
	    log(vertices[i]);
	}

	numTriangles = u32();
	triangles = new ARMRoomIndices[numTriangles];
	for (int i = 0; i < numTriangles; ++i) {
	    triangles[i] = new ARMRoomIndices(data);
	    triangles[i].read();
	}

	numQuads = u32();
	quads = new ARMRoomIndices[numQuads];
	for (int i = 0; i < numQuads; ++i) {
	    quads[i] = new ARMRoomIndices(data);
	    quads[i].read();
	}

	numFloorLines = u32();
	floorLines = new ARMRoomIndices[numFloorLines];
	for (int i = 0; i < numFloorLines; ++i) {
	    floorLines[i] = new ARMRoomIndices(data);
	    floorLines[i].read();
	    log(floorLines[i]);
	}

	numWallLines = u32();
	wallLines = new ARMRoomIndices[numWallLines];
	for (int i = 0; i < numWallLines; ++i) {
	    wallLines[i] = new ARMRoomIndices(data);
	    wallLines[i].read();
	    log(wallLines[i]);
	}

	numDoors = u32();
	doors = new ARMRoomIndices[numDoors];
	for (int i = 0; i < numDoors; ++i) {
	    doors[i] = new ARMRoomIndices(data);
	    doors[i].read();
	}
    }

    public void name() {
	name = text(0x24);
    }

    public void build(Viewer viewer) {
	buildMesh();
	buildLineMesh();

	if (viewer != null) {
	    geom = new Geometry("Map", mesh);
	    geom.scale(0.1f);
	    geom.rotate((float) Math.PI, 0, 0);
	    geom.setMaterial(viewer.flat(ColorRGBA.Blue));

	    lineGeom = new Geometry("MapLines", lineMesh);
	    lineGeom.scale(0.1f);
	    lineGeom.rotate((float) Math.PI, 0, 0);
	    lineGeom.setMaterial(viewer.wireframe(ColorRGBA.Red));
	}
    }

    public void buildMesh() {
	int nv = numTriangles * 3 + numQuads * 4;
	int ni = numTriangles * 3 + numQuads * 6;

	meshVertices = new Vector3f[nv];
	indices = new int[ni];
	normals = new Vector3f[nv];

	int iv = 0;
	int ii = 0;

	for (int i = 0; i < numTriangles; ++i) {
	    ARMRoomIndices p = triangles[i];

	    Vector3f v1 = vertices[p.p1];
	    Vector3f v2 = vertices[p.p2];
	    Vector3f v3 = vertices[p.p3];

	    meshVertices[iv + 0] = v1;
	    meshVertices[iv + 1] = v2;
	    meshVertices[iv + 2] = v3;

	    // compute normal
	    Vector3f n = v2.subtract(v1);
	    n.crossLocal(v3.subtract(v1));
	    n.normalizeLocal();
	    n.negateLocal();

	    normals[iv + 0] = n;
	    normals[iv + 1] = n;
	    normals[iv + 2] = n;

	    indices[ii + 0] = iv + 2;
	    indices[ii + 1] = iv + 1;
	    indices[ii + 2] = iv + 0;

	    iv += 3;
	    ii += 3;
	}

	for (int i = 0; i < numQuads; ++i) {
	    ARMRoomIndices p = quads[i];

	    Vector3f v1 = vertices[p.p1];
	    Vector3f v2 = vertices[p.p2];
	    Vector3f v3 = vertices[p.p3];
	    Vector3f v4 = vertices[p.p4];

	    meshVertices[iv + 0] = v1;
	    meshVertices[iv + 1] = v2;
	    meshVertices[iv + 2] = v3;
	    meshVertices[iv + 3] = v4;

	    // compute normal
	    Vector3f n = v2.subtract(v1);
	    n.crossLocal(v3.subtract(v1));
	    n.normalizeLocal();
	    n.negateLocal();

	    normals[iv + 0] = n;
	    normals[iv + 1] = n;
	    normals[iv + 2] = n;
	    normals[iv + 3] = n;

	    // 321
	    indices[ii + 0] = iv + 2;
	    indices[ii + 1] = iv + 1;
	    indices[ii + 2] = iv + 0;
	    // 432
	    indices[ii + 3] = iv + 0;
	    indices[ii + 4] = iv + 3;
	    indices[ii + 5] = iv + 2;

	    iv += 4;
	    ii += 6;
	}

	mesh = new Mesh();
	mesh.setBuffer(VertexBuffer.Type.Position, 3,
		BufferUtils.createFloatBuffer(meshVertices));
	mesh.setBuffer(VertexBuffer.Type.Index, 3, indices);
	mesh.setBuffer(VertexBuffer.Type.Normal, 3,
		BufferUtils.createFloatBuffer(normals));

	mesh.updateBound();
	mesh.updateCounts();
    }

    public void buildLineMesh() {
	int ni = numFloorLines * 2 + numWallLines * 2;

	lineIndices = new int[ni];

	int ii = 0;

	for (int i = 0; i < numFloorLines; ++i) {
	    ARMRoomIndices p = floorLines[i];

	    lineIndices[ii + 0] = p.p1;
	    lineIndices[ii + 1] = p.p2;

	    ii += 2;
	}

	for (int i = 0; i < numWallLines; ++i) {
	    ARMRoomIndices p = wallLines[i];

	    lineIndices[ii + 0] = p.p1;
	    lineIndices[ii + 1] = p.p2;

	    ii += 2;
	}

	lineMesh = new Mesh();
	lineMesh.setMode(Mesh.Mode.Lines);
	lineMesh.setBuffer(VertexBuffer.Type.Position, 3,
		BufferUtils.createFloatBuffer(vertices));
	lineMesh.setBuffer(VertexBuffer.Type.Index, 2, lineIndices);

	lineMesh.updateBound();
	lineMesh.updateCounts();
    }

    public ARM arm;

    public int u1;
    public int mapLength;
    public int zoneNumber;
    public int mapNumber;

    public int numVertices;
    public Vector3f[] vertices;

    public int numTriangles;
    public int numQuads;
    public int numFloorLines;
    public int numWallLines;
    public int numDoors;

    public ARMRoomIndices[] triangles;
    public ARMRoomIndices[] quads;
    public ARMRoomIndices[] floorLines;
    public ARMRoomIndices[] wallLines;
    public ARMRoomIndices[] doors;

    public String name;

    public Vector3f[] meshVertices;
    public Vector3f[] normals;
    public int[] indices;
    public Mesh mesh;

    public int[] lineIndices;
    public Mesh lineMesh;

    public Geometry geom;
    public Geometry lineGeom;
}
