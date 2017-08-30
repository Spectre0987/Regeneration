package com.afg.regeneration.sounds;

import com.afg.regeneration.Regeneration;

import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SoundReg {

	public static SoundEvent Reg_1;
	public static SoundEvent Reg_2;

	public static void init() {		
		Reg_1 = registerSound("regen_1");
		Reg_2 = registerSound("regen_2");
	}

	private static SoundEvent registerSound(String soundNameIn) {
		ResourceLocation SoundResource = new ResourceLocation(Regeneration.MODID, soundNameIn);
		SoundEvent e = new SoundEvent(SoundResource).setRegistryName(soundNameIn);
		ForgeRegistries.SOUND_EVENTS.register(e);
		return e;
	}



}