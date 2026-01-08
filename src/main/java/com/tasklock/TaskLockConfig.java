package com.tasklock;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("tasklock")
public interface TaskLockConfig extends Config
{
	@ConfigItem(
		keyName = "showInfoBox",
		name = "Show InfoBox",
		description = "Display the InfoBox in the client UI"
	)
	default boolean showInfoBox()
	{
		return true;
	}

    @ConfigItem(
            keyName = "allTasksJson",
            name = "All Tasks Json",
            description = "Json of all tasks currently saved in config",
            hidden = true,
            position = 1
    )
    default String allTasksJson() {return "";}
}
