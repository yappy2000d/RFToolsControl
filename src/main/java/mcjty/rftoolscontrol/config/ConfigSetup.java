package mcjty.rftoolscontrol.config;

import mcjty.lib.thirteen.ConfigSpec;
import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigSetup {

    public static final String CATEGORY_GENERAL = "general";

    public static ConfigSpec.IntValue processorMaxenergy;
    public static ConfigSpec.IntValue processorReceivepertick;

    public static ConfigSpec.IntValue processorMaxloglines;

    public static ConfigSpec.IntValue coreSpeed[] = new ConfigSpec.IntValue[3];
    public static ConfigSpec.IntValue coreRFPerTick[] = new ConfigSpec.IntValue[3];

    public static ConfigSpec.IntValue VARIABLEMODULE_RFPERTICK;
    public static ConfigSpec.IntValue INTERACTMODULE_RFPERTICK;
    public static ConfigSpec.IntValue CONSOLEMODULE_RFPERTICK;
    public static ConfigSpec.IntValue VECTORARTMODULE_RFPERTICK;

    public static ConfigSpec.BooleanValue doubleClickToChangeConnector;
    public static ConfigSpec.IntValue tooltipVerbosityLevel;

    public static ConfigSpec.IntValue maxGraphicsOpcodes;
    public static ConfigSpec.IntValue maxEventQueueSize;
    public static ConfigSpec.IntValue maxCraftRequests;
    public static ConfigSpec.IntValue maxStackSize;

    private static final ConfigSpec.Builder SERVER_BUILDER = new ConfigSpec.Builder();
    private static final ConfigSpec.Builder CLIENT_BUILDER = new ConfigSpec.Builder();

    static {
        SERVER_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        CLIENT_BUILDER.comment("General settings").push(CATEGORY_GENERAL);

        processorMaxenergy = SERVER_BUILDER
            .comment("Maximum RF storage that the processor can hold")
            .defineInRange("processorMaxRF", 100000, 1, Integer.MAX_VALUE);
        processorReceivepertick = SERVER_BUILDER
            .comment("RF per tick that the processor can receive")
            .defineInRange("processorRFPerTick", 1000, 1, Integer.MAX_VALUE);
        processorMaxloglines = SERVER_BUILDER
            .comment("Maximum number of lines to keep in the log")
            .defineInRange("processorMaxLogLines", 100, 0, 100000);
        maxStackSize = SERVER_BUILDER
            .comment("Maximum stack size for a program (used by 'call' opcode)")
            .defineInRange("maxStackSize", 100, 1, 10000);
        maxGraphicsOpcodes = SERVER_BUILDER
            .comment("Maximum amount of graphics opcodes that a graphics card supports")
            .defineInRange("maxGraphicsOpcodes", 30, 1, 10000);
        maxEventQueueSize = SERVER_BUILDER
            .comment("Maximum amount of event queue entries supported by a processor. More events will be ignored")
            .defineInRange("maxEventQueueSize", 100, 1, 10000);
        maxCraftRequests = SERVER_BUILDER
            .comment("Maximum amount of craft requests supported by the crafting station. More requests will be ignored")
            .defineInRange("maxCraftRequests", 200, 1, 10000);

        doubleClickToChangeConnector = SERVER_BUILDER
                .comment("If true double click is needed in programmer to change connector. If false single click is sufficient")
                .define("doubleClickToChangeConnector", true);
        tooltipVerbosityLevel = SERVER_BUILDER
                .comment("If 2 tooltips in the programmer gui are verbose and give a lot of info. With 1 the information is decreased. 0 means no tooltips")
                .defineInRange("tooltipVerbosityLevel", 2, 0, 2);

        coreSpeed[0] = SERVER_BUILDER
                .comment("Amount of instructions per tick for the CPU Core B500")
                .defineInRange("speedB500", 1, 1, 1000);
        coreSpeed[1] = SERVER_BUILDER
                .comment("Amount of instructions per tick for the CPU Core S1000")
                .defineInRange("speedS1000", 4, 1, 1000);
        coreSpeed[2] = SERVER_BUILDER
                .comment("Amount of instructions per tick for the CPU Core EX2000")
                .defineInRange("speedEX2000", 16, 1, 1000);
        coreRFPerTick[0] = SERVER_BUILDER
                .comment("RF per tick for the CPU Core B500")
                .defineInRange("rfB500", 4, 0, Integer.MAX_VALUE);
        coreRFPerTick[1] = SERVER_BUILDER
                .comment("RF per tick for the CPU Core S1000")
                .defineInRange("rfS1000", 14, 0, Integer.MAX_VALUE);
        coreRFPerTick[2] = SERVER_BUILDER
                .comment("RF per tick for the CPU Core EX2000")
                .defineInRange("rfEX2000", 50, 0, Integer.MAX_VALUE);

        VARIABLEMODULE_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the variable screen module")
                .defineInRange("variableModuleRFPerTick", 1, 0, Integer.MAX_VALUE);
        INTERACTMODULE_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the interaction screen module")
                .defineInRange("interactionModuleRFPerTick", 2, 0, Integer.MAX_VALUE);
        CONSOLEMODULE_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the console screen module")
                .defineInRange("consoleModuleRFPerTick", 2, 0, Integer.MAX_VALUE);
        VECTORARTMODULE_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the vector art screen module")
                .defineInRange("vectorArtModuleRFPerTick", 2, 0, Integer.MAX_VALUE);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }

    public static ConfigSpec SERVER_CONFIG;
    public static ConfigSpec CLIENT_CONFIG;


    public static Configuration mainConfig;

    public static void init() {
        mainConfig = new Configuration(new File(RFToolsControl.setup.getModConfigDir().getPath() + File.separator + "rftools", "control.cfg"));
        Configuration cfg = mainConfig;
        try {
            cfg.load();
            SERVER_CONFIG = SERVER_BUILDER.build(mainConfig);
            CLIENT_CONFIG = CLIENT_BUILDER.build(mainConfig);
        } catch (Exception e1) {
            FMLLog.log(Level.ERROR, e1, "Problem loading config file!");
        }
    }

    public static void postInit() {
        if (mainConfig.hasChanged()) {
            mainConfig.save();
        }
    }
}
