/**
 * @author glittle
 * @version 10 Jan 2011
 */
package org.gwl.android.mandroid.background;

import java.util.Observable;

import org.gwl.android.mandroid.Coloriser;
import org.gwl.android.mandroid.MandroidException;
import org.gwl.android.mandroid.StandardColoriser;
import org.gwl.android.mandroid.MandelbrotParams;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 
 */
public class MandroidCreator extends Observable implements Runnable {

	private static final String TAG = MandroidCreator.class.getName();
	
	static private final int MAX_ITERATIONS = 50;

	public static final int INITIALISED = 0;
	public static final int FINISHED = 3;
	public static final int PROGRESS = 4;
	
	private MandelbrotParams _params;
	private int _progress;
	private boolean _stopRequested;
	private int _width;
	private int _height;
	private int[] _buffer;
	private double _scale;
	private Handler _handler;
	
	public MandroidCreator(Handler handler, MandelbrotParams params, int width, int height) {
		_handler = handler;
		_width = width;
		_height = height;
		_params = params;
		_buffer = new int[_width * _height];
		_progress = 0;
		_stopRequested = false;
		// scale records the resolution of 1 pixel in the complex plane
		_scale = 4.0 / (_params.getZoom() * _width);
		Log.d(TAG, "Creating mandelbrot for view " + _params.toString());
		Log.d(TAG, "scale is " + _scale);
	}
	
	public synchronized void requestStop() {
		_stopRequested = true;
	}
	
	private synchronized boolean stopRequested() {
		return _stopRequested;
	}
	
	private synchronized void setProgress(int pct) {
		_progress = pct;
		if(pct % 10 == 0) {
			Message msg = Message.obtain(_handler, PROGRESS, pct, 0);
			msg.sendToTarget();
		}
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
		
	public void run() {
		
		for(int row = 0; row < _height; row++) {
			if(row % 50 == 0) {
				Log.d(TAG, "row: " + row);
			}
			
			double im = y2im(row);
			for(int col = 0; col < _width; col++) {
				double re = x2re(col);
				_buffer[xy2index(col, row)] = depth(re, im);
			}
			if(stopRequested()) {
				return;
			}
			setProgress(row * 100 / _height);
		}
		//int[] buffer = creator.getBuffer();
		Log.d(TAG, "done. instantiating coloriser");
		Coloriser c = new StandardColoriser();
		Log.d(TAG, "about to colorise");
		_buffer = c.colorise(_buffer);
		
		Log.d(TAG, "about to create bitmap object");
		Bitmap bmp = Bitmap.createBitmap(_buffer, _width, _height, Bitmap.Config.ARGB_8888);
		Log.d(TAG, "done. about to notify observers");
		setChanged();
		notifyObservers(bmp);
	}
	
	private int xy2index(int col, int row) {
		return row * _width + col;
	}
	
	private double x2re(int col) {
		return ((col - _width / 2) * _scale) + _params.getReal(); 
	}
	
	private double y2im(int row) {
		return ((_height / 2 - row) * _scale) + _params.getImag();
	}
	
/*	private int real2x(double re) {
		return (int) (_params.width * (re - _params.left) / _params.scale); 
	}
	
	private int imag2y(double im) {
		return (int) (_params.height * (_params.top - im) / _params.scale);
	}
	
	private double x2real(int x) {
		return ((double) x) * _params.scale / ((double) _params.width) + _params.left; 
	}*/

//	private int xy2index(int x, int y) {
//		return y * _params.getWidth() + x;
//	}
	
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
