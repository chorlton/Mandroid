/**
 * @author glittle
 * @version 31 Jan 2011
 */
package org.gwl.android.mandroid;

import java.util.Observable;

import org.gwl.android.mandroid.PanZoomListener.Mode;

import android.util.Log;

/**
 * Responsible for tracking the current state of panning and zooming. It should
 * not need the view dimensions and so can be used immediately. This is only
 * interested in screen coordinates and is in no way Mandelbrot specific. A
 * separate state type will track the Mandelbrot view
 */
public class PanZoomState extends Observable {

	private static final String TAG = PanZoomState.class.getName();
	
	private float _dX;
	private float _dY;
//	private float _zoom;
	private float _x;
	private float _y;
	
	private PanZoomListener.Mode _mode;
	
	/**
	 * 
	 */
	public PanZoomState() {
		_x = 0.0f;
		_y = 0.0f;
		_dX = 0.0f;
		_dY = 0.0f;
		_mode = Mode.PAN;
//		_zoom = 1.0f;
	}

	public Mode getMode() {
		return _mode;
	}
	
	public void setMode(Mode mode) {
		if(_mode != mode) {
			_mode = mode;
			setChanged();
		}
	}
	
	public float getDX() {
		return _dX;
	}
	
	public float getDY() {
		return _dY;
	}
	
//	public float getZoom() {
//		return _zoom;
//	}
	
	public float getX() {
		return _x;
	}
	
	public float getY() {
		return _y;
	}
	
	public void setDX(float dX) {
		if(_dX != dX) {
			_dX = dX;
			setChanged();
		}
	}

	public void setDY(float dY) {
		if(_dY != dY) {
			_dY = dY;
			setChanged();
		}
	}
	
//	public void setZoom(float zoom) {
//		if(_zoom != zoom) {
//			_zoom = zoom;
//			setChanged();
//		}
//	}

	public void setX(float x) {
		if(_x != x) {
			_x = x;
			setChanged();
		}
	}
	
	public void setY(float y) {
		if(_y != y) {
			_y = y;
			setChanged();
		}
	}
	
	/**
	 * Calculate the zoom factor for a given height.
	 * If we are in PAN mode then the result is always 1.0f.
	 * If zooming then it such that moving up by height/2 is a factor of 2.0f.
	 * Moving down by height/2 is a factor of 0.5f.
	 * The factor is based on total distance so it is possible to more than
	 * double the zoom by moving from centre to top right. (or indeed by moving from
	 * bottom left to top right which would end up as a factor of over 4.0f.)
	 * 
	 * @param height
	 * @return
	 */
	public float calculateZoom(int height) {
		if(_mode == Mode.PAN) {
			return 1.0f;
		}
		
		float sign = _dY == 0.0f ? 1.0f : _dY / Math.abs(_dY);
		float scale = (float) Math.sqrt(_dX * _dX + _dY * _dY) * sign / height;
		Log.d(TAG, "scale = " + scale);
		return  (float) Math.pow(2.0, -2.0 * scale);
	}
}
