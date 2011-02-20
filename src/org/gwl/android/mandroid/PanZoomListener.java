/**
 * @author gordon
 * @version 28 Jan 2011
 */
package org.gwl.android.mandroid;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * 
 */
public class PanZoomListener implements View.OnTouchListener {

	private static final String TAG = PanZoomListener.class.getName();
	private static final int PAN_TIMEOUT = ViewConfiguration.getLongPressTimeout(); // milliseconds
	private static final int PAN_TOLERANCE = ViewConfiguration.getTouchSlop();
	private static final int VIBRATE_TIME = 50; // milliseconds
	
	private final Vibrator _vibrator;
	
	public enum Mode { UNDEFINED, PAN, ZOOM, UP }

	private double _x;
	private double _y;
	private MandroidView _view;
	private TouchOp _touchOp;
	
	private final Runnable _SWITCH_TO_ZOOM = new Runnable() {
		public void run() {
			_touchOp = new Zoom(_x, _y, _view.getHeight());
			Log.d(TAG, "Switching to Zoom Mode");
			_vibrator.vibrate(VIBRATE_TIME);
		}
	};
	
	public PanZoomListener(Context context) {
		_vibrator = (Vibrator) context.getSystemService("vibrator");
	}
		
	/* (non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(TAG, "onTouch");
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			
			// detecting a pan will remove this. If we hang around, it will trigger a switch to ZOOM mode
			v.postDelayed(_SWITCH_TO_ZOOM, PAN_TIMEOUT);
			
			_touchOp = null;
			_x = event.getX();
			_y = event.getY();
			_view = (MandroidView) v;
			
			Log.d(TAG, "DOWN: " + _x + ", " + _y);
			
			break;
		case MotionEvent.ACTION_MOVE:

			if(_touchOp == null) {
				// we don't know what we are yet. See if we've moved far enough to enter PAN mode
				double dx = event.getX() - _x;
				double dy = event.getY() - _y;
				
				if(dx * dx + dy * dy > PAN_TOLERANCE * PAN_TOLERANCE) {
					v.removeCallbacks(_SWITCH_TO_ZOOM);
					_touchOp = new Pan(_x, _y);
					Log.d(TAG, "Switching to Pan Mode");
				}
			}

			if(_touchOp != null) {
				_touchOp.track(event.getX(), event.getY());
				_touchOp.trackView((MandroidView) v);
			}

			break;			
		case MotionEvent.ACTION_UP:
			if(_touchOp != null) {
				_touchOp.finish((MandroidView) v);
			}
			break;
		}
					
		return true;
	}

}
