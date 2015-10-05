package it.angelic.soulissclient.model;

import android.content.Context;

import it.angelic.soulissclient.db.SoulissCommandDTO;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissTriggerDTO;

public class SoulissTrigger extends SoulissCommand {

    private static final long serialVersionUID = -3734884921250844802L;
    private SoulissTriggerDTO triggerDto;

    public SoulissTrigger(Context ct, SoulissCommandDTO dto, SoulissTypical pare) {
        super(dto, pare);
        // TODO Auto-generated constructor stub
    }

    /*public SoulissCommand getComand() {
        return this;
    }*/

    public SoulissCommandDTO getCommandDto() {
        return super.getCommandDTO();
    }

    /* WRAPPED */
    public Short getInputNodeId() {
        return triggerDto.getInputNodeId();
    }

    public short getInputSlot() {
        return triggerDto.getInputSlot();
    }

    public String getOp() {
        return triggerDto.getOp();
    }

    public float getThreshVal() {
        return triggerDto.getThreshVal();
    }

    public SoulissTriggerDTO getTriggerDto() {
        return triggerDto;
    }

    public void setTriggerDto(SoulissTriggerDTO triggerDto) {
        this.triggerDto = triggerDto;
    }

    public void persist(SoulissDBHelper dbh) {
        //ripetere non nuoce, sceneId sempre nullo nei trigger
        super.getCommandDTO().setSceneId(null);
        super.getCommandDTO().persistCommand();
        triggerDto.persist(dbh);
    }
}
