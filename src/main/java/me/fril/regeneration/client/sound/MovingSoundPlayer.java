package me.fril.regeneration.client.sound;

import java.util.function.Supplier;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

/**
 * Created by Sub
 * on 20/09/2018.
 */
public class MovingSoundPlayer extends MovingSound {
	
	private final EntityPlayer player;
	private final Supplier<Boolean> stopCondition;
	
	public MovingSoundPlayer(EntityPlayer playerIn, SoundEvent soundIn, SoundCategory categoryIn, boolean repeat, Supplier<Boolean> stopCondition) {
		super(soundIn, categoryIn);
		
		this.player = playerIn;
		this.stopCondition = stopCondition;
		super.repeat = repeat;
	}
	
	// FIXME Sometimes ConcurrentModificationException's in subtitle renderer, probably due to a race condition because we're modifying it here and in ConditionalSound
	@Override
	public void update() {
		super.xPosF = (float) player.posX;
		super.yPosF = (float) player.posY;
		super.zPosF = (float) player.posZ;
	}
	
	@Override
	public boolean isDonePlaying() {
		return player.isDead || stopCondition.get();
	}
	
}
