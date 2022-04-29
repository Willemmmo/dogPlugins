/*
 * Copyright (c) 2018 gazivodag <https://github.com/gazivodag>
 * Copyright (c) 2019 lucwousin <https://github.com/lucwousin>
 * Copyright (c) 2019 infinitay <https://github.com/Infinitay>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.oneclickblackjack;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.client.Notifier;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.MenuEntry;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.apache.commons.lang3.ArrayUtils;
import org.pf4j.Extension;
import net.runelite.api.NPC;


import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Extension
@PluginDescriptor(
		name = "Oneclick Blackjack" ,
		enabledByDefault = false,
		description = "Allows for one-click blackjacking, both knocking out and pickpocketing",
		tags = {"blackjack", "thieving","Dog22"}
)
public class OneclickBlackjackPlugin extends Plugin {
	private static final int POLLNIVNEACH_REGION = 13358;
	Set<Integer> foodBlacklist = Set.of(139, 141, 143, 2434, 3024, 3026, 3028, 3030, 24774, 189, 191, 193, 2450, 26340, 26342, 26344, 26346);
	private static final String SUCCESS_BLACKJACK = "You smack the bandit over the head and render them unconscious.";
	private static final String FAILED_BLACKJACK = "Your blow only glances off the bandit's head.";
	private static final String PICKPOCKET = "Pickpocket";
	private static final String KNOCK_OUT = "Knock-Out";
	private static final String BANDIT = "Bandit";
	private static final String MENAPHITE = "Menaphite Thug";
	private boolean shouldHeal = false;
	Set<String> foodMenuOption = Set.of("Drink", "Eat");

	@Inject
	private Notifier notifier;

	@Inject
	private ItemManager itemManager;

	@Inject
	private Client client;

	@Inject
	private OneclickBlackjackConfig config;

	@Inject
	private EventBus eventBus;

	@Inject
	private MenuManager menuManager;

	private long nextKnockOutTick = 0;
	private boolean knockout = true;

	@Provides
	OneclickBlackjackConfig getConfig(ConfigManager configManager) {
		return configManager.getConfig(OneclickBlackjackConfig.class);
	}

	@Subscribe

	public void onMenuOptionClicked(MenuOptionClicked event) {
		if (event.getMenuOption().equals("<col=00ff00>One Click blackjack")) {
			handleClick(event);
		}
	}

	@Subscribe
	private void onClientTick(ClientTick event) {
		if (client.getGameState() != GameState.LOGGED_IN ||
				client.getMapRegions() == null ||
				!ArrayUtils.contains(client.getMapRegions(), POLLNIVNEACH_REGION)) {
			return;
		}
		String text = "<col=00ff00>One Click blackjack";
		this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
				.getId(), 0, 0, 0, true);
	}

	@Subscribe
	private void onGameTick(GameTick event) {
		if (client.getTickCount() >= nextKnockOutTick) {
			knockout = true;

		}
		shouldHeal = (client.getBoostedSkillLevel(Skill.HITPOINTS) <= Math.max(5, config.HPBottomThreshold()));
	}


	@Subscribe
	private void onChatMessage(ChatMessage event) {
		final String msg = event.getMessage();

		if (event.getType() == ChatMessageType.SPAM && (msg.equals(SUCCESS_BLACKJACK) || (msg.equals(FAILED_BLACKJACK) && config.pickpocketOnAggro()))) {
			knockout = false;
			final int ticks = 4;
			nextKnockOutTick = client.getTickCount() + ticks;
		}
	}

	@Subscribe
	private void handleClick(MenuOptionClicked event) {
		int id = config.getBlackJackNpc().getId();
		NPC npc = new NPCQuery().idEquals(id).result(client).nearestTo(client.getLocalPlayer());
		if (!knockout && !shouldHeal || !knockout && !config.enableHeal()) {
			event.setMenuEntry(client.createMenuEntry(
					PICKPOCKET,
					npc.getName(),
					npc.getIndex(),
					11,
					0,
					0,
					true
			));


		}
		if (knockout && !shouldHeal || knockout && !config.enableHeal()) {
			event.setMenuEntry(client.createMenuEntry(
					KNOCK_OUT,
					npc.getName(),
					npc.getIndex(),
					13,
					0,
					0,
					true
			));

		}
		if (shouldHeal && config.enableHeal()) {
			Widget food = getItem(foodMenuOption, foodBlacklist);
			if (food != null) {
				String[] foodMenuOptions = itemManager.getItemComposition(food.getId()).getInventoryActions();
				event.setMenuEntry(createMenuEntry(2,MenuAction.CC_OP,food.getIndex(),WidgetInfo.INVENTORY.getId(),false));

			} else {
				notifier.notify("Out of food");
			}
		}
	}

	private Widget getItem(Collection<String> menuOptions, Collection<Integer> ignoreIDs) {
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null && inventoryWidget.getChildren() != null) {
			Widget[] items = inventoryWidget.getChildren();
			for (Widget item : items) {
				if (ignoreIDs.contains(item.getItemId())) {
					continue;
				}
				String[] menuActions = itemManager.getItemComposition(item.getItemId()).getInventoryActions();
				for (String action : menuActions) {
					if (action != null && menuOptions.contains(action)) {
						return item;
					}
				}
			}
		}
		return null;
	}
	public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
		return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
				.setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
	}

}

