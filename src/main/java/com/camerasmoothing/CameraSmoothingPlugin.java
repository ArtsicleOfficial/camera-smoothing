package com.camerasmoothing;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.VarClientInt;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Camera Smoothing"
)
public class CameraSmoothingPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private CameraSmoothingConfig config;

	private final int HALF_ROTATION = 1024;
	private final int FULL_ROTATION = 2048;

	private final int PITCH_INDEX = 0;
	private final int YAW_INDEX = 1;
	//Made an array just in case a setter for camera pitch is ever added
	private int[] deltaCamera = new int[2];
	private int[] previousCamera = new int[2];

	private int lerp(int x, int y, float alpha) {
		return x+(int)((y-x)*alpha);
	}
	private int getSmallestAngle(int x, int y) {
		return ((y-x) + HALF_ROTATION) % FULL_ROTATION - HALF_ROTATION;
	}

	@Subscribe
	public void onBeforeRender(BeforeRender render) {
		if(client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
		int deltaChange;
		int changed;
		int newDeltaAngle;

		//Pitch stuff to be added if runelite ever decides to add a Client.setCameraPitchTarget method
		//Until then, yaw going to have to stick with yaw!

		newDeltaAngle = getSmallestAngle(previousCamera[YAW_INDEX],client.getMapAngle());
		deltaCamera[YAW_INDEX] += newDeltaAngle;

		deltaChange = lerp(deltaCamera[YAW_INDEX],0,(config.smoothness()/100.0f));
		changed = (previousCamera[YAW_INDEX] + deltaChange);

		deltaCamera[YAW_INDEX] -= deltaChange;
		client.setCameraYawTarget(changed);

		previousCamera[YAW_INDEX] += deltaChange;
	}

	@Provides
	CameraSmoothingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CameraSmoothingConfig.class);
	}
}
