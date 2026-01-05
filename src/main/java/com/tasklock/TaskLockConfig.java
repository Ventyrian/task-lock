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
            keyName = "activeTasks",
            name = "Active Tasks",
            description = "Comma separated list of active tasks",
            hidden = true,
            position = 1
    )
    default String activeTasks() {return "";}

    @ConfigItem(
            keyName = "backlogTasks",
            name = "Backlogged Tasks",
            description = "Comma separated list of backlogged tasks",
            hidden = true,
            position = 2
    )
    default String backlogTasks() {return "";}

    @ConfigItem(
            keyName = "completedTasks",
            name = "Completed Tasks",
            description = "Comma separated list of completed tasks",
            hidden = true,
            position = 3
    )
    default String completedTasks() {return "";}
}
