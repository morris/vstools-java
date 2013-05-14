package vstools;

import java.io.File;
import java.io.IOException;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.Skeleton;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

public class Viewer extends App {

    protected void initKeys() {
	inputManager.addMapping("prevAnim", new KeyTrigger(KeyInput.KEY_LEFT));
	inputManager.addMapping("nextAnim", new KeyTrigger(KeyInput.KEY_RIGHT));

	inputManager.addListener(this, new String[] { "prevAnim", "nextAnim" });
    }

    public void update(float dt) {
	super.update(dt);
	
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
	    mpd = new MPD(Util.read(file));
	    mpd.read(znd);
	    mpd.build();
	    if(znd == null) {
		mpd.setMaterial(flat(ColorRGBA.Orange));
	    }
	    node = mpd.node;
	    //node.setMaterial(wireframe(ColorRGBA.Red));
	    rootNode.attachChild(node);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void openZND(File file) {
	try {
	    znd = new ZND(Util.read(file));
	    znd.app = this;
	    znd.read();
	    
	    //znd.frameBuffer.build(this);
	    //znd.frameBuffer.node.setMaterial(znd.getMaterial(11, 14457));
	    rootNode.attachChild(znd.frameBuffer.node);
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

    private AnimChannel channel;
    private int animIndex = 0;

    private SHP shp; // current shp
    private SEQ seq; // current seq
    private Node node; // current node in viewer
    private Skeleton skeleton; // skeleton of current node
    
    private ZND znd; // current znd
    private MPD mpd; // current mpd
}
