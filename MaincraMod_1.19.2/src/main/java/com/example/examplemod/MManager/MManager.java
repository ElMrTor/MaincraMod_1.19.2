package com.example.examplemod.MManager;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import com.example.examplemod.MobTracker.MobTracker;
import com.example.examplemod.OreFinder.OreFinder;
import com.example.examplemod.Renderer.Renderer;
import com.example.examplemod.autoattacker.AutoAttacker;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraftforge.client.event.InputEvent.Key;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class MManager {
	
	private final long KEYPRESSACTIVATIONDELAY = 300;
	
	private Map<Integer, Long> pressedKeys;	 
	private static final Logger LOG = LogManager.getLogger();
	public OreFinder oreFinder;
	public Renderer renderer;
	public MobTracker mobTracker;
	public AutoAttacker autoAttack;
	
	public MManager() {
		pressedKeys = new HashMap<>();
		oreFinder = new OreFinder();		
		mobTracker = new MobTracker();
		autoAttack = new AutoAttacker();
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
	
}
