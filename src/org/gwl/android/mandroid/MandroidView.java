/**
 * @author glittle
 * @version 10 Jan 2011
 */
package org.gwl.android.mandroid;

import java.util.Observable;
import java.util.Observer;

import org.gwl.android.mandroid.background.MandroidCreator;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 
 */
public class MandroidView extends View implements Observer {
	
	private static final String TAG = MandroidView.class.getName();
	
	private Paint _textPaint = null;

//	private int[] _buffer = null;
//	private PanZoomState _panZoomState;
//	private boolean _dirty = true;
//	private MandroidCreator _creator = null;
	
	private Bitmap _bitmap;
	private Rect _last; // stores the view rectangle from the last touch event
	private Rect _dst;
	private MandelbrotParams _params;
	private MandroidCreator _creator = null;
	private ProgressDialog _progress = null;
	
	private Handler _creationHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.d(TAG, "bitmapHandler.handleMessage()");
			switch(msg.what) {
			case MandroidCreator.FINISHED:
				_progress.dismiss();
				setBitmap((Bitmap) msg.obj);
				break;
			case MandroidCreator.PROGRESS:
				_progress.setProgress(msg.arg1);
				break;
			}
		}
	};
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object data) {
		// data should be a bitmap
		if(data != null) {
			Bitmap bitmap = (Bitmap) data;
			Message msg = Message.obtain(_creationHandler, MandroidCreator.FINISHED, bitmap);
			_creationHandler.sendMessage(msg);
		}		
	}


	/**
	 * @param context
	 */
	public MandroidView(Context context) {
		super(context);

		_params = new MandelbrotParams();
		_textPaint = initaliseTextPaint();
		
		setFocusable(true);
        setFocusableInTouchMode(true);
	}
	
	public MandroidView(Context context, AttributeSet attrs) {
		super(context, attrs);

		_params = new MandelbrotParams();
		_textPaint = initaliseTextPaint();
		
		setFocusable(true);
        setFocusableInTouchMode(true);
		
	}

	public void zoom(Zoom zoom) {
		Log.d(TAG, "zoom");

		// The current zoom is usually 1 (it's the zoom on the bitmap, not the Mandelbrot plane).
		// However, we might get two zoom actions before the bitmap has time to recalculate in which case
		// it needs worked out.
		double currentZoom = ((double) _last.width()) / ((double) _bitmap.getWidth());
		
		// calculate the point on the original bitmap which our start point represents.
		double x = (zoom.getx() - _last.left) / currentZoom;
		double y = (zoom.gety() - _last.top) / currentZoom;
		Log.d(TAG, "x = " + x + ", y = " + y);

		double newZoom = currentZoom * zoom.getZoom();
		
		_dst.left = (int) (zoom.getx() - x * newZoom);
		_dst.top = (int) (zoom.gety() - y * newZoom);
		_dst.right = (int) (_dst.left + _bitmap.getWidth() * newZoom);
		_dst.bottom = (int) (_dst.top + _bitmap.getHeight() * newZoom);
		Log.d(TAG, "last: " + _last.flattenToString());
		Log.d(TAG, "dst: " + _dst.flattenToString());
		
		invalidate();
	}
	
	public void pan(Pan pan) {
		Log.d(TAG, "pan");
		
		// The current zoom is usually 1 (it's the zoom on the bitmap, not the Mandelbrot plane).
		// However, we might get two zoom actions before the bitmap has time to recalculate in which case
		// it needs worked out.
		double currentZoom = ((double) _last.width()) / ((double) _bitmap.getWidth());

		_dst.left = (int) (_last.left + pan.getdx());
		_dst.top = (int) (_last.top + pan.getdy());
		_dst.right = (int) (_dst.left + _bitmap.getWidth() * currentZoom);
		_dst.bottom = (int) (_dst.top + _bitmap.getHeight() * currentZoom);
		Log.d(TAG, "last: " + _last.flattenToString());
		Log.d(TAG, "dst: " + _dst.flattenToString());
		
		invalidate();

	}
	
	public void finish(TouchOp op) {
		_params.update(op, getWidth(), getHeight());
		generateBitmap(getWidth(), getHeight());
	}
		
//	private synchronized boolean isDirty() {
//		return _dirty;
//	}
	
//	private synchronized void setDirty(boolean dirty) {
//		_dirty = dirty;
//	}
	
	public synchronized void setBitmap(Bitmap bitmap) {
		_bitmap = bitmap;
		_last = new Rect(0, 0, _bitmap.getWidth(), _bitmap.getHeight());
		_dst = new Rect(0, 0, _bitmap.getWidth(), _bitmap.getHeight());
		invalidate();
	}
	
	private synchronized Bitmap getBitmap() {
		return _bitmap;
	}
	
	private void generateBitmap(int width, int height) {
		Log.d(TAG, "generateBitmap()");
		// fire and forget
		if(_creator != null) {
			_creator.requestStop();
			_creator = null;
		}
		
		_creator = new MandroidCreator(_creationHandler, _params, width, height);
		_creator.addObserver(this);
		
		_progress = new ProgressDialog(getContext());
	    _progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		_progress.setTitle("Generating...");
		_progress.setMax(100);
	    _progress.setProgress(0);
		_progress.setIndeterminate(false);
		_progress.show();
		
		Thread thread = new Thread(_creator, "mandcreator");
		thread.start();
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		Log.d(TAG, "onDraw()");
/*		if(isDirty()) {
			generateBitmap();
		}
		else {*/

//			calculatePanZoomDestination(_panZoomState);
			canvas.drawBitmap(getBitmap(), null, _dst, _textPaint);				
		/*}*/
	}

/*	private void calculatePanZoomDestination(PanZoomState panZoomState) {
		// _src records the dimensions of the rectangle after our last pan/zoom.
		// panZoomState records the movement of the current touch event.
		
		if(_panZoomState.getMode() == Mode.ZOOM) {
		}
		else if(_panZoomState.getMode() == Mode.PAN){ // PAN
			Log.d(TAG, "Panning");
			_dst.left = (int) (_last.left + _panZoomState.getDX());
			_dst.top = (int) (_last.top + _panZoomState.getDY());
			_dst.right = (int) (_dst.left + _bitmap.getWidth() * currentZoom);
			_dst.bottom = (int) (_dst.top + _bitmap.getHeight() * currentZoom);
			Log.d(TAG, "last: " + _last.flattenToString());
			Log.d(TAG, "dst: " + _dst.flattenToString());
		}
		else { // UP
			_last.set(_dst);
		}
	}*/
	
	private Paint initaliseTextPaint() {
		Paint p = new Paint();
		p.setColor(0xff00ff00);
		p.setAntiAlias(true);
		
		return p;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		// trigger regeneration of the mandelbrot
		generateBitmap(right - left, bottom - top);
	}

}
