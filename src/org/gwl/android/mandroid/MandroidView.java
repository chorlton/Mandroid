/**
 * @author glittle
 * @version 10 Jan 2011
 */
package org.gwl.android.mandroid;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
//import android.view.MotionEvent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
//import android.view.View.OnTouchListener;
import android.view.View.OnTouchListener;

/**
 * 
 */
public class MandroidView extends View implements Observer, OnTouchListener {
	
	private static final String TAG = MandroidView.class.getName();
	
	private Paint _textPaint = null;

//	private int[] _buffer = null;
	private ViewParams _viewParams;
	private boolean _dirty = true;
	private MandroidCreator _creator = null;
	
	private Bitmap _bitmap;
	
	private float _touchX;
	private float _touchY;
	
	private Handler _bitmapHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.d(TAG, "bitmapHandler.handleMessage()");
			if(msg.what == MandroidCreator.FINISHED) {
				setBitmap((Bitmap) msg.obj);
				setDirty(false);
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

        this.setOnTouchListener(this);
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable creator, Object bitmap) {
		Log.d(TAG, "update()");
		Message msg = Message.obtain(_bitmapHandler, MandroidCreator.FINISHED, bitmap);
		_bitmapHandler.sendMessage(msg);
	}
	
	/* (non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
/*	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int r = (int) (256 * event.getX() / getWidth());
		int g = (int) (256 * event.getY() / getHeight()); 
		_color = (255 << 24) + (r << 16) + (g << 8) + 255;
		invalidate();
		return true;
	}
*/
	private synchronized boolean isDirty() {
		return _dirty;
	}
	
	private synchronized void setDirty(boolean dirty) {
		_dirty = dirty;
	}
	
	private synchronized void setBitmap(Bitmap bitmap) {
		_bitmap = bitmap;
	}
	
	private synchronized Bitmap getBitmap() {
		return _bitmap;
	}
	
	private void initViewParams() {
		Log.d(TAG, "Initialising view parameters");
		if(_viewParams == null) {
			_viewParams = new ViewParams(getWidth(), getHeight());
		}
	}
	
/*	private synchronized int[] getBuffer() {
		return _buffer;
	}
	
	private synchronized void setBuffer(int[] buffer) {
		_buffer = buffer;
	}*/
	
	private void generateBitmap() {
		Log.d(TAG, "generateBitmap()");
		initViewParams();
		// fire and forget
		_creator = new MandroidCreator(_viewParams);
		_creator.addObserver(this);
		Thread thread = new Thread(_creator, "mandcreator");
		thread.start();
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		Log.d(TAG, "onDraw()");
		if(isDirty()) {
			generateBitmap();
		}
		else {
			canvas.drawBitmap(getBitmap(), 0.0f, 0.0f, _textPaint);				
		}
	}
	
	private Paint initaliseTextPaint() {
		Paint p = new Paint();
		p.setColor(0xff00ff00);
		p.setAntiAlias(true);
		
		return p;
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(TAG, "onTouch");
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			_touchX = event.getX();
			_touchY = event.getY();
			Log.d(TAG, "DOWN: " + _touchX + ", " + _touchY);
			Log.d(TAG, "reim: " + _viewParams.x2re((int) _touchX) + ", " + _viewParams.y2im((int) _touchY));
			break;
		case MotionEvent.ACTION_UP:
			Log.d(TAG, "UP: " + event.getX() + ", " + event.getY());
			Log.d(TAG, "reim: " + _viewParams.x2re((int) event.getX()) + ", " + _viewParams.y2im((int) event.getY()));
			_viewParams = new ViewParams(_viewParams.getWidth(),
					_viewParams.getHeight(),
					_viewParams.getReal() + _viewParams.x2re((int) _touchX) - _viewParams.x2re((int) event.getX()),
					_viewParams.getImag() + _viewParams.y2im((int) _touchY) - _viewParams.y2im((int) event.getY()),
					_viewParams.getScale());
			setDirty(true);
			invalidate();
			break;
		}
					
		return true;
	}
}
