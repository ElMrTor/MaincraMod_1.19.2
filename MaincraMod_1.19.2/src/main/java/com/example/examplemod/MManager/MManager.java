package com.example.examplemod.MManager;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.examplemod.OreFinder.OreFinder;

import net.minecraftforge.client.event.InputEvent.Key;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class MManager {
	
	private final long KEYPRESSACTIVATIONDELAY = 1000;
	
	private Map<Integer, Long> pressedKeys;	 
	private static final Logger LOG = LogManager.getLogger();
	public OreFinder oreFinder;
	
	public MManager() {
		pressedKeys = new HashMap<>();
		oreFinder = new OreFinder();
	}
	
	@SubscribeEvent
	public void checkKey(Key event) {
		if (!checkKeyPressTimer(event.getKey()) ) {			
			return;
		}
		if (event.getKey() == KeyEvent.VK_R) {
			// Do something
			LOG.info("Detected key press!");
		} else if (event.getKey() == KeyEvent.VK_EQUALS) {
			oreFinder.findOre();
		}
		
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
