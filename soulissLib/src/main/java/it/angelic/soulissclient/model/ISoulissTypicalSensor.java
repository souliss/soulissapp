package it.angelic.soulissclient.model;


import java.io.Serializable;

public interface ISoulissTypicalSensor extends ISoulissTypical, Serializable {
	/**
	 * Lettura del sensore
	 */
	float getOutputFloat() ;
	
	
}
