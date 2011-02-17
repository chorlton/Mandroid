/**
 * @author glittle
 * @version 10 Jan 2011
 */
package org.gwl.android.mandroid;

import java.util.Observable;

import android.util.Log;

/**
 * Responsible for representing the current view of the Mandelbrot plane.
 * It maintains a view centre point and the current zoom factor. It is not
 * aware of width and height or x,y coords in the View plane. these must be
 * passed in when modifying.
 */
public class MandelbrotParams extends Observable {

	private static final String TAG = MandelbrotParams.class.getName();
	
	private double _im;
	private double _re;
	private double _zoom;
	
	public MandelbrotParams() {
		_im = 0.0;
		_re = 0.0;
		_zoom = 1.0;
	}
	
	public MandelbrotParams(double re, double im, double zoom) {
		_re = re;
		_im = im;
		_zoom = zoom;
	}
	
	public double getReal() {
		return _re;
	}
	
	public void setReal(double real) {
		_re = real;
		setChanged();
	}
	
	public double getImag() {
		return _im;
	}
	
	public void setImag(double imag) {
		_im = imag;
		setChanged();
	}
	
	public double getZoom() {
		return _zoom;
	}
	
	public void setZoom(double zoom) {
		_zoom = zoom;
		setChanged();
	}
	
	public String toString() {
		return "MP(" + _re + ", " + _im + ", " + _zoom;
	}
	
	/**
	 * Update the params with a completed PanZoom movement.
	 * A pan only changes our centre point. note if we pan by +10 to the right,
	 * then the centre point is actually moved to the left!
	 * for zoom, we have to
	 *   convert the x,y coordinate into the imaginary plane,
	 *   apply the new zoom
	 *   recalculate the new centre point
	 */
	public void update(TouchOp op, int width, int height) {
		Log.d(TAG, "update");
		double scale = 4.0 / (getZoom() * width);
		if(op instanceof Pan) {
			Pan pan = (Pan) op;
			Log.d(TAG, "Panning (" + pan.getdx() + ", " + pan.getdy() + ")");
			setReal(getReal() - pan.getdx() * scale);
			setImag(getImag() + pan.getdy() * scale);
		}
		else if(op instanceof Zoom) {
			Zoom zoom = (Zoom) op;
			Log.d(TAG, "Zooming: mousedown(" + zoom.getx() + ", " + zoom.gety() + ")");
			Log.d(TAG, "Zooming: centre(" + _re + ", " + _im +"), zoom = " + _zoom);
			
			double dre = (zoom.getx() - width / 2) * scale + _re;
			double dim = _im - (zoom.gety() - height / 2) * scale;
			Log.d(TAG, "Zooming: focus = (" + dre + ", " + dim + ")");
			
			Log.d(TAG, "zoom = " + _zoom + " * " + zoom.getZoom() + " => " + _zoom * zoom.getZoom());
			setZoom(getZoom() * zoom.getZoom());
			
			setReal(dre + (_re - dre) / zoom.getZoom());
			setImag(dim + (_im - dim) / zoom.getZoom());
		}
		
//		_zoom = zoom * state.
	}
	
//	public double x2re(int x) {
//		return _scale * (x - _width / 2.0) + _re;
//	}
	
//	public double y2im(int y) {
//		return _scale * (_height / 2.0 - y) + _im;
//	}*/
}
