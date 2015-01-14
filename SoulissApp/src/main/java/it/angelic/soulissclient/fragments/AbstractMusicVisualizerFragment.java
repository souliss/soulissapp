package it.angelic.soulissclient.fragments;

/**
 * Generic LED typical with dimmable lights
 * @author Ale
 *
 */
public abstract class AbstractMusicVisualizerFragment  extends AbstractTypicalFragment  {
	/**
	 * In case of single channel only red is used
	 * @param val
	 * @param r
	 * @param g
	 * @param b
	 * @param multicast
	 */
	public abstract void issueIrCommand(final short val, final int r, final int g, final int b, final boolean multicast) ;
	protected int getShownIndex() {
		if (getArguments() != null)
			return getArguments().getInt("index", 0);
		else
			return 0;
	}

}
