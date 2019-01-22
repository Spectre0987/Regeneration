package me.fril.regeneration.compat.lucraft;

import static me.fril.regeneration.util.RegenUtil.*;

import lucraft.mods.lucraftcore.materials.potions.PotionRadiation;
import lucraft.mods.lucraftcore.sizechanging.capabilities.CapabilitySizeChanging;
import lucraft.mods.lucraftcore.sizechanging.capabilities.ISizeChanging;
import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.abilitybar.AbilityBarHandler;
import lucraft.mods.lucraftcore.util.abilitybar.AbilityBarKeys;
import me.fril.regeneration.RegenConfig;
import me.fril.regeneration.common.capability.CapabilityRegeneration;
import me.fril.regeneration.common.capability.IRegeneration;
import me.fril.regeneration.handlers.IActingHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class LucraftCoreHandler implements IActingHandler {
	
	public static final Superpower TIMELORD = new SuperpowerTimelord("timelord").setRegistryName("timelord");
	
	public static void registerEntry() {
		AbilityBarHandler.registerProvider(new LCCoreBarEntry());
	}
	
	public static void registerEventBus() {
		MinecraftForge.EVENT_BUS.register(new LucraftCoreHandler());
	}
	
	
	@Override
	public void onRegenTick(IRegeneration cap) {
		
	}
	
	@Override
	public void onEnterGrace(IRegeneration cap) {
		
	}
	
	@Override
	public void onHandsStartGlowing(IRegeneration cap) {
		
	}
	
	@Override
	public void onRegenFinish(IRegeneration cap) {
		
	}
	
	@Override
	public void onRegenTrigger(IRegeneration cap) {
		if (RegenConfig.modIntegrations.lucraftcore.lucraftcoreSizeChanging) {
			EntityPlayer player = cap.getPlayer();
			ISizeChanging sizeCap = player.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null);
			sizeCap.setSize(randFloat(RegenConfig.modIntegrations.lucraftcore.sizeChangingMin, RegenConfig.modIntegrations.lucraftcore.sizeChangingMax));
		}
	}
	
	@Override
	public void onGoCritical(IRegeneration cap) {
		
	}
	
	@SubscribeEvent
	public void onHurt(LivingHurtEvent e) {
		if (e.getEntityLiving() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) e.getEntityLiving();
			IRegeneration data = CapabilityRegeneration.getForPlayer(player);
			boolean flag = data.canRegenerate() && e.getSource() == PotionRadiation.RADIATION && RegenConfig.modIntegrations.lucraftcore.immuneToRadiation;
			e.setCanceled(flag);
		}
	}
	
	@SubscribeEvent
	public void onRegisterSuperpowers(RegistryEvent.Register<Superpower> e) {
		if (RegenConfig.modIntegrations.lucraftcore.superpower)
			e.getRegistry().register(TIMELORD);
	}
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent e) {
		IRegeneration cap = CapabilityRegeneration.getForPlayer(e.player);
		if (!e.player.world.isRemote && e.phase == TickEvent.Phase.END && RegenConfig.modIntegrations.lucraftcore.superpower) {
			boolean hasPower = SuperpowerHandler.hasSuperpower(e.player, TIMELORD);
			
			if (!hasPower && cap.getRegenerationsLeft() > 0) {
				SuperpowerHandler.giveSuperpower(e.player, TIMELORD);
				
				// I test this to see if giving the player this superpower actually worked (events and other things can cancel it). This would be easier if giveSuperpower returned a boolean for that.
				// If giving the player the timelord power didnt work, I'll remove his left regenerations
				if (SuperpowerHandler.getSuperpower(e.player) != TIMELORD)
					cap.extractRegeneration(cap.getRegenerationsLeft());
				
			} else if (cap.getRegenerationsLeft() == 0 && hasPower) {
				SuperpowerHandler.removeSuperpower(e.player);
			}
		}
	}
	
	public static String getKeyBindDisplayName() {
		for (int i = 0; i < AbilityBarHandler.ENTRY_SHOW_AMOUNT; i++) {
			if (AbilityBarHandler.getEntryFromKey(i) instanceof LCCoreBarEntry) {
				return AbilityBarKeys.KEYS.get(i).getDisplayName();
			}
		}
		return "???";
	}
}
