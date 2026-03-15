package com.example.outputexporter.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
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
        var registryAccess = server.registryAccess();
        var itemRegistry = registryAccess.registryOrThrow(Registries.ITEM);
        
        JsonArray itemsArray = new JsonArray();
        
        List<Item> sortedItems = itemRegistry.stream()
            .sorted(Comparator.comparing(item -> itemRegistry.getKey(item).toString()))
            .collect(Collectors.toList());

        for (Item item : sortedItems) {
            ResourceLocation itemId = itemRegistry.getKey(item);
            JsonObject itemJson = new JsonObject();
            itemJson.addProperty("id", itemId.toString());
            
            ItemStack stack = new ItemStack(item);
            String displayName = stack.getDisplayName().getString();
            itemJson.addProperty("name", displayName);
            
            itemsArray.add(itemJson);
        }

        Path outputFile = exportDir.resolve("items.json");
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            GSON.toJson(itemsArray, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write items export file", e);
        }

        return itemsArray.size();
    }

    public int exportSounds() {
        var registryAccess = server.registryAccess();
        var soundRegistry = registryAccess.registryOrThrow(Registries.SOUND_EVENT);
        
        JsonArray soundsArray = new JsonArray();
        
        List<SoundEvent> sortedSounds = soundRegistry.stream()
            .sorted(Comparator.comparing(sound -> soundRegistry.getKey(sound).toString()))
            .collect(Collectors.toList());

        for (SoundEvent sound : sortedSounds) {
            ResourceLocation soundId = soundRegistry.getKey(sound);
            JsonObject soundJson = new JsonObject();
            soundJson.addProperty("id", soundId.toString());
            soundJson.addProperty("location", sound.getLocation().toString());
            
            soundsArray.add(soundJson);
        }

        Path outputFile = exportDir.resolve("sounds.json");
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            GSON.toJson(soundsArray, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write sounds export file", e);
        }

        return soundsArray.size();
    }
}
