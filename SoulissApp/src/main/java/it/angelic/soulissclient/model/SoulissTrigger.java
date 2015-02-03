package it.angelic.soulissclient.model;

import android.content.Context;
import it.angelic.soulissclient.db.SoulissCommandDTO;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissTriggerDTO;

public class SoulissTrigger extends SoulissCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3734884921250844802L;

	public SoulissTrigger(Context ct, SoulissCommandDTO dto, SoulissTypical pare) {
		super( dto,pare);
		// TODO Auto-generated constructor stub
	}
	private SoulissTriggerDTO triggerDto;
	
	public SoulissTriggerDTO getTriggerDto() {
		return triggerDto;
	}
	public void setTriggerDto(SoulissTriggerDTO triggerDto) {
		this.triggerDto = triggerDto;
	}
	public SoulissCommandDTO getCommandDto() {
		return super.getCommandDTO();
	}
	public void persist(SoulissDBHelper dbh) {
		super.getCommandDTO().setSceneId(null);//ripetere non nuoce
		super.getCommandDTO().persistCommand(dbh);
		triggerDto.persist(dbh);
	}
	
}
