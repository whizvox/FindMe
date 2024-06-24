# Find Me

Find Me is a Spigot plugin for adding findable objects in your Minecraft server. Supports collection tracking, multiple
collections, text localization, and plenty of flexible admin commands to manage everything.

## Version Compatibility

Only testing has been done on Spigot 1.20.4. Will update this section once more testing has been done with
other versions.

## How to Run

Requirements: Java 17+ and CraftBukkit-based server (i.e. Spigot, Paper) running Minecraft 1.20+

If you just want a JAR file to put into your own plugins folder, run `./gradlew jar`, and the output will be located in
the `build/libs` folder.

If you want a more automated solution, run `./gradlew copyToRun`. This will produce a JAR file and copy it in the
`run/plugins` folder while deleting older versions of the same plugin. Ideally, you would run a Bukkit-based server
executable in the `run` folder.

## License

This project is licensed under the MIT License, a copy of which is provided as `LICENSE.txt`.