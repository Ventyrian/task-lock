package com.tasklock;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.SwingUtilities;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import java.awt.image.BufferedImage;


@Slf4j
@PluginDescriptor(
	name = "Task Lock"
)
public class TaskLockPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private TaskLockConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ConfigManager configManager;

    private TaskLockPanel panel;
    private NavigationButton navButton;



	@Override
	protected void startUp() throws Exception
	{
		log.debug("Task Lock started!");

        BufferedImage icon = ImageUtil.loadImageResource(getClass(),"/icon.png");

        SwingUtilities.invokeLater( () -> {
            panel = new TaskLockPanel(configManager);
            panel.setActiveTasks();
            panel.setBacklogTasks();
            panel.setCompletedTasks();

            navButton = NavigationButton.builder()
                    .tooltip("Task Lock")
                    .icon(icon)
                    .panel(panel)
                    .build();

            clientToolbar.addNavigation(navButton);

        });
	}

	@Override
	protected void shutDown() throws Exception
	{
        clientToolbar.removeNavigation(navButton);
		log.debug("Task Lock stopped!");
	}

	@Provides
	TaskLockConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TaskLockConfig.class);
	}
}
