package com.example.outputexporter.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class Exporter {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final MinecraftServer server;
    private final Path exportDir;

    public Exporter(MinecraftServer server) {
        this.server = server;
        this.exportDir = server.getServerDirectory().toPath().resolve("export");
        try {
            Files.createDirectories(exportDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create export directory", e);
        }
    }

    public int exportItems() {
        JsonObject output = new JsonObject();
        JsonObject objectSection = new JsonObject();
        output.add("object", objectSection);

        var registryAccess = server.registryAccess();
        var itemRegistry = registryAccess.registryOrThrow(Registries.ITEM);

        itemRegistry.stream()
                .sorted(Comparator.comparing(item -> itemRegistry.getKey(item).toString()))
                .forEach(item -> {
                    ResourceLocation itemId = itemRegistry.getKey(item);
                    String namespace = itemId.getNamespace();
                    String path = itemId.getPath();

                    ItemStack stack = new ItemStack(item);
                    String displayName = stack.getDisplayName().getString();
                    String modName = getModName(namespace);

                    JsonObject modSection = getOrCreateModSection(objectSection, namespace);
                    JsonObject itemData = new JsonObject();
                    itemData.addProperty("name", displayName);
                    itemData.addProperty("mod_name", modName);
                    modSection.add(path, itemData);
                });

        Path outputFile = exportDir.resolve("items.json");
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            GSON.toJson(output, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write items export file", e);
        }

        return (int) itemRegistry.count();
    }

    public int exportSounds() {
        JsonObject output = new JsonObject();
        JsonObject musicSection = new JsonObject();
        output.add("music", musicSection);

        var registryAccess = server.registryAccess();
        var soundRegistry = registryAccess.registryOrThrow(Registries.SOUND_EVENT);

        soundRegistry.stream()
                .sorted(Comparator.comparing(sound -> soundRegistry.getKey(sound).toString()))
                .forEach(sound -> {
                    ResourceLocation soundId = soundRegistry.getKey(sound);
                    String namespace = soundId.getNamespace();
                    String path = soundId.getPath();

                    String modName = getModName(namespace);

                    JsonObject modSection = getOrCreateModSection(musicSection, namespace);
                    JsonObject soundData = new JsonObject();
                    soundData.addProperty("name", sound.getLocation().toString());
                    soundData.addProperty("mod_name", modName);
                    modSection.add(path, soundData);
                });

        Path outputFile = exportDir.resolve("sounds.json");
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            GSON.toJson(output, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write sounds export file", e);
        }

        return (int) soundRegistry.count();
    }

    public int exportAll() {
        JsonObject output = new JsonObject();

        // Object section
        JsonObject objectSection = new JsonObject();
        output.add("object", objectSection);

        // Music section
        JsonObject musicSection = new JsonObject();
        output.add("music", musicSection);

        var registryAccess = server.registryAccess();
        var itemRegistry = registryAccess.registryOrThrow(Registries.ITEM);
        var soundRegistry = registryAccess.registryOrThrow(Registries.SOUND_EVENT);

        // Export items
        itemRegistry.stream()
                .sorted(Comparator.comparing(item -> itemRegistry.getKey(item).toString()))
                .forEach(item -> {
                    ResourceLocation itemId = itemRegistry.getKey(item);
                    String namespace = itemId.getNamespace();
                    String path = itemId.getPath();

                    ItemStack stack = new ItemStack(item);
                    String displayName = stack.getDisplayName().getString();
                    String modName = getModName(namespace);

                    JsonObject modSection = getOrCreateModSection(objectSection, namespace);
                    JsonObject itemData = new JsonObject();
                    itemData.addProperty("name", displayName);
                    itemData.addProperty("mod_name", modName);
                    modSection.add(path, itemData);
                });

        // Export sounds
        soundRegistry.stream()
                .sorted(Comparator.comparing(sound -> soundRegistry.getKey(sound).toString()))
                .forEach(sound -> {
                    ResourceLocation soundId = soundRegistry.getKey(sound);
                    String namespace = soundId.getNamespace();
                    String path = soundId.getPath();

                    String modName = getModName(namespace);

                    JsonObject modSection = getOrCreateModSection(musicSection, namespace);
                    JsonObject soundData = new JsonObject();
                    soundData.addProperty("name", sound.getLocation().toString());
                    soundData.addProperty("mod_name", modName);
                    modSection.add(path, soundData);
                });

        Path outputFile = exportDir.resolve("output.json");
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            GSON.toJson(output, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write output export file", e);
        }

        return (int) (itemRegistry.count() + soundRegistry.count());
    }

    private JsonObject getOrCreateModSection(JsonObject parent, String namespace) {
        if (!parent.has(namespace)) {
            parent.add(namespace, new JsonObject());
        }
        return parent.getAsJsonObject(namespace);
    }

    private String getModName(String namespace) {
        if ("minecraft".equals(namespace)) {
            return "Minecraft";
        }

        return ModList.get().getModContainerById(namespace)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(namespace);
    }
}
