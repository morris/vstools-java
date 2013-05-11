package vstools;

import java.io.File;
import java.io.IOException;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.Skeleton;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.SkeletonDebugger;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;

public class Viewer extends SimpleApplication implements ActionListener {

    @Override
    public void simpleInitApp() {

	// register "assets" subdirectory
	assetManager.registerLocator("assets", FileLocator.class);

	// unit cube
	box = new Box(0.5f, 0.5f, 0.5f);

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
	if (node != null) {
	    node.updateModelBound();
	}
    }

    public void prevAnim() {
	if (shp != null && seq != null) {
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
	if (name.equals("prevAnim") && !keyPressed) {
	    prevAnim();
	} else if (name.equals("nextAnim") && !keyPressed) {
	    nextAnim();
	} else if (name.equals("debugSkeleton") && !keyPressed) {
	    debugSkeleton(skeleton, node);
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
	    wep.build(unshaded());

	    debugSkeleton(wep.skeleton, wep.getNode());

	    // finally, attach node to scene
	    node = wep.getNode();
	    rootNode.attachChild(node);
	} catch (Exception e) {
	    e.printStackTrace();
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
	    shp.build(unshaded());

	    // scene creation
	    node = shp.node;
	    skeleton = shp.skeleton;
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

    /**
     * This only loads the SHP and SEQ. Weapons are not displayed (yet).
     * 
     * @param file
     */
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
	    shp.build(unshaded());

	    seq = zud.com;
	    if (seq == null) {
		seq = zud.bt;
	    }

	    if (seq != null) {
		shp.setSEQ(seq);
		channel = shp.control.createChannel();
		channel.setAnim("Animation0");
	    }

	    // debug skeleton
	    debugSkeleton(shp.skeleton, shp.node);

	    node = zud.getNode();
	    rootNode.attachChild(node);
	} catch (Exception e) {
	    e.printStackTrace();
	}
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

    public void openZND(File file) {
	// TODO
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

    public void debugSkeleton(Skeleton skeleton, Node node) {
	if (skeleton != null && node != null) {
	    skeletonDebug = new SkeletonDebugger("skeleton", skeleton);
	    Material mat = unshaded();
	    mat.setColor("Color", ColorRGBA.Green);
	    mat.getAdditionalRenderState().setDepthTest(false);
	    skeletonDebug.setMaterial(mat);
	    node.attachChild(skeletonDebug);
	}
    }

    public Material wireframe(ColorRGBA c) {
	Material mat = unshaded();
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

    public Material unshaded() {
	return new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
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

    public Box getBox() {
	return box;
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
    private Skeleton skeleton; // skeleton of current node
    private SkeletonDebugger skeletonDebug;
}
