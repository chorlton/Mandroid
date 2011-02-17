/**
 * @author Gordon
 * @version 9 Feb 2011
 */
package org.gwl.android.mandroid;

import android.util.Log;

/**
 * 
 */
public class Pan extends TouchOp {

	private static final String TAG = Pan.class.getName();

	private double _x;
	private double _y;
	private double _dx;
	private double _dy;
	
	public Pan(double x, double y) {
		Log.d(TAG, "Panning from " + x + ", " + y);
		_x = x; _y = y;
	}
	
	@Override
	public void track(double x, double y) {
		_dx = x - _x;
		_dy = y - _y;
	}
	
	public double getdx() {
		return _dx;
	}
	
	public double getdy() {
		return _dy;
	}
	
	@Override
	public void trackView(MandroidView view) {
		view.pan(this);
	}

	@Override
	public void finish(MandroidView view) {
		view.finish(this);
	}

}
