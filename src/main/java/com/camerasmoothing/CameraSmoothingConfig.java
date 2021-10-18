package com.camerasmoothing;

import net.runelite.client.config.*;

@ConfigGroup("camerasmoothing")
public interface CameraSmoothingConfig extends Config
{
	@Units(
			"%"
	)
	@Range(
			min = 0,
			max = 100
	)
	@ConfigItem(
		keyName = "smoothingSpeed",
		name = "Smoothness",
		description = "How slowly the camera moves (0-100%)"
	)
	default int smoothness()
	{
		return 50;
	}

}
