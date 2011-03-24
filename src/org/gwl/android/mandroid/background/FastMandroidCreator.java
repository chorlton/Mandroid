/**
 * @author Gordon
 * @version 16 Mar 2011
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
public class FastMandroidCreator extends Observable implements Runnable {

	private static final String TAG = FastMandroidCreator.class.getName();
	
	static private final int MAX_ITERATIONS = 50;

	private static final int STEP = 3;
	private static final int TOLERANCE = 3;
	
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
	
	private int _widthStep;
	private int _heightStep;
	private int[][] _bufferStep;
	
	public FastMandroidCreator(Handler handler, MandelbrotParams params, int width, int height) {
		_handler = handler;
		_width = width;
		_height = height;
		_params = params;
		_buffer = new int[_width * _height];
		// round up to one greater than multiple of STEP
		_widthStep = ((_width + STEP - 2) / STEP) * STEP + 1;  
		_heightStep = ((_height + STEP - 2) / STEP) * STEP + 1;  
		_bufferStep = new int[_widthStep][];
		Log.d(TAG, "Widening to " + _widthStep + " by " + _heightStep);
		for(int i = 0; i < _widthStep; i++) {
			_bufferStep[i] = new int[_heightStep];
		}
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
		
		// We have ensured the buffer is the corrrect width and height so that STEPping hits the edges
		double re;
		double im;
		
		// Part 1: Calculate every STEPth value.
		for(int row = 0; row < _heightStep; row += STEP) {
			if(row % 50 == 0) {
				Log.d(TAG, "row: " + row);
			}
			
			im = y2im(row);
			for(int col = 0; col < _widthStep; col += STEP) {
				re = x2re(col);
				_bufferStep[col][row] = depth(re, im);
			}
			if(stopRequested()) {
				return;
			}
			setProgress(row * 100 / _height);
		}

		// Part 2: interpolate horizontally
		for(int row = 0; row < _heightStep; row += STEP) {
			if(row % 50 == 0) {
				Log.d(TAG, "row: " + row);
			}
			im = y2im(row);
			for(int col = 0; col < _widthStep - STEP; col += STEP) {
				int left = _bufferStep[col][row]; 
				int right = _bufferStep[col + STEP][row];
				if(Math.abs(left - right) <= TOLERANCE) {
					// interpolate
					for(int step = col + 1; step < col + STEP; step++) {
						_bufferStep[step][row] = left + ((right - left) * (step - col)) / (STEP - 1);
					}
				}
				else {
					// calculate
					for(int step = col + 1; step < col + STEP; step++) {
						re = x2re(step);
						_bufferStep[step][row] = depth(re, im);
					}
				}
			}
			if(stopRequested()) {
				return;
			}
			setProgress(row * 100 / _height);
		}
		
		// Part 3: interpolate vetically
		for(int col = 0; col < _widthStep; col++) {
			if(col % 50 == 0) {
				Log.d(TAG, "col: " + col);
			}
			re = x2re(col);
			for(int row = 0; row < _heightStep - STEP; row += STEP) {
				int up = _bufferStep[col][row]; 
				int down = _bufferStep[col][row + STEP];
				if(Math.abs(up - down) <= TOLERANCE) {
					// interpolate
					for(int step = row + 1; step < row + STEP; step++) {
						_bufferStep[col][step] = up + ((down - up) * (step - row)) / (STEP - 1);
					}
				}
				else {
					// calculate
					for(int step = row + 1; step < row + STEP; step++) {
						im = y2im(step);
						_bufferStep[col][step] = depth(re, im);
					}
				}
			}
			if(stopRequested()) {
				return;
			}
			setProgress(col * 100 / _height);
		}
		
		_buffer = flattenBuffer();
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
	
	private int[] flattenBuffer() {
		Log.d(TAG, "Flattening");
		int[] flat = new int[_width * _height];
		for(int row = 0; row < _height; row++) {
			for(int col = 0 ; col < _width; col++) {
				flat[row * _width + col] = _bufferStep[col][row];
			}
		}
		return flat;
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
