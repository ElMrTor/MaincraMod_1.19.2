package fly;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Fly {

	public static final int DEFAULT_TICK_COUNT = 80;
	public static final double DEFAULT_BYPASS_AMOUNT = 0.035D;
	private int tickCount;
	private boolean lastPacketBypass;
	private boolean isActive;
	private double acceleration;
	public static final Logger LOG = LogManager.getLogger();
	
	{
//		try {
//			Class<?> ServerboundMove = Class.forName("net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Pos");
//			Method[] m = ServerboundMove.getDeclaredMethod(null, null)
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
	public Fly() {
		tickCount = 0;
		isActive = false;
		acceleration = 0.1;
		lastPacketBypass = false;
	}
	
	public void toggle() {
		isActive = !isActive;		
		announceState();
	}
	
	private void resetVelocity(Entity player) {
		player.setDeltaMovement(0.0, 0.0, 0.0);
	}
	
	@SubscribeEvent
	public void doFly(ClientTickEvent event) {		
		Minecraft mc = Minecraft.getInstance();
		Options op = mc.options;
		Player player = mc.player;
		var level = mc.level;
		if (!isActive || player == null || level == null) {
			if (player != null) {
				player.getAbilities().flying = false;
				if (player.getVehicle() != null)
					player.getVehicle().setNoGravity(false);;
			}
			
			return;
		}		
//		if (player.isOnGround()) {			
//			mc.gui.getChat().addMessage(Component.literal("Cannot be standing on ground to enable fly!").withStyle(ChatFormatting.YELLOW));
//			toggle();
//			return;
//		}
		if (isActive && !player.getAbilities().flying) {
			player.getAbilities().flying = true;
			
		}
		boolean leftPressed = op.keyLeft.isDown();
		boolean rightPressed = op.keyRight.isDown();
		boolean forwardPressed = op.keyUp.isDown();
		boolean backPressed = op.keyDown.isDown();
		boolean jumpPressed = op.keyJump.isDown();
		boolean shiftPressed = op.keyShift.isDown();
		boolean ctrlPressed = op.keyUse.isDown();
				
		Vec3 vel = player.getDeltaMovement();
		boolean blockBelowAir = level.getBlockState(player.blockPosition().below()).isAir();
		if (player.getVehicle() != null && player.getVehicle() instanceof Boat) {
			var v = (Boat) player.getVehicle();			
			v.setNoGravity(true);
			var vMove = v.getDeltaMovement();			
//			player.stopRiding();
			if (lastPacketBypass && blockBelowAir) {
				lastPacketBypass = false;
				v.setPos(v.getX(), v.getY() + DEFAULT_BYPASS_AMOUNT, v.getZ());
				level.sendPacketToServer(new ServerboundMoveVehiclePacket(v));
			}			
			if (jumpPressed)
				v.setDeltaMovement(vMove.x, 0.3D, vMove.z);
			else if (ctrlPressed)
				v.setDeltaMovement(vMove.x, -0.3D, vMove.z);
			else {
				v.setDeltaMovement(vMove.x, 0.0D, vMove.z);
				player.setDeltaMovement(player.position().x, 0.0D, player.position().z);
			}
			
		}		
		if (tickCount > DEFAULT_TICK_COUNT/2) {
			tickCount = 0;
			var pPos = player.position();			
			if (player.getVehicle() != null && blockBelowAir) {
				var vehicle = player.getVehicle();
				var vMove = player.getVehicle().getDeltaMovement();
				var vPos = player.getVehicle().position();
//				vehicle.setPos(vehicle.getX(), vehicle.getY() - DEFAULT_BYPASS_AMOUNT, vehicle.getZ());
				vehicle.setPos(vehicle.getX(), vehicle.getY() - DEFAULT_BYPASS_AMOUNT, vehicle.getZ());
//				level.sendPacketToServer(new ServerboundMoveVehiclePacket(player.getVehicle()));
				level.sendPacketToServer(new ServerboundMoveVehiclePacket(vehicle));
//				player.getVehicle().setPos(vPos.x, vPos.y + DEFAULT_BYPASS_AMOUNT, vPos.z);
//				LOG.info("Sent vehicle packet to server.");
//				player.getVehicle().setDeltaMovement(vMove.x, -)				
				lastPacketBypass = true;
			} 
			else if (!player.isOnGround()) {
				ServerboundMovePlayerPacket.Pos posPacket = new ServerboundMovePlayerPacket.Pos(pPos.x, pPos.y - DEFAULT_BYPASS_AMOUNT, pPos.z, player.isOnGround());
				level.sendPacketToServer(posPacket);
			}
		}
		tickCount++;
	}	
	
	private void announceState() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null)
			return;
		ChatComponent chat = mc.gui.getChat();
		if (isActive) {
			chat.addMessage(Component.literal("Fly Activated").withStyle(ChatFormatting.GREEN));
		}else {
			chat.addMessage(Component.literal("Fly Deactivated").withStyle(ChatFormatting.RED));
		}
	}			
	
//	public class C2SMoveVehiclePacket extends ServerboundMoveVehiclePacket {
//		public C2SMoveVehiclePacket(Vec3 vec) {
//			
//		}
//	}
	
}
 