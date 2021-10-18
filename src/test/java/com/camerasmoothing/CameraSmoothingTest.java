package com.camerasmoothing;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CameraSmoothingTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CameraSmoothingPlugin.class);
		RuneLite.main(args);
	}
}