package it.angelic.soulissclient.model;

/**
 * Metodi comuni tra nodi e scene e tipici per il polimorfismo
 * delle rinominazioni e cambi icona
 * 
 * @author Ale
 *
 */
public interface ISoulissObject {

	void setIconResourceId(int resId);
	int getIconResourceId();
	
	void setName(String newName);
    String getName();
	
	String getNiceName();
}
