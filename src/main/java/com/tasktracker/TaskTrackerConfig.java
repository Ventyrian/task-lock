package com.tasktracker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.Color;

@ConfigGroup("tasktracker")
public interface TaskTrackerConfig extends Config
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
            keyName = "enableSfx",
            name = "Enable UI Sounds",
            description = "Play sound effects when clicking the Roll, Backlog, and Complete buttons.",
            position = 1
    )
    default boolean enableSfx()
    {
        return true;
    }

    @ConfigItem(
            keyName = "currentTaskBorderColor",
            name = "Border Color",
            description = "The color of the border displayed around the current task in the active tasks list.",
            position = 2
    )
    default Color currentTaskBorderColor()
    {
        return Color.GREEN;
    }

    @Getter
    @RequiredArgsConstructor
    enum TimestampFormat
    {
        // Format: (Pattern, Label shown in Dropdown)
        US("MM/dd/yyyy HH:mm", "US (12/31/2024)"),
        EU("dd/MM/yyyy HH:mm", "EU (31/12/2024)"),
        ISO("yyyy-MM-dd HH:mm", "ISO (2024-12-31)");

        private final String pattern;
        private final String label;

        @Override
        public String toString()
        {
            return label;
        }
    }

    @ConfigItem(
            keyName = "timestampFormat",
            name = "Date Format",
            description = "Choose how the completed date will be displayed.",
            position = 3
    )
    default TimestampFormat timestampFormat()
    {
        return TimestampFormat.US;
    }

    @ConfigItem(
            keyName = "allTasksJson",
            name = "All Tasks Json",
            description = "Json of all tasks currently saved in config",
            hidden = true,
            position = 10
    )
    default String allTasksJson()
    {
        return "";
    }
}
