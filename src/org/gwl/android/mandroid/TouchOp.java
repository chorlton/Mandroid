/**
 * @author Gordon
 * @version 9 Feb 2011
 */
package org.gwl.android.mandroid;

/**
 * 
 */
public abstract class TouchOp {
		
	public abstract void track(double x, double y);
	public abstract void trackView(MandroidView view);
	public abstract void finish(MandroidView view);
}
