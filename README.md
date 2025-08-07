# LimeFrameGUI

LimeFrameGUI is a powerful and flexible library for creating and managing in-game GUIs (Graphical User Interfaces) in Minecraft Bukkit/Spigot plugins.
It simplifies the process of building interactive inventories, handling clicks, and managing multiple pages.
It is based on KotlinDSL

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
       <artifactId>LimeFRAMEGUI</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```

   **Gradle:**
   ```gradle
   repositories {
       maven { url 'https://jitpack.io' }
   }

   dependencies {
       implementation 'com.github.ItzYashvardhan:LimeFrameGUI:1.0.0'
   }
   ```


---
## Usage


### Initialize and setup the api 
```kotlin
override fun onEnable() {
    LimeFrameAPI.init(this)
    
    //These keys use for loading and saving item data in configuration
    //See sample config file bellow
    LimeFramAPI.setKeys{
        material = "item" //Default "material"
        name = "displayName" //Default "name"
        lore = "lore" //Default "lore"
    }
    FrameColor.colorType = ColorType.MINI_MESSAGE //Set Color support like this
}
```

### sample config file "simple.yml"
```yaml
simplegui:
  size: 54
  title: Inventory
  items:
    '13':
      displayName: Simple
      material: NETHERRACK
      lore: []
      glow: true
      flags:
      - HIDE_ENCHANTS
      amount: 64
    '22':
      displayName: Simple
      material: NETHERRACK
      lore: []
      glow: true
      flags:
      - HIDE_ENCHANTS
      amount: 64
```


### PREPARE ITEMS

```kotlin
val nextItem = ItemStack(Material.ARROW).toGuiItem().apply { displayName = "next" }
val prevItem = ItemStack(Material.ARROW).toGuiItem().apply { displayName = "prev" }

val item1 = ItemStack(Material.PAPER).toGuiItem()
val item2 = ItemStack(Material.DIAMOND).toGuiItem()
val item3 = ItemStack(Material.STONE).toGuiItem()

// You can use Config Handler to load or store item
private val config = ConfigHandler("simple.yml")

//load item
val item4 = config.loadItem("simplegui.items.13") ?: ItemStack(Material.IRON_SWORD).toGuiItem()
val item5 = config.loadItem("simplegui.items.22") ?: ItemStack(Material.IRON_SWORD).toGuiItem()

//save example
config.saveItem("simplegui.items.10", item1)
config.saveItem("simplegui.items.item2", item2)

//for loading inventorySetting
val setting = config.loadInventorySetting("simplegui") //return GUISetting

//call reload to update new changes if made
config.reload()

//Note: You can also load and save a whole inventory or list of items
```


### CREATE SIMPLE GUI

```kotlin
ChestGUI(setting.rows,setting.title) {
    //GUI Configuration Code Goes here
}.open(player)
```

### SETUP FUNCTIONALITIES

```kotlin
ChestGUI(6, "Pager GUI") {

    //Global Click handler
    onClick { it.isCancelled = true }

    //Navigation Buttons 
    nav {
        //Buttons always appear in bottom
        this.nextItem = nextItem
        this.prevItem = prevItem
        this.margin = 3

        //Custom slot supported but it will not support dynamic rows
        //e.g. this.nextSlot = 11 
        //e.g. this.prevSlot = 11
    }

    //Global Open Event Handlers. It will not call for page Opening
    onOpen {
        sender.sendMessage("Opening")
    }

    //Global Close Event Handlers. It will not call for page Closing
    onClose {
        //Only close if inventory successfully closed
        sender.sendMessage("Closing")
    }

    //Use custom inbuilt placeholder {page} to get current Page
    addPage(title = "PAGE {page}", gui = 3) {
        addItem(item1) {
            it.whoClicked.sendMessage("Clicked on Item 1")
        }
        
        addItem(item2) {
            it.whoClicked.sendMessage("Clicked on Item 2")
        }
    }
    
    //You can used single and multiple slot to set item
    item3.slot = 13
    item4.slotList = listOf(0, 1, 2, 3,4,5)
    item5.slot = 3

    addPage(title = "PAGE {page}", gui = 4) {
        setItem(item3) {
            it.whoClicked.sendMessage("Clicked on Item 3")
        }
        setItem(item4) {
            it.whoClicked.sendMessage("Clicked on Item 4")
        }

        //This will override the current slot, same thing applied for multiple slot too.
        setItem(item5,10) {
            it.whoClicked.sendMessage("Clicked on Item 2")
        }
    }

    //Calls Everytime a page has been opened
    onPageOpen {
        sender.sendMessage("Opening a Page")
    }

    //Calls Everytime a page has been closed
    onPageClose {
        sender.sendMessage("Closing a Page")
    }
}
```
**Order of Steps doesn't matter** 





