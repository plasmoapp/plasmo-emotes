[![](http://img.shields.io/discord/833693644501286993?label=Discord&style=flat&logo=discord)](https://discord.gg/uueEqzwCJJ)

![](https://i.imgur.com/yRmcrIH.png)

<div>
  <div>
    <a href="https://github.com/plasmoapp/plasmo-emotes/">GitHub</a>
    <span> | </span>
    <a href="https://www.spigotmc.org/resources/plasmo-emotes.100598/">Spigot</a>
    <span> | </span>
    <a href="https://discord.com/invite/uueEqzwCJJ">Discord</a>
  </div>
</div>

# Plasmo Emotes

Plasmo Emotes is a server plugin that automatically generates a resource pack that contains custom chat emotes.

It uses a clever trick with translatable components. Players without the resource pack won't notice a difference. They will see just the text. 

![](https://i.imgur.com/f9o5fFz.gif)

Components are replaced when the message is received. Old messages have these weird rectangles, but the new ones don't.

You also need to have a plugin installed on the server if you want it to work. It replaces text with symbols.

May not work with some chat plugins. You can fix this with [Plasmo Emotes API](https://github.com/plasmoapp/plasmo-emotes/blob/main/API.md).

## How to use

1. Download the plugin and put it into the `/plugins/` folder
2. Restart the server
3. Drop your custom emotes into the `/plugins/PlasmoEmotes/emotes/` folder. The plugin will automatically generate a resource pack that will replace `Kappa` with `Kappa.png`. The file name is used as the emote name.
4. Restart the server
5. There is now `emotes.zip` in the plugin folder. Share this resource pack among the players, set it as your default server pack, or merge it with your other resource pack. It's optional, so you can do whatever you want with it. 

## bStats

[![](https://bstats.org/signatures/bukkit/plasmo%20emotes.svg)](https://bstats.org/plugin/bukkit/Plasmo%20Emotes/14583)
