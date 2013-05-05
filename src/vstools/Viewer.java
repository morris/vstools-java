package vstools;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.Skeleton;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;

import com.jme3.scene.debug.SkeletonDebugger;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;

public class Viewer extends SimpleApplication implements ActionListener {

    @Override
    public void simpleInitApp() {

	// register "assets" subdirectory
	assetManager.registerLocator("assets", FileLocator.class);

	// unit box
	box = new Box(Vector3f.ZERO, 1, 1, 1);

	initFlyCam();

	initKeys();

	initStuff();
    }

    private void initFlyCam() {
	// setup flycam and light
	// flycam is enabled by default
	flyCam.setMoveSpeed(100);
	flyCamLight = new SpotLight();
	flyCamLight.setColor(ColorRGBA.White);
	flyCamLight.setSpotRange(1000);
	flyCamLight.setSpotInnerAngle(80f * FastMath.DEG_TO_RAD);
	flyCamLight.setSpotOuterAngle(90f * FastMath.DEG_TO_RAD);
	rootNode.addLight(flyCamLight);
    }

    private void initStuff() {
	// debug box for testing
	Geometry geom = new Geometry("Box", getBox());
	geom.setLocalScale(1, 1, 1);
	geom.setMaterial(wireframe(ColorRGBA.Green));
	getRootNode().attachChild(geom);
    }

    private void initKeys() {
	inputManager.addMapping("prevAnim", new KeyTrigger(KeyInput.KEY_LEFT));
	inputManager.addMapping("nextAnim", new KeyTrigger(KeyInput.KEY_RIGHT));

	inputManager.addListener(this, new String[] { "prevAnim", "nextAnim" });
    }

    @Override
    public void simpleUpdate(float tpf) {
	// deterministic game loop
	acc += tpf;

	while (acc >= dt) {
	    update(dt);
	    acc -= dt;
	}
    }

    public void update(float dt) {
	if (flyCamLight != null) {
	    flyCamLight.setPosition(cam.getLocation());
	    flyCamLight.setDirection(cam.getDirection());
	}
    }

    public void prevAnim() {
	if (shp != null && seq != null) {
	    System.out.println("foo");
	    animIndex = (animIndex - 1 + seq.animations.length)
		    % seq.animations.length;
	    channel.setAnim("Animation" + animIndex, 0.5f);
	}
    }

    public void nextAnim() {
	if (shp != null && seq != null) {
	    animIndex = (animIndex + 1) % seq.animations.length;
	    channel.setAnim("Animation" + animIndex, 0.5f);
	}
    }

    public void onAction(String name, boolean keyPressed, float tpf) {
	if(name.equals("prevAnim") && !keyPressed) {
	    prevAnim();
	} else if(name.equals("nextAnim") && !keyPressed) {
	    nextAnim();
	}
    }

