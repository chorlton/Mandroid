/**
 * @author glittle
 * @version 10 Jan 2011
 */
package org.gwl.android.mandroid;

import java.util.Observable;
import java.util.Observer;

import org.gwl.android.mandroid.PanZoomListener.Mode;
import org.gwl.android.mandroid.background.MandroidCreator;

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
	private PanZoomState _panZoomState;
//	private boolean _dirty = true;
//	private MandroidCreator _creator = null;
	
	private Bitmap _bitmap;
	private Rect _last; // stores the view rectangle from the last touch event
	private Rect _dst;
	
	private Handler _bitmapHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.d(TAG, "bitmapHandler.handleMessage()");
			if(msg.what == MandroidCreator.FINISHED) {
				setBitmap((Bitmap) msg.obj);
//				setDirty(false);
				invalidate();
			}
		}
	};

	/**
	 * @param context
	 */
	public MandroidView(Context context) {
		super(context);

		_textPaint = initaliseTextPaint();
		
		setFocusable(true);
        setFocusableInTouchMode(true);
	}
	
	public MandroidView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable trigger, Object bitmap) {
		Log.d(TAG, "update()");
		if(trigger == _panZoomState) {
			invalidate();
		}
		else {
			Message msg = Message.obtain(_bitmapHandler, MandroidCreator.FINISHED, bitmap);
			_bitmapHandler.sendMessage(msg);
		}
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
	}
	
	private synchronized Bitmap getBitmap() {
		return _bitmap;
	}
	
//	private void initViewParams() {
//		Log.d(TAG, "Initialising view parameters");
//		if(_state == null) {
//			_state = new ViewState(getWidth(), getHeight());
//		}
//	}
	
	public void setPanZoomState(PanZoomState panZoomState) {
		if(_panZoomState != null) {
			_panZoomState.deleteObserver(this);
		}
		
		_panZoomState = panZoomState;
		_panZoomState.addObserver(this);
	}
	
/*	private void generateBitmap() {
		Log.d(TAG, "generateBitmap()");
		initViewParams();
		// fire and forget
//		_creator = new MandroidCreator(_state);
		_creator.addObserver(this);
		Thread thread = new Thread(_creator, "mandcreator");
		thread.start();
	}*/
	
	@Override
	public void onDraw(Canvas canvas) {
		Log.d(TAG, "onDraw()");
/*		if(isDirty()) {
			generateBitmap();
		}
		else {*/
		if(_panZoomState.getMode() == Mode.PAN) {
			
		}
			calculatePanZoomDestination(_panZoomState);
			canvas.drawBitmap(getBitmap(), null, _dst, _textPaint);				
		/*}*/
	}

	private void calculatePanZoomDestination(PanZoomState panZoomState) {
		// _src records the dimensions of the rectangle after our last pan/zoom.
		// panZoomState records the movement of the current touch event.
		float currentZoom = ((float) _last.width()) / ((float) _bitmap.getWidth());
		
		if(_panZoomState.getMode() == Mode.ZOOM) {
			Log.d(TAG, "Zooming");
			float zoomFactor = _panZoomState.calculateZoom(_bitmap.getHeight());
			Log.d(TAG, "factor = " + zoomFactor);
			// calculate the point on the original bitmap which our start point represents.
			float x = (panZoomState.getX() - _last.left) / currentZoom;
			float y = (panZoomState.getY() - _last.top) / currentZoom;
			Log.d(TAG, "x = " + x + ", y = " + y);
			float newZoom = currentZoom * zoomFactor;
			
			_dst.left = (int) (_panZoomState.getX() - x * newZoom);
			_dst.top = (int) (_panZoomState.getY() - y * newZoom);
			_dst.right = (int) (_dst.left + _bitmap.getWidth() * newZoom);
			_dst.bottom = (int) (_dst.top + _bitmap.getHeight() * newZoom);
			Log.d(TAG, "last: " + _last.flattenToString());
			Log.d(TAG, "dst: " + _dst.flattenToString());
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
	}
	
	private Paint initaliseTextPaint() {
		Paint p = new Paint();
		p.setColor(0xff00ff00);
		p.setAntiAlias(true);
		
		return p;
	}

}
