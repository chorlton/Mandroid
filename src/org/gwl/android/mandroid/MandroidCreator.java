/**
 * @author glittle
 * @version 10 Jan 2011
 */
package org.gwl.android.mandroid;

import java.util.Observable;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * 
 */
public class MandroidCreator extends Observable implements Runnable {

	private static final String TAG = MandroidCreator.class.getName();
	
	static private final int MAX_ITERATIONS = 50;

	public static final int INITIALISED = 0;
	public static final int FINISHED = 3;
	
	private ViewParams _params;
	private int _progress;
	
	private int[] _buffer;
	
	public MandroidCreator(ViewParams params) {
		_params = params;
		_buffer = new int[_params.getWidth() * _params.getHeight()];
		_progress = 0;
	}
	
	private synchronized void setProgress(int pct) {
		_progress = pct;
	}
	
	public synchronized int getProgress() {
		return _progress;
	}
	
	public int[] getBuffer() throws MandroidException {
		if(getProgress() != 100) {
			throw new MandroidException("Buffer not ready!");
		}
		
		return _buffer;
	}
	
	private void enumerateObservers() {
		int count = countObservers();
		Log.d(TAG, "I have " + count + " observers");
	}
	
	public void run() {
		
		enumerateObservers();
		for(int row = 0; row < _params.getHeight(); row++) {
			Log.d(TAG, "row: " + row);
			double im = _params.y2im(row);
			for(int col = 0; col < _params.getWidth(); col++) {
				double re = _params.x2re(col);
				_buffer[xy2index(col, row)] = depth(re, im);
			}
			setProgress(row * 100 / _params.getHeight());
		}
		//int[] buffer = creator.getBuffer();
		Log.d(TAG, "done. instantiating coloriser");
		Coloriser c = new StandardColoriser();
		Log.d(TAG, "about to colorise");
		_buffer = c.colorise(_buffer);
		
		Log.d(TAG, "about to create bitmap object");
		Bitmap bmp = Bitmap.createBitmap(_buffer, _params.getWidth(), _params.getHeight(), Bitmap.Config.ARGB_8888);
		Log.d(TAG, "done. about to notify observers");
		setChanged();
		notifyObservers(bmp);
	}
	
/*	private int real2x(double re) {
		return (int) (_params.width * (re - _params.left) / _params.scale); 
	}
	
	private int imag2y(double im) {
		return (int) (_params.height * (_params.top - im) / _params.scale);
	}
	
	private double x2real(int x) {
		return ((double) x) * _params.scale / ((double) _params.width) + _params.left; 
	}
*/	
	private int xy2index(int x, int y) {
		return y * _params.getWidth() + x;
	}
	
	private int depth(double re, double im) {
		double zx = 0.0;
		double zy = 0.0;
		
		for(int i = 0; i < MAX_ITERATIONS; i++) {
			double zzx = zx * zx - zy * zy;
			double zzy = zx * zy * 2;
			
			zx = zzx + re;
			zy = zzy + im;
			
			if(zx * zx + zy * zy > 4.0) {
				return i;
			}
		}
		
		return MAX_ITERATIONS;
	}
}
