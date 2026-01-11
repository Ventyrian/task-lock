package com.tasktracker;

import net.runelite.api.SpriteID;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskTrackerPanel extends PluginPanel
{
    // Strings used in ListPanels and Headers
    public static final String currentString = "Current Task";
    public static final String activeString = "Active Tasks";
    public static final String backlogString = "Backlog";
    public static final String completedString = "Completed Tasks";
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
    private final TaskTrackerPlugin plugin;
    private final SpriteManager spriteManager;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TaskTrackerPanel.class);
    // Button Icons
    private static final ImageIcon ROLL_ICON;
    private static final ImageIcon ARROW_ICON;
    private static final ImageIcon CHECK_ICON;


    // Code run only once after initialized
    static {
        // This block runs once when the class is loaded
        final BufferedImage rollImg = ImageUtil.loadImageResource(TaskTrackerPlugin.class, "img/roll.png");
        ROLL_ICON = new ImageIcon(ImageUtil.resizeImage(rollImg, 16, 16));

        final BufferedImage backlogImg = ImageUtil.loadImageResource(TaskTrackerPlugin.class, "img/arrow.png");
        ARROW_ICON = new ImageIcon(ImageUtil.resizeImage(backlogImg, 16, 16));

        final BufferedImage checkImg = ImageUtil.loadImageResource(TaskTrackerPlugin.class, "img/icon.png");
        CHECK_ICON = new ImageIcon(ImageUtil.resizeImage(checkImg, 16, 16));

    }

    // Constructor
    public TaskTrackerPanel(TaskTrackerPlugin plugin, SpriteManager spriteManager)
    {
        super();
        this.plugin = plugin;
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
        TaskTrackerData data = plugin.getTaskData();

        // SECTION 1: Current Task
        addSection(this, c, new JLabel(currentString), currentTaskPanel, rollTaskButton, data.getActive(), currentString);

        // SECTION 2: Active Tasks
        addSection(this, c, activeHeader, activeListPanel, activeButton, data.getActive(), activeString);

        // SECTION 3: Backlog
        addSection(this, c, backlogHeader, backlogListPanel, backlogButton, data.getBacklog(), backlogString);

        // SECTION 4: Completed Tasks
        addSection(this, c, completedHeader, completedListPanel, completedButton, plugin.getCompletedTaskList(), completedString);

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
                logger.error("Error refreshing TaskTracker panel",e);
            }

        });
    }

    // Button function allows user to edit tasks
    private void openEditDialog(String title, String key)
    {
        TaskTrackerData data = plugin.getTaskData();
        List<CompletedTask> completedTasks = data.getCompleted();
        SimpleDateFormat dateFormat = plugin.getDateTimeFormat();
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
                    if (dateFormat != null)
                    {
                        Date date = new Date(task.getCompletedAt());

                        currentList.add(dateFormat.format(date) + " - " + task.getTask());
                    }
                    else
                    {
                        currentList.add(task.getTask());
                    }
                }
                break;
            default:
                return;
        }

        // Convert List to a single String with new lines, this will be the starting text
        String textToShow = String.join("\n", currentList);

        while (true)
        {
            // Create a Text Area for the user to type in
            JTextArea textArea = new JTextArea(textToShow);
            textArea.setRows(10);
            textArea.setColumns(40);
            textArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            textArea.setForeground(Color.WHITE);
            textArea.setCaretColor(Color.WHITE);

            JScrollPane scrollPane = new JScrollPane(textArea);

            // Layout the panel
            JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
            mainPanel.add(new JLabel("One task per line:"), BorderLayout.NORTH);
            mainPanel.add(scrollPane, BorderLayout.CENTER);


            // Create the "Clear All" button
            JButton clearButton = new JButton("Clear All");
            clearButton.setFocusable(false);
            clearButton.addActionListener(e -> textArea.setText(""));
            mainPanel.add(clearButton, BorderLayout.SOUTH);


            // Show the Dialog
            int result = JOptionPane.showConfirmDialog(
                    this,
                    mainPanel,
                    windowTitle,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            // User cancelled
            if (result != JOptionPane.OK_OPTION)
            {
                break;
            }

            // Try to update
            boolean success = plugin.updateListFromText(data, key, textArea.getText());

            if (success)
            {
                break;
            }

            // If success is FALSE, the loop runs again
            // Update textToShow so the textArea re-opens with the User's bad input so they can fix it instead of typing it all again
            textToShow = textArea.getText();

        }
    }

    // Helper function for clean error alerts
    public void showError(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Edit Failed", JOptionPane.ERROR_MESSAGE);
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

    // Helper function to set the roll task button label
    private void updateTaskButtonLabel()
    {
        String currentTask = plugin.getCurrentTaskAsString();

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
        String currentTask = plugin.getCurrentTaskAsString();

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
                int index = 1;
                for (String task : contentList)
                {
                    taskLabel = new JLabel();
                    if (baseHeader.equals(completedString))
                    {
                        taskLabel.setText(index++ + ". " + task);
                    }
                    else
                    {
                        taskLabel.setText("• " + task);
                    }
                    taskLabel.setForeground(Color.WHITE);
                    if (task.equals(currentTaskLabel.getText()) && baseHeader.equals(activeString))
                    {
                        taskLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(plugin.getCurrentTaskBorderColor()),BorderFactory.createEmptyBorder(3,0,3,3)));
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
        rollTaskButton.addActionListener(e -> plugin.rollTask());
        backlogTaskButton.addActionListener(e -> plugin.backlogCompleteTask("backlog"));
        completeTaskButton.addActionListener(e -> plugin.backlogCompleteTask("complete"));
        activeButton.addActionListener(e -> openEditDialog("Active Tasks","active"));
        backlogButton.addActionListener(e -> openEditDialog("Backlog","backlog"));
        completedButton.addActionListener(e -> openEditDialog("Completed Tasks", "completed"));
    }

    // Helper function to add the right click menu to list items within panels
    private JPopupMenu createPopupMenu(String task, String baseHeader)
    {
        JPopupMenu menu = new JPopupMenu();

        // Option 1: Delete specific task
        JMenuItem deleteItem = new JMenuItem("Delete Task");
        deleteItem.addActionListener(e -> {
            plugin.deleteTask(task,baseHeader);
        });

        //Option 2: Move back to Active
        if (!baseHeader.equals(activeString))
        {
            JMenuItem reactiveItem = new JMenuItem("Move to Active");
            reactiveItem.addActionListener(e -> {
                plugin.moveTaskToActive(task,baseHeader);
            });
            menu.add(reactiveItem);
        }
        // Option 3: Make current task / backlog task
        else
        {
            if (!task.equals(plugin.getCurrentTaskAsString()))
            {
                JMenuItem makeCurrentItem = new JMenuItem("Make Current Task");
                makeCurrentItem.addActionListener(e -> {
                    plugin.makeCurrentTask(task);
                });
                menu.add(makeCurrentItem);
            }
            else
            {
                JMenuItem resetCurrentItem = new JMenuItem("Reset Current Task");
                resetCurrentItem.addActionListener(e -> {
                    plugin.makeCurrentTask("");
                });
                menu.add(resetCurrentItem);
            }

            JMenuItem backlogItem = new JMenuItem("Backlog Task");
            backlogItem.addActionListener(e -> {
                plugin.backlogTask(task);
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

}
