/**
 * @author glittle
 * @version 10 Jan 2011
 */
package org.gwl.android.mandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelXorXfermode;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * 
 */
public class VortexView extends View implements OnTouchListener {

	private int _color = 0x0000ff00;
	
	private Paint _textPaint = null;
	
	/**
	 * @param context
	 */
	public VortexView(Context context) {
		super(context);

		_textPaint = initaliseTextPaint();
		
		setFocusable(true);
        setFocusableInTouchMode(true);

        this.setOnTouchListener(this);
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int r = (int) (256 * event.getX() / getWidth());
		int g = (int) (256 * event.getY() / getHeight()); 
		_color = (255 << 24) + (r << 16) + (g << 8) + 255;
		invalidate();
		return true;
	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawColor(_color);
		
		canvas.drawText("Width: " + getWidth(), 10, 20, _textPaint);
		canvas.drawText("Height: " + getHeight(), 10, 40, _textPaint);
	}
	
	private Paint initaliseTextPaint() {
		Paint p = new Paint();
		p.setColor(0xff000000);
		p.setAntiAlias(true);
		
		return p;
	}
}
