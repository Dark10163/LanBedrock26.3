package dev.bedrocklan;

import net.fabricmc.loader.api.FabricLoader;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GeyserLauncher {
    private static final String URL =
        "https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/standalone";
    private static Process proc;

    public static void start(int javaPort) {
        Thread t = new Thread(() -> run(javaPort), "bedrock-lan-launcher");
        t.setDaemon(true);
        t.start();
    }

    private static void run(int javaPort) {
        try {
            Path dir = FabricLoader.getInstance().getGameDir().resolve("bedrock-lan");
            Files.createDirectories(dir);
            Path jar = dir.resolve("Geyser-Standalone.jar");
            if (!Files.exists(jar)) {
                try (var in = URI.create(URL).toURL().openStream()) {
                    Files.copy(in, jar);
                }
            }

            // First run: let Geyser generate its own default config, then patch it.
            // This keeps working even if Geyser changes its config layout.
            Path cfg = dir.resolve("config.yml");
            if (!Files.exists(cfg)) {
                Process first = spawn(dir, jar);
                for (int i = 0; i < 60 && !Files.exists(cfg); i++) Thread.sleep(500);
                Thread.sleep(1500);
                first.destroy();
                first.waitFor(5, TimeUnit.SECONDS);
            }
            patchConfig(cfg, javaPort);
            proc = spawn(dir, jar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Process spawn(Path dir, Path jar) throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        return new ProcessBuilder(java, "-jar", jar.toString(), "--nogui")
            .directory(dir.toFile()).inheritIO().start();
    }

    /** Sets address/port/auth-type inside the remote: (older) or java: (newer) section. */
    private static void patchConfig(Path cfg, int javaPort) throws Exception {
        List<String> out = new ArrayList<>();
        String section = "";
        for (String line : Files.readAllLines(cfg)) {
            if (line.matches("^[A-Za-z0-9_-]+:\\s*(#.*)?$")) {
                section = line.substring(0, line.indexOf(':'));
            } else if (section.equals("remote") || section.equals("java")) {
                String trimmed = line.trim();
                String indent = line.substring(0, line.length() - line.stripLeading().length());
                if (trimmed.startsWith("address:")) line = indent + "address: 127.0.0.1";
                else if (trimmed.startsWith("port:")) line = indent + "port: " + javaPort;
                else if (trimmed.startsWith("auth-type:")) line = indent + "auth-type: offline";
            }
            out.add(line);
        }
        Files.write(cfg, out);
    }

    public static void stop() {
        if (proc != null) {
            proc.destroy();
            proc = null;
        }
    }
}
