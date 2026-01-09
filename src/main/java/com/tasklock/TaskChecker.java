package com.tasklock;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Arrays;

public class TaskChecker
{
    private static final Pattern SKILL_PATTERN = compilePattern("Attack", "Strength", "Defence", "Ranged",
            "Prayer", "Magic", "Runecraft", "Construction", "Hitpoints", "Agility", "Herblore", "Thieving",
            "Crafting", "Fletching", "Slayer", "Hunter", "Mining", "Smithing", "Fishing", "Cooking",
            "Firemaking", "Woodcutting", "Farming", "Sailing");
    private static final Pattern QUEST_PATTERN = compilePattern("A Kingdom Divided", "A Night at the Theatre", "A Porcine of Interest", "A Soul's Bane", "A Tail of Two Cats", "A Taste of Hope", "Alfred Grimhand's Barcrawl", "Animal Magnetism", "Another Slice of H.A.M.", "Architectural Alliance",
            "At First Light", "Barbarian Training", "Bear Your Soul", "Below Ice Mountain", "Between a Rock...", "Big Chompy Bird Hunter", "Biohazard", "Black Knights' Fortress", "Blood Runs Deep", "Bone Voyage",
            "Cabin Fever", "Children of the Sun", "Clock Tower", "Cold War", "Contact!", "Cook's Assistant", "Creature of Fenkenstrain", "Curse of the Empty Lord", "Daddy's Home", "Death on the Isle",
            "Death Plateau", "Death to the Dorgeshuun", "Demon Slayer", "Desert Treasure I", "Desert Treasure II - The Fallen Empire", "Devious Minds", "Dig Site", "Dragon Slayer I", "Dragon Slayer II", "Dream Mentor",
            "Eadgar's Ruse", "Eagles' Peak", "Elemental Workshop I", "Elemental Workshop II", "Enakhra's Lament", "Enchanted Key", "Enlightened Journey", "Enter the Abyss", "Ernest the Chicken", "Ethical Homunculus",
            "Fairytale I - Growing Pains", "Fairytale II - Cure a Queen", "Family Crest", "Family Pest", "Fight Arena", "Fishing Contest", "Forgettable Tale...", "Garden of Death", "Gerev's Grimoire", "Ghosts Ahoy",
            "Giant Dwarf", "Goblin Diplomacy", "Grim Tales", "Haunted Mine", "Hazeel Cult", "Heroes' Quest", "His Faithful Servants", "Holy Grail", "Hopespear's Will", "Horror from the Deep",
            "Icthlarin's Little Helper", "Imp Catcher", "In Aid of the Myreque", "In Search of Knowledge", "In Search of the Myreque", "Into the Tombs", "Jungle Potion", "King's Ransom", "Lair of Tarn Razorlor", "Legends' Quest",
            "Lost City", "Lunar Diplomacy", "Mage Arena I", "Mage Arena II", "Making Friends with Myarm", "Making History", "Merlin's Crystal", "Misthalin Mystery", "Monkey Madness I", "Monkey Madness II",
            "Monk's Friend", "Mountain Daughter", "Mourning's End Part I", "Mourning's End Part II", "Murder Mystery", "My Arm's Big Adventure", "Nature Spirit", "Observatory Quest", "Olaf's Quest", "One Small Favour",
            "Path of Glouphrie", "Perilous Moons", "Pirate's Treasure", "Plague City", "Priest in Peril", "Prince Ali Rescue", "Rag and Bone Man I", "Rag and Bone Man II", "Ratcatchers", "Recipe for Disaster",
            "Recruitment Drive", "Regicide", "Roving Elves", "Royal Trouble", "Rum Deal", "Rune Mysteries", "Scorpion Catcher", "Sea Slug", "Shades of Mort'ton", "Shadow of the Storm",
            "Sheep Herder", "Sheep Shearer", "Shield of Arrav", "Shilo Village", "Sins of the Father", "Skippy and the Mogres", "Sleeping Giants", "Song of the Elves", "Spirits of the Elid", "Swan Song",
            "Tai Bwo Wannai Trio", "Tears of Guthix", "Temple of Ikov", "Temple of the Eye", "The Ascent of Arceuus", "The Corsair Curse", "The Dig Site", "The Eyes of Glouphrie", "The Feud", "The Forsaken Tower",
            "The Fremennik Isles", "The Fremennik Trials", "The Frozen Door", "The General's Shadow", "The Golem", "The Grand Tree", "The Hand in the Sand", "The Knight's Sword", "The Lost Tribe", "The Restless Ghost",
            "The Ribbiting Tale of a Lily Pad Laborer", "The Slug Menace", "The Tourist Trap", "The Vault", "Throne of Miscellania", "Tower of Life", "Tree Gnome Village", "Tribal Totem", "Troll Romance", "Troll Stronghold",
            "Twilight's Promise", "Underground Pass", "Vampyre Slayer", "Watchtower", "Waterfall Quest", "What Lies Below", "While Guthix Sleeps", "Witch's House", "Witch's Potion", "X Marks the Spot",
            "Zogre Flesh Eaters");

    public static boolean containsSkill(String text)
    {
        return contains(SKILL_PATTERN,text);
    }

    public static boolean containsQuest(String text)
    {
        return contains(QUEST_PATTERN,text);
    }

    private static boolean contains(Pattern pattern, String text)
    {
        if (text == null)
        {
            return false;
        }
        return pattern.matcher(text).find();
    }

    private static Pattern compilePattern(String... terms)
    {
        String regex = Arrays.stream(terms)
                .map(Pattern::quote) // Safely handles special characters like "..."
                .collect(Collectors.joining("|"));

        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

}
