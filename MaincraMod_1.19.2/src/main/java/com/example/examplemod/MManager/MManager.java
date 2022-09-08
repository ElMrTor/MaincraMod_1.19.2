package com.example.examplemod.MManager;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.examplemod.MobTracker.MobTracker;
import com.example.examplemod.OreFinder.OreFinder;
import com.example.examplemod.Renderer.Renderer;
import com.example.examplemod.autoattacker.AutoAttacker;

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
		renderer = new Renderer(this);
		mobTracker = new MobTracker();
		autoAttack = new AutoAttacker();
		oreFinder.setRenderer(renderer);
		mobTracker.setRenderer(renderer);
		autoAttack.activate();
	}
	
	@SubscribeEvent
	public void checkKey(Key event) {
		if (!checkKeyPressTimer(event.getKey())) {			
			return;
		}
		if (event.getKey() == KeyEvent.VK_R) {
			// Do something
			LOG.info("Detected key press!");
			autoAttack.attackAllNearbyMonsters();
		} else if (event.getKey() == KeyEvent.VK_EQUALS) {
			renderer.toggleRenderer();
			
		} else if (event.getKey() == KeyEvent.VK_P) {
//			renderer.toggleRenderer();
//			oreFinder.toggle();
			mobTracker.toggle();
		} else if (event.getKey() == KeyEvent.VK_O) {
			oreFinder.toggle();
			
		}
//		Log.info("Detected key press: {}", event.toString());
		
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
