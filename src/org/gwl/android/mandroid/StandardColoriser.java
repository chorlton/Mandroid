/**
 * @author glittle
 * @version 10 Jan 2011
 */
package org.gwl.android.mandroid;

import org.gwl.android.mandroid.background.MandroidCreator;

import android.graphics.Color;
import android.util.Log;

/**
 * 
 */
public class StandardColoriser implements Coloriser {

	private static final String TAG = StandardColoriser.class.getName();
	
	private static final int[] PALETTE;
	private static final int NUM_COLOURS = 50 * MandroidCreator.SMOOTH_INT;
	
	static {
		PALETTE = new int[NUM_COLOURS];
		for(int i = 0; i < NUM_COLOURS; i++) {
			PALETTE[i] = Color.HSVToColor(new float[] { ((float) i) * 360.0f / ((float) NUM_COLOURS), 1.0f, 1.0f });
		}
	}
	
	/* (non-Javadoc)
	 * @see org.gwl.android.mandroid.Coloriser#colorise(int[])
	 */
	@Override
	public int[] colorise(int[] depth) {
		int[] result = new int[depth.length];
		for(int i = 0; i < depth.length; i++) {
			try {
				if(depth[i] == -1) {
					result[i] = Color.BLACK;
				}
				else {
					result[i] = PALETTE[depth[i] % NUM_COLOURS];
				}
			}
			catch(ArrayIndexOutOfBoundsException oob) {
				// just don't colour if I've been a mental.
			}
		}
		return result;
	}

}
