package com.example.outputexporter;

import com.example.outputexporter.command.OutputCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(OutputExporterMod.MOD_ID)
public class OutputExporterMod {
    public static final String MOD_ID = "output_exporter";

    public OutputExporterMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        OutputCommand.register(event.getDispatcher());
    }
}
