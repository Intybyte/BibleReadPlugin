rootProject.name = "BibleReadPlugin"

include("api")

include("bukkit")
project(":bukkit").projectDir = file("./bukkit_main/bukkit")

include("spigot")
project(":spigot").projectDir = file("./bukkit_main/spigot")

include("paper")
project(":paper").projectDir = file("./bukkit_main/paper")

include("sponge8")