package com.example.examplemod.MManager;

import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import com.example.examplemod.MobTracker.MobTracker;
import com.example.examplemod.OreFinder.OreFinder;
import com.example.examplemod.Renderer.Renderer;
import com.example.examplemod.autoattacker.AutoAttacker;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;

import fly.Fly;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraftforge.client.event.InputEvent.Key;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class MManager {
	
	private final long KEYPRESSACTIVATIONDELAY = 200;
	public static final ChatFormatting RED_CHAT = ChatFormatting.RED;
	public static final ChatFormatting GREEN_CHAT = ChatFormatting.GREEN;
	public static final ChatFormatting YELLOWCHAT = ChatFormatting.YELLOW;
	
	private Map<Integer, Long> pressedKeys;	
	public static final Logger LOG = LogManager.getLogger();
	public OreFinder oreFinder;
	public Renderer renderer;
	public MobTracker mobTracker;
	public AutoAttacker autoAttack;
	public Fly fly;
	private AbstractClientPlayer dummyEntity;
	
	public MManager() {
		pressedKeys = new HashMap<>();
		oreFinder = new OreFinder();		
		mobTracker = new MobTracker();
		autoAttack = new AutoAttacker();
		fly = new Fly();
		renderer = new Renderer(this);
		oreFinder.setRenderer(renderer);
		mobTracker.setRenderer(renderer);
		autoAttack.activate();
	}
	
	@SubscribeEvent
	public void checkKey(Key event) {
		if (event.getAction() != InputConstants.PRESS)
			return;
		if (!checkKeyPressTimer(event.getKey())) {			
			return;
		}		
		if (event.getKey() == KeyEvent.VK_R) {
			// Do something
			LOG.info("Detected key press!");
			autoAttack.attackAllNearbyMonsters();
			autoAttack.clearDumbGrass();
		} else if (event.getKey() == KeyEvent.VK_EQUALS && isAltPressed(event)) {
			renderer.toggle();			
		} else if (event.getKey() == KeyEvent.VK_P && isAltPressed(event)) {
			mobTracker.toggle();
		} else if (event.getKey() == KeyEvent.VK_O && isAltPressed(event)) {
			oreFinder.toggle();			
		} else if (event.getKey() == KeyEvent.VK_BACK_SLASH) {
			autoAttack.toggleHostile();
		} else if (event.getKey() == KeyEvent.VK_L) {
			fly.toggle();			
//			var mc = Minecraft.getInstance();
//			var player = mc.player;
//			var level = mc.level;
//			var gamemode = mc.gameMode;
//			player.jumpFromGround();
//			player.getAbilities().flying = !player.getAbilities().flying;
			
//			return;
			
//			if (!player.isCreative()) {
//				return;
//			}
//			if (dummyEntity == null) {
////				gamemode.createPlayer(null, null, null)
//				addChatMessage("Creating pendejo....", GREEN_CHAT);
//				dummyEntity = new AbstractClientPlayer(level, new GameProfile(UUID.randomUUID(), "Un Pendejo"), (ProfilePublicKey) null) {					
//				
//					@Override
//					public boolean isSpectator() {
//						// TODO Auto-generated method stub
//						return false;
//					}
//
//					@Override
//					public boolean isCreative() {
//						// TODO Auto-generated method stub
//						return false;
//					}
//					
//				};			
//				level.addPlayer(0, dummyEntity);
//				dummyEntity.setPos(player.position());
//			}
//			else {
//				addChatMessage("Destroying Pendejo....", RED_CHAT);
//				dummyEntity.kill();
//				dummyEntity.discard();
//				dummyEntity = null;
//			}
		}
//		Log.info("Detected key press: {}", event.toString());
		
	}
	
	public boolean isAltPressed(Key event) {
		return (event.getModifiers() & GLFW.GLFW_MOD_ALT) == GLFW.GLFW_MOD_ALT;
	}
	
	private boolean checkKeyPressTimer(int key) {
		if (!pressedKeys.containsKey(key)) {
			pressedKeys.put(key, System.currentTimeMillis());
			return true;
		} else {
			long timePressed = pressedKeys.get(key);
			if (timePressed + KEYPRESSACTIVATIONDELAY > System.currentTimeMillis()) {
				return false;
			} else {
				pressedKeys.put(key, System.currentTimeMillis());
				return true;
			}			
		}
	}
	
	public void addChatMessage(String message, ChatFormatting formatting) {
		var mc = Minecraft.getInstance();
		var chat = mc.gui.getChat();
		chat.addMessage(Component.literal(message).withStyle(formatting));
	}
}
