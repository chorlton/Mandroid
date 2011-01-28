/**
 * @author glittle
 * @version 10 Jan 2011
 */
package org.gwl.android.mandroid;

/**
 * 
 */
public class ViewParams {

	private double _im;
	private double _re;
	private double _scale;
	private int _width;
	private int _height;
	
	public ViewParams(int width, int height) {
		_im = 0.0;
		_re = 0.0;
		_scale = 4.0 / width;
		_width = width;
		_height = height;
	}
	
	public ViewParams(int width, int height, double re, double im, double scale) {
		_re = re;
		_im = im;
		_scale = scale;
		_width = width;
		_height = height;
	}
	
	public double getReal() {
		return _re;
	}
	
	public double getImag() {
		return _im;
	}
	
	public double getScale() {
		return _scale;
	}
	
	public int getHeight() {
		return _height;
	}
	
	public int getWidth() {
		return _width;
	}
	
	public double x2re(int x) {
		return _scale * (x - _width / 2.0) + _re;
	}
	
	public double y2im(int y) {
		return _scale * (_height / 2.0 - y) + _im;
	}
}
