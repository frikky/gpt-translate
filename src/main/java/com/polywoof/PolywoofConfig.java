package com.polywoof;

import net.runelite.client.config.*;
import net.runelite.client.ui.overlay.OverlayPosition;

import java.awt.*;

@ConfigGroup("polywoof")
public interface PolywoofConfig extends Config
{
	@ConfigSection( name = "Primary", description = "General stuff", position = 0 )
	String primarySection = "primarySection";

	@ConfigItem( keyName = "language", name = "Target language", description = "Type your desired one", section = primarySection, position = 0 )
	default String language()
	{
		return "RU";
	}

	@ConfigItem( keyName = "token", name = "DeepL API Token", description = "This is REQUIRED, see www.DeepL.com", secret = true, section = primarySection, position = 1 )
	default String token()
	{
		return "";
	}

	@ConfigSection( name = "Behavior", description = "What does this do", position = 1 )
	String behaviorSection = "behaviorSection";

	@Range( min = 1, max = 99 )
	@ConfigItem( keyName = "readingSpeed", name = "Reading speed", description = "How quickly do you read", section = behaviorSection, position = 0 )
	default int readingSpeed()
	{
		return 10;
	}

	@ConfigItem( keyName = "enableChat", name = "Chat messages", description = "Translate chat messages", section = behaviorSection, position = 1 )
	default boolean enableChat()
	{
		return false;
	}

	@ConfigItem( keyName = "enableOverhead", name = "Overhead text", description = "Translate overhead text", section = behaviorSection, position = 2 )
	default boolean enableOverhead()
	{
		return false;
	}

	@ConfigItem( keyName = "enableDiary", name = "Diary and clues", description = "Translate diary and clues", section = behaviorSection, position = 3 )
	default boolean enableDiary()
	{
		return false;
	}

	@ConfigSection( name = "Font", description = "Appearance stuff", position = 2 )
	String fontSection = "fontSection";

	@ConfigItem( keyName = "fontName", name = "Font name", description = "Checkout your fonts viewer", section = fontSection, position = 0 )
	default String fontName()
	{
		return "Consolas";
	}

	@Range( min = 1, max = 99 )
	@ConfigItem( keyName = "fontSize", name = "Font size", description = "Because size does matter", section = fontSection, position = 1 )
	default int fontSize()
	{
		return 12;
	}

	@ConfigSection( name = "Appearance", description = "Visual stuff", position = 3 )
	String visualSection = "visualSection";

	@ConfigItem( keyName = "overlayPosition", name = "Position on screen", description = "Put the thing where it belongs", section = visualSection, position = 0 )
	default OverlayPosition overlayPosition()
	{
		return OverlayPosition.BOTTOM_LEFT;
	}

	@Alpha
	@ConfigItem( keyName = "overlayColor", name = "Background color", description = "Background color for subtitles", section = visualSection, position = 1 )
	default Color overlayColor()
	{
		return new Color(32, 32, 32, 128);
	}

	@ConfigSection( name = "Formatting", description = "Format stuff", position = 4 )
	String formatSection = "formatSection";

	@Range( min = 32 )
	@ConfigItem( keyName = "wrapWidth", name = "Text wrap width", description = "Widest text ever", section = formatSection, position = 0 )
	default int wrapWidth()
	{
		return 480;
	}

	@ConfigItem( keyName = "sourceName", name = "Source name", description = "Tell me who said that", section = formatSection, position = 1 )
	default boolean sourceName()
	{
		return true;
	}

	@ConfigItem( keyName = "sourceSeparator", name = "Source separator", description = "Between source and text", section = formatSection, position = 2 )
	default String sourceSeparator()
	{
		return ": ";
	}

	@ConfigSection( name = "Experimental", description = "Go away", position = 5, closedByDefault = true )
	String experimental = "experimental";

	@ConfigItem( keyName = "URL", name = "URL to request", description = "Token - 1$; Text - 2$; Language - 3$", section = experimental, position = 0 )
	default String URL()
	{
		return "https://api-free.deepl.com/v2/translate?auth_key=%1$s&text=%2$s&target_lang=%3$s&source_lang=en&preserve_formatting=1&split_sentences=1&tag_handling=html";
	}

	@ConfigItem( keyName = "jsonArray", name = "Json array key", description = "response:{ ?:[ sentence:text, sentence:text.. ] }", section = experimental, position = 1 )
	default String jsonArray()
	{
		return "translations";
	}

	@ConfigItem( keyName = "jsonString", name = "Json string key", description = "response:{ array:[ sentence:?, sentence:?.. ] }", section = experimental, position = 2 )
	default String jsonString()
	{
		return "text";
	}
}
