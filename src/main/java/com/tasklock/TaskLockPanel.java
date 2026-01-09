package com.tasklock;

import com.google.gson.Gson;
import net.runelite.api.SpriteID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class TaskLockPanel extends PluginPanel
{
    // Strings used in ListPanels and Headers
    private final String currentString = "Current Task";
    private final String activeString = "Active Tasks";
    private final String backlogString = "Backlog";
    private final String completedString = "Completed Tasks";
    // Current Task section of UI
    private final JPanel currentTaskPanel = new JPanel();
    private final JLabel currentTaskLabel = new JLabel("No Current Task");
    private final JButton rollTaskButton = new JButton("Roll Task", ROLL_ICON);
    private final JButton completeTaskButton = new JButton("Complete Task", CHECK_ICON);
    private final JButton backlogTaskButton = new JButton("Backlog Task", ARROW_ICON);
    // Active List section of UI
    private final JLabel activeHeader = new JLabel(activeString);
    private final JPanel activeListPanel =  new JPanel();
    private final JButton activeButton = new JButton("Edit");
    // Backlog List section of UI
    private final JLabel backlogHeader =  new JLabel(backlogString);
    private final JPanel backlogListPanel =   new JPanel();
    private final JButton backlogButton =  new JButton("Edit");
    // Completed List section of UI
    private final JLabel completedHeader =  new JLabel(completedString);
    private final JPanel completedListPanel = new JPanel();
    private final JButton completedButton =  new JButton("Details");
    // Borders used in UI construction
    private final Border margin = BorderFactory.createEmptyBorder(10,10,10,10);
    private final Border line = BorderFactory.createLineBorder(Color.WHITE);
    private final Border compoundBorder = BorderFactory.createCompoundBorder(line, margin);
    // Managers and logger
    private final ConfigManager configManager;
    private final Gson gson;
    private final SpriteManager spriteManager;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TaskLockPanel.class);
    // Button Icons
    private static final ImageIcon ROLL_ICON;
    private static final ImageIcon ARROW_ICON;
    private static final ImageIcon CHECK_ICON;


    // Code run only once after initialized
    static {
        // This block runs once when the class is loaded
        final BufferedImage rollImg = ImageUtil.loadImageResource(TaskLockPlugin.class, "roll.png");
        ROLL_ICON = new ImageIcon(ImageUtil.resizeImage(rollImg, 16, 16));

        final BufferedImage backlogImg = ImageUtil.loadImageResource(TaskLockPlugin.class, "arrow.png");
        ARROW_ICON = new ImageIcon(ImageUtil.resizeImage(backlogImg, 16, 16));

        final BufferedImage checkImg = ImageUtil.loadImageResource(TaskLockPlugin.class, "icon.png");
        CHECK_ICON = new ImageIcon(ImageUtil.resizeImage(checkImg, 16, 16));

    }

    // Constructor
    public TaskLockPanel(ConfigManager configManager, Gson gson, SpriteManager spriteManager)
    {
        super();
        this.configManager = configManager;
        this.gson = gson;
        this.spriteManager = spriteManager;

        setLayout(new GridBagLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Add action listeners to buttons
        addButtonListeners();

    }

    // The main function to set up the UI
    public void setupSections()
    {
        // Initialize parent JPanel
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 0; // Row counter
        c.insets = new Insets(0, 0, 10, 0); // Bottom margin for spacing

        // Get content from config
        TaskLockData data = getTaskData();

        // SECTION 1: Current Task
        addSection(this, c, new JLabel(currentString), currentTaskPanel, rollTaskButton, getTaskData().getActive(), currentString);

        // SECTION 2: Active Tasks
        addSection(this, c, activeHeader, activeListPanel, activeButton, data.getActive(), activeString);

        // SECTION 3: Backlog
        addSection(this, c, backlogHeader, backlogListPanel, backlogButton, data.getBacklog(), backlogString);

        // SECTION 4: Completed Tasks
        addSection(this, c, completedHeader, completedListPanel, completedButton, getCompletedTaskList(), completedString);

        revalidate();
        repaint();
    }

    // Helper function to add specific section in UI
    private void addSection(JPanel parent, GridBagConstraints c, JLabel header, JPanel panel, JButton button, List<String> contentList, String baseHeader)
    {

        updateTaskButtonLabel();

        setupAndAddHeader(parent, c, header, contentList, baseHeader);

        setupAndAddListPanel(parent, c, panel, contentList, baseHeader);

        setupAndAddButtons(parent, c, panel, button, header, contentList, baseHeader);

        // Reset fill for next section's labels
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 10, 0);

    }

    // Function used to refresh UI after any config change
    public void refresh()
    {
        // Use SwingUtilities to ensure UI changes happen on the correct thread
        SwingUtilities.invokeLater(() -> {
            try
            {
                this.removeAll(); // Clear the entire panel
                setupSections();  // Re-run logic to add headers, lists, and buttons
                this.revalidate();
                this.repaint();
            }
            catch (Exception e)
            {
                logger.error("Error refreshing TaskLock panel",e);
            }

        });
    }

    // Get task data from config
    private TaskLockData getTaskData()
    {
        String json = configManager.getConfiguration("tasklock","allTasksJson");
        if (json == null || json.isEmpty())
        {
            return new TaskLockData();
        }
        return gson.fromJson(json, TaskLockData.class);
    }

    // Save task data to config
    private void saveTaskData(TaskLockData data)
    {
        String json = gson.toJson(data);
        configManager.setConfiguration("tasklock","allTasksJson",json);
    }

    // Button function roll a unique task, rerolling the same task is impossible
    private void rollTask()
    {
        logger.info("Rolling Task");
        TaskLockData data = getTaskData();

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

        Random random = new Random();
        String newCurrentTask = rollableTasks.get(random.nextInt(rollableTasks.size()));

        data.setCurrentTask(newCurrentTask);
        saveTaskData(data);
    }

    // Button function backlog or complete a task based on key
    private void backlogCompleteTask(String key)
    {
        logger.info("Backlog Complete Task Button Clicked");
        TaskLockData data = getTaskData();
        String currentTask = data.getCurrentTask();
        List<String> activeTasks = data.getActive();

        if (currentTask == null || currentTask.isEmpty() || currentTask.equals("No Current Task"))
        {
            logger.info("No Current Task");
            return;
        }

        if (activeTasks.isEmpty() || !activeTasks.contains(currentTask))
        {
            logger.info("No Active Tasks or Current Task not in list");
            return;
        }

        if (key.equals("backlog"))
        {
            data.getBacklog().add(currentTask);
        }
        else if (key.equals("complete"))
        {
            data.getCompleted().add(new CompletedTask(currentTask));
        }
        data.getActive().remove(currentTask);
        data.setCurrentTask("");

        saveTaskData(data);
    }

    // Button function allows user to edit active or backlogged tasks
    private void openEditDialog(String title, String key)
    {
        TaskLockData data = getTaskData();
        List<CompletedTask> completedTasks = data.getCompleted();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm").withZone(ZoneId.systemDefault());
        List<String> currentList;
        String windowTitle = "Edit " + title;

        // Determine which list we are editing
        switch (key) {
            case "active":
                currentList = data.getActive();
                break;
            case "backlog":
                currentList = data.getBacklog();
                break;
            case "completed":
                currentList = new ArrayList<>();
                for (CompletedTask task : completedTasks)
                {
                    currentList.add(formatter.format(task.getCompletedAt()) + " - " + task.getTask());
                }
                break;
            default:
                return;
        }

        // Convert List to a single String with new lines
        String currentText = String.join("\n", currentList);

        // Create a Text Area for the user to type in
        JTextArea textArea = new JTextArea(currentText);
        textArea.setRows(10);
        textArea.setColumns(40);
        textArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        textArea.setForeground(Color.WHITE);
        textArea.setCaretColor(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(textArea);

        // Layout the panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        if (!key.equals("completed"))
        {
            mainPanel.add(new JLabel("One task per line:"), BorderLayout.NORTH);
        }
        mainPanel.add(scrollPane, BorderLayout.CENTER);


        // Create the "Clear All" button
        JButton clearButton = new JButton("Clear All");
        clearButton.setFocusable(false);
        clearButton.addActionListener(e -> textArea.setText(""));
        mainPanel.add(clearButton, BorderLayout.SOUTH);


        if (key.equals("completed"))
        {
            textArea.setEditable(false);
            windowTitle = "Completed Tasks Details";
        }

        // Show the Dialog
        int result = JOptionPane.showConfirmDialog(
                this,
                mainPanel,
                windowTitle,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION)
        {
           if (key.equals("backlog") || key.equals("active") || (textArea.getText().isEmpty()))
           {
                updateListFromText(data, key, textArea.getText());
           }
        }
    }

    // Helper function to update inner TaskData Lists from text
    private void updateListFromText(TaskLockData data, String key, String text)
    {
        List<String> newList = new ArrayList<>();
        List<CompletedTask> completedTasks = new  ArrayList<>();

        for (String line : text.split("\n"))
        {
            if (!line.trim().isEmpty())
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
                data.setCompleted(completedTasks);
                break;
        }

        saveTaskData(data);
    }

    // Sets the current task icon based on recognized strings in taskText
    private void setTaskIcon(JLabel iconLabel, String taskText)
    {
        String text = taskText.toLowerCase();
        int spriteId = -1;

        if (text.contains("quest") || TaskChecker.containsQuest(text))
        {
            spriteId = SpriteID.TAB_QUESTS;
        }
        else if (text.contains("kill") || text.contains("slayer"))
        {
            spriteId = SpriteID.TAB_COMBAT;
        }
        else if (text.contains("level") || text.contains("xp") || TaskChecker.containsSkill(text))
        {
            spriteId = SpriteID.TAB_STATS;
        }
        else if (text.contains("obtain"))
        {
            spriteId = SpriteID.TAB_EQUIPMENT;
        }
        else if (text.contains("diary"))
        {
            spriteId = SpriteID.TAB_QUESTS_GREEN_ACHIEVEMENT_DIARIES;
        }
        else if (text.contains("buy"))
        {
            spriteId = SpriteID.GE_GUIDE_PRICE;
        }

        if (spriteId != -1)
        {
            // This helper handles the asynchronous loading of game sprites
            spriteManager.getSpriteAsync(spriteId, 0, (BufferedImage img) ->
            {
                if (img != null)
                {
                    SwingUtilities.invokeLater(() ->
                    {
                        iconLabel.setIcon(new ImageIcon(img));
                        iconLabel.revalidate();
                        iconLabel.repaint();
                    });
                }
            });
        }
        else
        {
            iconLabel.setIcon(null); // Clear if no match
        }
    }

    // Helper function to get the completed task list in the List<String> format
    private List<String> getCompletedTaskList()
    {
        TaskLockData data = getTaskData();
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
    private String getCurrentTaskAsString()
    {
        // Get current task
        String currentTask = getTaskData().getCurrentTask();
        currentTask = currentTask == null || currentTask.isEmpty() ? "No Current Task" : currentTask;
        return currentTask;

    }

    // Helper function to set the roll task button label
    private void updateTaskButtonLabel()
    {
        String currentTask = getCurrentTaskAsString();

        // Set Button Text
        if (currentTask.equals("No Current Task"))
        {
            rollTaskButton.setText("Roll Task");
        }
        else
        {
            rollTaskButton.setText("Reroll Task");
        }
    }

    // Helper function used to set up and add current header into parent panel
    private void setupAndAddHeader(JPanel parent, GridBagConstraints c, JLabel header, List<String> contentList, String baseHeader)
    {
        // Set Header Text for lists
        if(!contentList.isEmpty() && !baseHeader.equals(currentString))
        {
            header.setText(baseHeader + " (" + contentList.size() + ")");
        }
        else
        {
            header.setText(baseHeader);
        }

        // Add Header (Left Aligned)
        header.setFont(FontManager.getRunescapeBoldFont());
        c.anchor = GridBagConstraints.WEST;
        parent.add(header, c);
        c.gridy++;
    }

    // Helper function used to set up and add list panel into parent panel
    private void setupAndAddListPanel(JPanel parent, GridBagConstraints c, JPanel panel, List<String> contentList, String baseHeader )
    {
        String currentTask = getCurrentTaskAsString();

        // Add Content Text (Left Aligned)
        panel.removeAll();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(compoundBorder);
        panel.setOpaque(false);

        JLabel taskLabel;
        JLabel taskIcon = new JLabel();

        if (baseHeader.equals(currentString))
        {
            panel.setLayout(new BorderLayout(10,0));
            setTaskIcon(taskIcon,currentTask);
            currentTaskLabel.setText(currentTask);
            currentTaskLabel.setForeground(Color.WHITE);
            currentTaskLabel.setToolTipText(currentTask);
            currentTaskLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            panel.add(taskIcon, BorderLayout.WEST);
            panel.add(currentTaskLabel, BorderLayout.CENTER);

        }
        else
        {
            // If list is empty show the no tasks label
            if (contentList.isEmpty())
            {
                JLabel emptyLabel = new JLabel("No " + baseHeader);
                emptyLabel.setForeground(Color.WHITE);
                panel.add(emptyLabel);
            }
            // If list is not empty, add each task to the panel
            else
            {
                for (String task : contentList)
                {
                    taskLabel = new JLabel();
                    taskLabel.setText("• " + task);
                    taskLabel.setForeground(Color.WHITE);
                    if (task.equals(currentTaskLabel.getText()) && baseHeader.equals(activeString))
                    {
                        taskLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GREEN),BorderFactory.createEmptyBorder(3,0,3,3)));
                    }

                    taskLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    taskLabel.setToolTipText(task);
                    addMouseListeners(taskLabel,createPopupMenu(task,baseHeader));
                    panel.add(taskLabel);

                }
            }
        }

        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 1, 5, 1); // Tighten gap between text and button
        parent.add(panel, c);
        c.gridy++;
    }

    // Helper function used to set up buttons into parent panel
    private void setupAndAddButtons(JPanel parent, GridBagConstraints c, JPanel panel, JButton button, JLabel header, List<String> contentList, String baseHeader)
    {
        // Add Button (Centered)
        // Change anchor to CENTER and fill to NONE
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(5, 0, 20, 0); // Large margin at bottom of section

        if (baseHeader.equals(currentString))
        {
            JPanel buttonContainer = createAndAddTaskButtons();
            parent.add(buttonContainer, c);
        }
        else
        {
            parent.add(button, c);
        }

        c.gridy++;
    }

    // Helper function used to create the buttonContainer for buttons below current task
    private JPanel createAndAddTaskButtons()
    {
        JPanel buttonContainer = new JPanel(new GridBagLayout());
        buttonContainer.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 0, 2, 0);
        // Add roll task button ROW 1 BUTTON 1 (LEFT)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        rollTaskButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        rollTaskButton.setMargin(new Insets(2, 2, 2, 2));
        buttonContainer.add(rollTaskButton, gbc);
        // Add Backlog Task Button ROW 1 BUTTON 2 (RIGHT)
        gbc.gridx = 1;
        gbc.gridy = 0;
        backlogTaskButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        backlogTaskButton.setMargin(new Insets(2, 2, 2, 2));
        buttonContainer.add(backlogTaskButton, gbc);
        // Add Complete Task Button ROW 2 BUTTON 3 (CENTER)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;  // This makes the button span across both columns
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        completeTaskButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        buttonContainer.add(completeTaskButton,gbc);

        return buttonContainer;
    }

    // Helper function to add all action listeners to buttons
    private void addButtonListeners()
    {
        rollTaskButton.addActionListener(e -> rollTask());
        backlogTaskButton.addActionListener(e -> backlogCompleteTask("backlog"));
        completeTaskButton.addActionListener(e -> backlogCompleteTask("complete"));
        activeButton.addActionListener(e -> openEditDialog("Active Tasks","active"));
        backlogButton.addActionListener(e -> openEditDialog("Backlog","backlog"));
        completedButton.addActionListener(e -> openEditDialog("Completed Tasks", "completed"));
    }

    private JPopupMenu createPopupMenu(String task, String baseHeader)
    {
        JPopupMenu menu = new JPopupMenu();

        // Option 1: Delete specific task
        JMenuItem deleteItem = new JMenuItem("Delete Task");
        deleteItem.addActionListener(e -> {
            deleteTask(task,baseHeader);
        });

        //Option 2: Move back to Active
        if (!baseHeader.equals(activeString))
        {
            JMenuItem reactiveItem = new JMenuItem("Move to Active");
            reactiveItem.addActionListener(e -> {
                moveTaskToActive(task,baseHeader);
            });
            menu.add(reactiveItem);
        }
        // Option 3: Make current task / backlog task
        else
        {
            JMenuItem makeCurrentItem = new JMenuItem("Make Current Task");
            makeCurrentItem.addActionListener(e -> {
                makeCurrentTask(task);
            });
            menu.add(makeCurrentItem);

            JMenuItem backlogItem = new JMenuItem("Backlog Task");
            backlogItem.addActionListener(e -> {
                backlogTask(task);
            });
            menu.add(backlogItem);

        }

        menu.add(deleteItem);

        return menu;

    }

    // Helper function to add all mouse listeners
    private void addMouseListeners(JLabel label, JPopupMenu menu)
    {
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                label.setCursor(new Cursor(Cursor.HAND_CURSOR));
                label.setForeground(ColorScheme.BRAND_ORANGE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setForeground(Color.WHITE);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showMenu(e);
            }

            private void showMenu(MouseEvent e) {
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    // Menu function for right click delete task
    private void deleteTask(String task, String section)
    {
        TaskLockData data = getTaskData();

        switch(section)
        {
            case activeString:
                data.getActive().remove(task);
                break;
            case backlogString:
                data.getBacklog().remove(task);
                break;
            case completedString:
                data.getCompleted().removeIf(t -> t.getTask().equals(task));
                break;
            default:
                break;
        }

        saveTaskData(data);
    }

    // Menu function for right click move task to active
    private void moveTaskToActive(String task, String section)
    {
        TaskLockData data = getTaskData();

        switch(section)
        {
            case backlogString:
                data.getActive().add(task);
                data.getBacklog().remove(task);
                break;
            case completedString:
                data.getActive().add(task);
                data.getCompleted().removeIf(t -> t.getTask().equals(task));
                break;
            default:
                break;
        }

        saveTaskData(data);
    }

    // Menu function for right click make current task
    private void makeCurrentTask(String task)
    {
        TaskLockData data = getTaskData();

        data.setCurrentTask(task);

        saveTaskData(data);
    }

    // Menu function for right click backlog task
    private void backlogTask(String task)
    {
        TaskLockData data = getTaskData();

        data.getBacklog().add(task);
        data.getActive().remove(task);

        saveTaskData(data);
    }

}
