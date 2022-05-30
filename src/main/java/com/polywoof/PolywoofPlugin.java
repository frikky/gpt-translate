package com.polywoof;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPriority;
import okhttp3.OkHttpClient;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor( name = "Polywoof", description = "Translation for almost all NPC and any related text, so you can understand what's going on!", tags = { "helper", "language", "translator", "translation" } )

public class PolywoofPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private PolywoofConfig config;

	@Inject
	private PolywoofOverlay polywoofOverlay;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private OkHttpClient okHttpClient;

	private int dialog;
	private boolean notify = true;
	private String previous = null;
	private PolywoofTranslator translator;

	@Override
	protected void startUp() throws Exception
	{
		translator = new PolywoofTranslator(okHttpClient, config.token());

		polywoofOverlay.update();
		polywoofOverlay.setPosition(config.overlayPosition());
		polywoofOverlay.setLayer(OverlayLayer.ABOVE_WIDGETS);
		polywoofOverlay.setPriority(OverlayPriority.LOW);
		overlayManager.add(polywoofOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		polywoofOverlay.clear();
		overlayManager.remove(polywoofOverlay);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if(!configChanged.getGroup().equals("polywoof")) return;

		switch (configChanged.getKey())
		{
			case ("token"):
				translator = new PolywoofTranslator(okHttpClient, config.token());
				break;
			case ("showUsage"):
				notify = true;
				break;
			case ("fontName"):
			case ("fontSize"):
				polywoofOverlay.update();
				break;
			case ("overlayPosition"):
				polywoofOverlay.setPosition(config.overlayPosition());
				break;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if(config.showUsage() && gameStateChanged.getGameState() == GameState.LOGGED_IN && notify)
		{
			notify = false;

			translator.usage((character_count, character_limit) ->
			{
				String message = new ChatMessageBuilder()
						.append(ChatColorType.NORMAL)
						.append("Your current DeepL API usage is ")
						.append(ChatColorType.HIGHLIGHT)
						.append(Math.round(100f * ((float)character_count / character_limit)) + "%")
						.append(ChatColorType.NORMAL)
						.append(" of monthly quota!")
						.build();

				chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.CONSOLE).runeLiteFormattedMessage(message).build());
			});
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		String text;
		String source;

		switch (chatMessage.getType())
		{
			case NPC_EXAMINE:
			case ITEM_EXAMINE:
			case OBJECT_EXAMINE:
				if(!config.enableExamine()) return;

				text = chatMessage.getMessage();
				source = "Examine";
				break;
			case GAMEMESSAGE:
				if(!config.enableChat()) return;

				text = chatMessage.getMessage();
				source = "Game";
				break;
			default:
				return;
		}

		translator.translate(translator.stripTags(text), config.language().toLowerCase(), translation ->
		{
			polywoofOverlay.put((config.sourceName() ? translator.stripTags(source) + config.sourceSeparator() : "") + translation);
		});
	}

	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged overheadTextChanged)
	{
		if(!config.enableOverhead()) return;

		Actor actor = overheadTextChanged.getActor();

		if(actor == client.getLocalPlayer() || actor instanceof NPC)
		{
			String text = overheadTextChanged.getOverheadText();
			String source = actor.getName();

			translator.translate(translator.stripTags(text), config.language().toLowerCase(), translation ->
			{
				polywoofOverlay.put((config.sourceName() ? source + config.sourceSeparator() : "") + translation);
			});
		}
	}

	/*
		11 - Alt Sprite
		222 - Scroll Text
		229 - Other Dialog
		392 - Book
	 */

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		switch (widgetLoaded.getGroupId())
		{
			case WidgetID.DIALOG_NPC_GROUP_ID:
			case WidgetID.DIALOG_PLAYER_GROUP_ID:
			case WidgetID.DIALOG_SPRITE_GROUP_ID:
			case WidgetID.DIALOG_OPTION_GROUP_ID:
			case WidgetID.DIARY_QUEST_GROUP_ID:
			case WidgetID.CLUE_SCROLL_GROUP_ID:
			case 11:
			case 229:
			case 392:
				dialog = widgetLoaded.getGroupId();
				break;
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed)
	{
		switch (widgetClosed.getGroupId())
		{
			case WidgetID.DIALOG_NPC_GROUP_ID:
			case WidgetID.DIALOG_PLAYER_GROUP_ID:
			case WidgetID.DIALOG_SPRITE_GROUP_ID:
			case WidgetID.DIALOG_OPTION_GROUP_ID:
			case WidgetID.DIARY_QUEST_GROUP_ID:
			case WidgetID.CLUE_SCROLL_GROUP_ID:
			case 11:
			case 229:
			case 392:
				dialog = 0;
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		String text;
		String source;

		Widget widget1;
		Widget widget2;
		Widget widget3;

		switch (dialog)
		{
			case WidgetID.DIALOG_NPC_GROUP_ID:
				widget1 = client.getWidget(WidgetInfo.DIALOG_NPC_NAME);
				widget2 = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);

				if(widget1 == null || widget2 == null) return;

				source = widget1.getText();
				text = widget2.getText();
				break;
			case WidgetID.DIALOG_PLAYER_GROUP_ID:
				widget1 = client.getWidget(dialog, 4);
				widget2 = client.getWidget(WidgetInfo.DIALOG_PLAYER_TEXT);

				if(widget1 == null || widget2 == null) return;

				source = widget1.getText();
				text = widget2.getText();
				break;
			case WidgetID.DIALOG_SPRITE_GROUP_ID:
				widget1 = client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT);

				if(widget1 == null) return;

				source = "Game";
				text = widget1.getText();
				break;
			case WidgetID.DIALOG_OPTION_GROUP_ID:
				widget1 = client.getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS);

				if(widget1 == null) return;

				int index = -1;
				StringBuilder options = new StringBuilder();

				for(Widget children : widget1.getDynamicChildren())
				{
					if(children.getType() == WidgetType.TEXT && children.getText().length() > 0) options.append(++index == 0 ? "" : index + ". ").append(children.getText()).append("\n");
				}

				source = "Options";
				text = options.toString();
				break;
			case WidgetID.DIARY_QUEST_GROUP_ID:
				if(!config.enableDiary()) return;

				widget1 = client.getWidget(WidgetInfo.DIARY_QUEST_WIDGET_TITLE);
				widget2 = client.getWidget(WidgetInfo.DIARY_QUEST_WIDGET_TEXT);

				if(widget1 == null || widget2 == null) return;

				StringBuilder diary = new StringBuilder();

				for(Widget children : widget2.getStaticChildren())
				{
					if(children.getType() == WidgetType.TEXT && children.getText().length() > 0) diary.append(children.getText()).append(" ");
				}

				source = widget1.getText();
				text = diary.toString();
				break;
			case WidgetID.CLUE_SCROLL_GROUP_ID:
				if(!config.enableClues()) return;

				widget1 = client.getWidget(WidgetInfo.CLUE_SCROLL_TEXT);

				if(widget1 == null) return;

				source = "Clue";
				text = widget1.getText();
				break;
			case 11:
				widget1 = client.getWidget(dialog, 2);

				if(widget1 == null) return;

				source = "Game";
				text = widget1.getText();
				break;
			case 229:
				widget1 = client.getWidget(dialog, 1);

				if(widget1 == null) return;

				source = "Game";
				text = widget1.getText();
				break;
			case 392:
				if(!config.enableBooks()) return;

				widget1 = client.getWidget(dialog, 6);
				widget2 = client.getWidget(dialog, 43);
				widget3 = client.getWidget(dialog, 59);

				if(widget1 == null || widget2 == null || widget3 == null) return;

				Widget[] pages = { widget2, widget3 };
				StringBuilder book = new StringBuilder();

				for(Widget page : pages)
				{
					for(Widget children : page.getStaticChildren())
					{
						if(children.getType() == WidgetType.TEXT && children.getText().length() > 0) book.append(children.getText()).append(" ");
					}
				}

				source = widget1.getText();
				text = book.toString();
				break;
			default:
				previous = "";

				polywoofOverlay.vanish(1);
				return;
		}

		if(text.equals(previous)) return;

		previous = text;

		translator.translate(translator.stripTags(text), config.language().toLowerCase(), translation ->
		{
			polywoofOverlay.vanish(1);
			polywoofOverlay.set(1, (config.sourceName() ? translator.stripTags(source) + config.sourceSeparator() : "") + translation);
		});
	}

	@Provides
	PolywoofConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PolywoofConfig.class);
	}
}
