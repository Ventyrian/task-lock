package com.tasktracker;

import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@PluginDescriptor(
	name = "Task Tracker"
)
public class TaskTrackerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private TaskTrackerConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ConfigManager configManager;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    private Gson gson;

    private TaskTrackerPanel panel;
    private NavigationButton navButton;

    private final ExecutorService audioExecutor = Executors.newSingleThreadExecutor();

	@Override
	protected void startUp() throws Exception
	{
		log.debug("Task Tracker started!");

        BufferedImage icon = ImageUtil.loadImageResource(getClass(), "img/icon.png");

        SwingUtilities.invokeLater( () -> {
            panel = new TaskTrackerPanel(this, spriteManager);
            panel.setupSections();

            navButton = NavigationButton.builder()
                    .tooltip("Task Tracker")
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
        audioExecutor.shutdown();
		log.debug("Task Tracker stopped!");
	}

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        // Check if the change belongs to your plugin group
        if (event.getGroup().equals("tasktracker"))
        {
            panel.refresh();
        }
    }

	@Provides
    TaskTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TaskTrackerConfig.class);
	}

    // Get task data from config
    public TaskTrackerData getTaskData()
    {
        String json = configManager.getConfiguration("tasktracker","allTasksJson");
        if (json == null || json.isEmpty())
        {
            return new TaskTrackerData();
        }
        return gson.fromJson(json, TaskTrackerData.class);
    }

    // Save task data to config
    private void saveTaskData(TaskTrackerData data)
    {
        Comparator<String> naturalOrder = createNaturalOrderComparator();

        // Sort Active Tasks
        if (data.getActive() != null)
        {
            data.getActive().sort(naturalOrder);
        }
        // Sort Backlog Tasks
        if (data.getBacklog() != null)
        {
            data.getBacklog().sort(naturalOrder);
        }
        // Sort Completed Tasks
        if (data.getCompleted() != null)
        {
            data.getCompleted().sort(Comparator.comparing(CompletedTask::getCompletedAt));
        }

        String json = gson.toJson(data);
        configManager.setConfiguration("tasktracker","allTasksJson",json);
    }

    // Button function roll a unique task, rerolling the same task is impossible
    public void rollTask()
    {
        log.info("Rolling Task");
        TaskTrackerData data = getTaskData();

        // Make sure we don't reroll the same task
        List<String> rollableTasks = new ArrayList<>(data.getActive());
        if (rollableTasks.contains(data.getCurrentTask()))
        {
            rollableTasks.remove(data.getCurrentTask());
        }
        // Make sure we have a task to roll
        if (rollableTasks.isEmpty())
        {
            return;
        }

        playSound("dice.wav");

        Random random = new Random();
        String newCurrentTask = rollableTasks.get(random.nextInt(rollableTasks.size()));

        data.setCurrentTask(newCurrentTask);
        saveTaskData(data);
    }

    // Button function backlog or complete a task based on key
    public void backlogCompleteTask(String key)
    {
        log.info("Backlog Complete Task Button Clicked");
        TaskTrackerData data = getTaskData();
        String currentTask = data.getCurrentTask();
        List<String> activeTasks = data.getActive();

        if (currentTask == null || currentTask.isEmpty() || currentTask.equals("No Current Task"))
        {
            log.info("No Current Task");
            return;
        }

        if (activeTasks.isEmpty() || !activeTasks.contains(currentTask))
        {
            log.info("No Active Tasks or Current Task not in list");
            return;
        }

        if (key.equals("backlog"))
        {
            playSound("equip.wav");
            data.getBacklog().add(currentTask);
        }
        else if (key.equals("complete"))
        {
            playSound("coins.wav");
            data.getCompleted().add(new CompletedTask(currentTask));
        }
        data.getActive().remove(currentTask);
        data.setCurrentTask("");

        saveTaskData(data);
    }

    // Menu function for right click delete task
    public void deleteTask(String task, String section)
    {
        TaskTrackerData data = getTaskData();

        switch(section)
        {
            case TaskTrackerPanel.activeString:
                data.getActive().remove(task);
                break;
            case TaskTrackerPanel.backlogString:
                data.getBacklog().remove(task);
                break;
            case TaskTrackerPanel.completedString:
                data.getCompleted().removeIf(t -> t.getTask().equals(task));
                break;
            default:
                break;
        }

        saveTaskData(data);
    }

    // Menu function for right click move task to active
    public void moveTaskToActive(String task, String section)
    {
        TaskTrackerData data = getTaskData();

        switch(section)
        {
            case TaskTrackerPanel.backlogString:
                data.getActive().add(task);
                data.getBacklog().remove(task);
                break;
            case TaskTrackerPanel.completedString:
                data.getActive().add(task);
                data.getCompleted().removeIf(t -> t.getTask().equals(task));
                break;
            default:
                break;
        }

        saveTaskData(data);
    }

    // Menu function for right click make current task
    public void makeCurrentTask(String task)
    {
        TaskTrackerData data = getTaskData();

        data.setCurrentTask(task);

        saveTaskData(data);
    }

    // Menu function for right click backlog task
    public void backlogTask(String task)
    {
        TaskTrackerData data = getTaskData();

        playSound("equip.wav");
        data.getBacklog().add(task);
        data.getActive().remove(task);

        saveTaskData(data);
    }

    // Function to play custom sounds from resources folder
    public void playSound(String soundFile)
    {
        // Check config
        if (!config.enableSfx())
        {
            return;
        }

        audioExecutor.submit( () ->
        {
            // Load the file from resources
            try (InputStream rawStream = getClass().getResourceAsStream("audio/" + soundFile);)
            {
                if (rawStream == null)
                {
                    return;
                }

                // Buffer the stream to prevent exceptions (This is raw data)
                InputStream bufferedSteam = new BufferedInputStream(rawStream);
                // Translate to an audioStream rawData -> audioData
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedSteam);

                // Get the clip (create small audio player)
                Clip clip = AudioSystem.getClip();

                // Add a listener to close the clip when done
                clip.addLineListener(event ->
                {
                    if (event.getType() == LineEvent.Type.STOP)
                    {
                        clip.close();
                    }
                });

                // Load the audio
                clip.open(audioStream);

                // Optional: Reduce volume slightly if it's too loud
                // FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                // gainControl.setValue(-10.0f); // Reduce by 10 decibels

                // Play the audio
                log.debug("Playing sound " +  soundFile);
                clip.start();
            }
            catch (Exception e)
            {
                log.error("Failed to play sound: " + soundFile, e);
            }
        });
    }

    // Helper function to update inner TaskData Lists from text
    public boolean updateListFromText(TaskTrackerData data, String key, String text)
    {
        // List for active and backlog tasks
        List<String> newList = new ArrayList<>();
        // List for completed tasks
        List<CompletedTask> newCompletedTasks = new  ArrayList<>();
        // Date format from configuration
        SimpleDateFormat dateFormat = getDateTimeFormat();

        // Don't allow "rolling over" invalid dates (e.g., 32nd of Jan -> 1st Feb)
        if (dateFormat != null)
        {
            dateFormat.setLenient(false);
        }

        // Split textArea by lines and initiate lineNumber
        String [] lines = text.split("\n");
        int lineNumber = 0;

        for (String line : lines)
        {
            lineNumber++;
            // Skip empty lines
            if (line.isEmpty())
            {
                continue;
            }
            // Completed Task Logic
            if (key.equals("completed"))
            {
                // If no format is selected just create task with current time (This should not happen)
                if (dateFormat == null)
                {
                    newCompletedTasks.add(new CompletedTask(line.trim()));
                    continue;
                }

                // Split string, limit=2 ensures if the task name has a hyphen it doesn't break
                String[] split = line.split(" - ",2);

                // Validate split and text format
                if (split.length < 2)
                {
                    panel.showError("Format Error on Line " + lineNumber + ":\n" +
                            "Missing separator ' - '\n" +
                            "Expected: [Date] - [Task Name]\n" +
                            "Found: " + line);
                    return false;
                }

                String datePart = split[0].trim();
                String taskPart = split[1].trim();

                // Validate date
                try
                {
                    Date date = dateFormat.parse(datePart);
                    newCompletedTasks.add(new CompletedTask(date.getTime(), taskPart));
                }
                catch (ParseException e)
                {
                    panel.showError("Date Error on Line " + lineNumber + ":\n" +
                            "Invalid date format: '" + datePart + "'\n" +
                            "Expected format: " + getTimestampFormat());
                    return false;
                }
            }
            // Active and Backlog Task Logic
            else
            {
                newList.add(line.trim());
            }
        }

        switch (key)
        {
            case "active":
                data.setActive(newList);
                break;
            case "backlog":
                data.setBacklog(newList);
                break;
            case "completed":
                data.setCompleted(newCompletedTasks);
                break;
        }

        saveTaskData(data);
        return true;
    }

    // Helper function to get the completed task list in the List<String> format
    public List<String> getCompletedTaskList()
    {
        TaskTrackerData data = getTaskData();
        List<CompletedTask> completed = new ArrayList<>(data.getCompleted());
        List<String> list = new ArrayList<>();

        if (completed != null)
        {
            for (CompletedTask task : completed)
            {
                list.add(task.getTask());
            }
        }

        return list;
    }

    // Helper function to get the current task as a string even if config is null
    public String getCurrentTaskAsString()
    {
        // Get current task
        String currentTask = getTaskData().getCurrentTask();
        currentTask = currentTask == null || currentTask.isEmpty() ? "No Current Task" : currentTask;
        return currentTask;

    }

    // Helper function to create the Comparator<String> for comparing numbers naturally
    private Comparator<String> createNaturalOrderComparator()
    {
        return (s1, s2) ->
        {
            // Extract leading numbers from both strings
            Integer n1 = extractLeadingNumber(s1);
            Integer n2 = extractLeadingNumber(s2);

            // If both have numbers, compare the numbers numerically
            if (n1 != null && n2 != null) {
                int numCompare = n1.compareTo(n2);
                if (numCompare != 0) return numCompare;
            }

            // If numbers are equal or one doesn't have a number, fallback to alphabetical
            return s1.compareToIgnoreCase(s2);
        };
    }

    // Helper function to grab the number at the start of the string
    private Integer extractLeadingNumber(String s) {
        try {
            String[] parts = s.split("\\s+"); // Split by space
            if (parts.length > 0) {
                // Remove any non-digit characters (like ':' or letters) from the first part
                String numStr = parts[0].replaceAll("\\D", "");
                return numStr.isEmpty() ? null : Integer.parseInt(numStr);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    // Helper function to get the current config color
    public Color getCurrentTaskBorderColor()
    {
        return config.currentTaskBorderColor();
    }

    // Helper function to get the current dateTimeFormat
    public SimpleDateFormat getDateTimeFormat()
    {
        return new SimpleDateFormat(config.timestampFormat().getPattern());
    }

    // Helper function to get the current timestamp format as a string
    public String getTimestampFormat()
    {
        return config.timestampFormat().getPattern();
    }
}
