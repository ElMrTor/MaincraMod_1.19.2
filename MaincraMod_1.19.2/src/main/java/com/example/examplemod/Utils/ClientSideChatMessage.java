package com.example.examplemod.Utils;


import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;

public class ClientSideChatMessage {
	
	public static final ChatFormatting GREEN = ChatFormatting.GREEN;
	public static final ChatFormatting RED = ChatFormatting.RED;
	public static final ChatFormatting YELLOW = ChatFormatting.YELLOW;

	public static void addMessage(ChatComponent chat, String message, ChatFormatting format) {
		if (format == null)
			chat.addMessage(Component.literal(message));
		else
			chat.addMessage(Component.literal(message).withStyle(format));
	}
	
	public static void addMessageGreen(ChatComponent chat, String message) {
		addMessage(chat, message, GREEN);
	}
	
	public static void addMessageRed(ChatComponent chat, String message) {
		addMessage(chat, message, RED);
	}
	
	public static void addMessageYellow(ChatComponent chat, String message) {
		addMessage(chat, message, YELLOW);
	}
	
	
}
