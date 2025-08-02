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


---
## Usage

### PREPARE ITEMS

```kotlin
val nextItem = ItemStack(Material.ARROW).toGuiItem().apply { displayName = "next" }
val prevItem = ItemStack(Material.ARROW).toGuiItem().apply { displayName = "prev" }

val item1 = ItemStack(Material.PAPER).toGuiItem()
val item2 = ItemStack(Material.DIAMOND).toGuiItem()
val item3 = ItemStack(Material.STONE).toGuiItem()
val item4 = ItemStack(Material.IRON_SWORD).toGuiItem()
```


### CREATE GUI

```kotlin
val gui = ChestGUI("Pager GUI", 6) {...}
```


### SETUP FUNCTIONALITIES

```kotlin
ChestGUI("Pager GUI", 6) {
    
    onClick{ it.isCancelled = true }
    
    
    nav {
        this.nextItem = nextItem
        this.prevItem = prevItem
        this.margin = 3
    }
   
    onOpen {
        sender.sendMessage("Opening")
    }
   
   addPage(title = "PAGE 1", gui = 3){
       addItem(item1) {
           it.whoClicked.sendMessage("Clicked on Item 1")
       }
      addItem(item2) {
          it.whoClicked.sendMessage("Clicked on Item 2")
      }
   }
   
   onPageClose{
       sender.sendMessage("Closing a Page")
   }
   
}
```





