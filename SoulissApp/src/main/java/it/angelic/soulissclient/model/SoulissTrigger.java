package it.angelic.soulissclient.model;

import it.angelic.soulissclient.model.db.SoulissCommandDTO;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.db.SoulissTriggerDTO;

public class SoulissTrigger extends SoulissCommand {

    private static final long serialVersionUID = -3734884921250844802L;
    private SoulissTriggerDTO triggerDto;

    public SoulissTrigger(SoulissCommandDTO dto, SoulissTypical pare) {
        super(pare.getContext(), dto, pare);
        // TODO Auto-generated constructor stub
    }

    /*public SoulissCommand getComand() {
        return this;
    }*/

    public SoulissCommandDTO getCommandDTO() {
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

    public void persist(SoulissDBHelper dbh) {
        //ripetere non nuoce, sceneId sempre nullo nei trigger
        super.getCommandDTO().setSceneId(null);
        super.getCommandDTO().persistCommand();
        triggerDto.persist(dbh);
    }

    public void setTriggerDTO(SoulissTriggerDTO triggerDto) {
        this.triggerDto = triggerDto;
    }
}
