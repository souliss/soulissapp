package it.angelic.soulissclient.model;

/**
 * Metodi comuni tra nodi e scene e tipici per il polimorfismo
 * delle rinominazioni e cambi icona
 *
 * @author Ale
 */
public interface ISoulissSortableObject extends ISoulissObject {

    void setOrder(Integer order);

    Integer getOrder();


}
