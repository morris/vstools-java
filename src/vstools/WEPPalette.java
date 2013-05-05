package vstools;

import java.awt.Color;

public class WEPPalette extends Data {

    public WEPPalette(ByteArray data) {
	super(data);
    }
    
    public void read(int size) {
	this.size = size;
	colors = new Color[size];
	for (int i = 0; i < size; ++i) {
	    colors[i] = color();
	}
    }

    public int size;
    public Color[] colors;
}
