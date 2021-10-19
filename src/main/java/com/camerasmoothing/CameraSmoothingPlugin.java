package com.camerasmoothing;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.client.callback.ClientThread;
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

	@Inject
	private ClientThread clientThread;

	private final int HALF_ROTATION = 1024;
	private final int FULL_ROTATION = 2048;

	private final int PITCH_INDEX = 0;
	private final int YAW_INDEX = 1;
	private final int SCROLL_INDEX = 2;
	//Made an array just in case a setter for camera pitch is ever added
	private int[] deltaCamera = new int[3];
	private int[] previousCamera = new int[3];

	private int lerp(int x, int y, float alpha) {
		return x+(int)((y-x)*alpha);
	}
	private int getSmallestAngle(int x, int y) {
		return ((y-x) + HALF_ROTATION) % FULL_ROTATION - HALF_ROTATION;
	}
	private void applySmoothingToAngle(int index) {
		int deltaChange;
		int changed;
		int newDeltaAngle;
		newDeltaAngle = getSmallestAngle(previousCamera[index],index == YAW_INDEX ? client.getMapAngle() : 0/*No pitch method in RL*/);
		deltaCamera[index] += newDeltaAngle;

		deltaChange = lerp(deltaCamera[index],0,(config.smoothness()/100.0f));
		changed = previousCamera[index] + deltaChange;

		deltaCamera[index] -= deltaChange;
		if(index == YAW_INDEX) {
			client.setCameraYawTarget(changed);
		}/* else if(index == PITCH_INDEX) {
			//No pitch method in RL yet
		}*/
		previousCamera[index] += deltaChange;
	}
	private void setZoom(int amount) {
		clientThread.invoke(()-> client.runScript(ScriptID.CAMERA_DO_ZOOM,amount,amount));
	}
	private void applySmoothingToZoom(int index) {
		int deltaChange;
		int changed;
		int newZoom;
		newZoom = client.getVar(VarClientInt.CAMERA_ZOOM_RESIZABLE_VIEWPORT) - previousCamera[index];
		deltaCamera[index] += newZoom;

		deltaChange = lerp(deltaCamera[index],0,(config.smoothness()) / 100.0f);
		changed = previousCamera[index] + deltaChange;

		deltaCamera[index] -= deltaChange;
		if(index == SCROLL_INDEX) {
			setZoom(changed);
		}
		previousCamera[index] += deltaChange;
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
		//applySmoothingToAngle(PITCH_INDEX);
		applySmoothingToAngle(YAW_INDEX);

		applySmoothingToZoom(SCROLL_INDEX);


	}

	@Provides
	CameraSmoothingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CameraSmoothingConfig.class);
	}
}
