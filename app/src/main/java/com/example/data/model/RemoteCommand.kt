package com.example.data.model

/**
 * Standard Sony Bravia IRCC-IP command definitions.
 * Sony Bravia and Google TVs with IP Control accept these base64 codes
 * over HTTP via SOAP /sony/ircc with ultra-low latency (<50ms).
 */
enum class RemoteCommand(
    val irccCode: String,
    val title: String,
    val category: CommandCategory,
    val commandName: String = title
) {
    // Power & Display
    POWER("AAAAAQAAAAEAAAAVAw==", "Power", CommandCategory.POWER, "Power"),
    POWER_OFF("AAAAAQAAAAEAAAAvAw==", "Power Off", CommandCategory.POWER, "PowerOff"),
    PICTURE_OFF("AAAAAQAAAAEAAAA+Aw==", "Picture Off", CommandCategory.POWER, "PictureOff"),

    // Volume & Audio
    VOLUME_UP("AAAAAQAAAAEAAAASAw==", "Vol +", CommandCategory.AUDIO, "VolumeUp"),
    VOLUME_DOWN("AAAAAQAAAAEAAAATAw==", "Vol -", CommandCategory.AUDIO, "VolumeDown"),
    MUTE("AAAAAQAAAAEAAAAUAw==", "Mute", CommandCategory.AUDIO, "Mute"),

    // Directional & Navigation (Verified Sony SIRCS codes)
    UP("AAAAAQAAAAEAAAB0Aw==", "Up", CommandCategory.NAVIGATION, "Up"),
    DOWN("AAAAAQAAAAEAAAB1Aw==", "Down", CommandCategory.NAVIGATION, "Down"),
    LEFT("AAAAAQAAAAEAAAA0Aw==", "Left", CommandCategory.NAVIGATION, "Left"),
    RIGHT("AAAAAQAAAAEAAAA1Aw==", "Right", CommandCategory.NAVIGATION, "Right"),
    CONFIRM("AAAAAQAAAAEAAABlAw==", "Enter", CommandCategory.NAVIGATION, "Confirm"),
    HOME("AAAAAQAAAAEAAABgAw==", "Home", CommandCategory.NAVIGATION, "Home"),
    BACK("AAAAAgAAAJcAAAAjAw==", "Back", CommandCategory.NAVIGATION, "Return"),
    EXIT("AAAAAQAAAAEAAABjAw==", "Exit", CommandCategory.NAVIGATION, "Exit"),
    ACTION_MENU("AAAAAgAAAMQAAABLAw==", "Action Menu", CommandCategory.NAVIGATION, "ActionMenu"),
    QUICK_SETTINGS("AAAAAgAAAMQAAABLAw==", "Settings", CommandCategory.NAVIGATION, "QuickSettings"),
    TV("AAAAAQAAAAEAAAAkAw==", "TV", CommandCategory.NAVIGATION, "Tv"),
    GUIDE("AAAAAQAAAAEAAABbAw==", "TV Guide", CommandCategory.NAVIGATION, "EPG"),

    // Inputs
    INPUT_TOGGLE("AAAAAQAAAAEAAAAlAw==", "Input", CommandCategory.INPUT, "Input"),
    HDMI1("AAAAAgAAABoAAABaAw==", "HDMI 1", CommandCategory.INPUT, "Hdmi1"),
    HDMI2("AAAAAgAAABoAAABbAw==", "HDMI 2", CommandCategory.INPUT, "Hdmi2"),
    HDMI3("AAAAAgAAABoAAABcAw==", "HDMI 3", CommandCategory.INPUT, "Hdmi3"),
    HDMI4("AAAAAgAAABoAAABdAw==", "HDMI 4", CommandCategory.INPUT, "Hdmi4"),
    VIDEO1("AAAAAQAAAAEAAABAAw==", "Video 1", CommandCategory.INPUT, "Video1"),
    COMPONENT1("AAAAAgAAABoAAAB2Aw==", "Component", CommandCategory.INPUT, "Component1"),

    // Channels (Verified Sony SIRCS codes)
    CHANNEL_UP("AAAAAQAAAAEAAAAQAw==", "CH +", CommandCategory.CHANNEL, "ChannelUp"),
    CHANNEL_DOWN("AAAAAQAAAAEAAAARAw==", "CH -", CommandCategory.CHANNEL, "ChannelDown"),

    // Media Playback
    PLAY("AAAAAgAAAJcAAAAaAw==", "Play", CommandCategory.MEDIA, "Play"),
    PAUSE("AAAAAgAAAJcAAAAZAw==", "Pause", CommandCategory.MEDIA, "Pause"),
    STOP("AAAAAgAAAJcAAAAYAw==", "Stop", CommandCategory.MEDIA, "Stop"),
    REWIND("AAAAAgAAAJcAAAAbAw==", "Rewind", CommandCategory.MEDIA, "Rewind"),
    FAST_FORWARD("AAAAAgAAAJcAAAAcAw==", "Forward", CommandCategory.MEDIA, "Forward"),
    PREV("AAAAAgAAAJcAAAA8Aw==", "Previous", CommandCategory.MEDIA, "Prev"),
    NEXT("AAAAAgAAAJcAAAA9Aw==", "Next", CommandCategory.MEDIA, "Next"),

    // Quick Apps / Google TV Services
    APP_YOUTUBE("AAAAAgAAABoAAABhAw==", "YouTube", CommandCategory.APP, "YouTube"),
    APP_NETFLIX("AAAAAgAAABoAAAB8Aw==", "Netflix", CommandCategory.APP, "Netflix"),
    APP_PRIME_VIDEO("AAAAAgAAABoAAABqAw==", "Prime Video", CommandCategory.APP, "PrimeVideo"),
    APP_GOOGLE_PLAY("AAAAAgAAAMQAAABGAw==", "Google Play", CommandCategory.APP, "GooglePlay"),
    APP_DISNEY("AAAAAgAAAMQAAAB7Aw==", "Disney+", CommandCategory.APP, "Disney"),

    // Color Keys (Sony Bravia)
    COLOR_RED("AAAAAgAAAJcAAAAlAw==", "Red", CommandCategory.COLOR, "Red"),
    COLOR_GREEN("AAAAAgAAAJcAAAAmAw==", "Green", CommandCategory.COLOR, "Green"),
    COLOR_YELLOW("AAAAAgAAAJcAAAAnAw==", "Yellow", CommandCategory.COLOR, "Yellow"),
    COLOR_BLUE("AAAAAgAAAJcAAAAoAw==", "Blue", CommandCategory.COLOR, "Blue"),

    // Number Pad
    NUM_0("AAAAAQAAAAEAAAAJAw==", "0", CommandCategory.NUMPAD, "Num0"),
    NUM_1("AAAAAQAAAAEAAAAAAw==", "1", CommandCategory.NUMPAD, "Num1"),
    NUM_2("AAAAAQAAAAEAAAABAw==", "2", CommandCategory.NUMPAD, "Num2"),
    NUM_3("AAAAAQAAAAEAAAACAw==", "3", CommandCategory.NUMPAD, "Num3"),
    NUM_4("AAAAAQAAAAEAAAADAw==", "4", CommandCategory.NUMPAD, "Num4"),
    NUM_5("AAAAAQAAAAEAAAAEAw==", "5", CommandCategory.NUMPAD, "Num5"),
    NUM_6("AAAAAQAAAAEAAAAFAw==", "6", CommandCategory.NUMPAD, "Num6"),
    NUM_7("AAAAAQAAAAEAAAAGAw==", "7", CommandCategory.NUMPAD, "Num7"),
    NUM_8("AAAAAQAAAAEAAAAHAw==", "8", CommandCategory.NUMPAD, "Num8"),
    NUM_9("AAAAAQAAAAEAAAAIAw==", "9", CommandCategory.NUMPAD, "Num9")
}

enum class CommandCategory {
    POWER,
    AUDIO,
    NAVIGATION,
    INPUT,
    CHANNEL,
    MEDIA,
    APP,
    COLOR,
    NUMPAD
}
