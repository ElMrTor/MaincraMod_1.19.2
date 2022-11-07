package com.example.examplemod.Fly;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.examplemod.Utils.ClientSideChatMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Fly {

	private static final int TICK_COUNT_FOR_SERVER_KICK = 80;
	private static final double DEFAULT_BYPASS_AMOUNT = 0.043D;
	private static final Logger LOG = LogManager.getLogger();
	private int tickCount;
	private boolean lastTickBypass;
	private boolean isActive;	
	private Minecraft mc;	
	private Player player;
	private Entity vehicle;
	private Options op;
	private Level level;	
	private double oldY;
	
	public Fly() {		
		this.tickCount = 0;
		this.isActive = false;
		this.lastTickBypass = false;
		this.mc = Minecraft.getInstance();
		oldY = Double.MAX_VALUE;
	}
	
	public void toggle() {		
		isActive = !isActive;
		if (isActive)
			announceActivate();
		else
			announceDeactivate();
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	@SubscribeEvent
	public void doFly(ClientTickEvent event) {	
		player = mc.player;
		level = mc.level;
		op = mc.options;		
		if (player == null && level == null && isActive)
			isActive = false;
		boolean jumpPressed = op.keyJump.isDown();
		boolean usePressed = op.keyUse.isDown();	
		if (!isActive || player == null || level == null) {
			if (player != null && !player.isCreative() && !player.isSpectator()) {
				player.getAbilities().flying = false;
				if (player.getVehicle() != null) {
					player.getVehicle().setNoGravity(false);
				}
			}
			return;
		}		
		Vec3 pPos = player.position();		
		oldY = pPos.y;
			
		vehicle = player.getVehicle();		
		boolean blockBelowAir = level.getBlockState(new BlockPos(player.position().subtract(0.0D, DEFAULT_BYPASS_AMOUNT, 0.0D))).isAir();
		if (!player.getAbilities().flying)
			player.getAbilities().flying = true;	
		
		if (vehicle != null) {
			vehicle.setNoGravity(true);
			if (lastTickBypass && blockBelowAir) {
				vehicle.setPos(vehicle.getX(), vehicle.getY() + DEFAULT_BYPASS_AMOUNT, vehicle.getZ());
				level.sendPacketToServer(new ServerboundMoveVehiclePacket(vehicle));
				lastTickBypass = false;
			}
			if (jumpPressed)
				vehicle.setDeltaMovement(vehicle.getDeltaMovement().x, 0.3D, vehicle.getDeltaMovement().z);
			else if (usePressed)
				vehicle.setDeltaMovement(vehicle.getDeltaMovement().x, -0.3D, vehicle.getDeltaMovement().z);
			else
				vehicle.setDeltaMovement(vehicle.getDeltaMovement().x, 0.0D, vehicle.getDeltaMovement().z);
		}
		
		if (tickCount > TICK_COUNT_FOR_SERVER_KICK/3) {
			tickCount = 0;
			if (vehicle != null) {
				if (blockBelowAir) {
//					vehicle.setPos(vehicle.getX(), vehicle.getY() - DEFAULT_BYPASS_AMOUNT, vehicle.getZ());
					vehicle.setPos(vehicle.position().subtract(0.0D, DEFAULT_BYPASS_AMOUNT, 0.0D));
					level.sendPacketToServer(new ServerboundMoveVehiclePacket(vehicle));
					lastTickBypass = true;
				}
			}
			else if (blockBelowAir) {
				level.sendPacketToServer(new ServerboundMovePlayerPacket.Pos(pPos.x, pPos.y - DEFAULT_BYPASS_AMOUNT, pPos.z, player.isOnGround()));
			}

		}
		if (player.position().y >= oldY - DEFAULT_BYPASS_AMOUNT)
			tickCount++;
	}
	
	
	private void announceDeactivate() {		
		if (mc.gui != null)					
		ClientSideChatMessage.addMessageRed(mc.gui.getChat(), "Fly Deactivated");
	}
	
	private void announceActivate() {		
		if (mc.gui != null)			
			ClientSideChatMessage.addMessageGreen(mc.gui.getChat(), "Fly Activated");
	}
	
}
