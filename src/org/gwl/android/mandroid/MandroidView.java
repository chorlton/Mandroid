/**
 * @author glittle
 * @version 10 Jan 2011
 */
package org.gwl.android.mandroid;

import java.util.Observable;
import java.util.Observer;

import org.gwl.android.mandroid.background.MandroidCreator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
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
			canvas.drawBitmap(getBitmap(), _panZoomState.getPanX(), _panZoomState.getPanY(), _textPaint);				
		/*}*/
	}
	
	private Paint initaliseTextPaint() {
		Paint p = new Paint();
		p.setColor(0xff00ff00);
		p.setAntiAlias(true);
		
		return p;
	}

}
