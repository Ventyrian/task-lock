# Task Tracker
Allows for the user to add custom tasks to a list and roll a random task to complete. Once the task is completed it will be logged with the date it was completed. The plugin also supports backlogging tasks if enabled.

## Features
- Ability to have three sets of tasks: active, backlog, and completed
- Ability to roll a random task from the set of active tasks
- Completed tasks will have a date associated with it, to keep track of progress
- Ability to configure UI elements such as border color, milestone highlight, date format, and more
- Icons associated with keywords will be displayed next to the current task

Keywords include: *Quest* or the *Quest Name*, *Kill* or *Slayer*, *Level*, *exp*, or any *skill name*, *Obtain*, *Diary*, and *Buy*

These are not case sensitive and some have priority over others

## Configuration

<img width="351" height="544" alt="image" src="https://github.com/user-attachments/assets/1e5662d5-1cc9-429d-b0a6-cafad1190613" />

- *Enable Sounds* determines if the SFX associated with the Roll, Backlog, and Complete Buttons is played
- *Enable Backlog* determines if the backlog box and button is displayed
- *Remove from Active* determines if the completed task is removed from the active task list after completion
- *Border Color* determines the color of the border the current active task will have within the active task list
- *Date Format* determines the format of the date and time to be displayed in the *Edit | Details* window of the completed task list
- *Sort Order* determines the order in which completed tasks are displayed
- *Milestone Interval* determines the number of completed tasks required to highlight the next milestone (if set to 0 no milestones will be set)
- *Milestone Color* determines the color of the highlight a milestone will have
- *WARNING Section* a warning to remind the user that if the *Reset* button is clicked in the configuration panel all active, backlogged, and completed tasks will be lost

## UI
<img width="404" height="1343" alt="image" src="https://github.com/user-attachments/assets/2ca90a55-1a10-4729-b6dd-50ec243bde5f" />

- The current task along with its associated icon is displayed at the top
- The roll/reroll, backlog, and complete task buttons are underneath the current task
- The active task list are all the rollable tasks, with the current task having the configured border
- The tasks within the list will light up orange when hovered, and may be right clicked to perform certain actions e.g.) *Make current task*, *Move to active*, *Delete Task*
- The *Edit* buttons allow the user to input data into each respective list, one task per line
- The *Edit | Details* button allows the user to see the date tasks were completed and add more data following the same format

