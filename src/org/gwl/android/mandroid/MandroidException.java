/**
 * @author glittle
 * @version 21 Jan 2011
 */
package org.gwl.android.mandroid;

/**
 * 
 */
public class MandroidException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public MandroidException() {
	}

	/**
	 * @param detailMessage
	 */
	public MandroidException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public MandroidException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public MandroidException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
