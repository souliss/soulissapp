package it.angelic.soulissclient.fragments;

import android.support.v4.app.Fragment;
/**
 * Generic LED typical with dimmable lights
 * @author Ale
 *
 */
public abstract class AbstractMusicVisualizerFragment  extends Fragment  {
	/**
	 * In case of single channel only red is used
	 * @param val
	 * @param r
	 * @param g
	 * @param b
	 * @param multicast
	 */
	public abstract void issueIrCommand(final short val, final int r, final int g, final int b, final boolean multicast) ;
}
