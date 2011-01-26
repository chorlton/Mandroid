/**
 * @author glittle
 * @version 10 Jan 2011
 */
package org.gwl.android.mandroid;

import android.graphics.Color;

/**
 * 
 */
public class StandardColoriser implements Coloriser {

	private static final int[] PALETTE;
	
	static {
		PALETTE = new int[50];
		for(int i = 0; i < 50; i++) {
			PALETTE[i] = Color.HSVToColor(new float[] { ((float) i) * 360.0f / 50.0f, 1.0f, 1.0f });
		}
	}
	
	/* (non-Javadoc)
	 * @see org.gwl.android.mandroid.Coloriser#colorise(int[])
	 */
	@Override
	public int[] colorise(int[] depth) {
		int[] result = new int[depth.length];
		for(int i = 0; i < depth.length; i++) {
			if(depth[i] < 50) {
				result[i] = PALETTE[depth[i]];
			}
			else {
				result[i] = Color.BLACK;
			}
		}
		return result;
	}

}
