package org.gwl.android.mandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

public class Mandroid extends Activity {
	
	private PanZoomListener _touchListener;
	private PanZoomState _panZoomState;
	private Bitmap _bitmap;
	private MandroidView _view;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        		
        setContentView(R.layout.main);
        
        _bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.title);

        _panZoomState = new PanZoomState();
        _touchListener = new PanZoomListener();
        _touchListener.setPanZoomState(_panZoomState);
        
        _view = (MandroidView) findViewById(R.id.mandroidview);
        _view.setPanZoomState(_panZoomState);
        _view.setBitmap(_bitmap);
        _view.setOnTouchListener(_touchListener);
    }
}