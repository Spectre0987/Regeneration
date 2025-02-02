package me.suff.mc.regen.handlers;

import com.mojang.blaze3d.platform.GlStateManager;
import me.suff.mc.regen.Regeneration;
import me.suff.mc.regen.client.RKeybinds;
import me.suff.mc.regen.client.rendering.JarTileRender;
import me.suff.mc.regen.client.rendering.entity.TimelordRenderer;
import me.suff.mc.regen.client.skin.SkinHandler;
import me.suff.mc.regen.common.item.GunItem;
import me.suff.mc.regen.common.objects.RSounds;
import me.suff.mc.regen.common.regen.RegenCap;
import me.suff.mc.regen.common.regen.state.RegenStates;
import me.suff.mc.regen.common.regen.transitions.TransitionType;
import me.suff.mc.regen.common.regen.transitions.TransitionTypes;
import me.suff.mc.regen.config.RegenConfig;
import me.suff.mc.regen.util.PlayerUtil;
import me.suff.mc.regen.util.RConstants;
import me.suff.mc.regen.util.RenderHelp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    public static ResourceLocation OLD = new ResourceLocation("textures/gui/icons.png");
    public static ResourceLocation NEW = new ResourceLocation(RConstants.MODID, "textures/gui/icons.png");
    public static ResourceLocation HEARTS = new ResourceLocation(RConstants.MODID, "textures/gui/regen_hearts.png");
    private static ISound iSound = null;

    @SubscribeEvent
    public static void onName(RenderNameplateEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        RegenCap.get(player).ifPresent(iRegen -> {
            if (iRegen.getCurrentState() == RegenStates.POST || iRegen.getCurrentState() == RegenStates.GRACE_CRIT) {
                event.setContent(new StringTextComponent(TextFormatting.OBFUSCATED + event.getContent().getString()));
            }
        });
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre playerEvent) {
        PlayerEntity player = playerEvent.getPlayer();
        SkinHandler.tick((AbstractClientPlayerEntity) playerEvent.getPlayer());
        RegenCap.get(player).ifPresent(iRegen -> {
            TransitionType< ? > type = iRegen.transitionType().get();
            type.getRenderer().onPlayerRenderPre(playerEvent);
        });
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post playerEvent) {
        PlayerEntity player = playerEvent.getPlayer();
        RegenCap.get(player).ifPresent(iRegen -> {
            TransitionType< ? > type = iRegen.transitionType().get();
            type.getRenderer().onPlayerRenderPost(playerEvent);
        });
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        RegenCap.get(Minecraft.getInstance().player).ifPresent(iRegen -> iRegen.transitionType().get().getRenderer().firstPersonHand(event));
    }

    @SubscribeEvent
    public static void onTickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.START)) return;

        RKeybinds.tickKeybinds();

        if (Minecraft.getInstance().player != null) {
            ClientPlayerEntity ep = Minecraft.getInstance().player;
            SoundHandler sound = Minecraft.getInstance().getSoundManager();
            RegenCap.get(ep).ifPresent(iRegen -> {
                if (iRegen.getCurrentState() == RegenStates.POST && PlayerUtil.isPlayerAboveZeroGrid(ep)) {

                    if (iSound == null) {
                        iSound = SimpleSound.forUI(RSounds.GRACE_HUM.get(), 1);
                    }

                    if (!sound.isActive(iSound)) {
                        sound.play(iSound);
                    }
                } else {
                    if (sound.isActive(iSound)) {
                        sound.stop(iSound);
                    }
                }
            });
        }


        //Clean up our mess we might have made!
        if (Minecraft.getInstance().level == null) {
            if (SkinHandler.PLAYER_SKINS.size() > 0) {
                SkinHandler.PLAYER_SKINS.forEach(((uuid, texture) -> Minecraft.getInstance().getTextureManager().release(texture)));
                SkinHandler.PLAYER_SKINS.clear();
                TimelordRenderer.TIMELORDS.forEach(((uuid, texture) -> Minecraft.getInstance().getTextureManager().release(texture)));
                TimelordRenderer.TIMELORDS.clear();
                JarTileRender.TEXTURES.forEach(((uuid, texture) -> Minecraft.getInstance().getTextureManager().release(texture)));
                JarTileRender.TEXTURES.clear();
                Regeneration.LOG.warn("Cleared Regeneration texture cache.");
            }
        }
    }

    @SubscribeEvent
    public static void onColorFog(EntityViewRenderEvent.RenderFogEvent.FogColors e) {
        Entity renderView = Minecraft.getInstance().getCameraEntity();
        if (!(Minecraft.getInstance().getCameraEntity() instanceof LivingEntity)) {
            return;
        }

        RegenCap.get((LivingEntity) renderView).ifPresent((data) -> {

            if (data.getCurrentState() == RegenStates.GRACE_CRIT) {
                e.setRed(0.5F);
                e.setBlue(0.5F);
                e.setGreen(0.5F);
            }

            if (data.transitionType() == TransitionTypes.TROUGHTON && data.getCurrentState() == RegenStates.REGENERATING) {
                e.setRed(0);
                e.setGreen(0);
                e.setBlue(0);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        RegenCap.get(player).ifPresent((cap) -> {
            String warning = null;

            handleGunCrosshair(event, player, cap);


            if (event.getType() != RenderGameOverlayEvent.ElementType.HELMET) return;
            if (cap.getCurrentState() == RegenStates.REGENERATING && event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
                event.setCanceled(true);
            }


            ITextComponent forceKeybind = RKeybinds.FORCE_REGEN.getKey().getDisplayName();

            switch (cap.getCurrentState()) {
                case ALIVE:
                    break;
                case GRACE:
                    RenderHelp.renderVig(cap.getPrimaryColors(), 0.3F);
                    warning = new TranslationTextComponent("regen.messages.warning.grace", forceKeybind.getString()).getString();
                    break;
                case GRACE_CRIT:
                    RenderHelp.renderVig(new Vector3d(1, 0, 0), 0.5F);
                    warning = new TranslationTextComponent("regen.messages.warning.grace_critical", forceKeybind.getString()).getString();
                    break;

                case REGENERATING:
                    RenderHelp.renderVig(cap.getSecondaryColors(), 0.5F);
                    break;

                case POST:
                    if (player.hurtTime > 0 || player.getEffect(Effects.CONFUSION) != null) {
                        RenderHelp.renderVig(cap.getSecondaryColors(), 0.5F);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + cap.getCurrentState());
            }

            if (cap.glowing()) {
                RenderHelp.renderVig(TransitionTypes.FIERY.get().getDefaultPrimaryColor(), 0.5F);
            }

            IRenderTypeBuffer.Impl renderImpl = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
            if (warning != null)
                Minecraft.getInstance().font.drawInBatch(warning, Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - Minecraft.getInstance().font.width(warning) / 2, 4, TextFormatting.WHITE.getColor(), false, TransformationMatrix.identity().getMatrix(), renderImpl, false, 0, 15728880);
            renderImpl.endBatch();
        });
    }

    private static void handleGunCrosshair(RenderGameOverlayEvent.Pre event, ClientPlayerEntity player, me.suff.mc.regen.common.regen.IRegen cap) {
        if (player.getMainHandItem().getItem() instanceof GunItem && player.getUseItemRemainingTicks() > 0) {
            AbstractGui.GUI_ICONS_LOCATION = NEW;
            if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS && event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
                event.setCanceled(true);
            }
        } else {
            AbstractGui.GUI_ICONS_LOCATION = cap.regens() > 0 && RegenConfig.CLIENT.heartIcons.get() ? HEARTS : OLD;
        }
    }

    @SubscribeEvent
    public static void onSetupFogDensity(EntityViewRenderEvent.RenderFogEvent.FogDensity event) {
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        if (viewer != null) {
            RegenCap.get((LivingEntity) viewer).ifPresent((data) -> {
                if (data.getCurrentState() == RegenStates.GRACE_CRIT) {
                    GlStateManager._fogMode(GlStateManager.FogMode.EXP.value);
                    event.setCanceled(true);
                    event.setDensity(0.10F);
                }
                if (data.transitionType() == TransitionTypes.TROUGHTON && data.updateTicks() > 0) {
                    event.setCanceled(true);
                    event.setDensity(0.3F);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        if (e.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) e.getEntityLiving();
            SkinHandler.PLAYER_SKINS.remove(player.getUUID());

            if (player.getUUID().equals(Minecraft.getInstance().player.getUUID())) {
                SkinHandler.sendResetMessage();
            }
        }
    }

    @SubscribeEvent
    public static void keyInput(InputUpdateEvent e) {
        if (Minecraft.getInstance().player == null) return;
        ClientPlayerEntity player = Minecraft.getInstance().player;
        RegenCap.get(Minecraft.getInstance().player).ifPresent((data -> {
            if (data.getCurrentState() == RegenStates.REGENERATING) { // locking user
                MovementInput moveType = e.getMovementInput();
                moveType.right = false;
                moveType.left = false;
                moveType.down = false;
                moveType.jumping = false;
                moveType.forwardImpulse = 0.0F;
                moveType.shiftKeyDown = false;
                moveType.leftImpulse = 0.0F;

                if (data.transitionType() == TransitionTypes.ENDER_DRAGON && RegenConfig.COMMON.allowUpwardsMotion.get()) {
                    if (player.blockPosition().getY() <= 100) {
                        BlockPos upwards = player.blockPosition().above(2);
                        BlockPos pos = upwards.subtract(player.blockPosition());
                        Vector3d vec = new Vector3d(pos.getX(), pos.getY(), pos.getZ()).normalize();
                        player.setDeltaMovement(player.getDeltaMovement().add(vec.scale(0.10D)));
                    }
                }
            }
        }));
    }

}
