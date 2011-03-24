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
	
	//static private final int MAX_ITERATIONS = 50;

	public static final int INITIALISED = 0;
	public static final int FINISHED = 3;
	public static final int PROGRESS = 4;
	public static final int OPT_STEP = 5;
	public static final int SMOOTH_INT = 100;
	
	private MandelbrotParams _params;
	private int _progress;
	private boolean _stopRequested;
	private int _width;
	private int _height;
	private int[] _buffer;
	private double _scale;
	private Handler _handler;
	private int _maxIterations;
	
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
		_maxIterations = (int) (50 + 10 * Math.log(_params.getZoom()));
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
				int depth = depth(re, im);
				_buffer[xy2index(col, row)] = depth;
				// optimisation - if we are at max iterations and therefore drawing black
				// lets check if we're in a big black area.
				if(depth == -1 && col < _width - OPT_STEP) {
					int rcol = col + OPT_STEP;
					double rre = x2re(rcol);
					int rdepth = depth(rre, im);
					_buffer[xy2index(rcol, row)] = rdepth;
					if(rdepth == -1) {
						for(int i = col + 1; i < col + OPT_STEP; i++) {
							_buffer[xy2index(i, row)] = -1;
						}
					}
					else {
						for(int i = col + 1; i < col + OPT_STEP; i++) {
							_buffer[xy2index(i, row)] = depth(x2re(i), im);
						}
					}
					col = col + OPT_STEP;
				}
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
	
	private int depth(double re, double im) {
		double zx = 0.0;
		double zy = 0.0;
		
		for(int i = 0; i < _maxIterations; i++) {
			double zzx = zx * zx - zy * zy;
			double zzy = zx * zy * 2;
			
			zx = zzx + re;
			zy = zzy + im;
			
			if(zx * zx + zy * zy > 4.0) {
				// normalize the depth
				return interpolate(i, zx, zy, re, im);
			}
		}
		
		return -1;
	}
	
	private int interpolate(int depth, double zx, double zy, double re, double im) {
		// do another couple of iterations to reduce error
		double zzx = zx * zx - zy * zy + re;
		double zzy = 2 * zx * zy + im;
		zx = zzx * zzx - zzy * zzy + re;
		zy = 2 * zzx * zzy + im;
		
		// this gives a smooth range
		double smooth = depth + 3 - Math.log(Math.log(Math.sqrt(zx * zx + zy * zy))) / Math.log(2);
		// normalize to SMOOTH_INT steps. So if max iterations is 50 and our SMOOTH_INT is 100, we
		// will have 5000 steps.
		return (int) (smooth * SMOOTH_INT);
	}
}
