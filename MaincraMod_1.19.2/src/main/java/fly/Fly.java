package fly;

import org.apache.logging.log4j.LogManager;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Fly {

	public static final int DEFAULT_TICK_COUNT = 40;
	private int tickCount;
	private boolean isActive;
	private double acceleration;
	
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
			if (player != null)
				player.getAbilities().flying = false;
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
		
		Vec3 vel = player.getDeltaMovement();
		if (player.getVehicle() != null) {
			var v = player.getVehicle();
			var vMove = v.getDeltaMovement();
			if (jumpPressed)
				v.setDeltaMovement(vMove.x, 0.3D, vMove.z);
			else if (shiftPressed)
				v.setDeltaMovement(vMove.x, -0.3D, vMove.z);
			else
				v.setDeltaMovement(vMove.x, 0.05D, vMove.z);
			
		}
		if (tickCount > DEFAULT_TICK_COUNT/2) {
			tickCount = 0;
			var pPos = player.position();
//			if (!player.isOnGround()) {
//			if (player.getVehicle() != null && player.getVehicle() instanceof Boat) {
////				new ServerboundMoveVehiclePacket()
//			}
			
			if (player.getVehicle() != null) {
				var vMove = player.getVehicle().getDeltaMovement();
				var vPos = player.getVehicle().position();
				player.getVehicle().setPos(vPos.x, vPos.y - 0.0433D, vPos.y);
//				player.getVehicle().setDeltaMovement(vMove.x, -)
			}
			ServerboundMovePlayerPacket.Pos posPacket = new ServerboundMovePlayerPacket.Pos(pPos.x, pPos.y - 0.0433D, pPos.z, player.isOnGround());
			level.sendPacketToServer(posPacket);
//			}
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
	
}
 