# Bedrock LAN (Fabric, Minecraft 26.3)

Adds an "Allow Bedrock" toggle to the Open to LAN screen. Turn it on, click Start LAN World,
and the mod starts Geyser Standalone (downloaded on first use into `bedrock-lan/`) pointed at
your LAN port. It also switches the LAN server to offline mode, so Bedrock players join with
only their gamertag: no Java account needed.

Bedrock players: Servers tab > Add Server > your PC's local IP, port 19132 (same Wi-Fi).

## Build
Needs Java 25 and Gradle 9.x. Run `gradle build`. The jar is in `build/libs/`.
If a version pin fails, generate a fresh template at https://fabricmc.net/develop for 26.3
and copy its build.gradle / gradle.properties / wrapper over mine; the src/ folder stays the same.

## Build with GitHub Actions (no local setup)
Upload this folder to a new GitHub repo (keep the .github folder). Open the Actions tab,
run "Build mod", then download the `bedrock-lan-jar` artifact. Use the jar without `-sources`.
