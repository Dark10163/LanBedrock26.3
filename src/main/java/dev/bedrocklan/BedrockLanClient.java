package dev.bedrocklan;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.Component;

public class BedrockLanClient implements ClientModInitializer {
    static boolean enabled = false;
    static boolean launched = false;

    @Override
    public void onInitializeClient() {
        // Add the toggle to the Open to LAN screen
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof ShareToLanScreen) {
                Screens.getButtons(screen).add(
                    CycleButton.onOffBuilder(enabled).create(
                        w - 160, 10, 150, 20,
                        Component.literal("Allow Bedrock"),
                        (btn, val) -> enabled = val));
            }
        });

        // Once the LAN world is published, start Geyser pointed at its port
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            var server = mc.getSingleplayerServer();
            if (enabled && !launched && server != null && server.isPublished()) {
                launched = true;
                // LAN worlds are online-mode, which blocks Bedrock players without a Java
                // account. Switch to offline so they can join with just their gamertag.
                server.setUsesAuthentication(false);
                GeyserLauncher.start(server.getPort());
            }
            if (server == null && launched) {
                launched = false;
                GeyserLauncher.stop();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(GeyserLauncher::stop));
    }
}
