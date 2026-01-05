package com.tasklock;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;


public class TaskLockPanel extends PluginPanel
{
    private final JLabel currentTask;
    private final JLabel activeHeader;
    private final JPanel activeListPanel;
    private final JLabel backlogHeader;
    private final JPanel backlogListPanel;
    private final JLabel completedHeader;
    private final JPanel completedTable;
    private final Border margin = BorderFactory.createEmptyBorder(10,10,10,10);
    private final Border line = BorderFactory.createLineBorder(Color.WHITE);
    private final Border compoundBorder = BorderFactory.createCompoundBorder(margin, BorderFactory.createCompoundBorder(line, margin));
    private final ConfigManager configManager;

    public TaskLockPanel(ConfigManager configManager)
    {
        super();
        this.configManager = configManager;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(6,6,6,6) );
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel content = new  JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ColorScheme.DARK_GRAY_COLOR);
        add(content, BorderLayout.NORTH);

        // Current Task
        JLabel currentTaskHeader = createHeader("Current Task");
        content.add(currentTaskHeader);
        currentTask = new  JLabel();
        currentTask.setBackground(ColorScheme.DARK_GRAY_COLOR);
        currentTask.setBorder(margin);
        setCurrentTask("");
        content.add(currentTask);
        JButton rollButton = createButton("Roll Task");
        content.add(rollButton);
        content.add(Box.createVerticalStrut(12));

        // Active Tasks
        activeHeader = createHeader("Active Tasks");
        content.add(activeHeader);
        activeListPanel = createListPanel();
        JScrollPane activeScroll = wrapScrollable(activeListPanel);
        content.add(activeScroll);

        JButton editActiveButton = createButton("Edit Active Task");
        content.add(editActiveButton);
        content.add(Box.createVerticalStrut(12));

        // Backlog Tasks
        backlogHeader = createHeader("Backlog");
        content.add(backlogHeader);
        backlogListPanel = createListPanel();
        JScrollPane backlogScroll = wrapScrollable(backlogListPanel);
        content.add(backlogScroll);
        JButton editBacklogButton = createButton("Edit Backlog");
        content.add(editBacklogButton);
        content.add(Box.createVerticalStrut(12));

        // Completed Tasks
        completedHeader = createHeader("Completed Tasks");
        content.add(completedHeader);
        completedTable = createListPanel();
        JScrollPane completedScroll = wrapScrollable(completedTable);
        content.add(completedScroll);

    }

    private JLabel createHeader(String title)
    {
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setBorder(margin);
        label.setBackground(ColorScheme.DARK_GRAY_COLOR);
        return label;
    }

    private JPanel createListPanel()
    {
        JPanel listPanel = new JPanel();
        listPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        return listPanel;
    }

    private JScrollPane wrapScrollable(JPanel listPanel)
    {
        JScrollPane scrollPane = new JScrollPane(listPanel);

        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        scrollPane.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        scrollPane.setBorder(compoundBorder);

        // This controls visible height
        scrollPane.setPreferredSize(new Dimension(0, 300));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        return scrollPane;
    }

    private JButton createButton(String title)
    {
        JButton button = new JButton(title);
        button.setBackground(ColorScheme.DARK_GRAY_COLOR);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        return button;
    }

    public void setCurrentTask(String task)
    {
        if (task.isEmpty() || task == null)
        {
            currentTask.setText("No current task");
        }
        else
        {
            currentTask.setText(task);
        }
    }

    public void setActiveTasks()
    {
        activeListPanel.removeAll();

        String configString = configManager.getConfiguration("tasklock","activeTasks");
        configString = configString == null ? "" : configString;
        List<String> tasks = new ArrayList<>(List.of(configString.split(",")));

        if(configString.isEmpty())
        {
            tasks.clear();
        }

        if(tasks.isEmpty())
        {
            activeListPanel.add(new JLabel("No active tasks"));
        }
        else
        {
            for( String task : tasks)
            {
                activeListPanel.add(new JLabel("• " + task));
            }
            activeHeader.setText("Active Tasks (" + (tasks.size()) + ")");
        }

        revalidate();
        repaint();
    }

    public void setBacklogTasks()
    {
        backlogListPanel.removeAll();

        String configString = configManager.getConfiguration("tasklock","backlogTasks");
        configString = configString == null ? "" : configString;
        List<String> tasks = new ArrayList<>(List.of(configString.split(",")));

        if(configString.isEmpty())
        {
            tasks.clear();
        }

        if(tasks.isEmpty())
        {
            backlogListPanel.add(new JLabel("No backlogged tasks"));
        }
        else
        {
            for( String task : tasks)
            {
                backlogListPanel.add(new JLabel("• " + task));
            }
            backlogHeader.setText("Backlog (" + (tasks.size()) + ")");
        }

        revalidate();
        repaint();
    }


    public void setCompletedTasks()
    {
        completedTable.removeAll();

        String configString = configManager.getConfiguration("tasklock","completedTasks");
        configString = configString == null ? "" : configString;
        List<String> tasks = new ArrayList<>(List.of(configString.split(",")));

        if(configString.isEmpty())
        {
            tasks.clear();
        }

        if (tasks.isEmpty())
        {
            completedTable.add(new JLabel("No completed tasks"));
        }
        else
        {
            for (String task : tasks)
            {
                String[] split = task.split("\\+");
                if (split.length == 2)
                {
                    String date =  split[0];
                    String name =  split[1];
                    completedTable.add(new JLabel("• " + name));
                }

            }
            completedHeader.setText("Completed Tasks (" + (tasks.size()) + ")");
        }

        revalidate();
        repaint();

    }

}