    public void openSHP(File file) {
	// remove current node
	if (node != null) {
	    node.removeFromParent();
	}
	
	// unset current seq
	seq = null;
	animIndex = 0;

	try {
	    shp = new SHP(Util.read(file));
	    shp.read();

	    shp.buildSkeleton();
	    shp.buildMesh();
	    shp.buildNode();
	    shp.buildMaterial(new Material(assetManager,
		    "Common/MatDefs/Misc/Unshaded.j3md"));

	    // scene creation
	    node = shp.node;
	    rootNode.attachChild(node);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void openSEQ(File file) {
	try {
	    seq = new SEQ(shp, Util.read(file));
	    seq.read();
	    shp.setSEQ(seq);

	    animIndex = 0;
	    channel = shp.control.createChannel();
	    channel.setAnim("Animation0");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void openARM(File file) {
	// remove current node
	if (node != null) {
	    node.removeFromParent();
	}

	try {
	    ARM arm = new ARM(Util.read(file));

	    arm.read();
	    arm.build(this);
	    node = arm.node;
	    rootNode.attachChild(node);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void openZUD(File file) {
	// remove current node
	if (node != null) {
	    node.removeFromParent();
	}

	try {
	    ZUD zud = null;

	    zud = new ZUD(Util.read(file));
	    zud.read();

	    shp = zud.shp;
	    shp.buildSkeleton();
	    shp.buildMesh();
	    shp.buildNode();
	    shp.buildMaterial(new Material(assetManager,
		    "Common/MatDefs/Misc/Unshaded.j3md"));

	    seq = zud.com;
	    if (seq == null) {
		seq = zud.bt;
	    }

	    shp.setSEQ(seq);

	    channel = shp.control.createChannel();
	    channel.setAnim("Animation0");

	    // debug skeleton
	    debugSkeleton(shp.skeleton, shp.node);

	    node = zud.getNode();
	    rootNode.attachChild(node);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void openWEP(File file) {
	// remove current node
	if (node != null) {
	    node.removeFromParent();
	}

	try {
	    WEP wep = null;

	    wep = new WEP(Util.read(file));
	    wep.read();
	    wep.build(0);

	    // wep.buildSkeleton();
	    wep.buildMesh();

	    Mesh mesh = wep.mesh;
	    Skeleton skeleton = wep.skeleton;

	    // scene creation
	    node = new Node();

	    if (mesh != null) {
		System.out.println("here");
		// create and attach node
		Geometry meshGeom = new Geometry("Mesh", mesh);

		// material
		Material mat = buildTexture(wep.textureMap.buffer,
			wep.textureMap.width, wep.textureMap.height);
		// mat = wireframe(ColorRGBA.Blue);

		meshGeom.setMaterial(mat);
		mesh.updateBound();

		node.attachChild(meshGeom);
	    }

	    node.scale(0.1f);
	    node.rotate(FastMath.PI, 0, 0);
	    node.setCullHint(CullHint.Never);

	    // debug skeleton
	    debugSkeleton(skeleton, node);

	    // finally, attach node to scene
	    rootNode.attachChild(node);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void openZND(File file) {
	// TODO
    }

    public void openMPD(File file) {
	// remove current node
	if (node != null) {
	    node.removeFromParent();
	}

	try {
	    MPD mpd = new MPD(Util.read(file));
	    mpd.read();
	    mpd.build(this);
	    node = mpd.node;
	    rootNode.attachChild(node);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public Material buildTexture(byte[] buffer, int width, int height) {
	Material mat = new Material(assetManager,
		"Common/MatDefs/Misc/Unshaded.j3md");
	mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

	ByteBuffer bb = BufferUtils.createByteBuffer(buffer);

	Image image = new Image(Image.Format.ABGR8, width, height, bb);

	Texture2D texture = new Texture2D(image);
	texture.setImage(image);
	texture.setMagFilter(Texture2D.MagFilter.Nearest);
	mat.setTexture("ColorMap", texture);

	return mat;
    }

    public void debugSkeleton(Skeleton skeleton, Node node) {
	if (skeleton != null && node != null) {
	    SkeletonDebugger skeletonDebug = new SkeletonDebugger("skeleton",
		    skeleton);
	    Material mat = new Material(assetManager,
		    "Common/MatDefs/Misc/Unshaded.j3md");
	    mat.setColor("Color", ColorRGBA.Green);
	    mat.getAdditionalRenderState().setDepthTest(false);
	    skeletonDebug.setMaterial(mat);
	    node.attachChild(skeletonDebug);
	}
    }

    public Material wireframe(ColorRGBA c) {
	Material mat = new Material(assetManager,
		"Common/MatDefs/Misc/Unshaded.j3md");
	mat.getAdditionalRenderState().setWireframe(true);
	mat.setColor("Color", c);
	return mat;
    }

    public Material debug() {
	Material mat = new Material(assetManager,
		"Common/MatDefs/Light/Lighting.j3md");

	Texture texture = assetManager.loadTexture("Textures/debug.png");
	texture.setMagFilter(Texture2D.MagFilter.Nearest);
	mat.setTexture("DiffuseMap", texture);
	return mat;
    }

    public Box getBox() {
	return box;
    }

    public Material flat(ColorRGBA c) {
	Material mat = new Material(assetManager,
		"Common/MatDefs/Light/Lighting.j3md");

	mat.setColor("Diffuse", c);
	mat.setColor("Ambient", c);
	mat.setColor("Specular", c);
	mat.setBoolean("UseMaterialColors", true);
	return mat;
    }

    public static void mainUnused(String[] args) {
	Viewer viewer = new Viewer();
	viewer.start();
    }

    private Box box;

    private final float dt = 0.001f;
    private float acc = 0.0f;

    private SpotLight flyCamLight;

    private AnimChannel channel;
    private int animIndex = 0;

    private SHP shp; // current shp
    private SEQ seq; // current seq
    private Node node; // current node in viewer
}
