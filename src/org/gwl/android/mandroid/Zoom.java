/**
 * @author Gordon
 * @version 9 Feb 2011
 */
package org.gwl.android.mandroid;

import android.util.Log;

/**
 * 
 */
public class Zoom extends TouchOp {

	private static final String TAG = Zoom.class.getName();
	private static final int FACTOR = 10;
	
	private double _x;
	private double _y;
	private double _zoom;
	private int _height;
	
	public Zoom(double x, double y, int height) {
		Log.d(TAG, "Zooming from " + x + ", " + y);
		_x = x; _y = y;
		_height = height;
	}
	
	@Override
	public void track(double x, double y) {
		double zoomin;
		if((y - _y) > 0.0) {
			zoomin = -1.0;
		}
		else {
			zoomin = 1.0;
		}
		double scale = FACTOR * Math.sqrt((x - _x) * (x - _x) + (y - _y) * (y - _y)) * zoomin / _height;
		_zoom = Math.pow(2.0, scale);
		
		Log.d(TAG, "zoom = " + _zoom);
	}
	
	@Override
	public void trackView(MandroidView view) {
		view.zoom(this);
	}

	@Override
	public void finish(MandroidView view) {
		view.finish(this);
	}

	public double getx() {
		return _x;
	}
	
	public double gety() {
		return _y;
	}
	
	public double getZoom() {
		return _zoom;
	}
	
}
