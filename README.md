
//Write how to create gui

# GuiManager

GuiManager is a powerful and flexible library for creating and managing in-game GUIs (Graphical User Interfaces) in Minecraft Bukkit/Spigot plugins. It simplifies the process of building interactive inventories, handling clicks, and managing multiple pages.

## Features

- **Easy GUI Creation**: Quickly create custom inventories with specified titles and row counts.
- **Item Management**: Add, set, and remove `GuiItem`s with associated click handlers.
- **Multi-Page Support**: Organize complex GUIs into multiple pages for better navigation.
- **Event Handling**: Register handlers for inventory open, close, and click events at both global GUI and individual item levels.
- **Persistent GUIs**: Save and load inventory contents to/from configuration files.
- **Skull Textures**: Easily use custom player heads with textures.

## Installation

1. **Download**: Download the latest release from the [releases page](link-to-releases-page).
2. **Add to your project**: Add the downloaded JAR file to your plugin's `libs` folder (if using Gradle/Maven) or directly to your build path.
3. **Dependency**: If you're using Maven or Gradle, add the following dependency to your `pom.xml` or `build.gradle` file:

   **Maven:**
   ```xml
   <repositories>
       <repository>
           <id>jitpack.io</id>
           <url>https://jitpack.io</url>
       </repository>
   </repositories>

   <dependency>
       <groupId>com.github.ItzYashvardhan</groupId>
       <artifactId>GuiManager</artifactId>
       <version>NOT-RELEASED-YET</version>
   </dependency>
   ```

   **Gradle:**
   ```gradle
   repositories {
       maven { url 'https://jitpack.io' }
   }

   dependencies {
       implementation 'com.github.ItzYashvardhan:GuiManager:NOT-RELEASED-YET'
   }
   ```

## Usage

### Creating a Simple GUI

To create a basic GUI, you first define a `GUISetting` which specifies the title and number of rows. Then, you use `GUI.create()` to instantiate the GUI.

```kotlin
val guiSetting = GUISetting("My Awesome GUI", 3) // 3 rows
val myGui = GUI.create(guiSetting)
```
### Adding Items

You can add `GuiItem`s to your GUI. `GuiItem` is a data class that represents an `ItemStack` with additional properties like `glow` and `skullTexture`. You can also attach an `onClick` handler to individual items.

```kotlin
val myGuiItem = GuiItem(Material.DIAMOND, "Shiny Diamond", lore = listOf("Very valuable!"), glow = true)

myGui.addItem(myGuiItem) { event ->
    event.whoClicked.sendMessage("You clicked the shiny diamond!")
}

// Or set an item at a specific slot
val anotherItem = GuiItem(Material.GOLD_INGOT, "Gold Bar")
myGui.setItem(4, anotherItem) { event ->
    event.whoClicked.sendMessage("You clicked the gold bar at slot 4!")
}
```

### Handling Events

You can register global handlers for click, open, and close events on your GUI.

```kotlin
myGui.onClick { event ->
    event.isCancelled = true // Prevent players from taking items out
    // Global click logic
}

myGui.onOpen { event ->
    event.player.sendMessage("Welcome to the GUI!")
}

myGui.onClose { event ->
    event.player.sendMessage("Thanks for visiting!")
}
```

### Multi-Page GUIs

GuiManager supports multiple pages within a single GUI instance. You can create and manage different pages and navigate between them.
val pagerGui = GUI.create(GUISetting("My Paged GUI", 6))

**Create page 1**
```kotlin
val page1Setting = GUISetting("Page 1", 3)
pagerGui[1] = pagerGui.createPage(page1Setting).apply {
    addItem(GuiItem(Material.STONE, "Item on Page 1")) { event ->
        event.whoClicked.sendMessage("You clicked an item on Page 1!")
    }
}
```
**Create page 2**
```kotlin


val page2Setting = GUISetting("Page 2", 3)
pagerGui[2] = pagerGui.createPage(page2Setting).apply {
    addItem(GuiItem(Material.IRON_INGOT, "Item on Page 2")) { event ->
        event.whoClicked.sendMessage("You clicked an item on Page 2!")
    }
}
```

**Add navigation items to the main GUI (or each page)**
```kotlin
pagerGui.setItem(48, GuiItem(Material.ARROW, "Go to Page 1")) { event ->
    pagerGui.openPage(event.whoClicked as Player, 1)
}
pagerGui.setItem(50, GuiItem(Material.ARROW, "Go to Page 2")) { event ->
    pagerGui.openPage(event.whoClicked as Player, 2)
}
```

**Open the initial page (main GUI)**
```kotlin
player.openInventory(pagerGui.inventory)
```

**To save an inventory**
```kotlin
ConfigHandler.saveInventory(plugin.dataFolder, "inventories", "my_saved_gui", myGui.inventory)
```

**To load an inventory**
```kotlin
val loadedInventory = ConfigHandler.loadInventory(plugin.dataFolder, "inventories", "my_saved_gui")
if (loadedInventory != null) {
    val loadedGui = GUI.load(loadedInventory, guiSetting)
    player.openInventory(loadedGui.inventory)
} else {
    player.sendMessage("Could not load inventory 'my_saved_gui'.")
}
```