/*
 * Copyright (c) 2019, gazivodag <https://github.com/gazivodag>
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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("oneclickblackjack")
public interface OneclickBlackjackConfig extends Config
{
	@ConfigItem(
			keyName = "ChooseId",
			name="What bandit",
			description = "What npc do you want to blackjack",
			position =  0

	)
	default BlackJackNpc getBlackJackNpc(){return BlackJackNpc.MENAPHITE;}
	@ConfigItem(
			keyName = "pickpocketOnAggro",
			name = "Pickpocket when aggro'd",
			description = "Switches to \"Pickpocket\" when bandit is aggro'd. Saves food at the cost of slight xp/h.",
			position = 1
	)
	default boolean pickpocketOnAggro()
	{
		return false;
	}

	@ConfigItem(
			keyName = "enableHeal",
			name = "Eat food",
			description = "This will eat/drink anything in your inventory when your HP gets low",
			position = 2
	)
	default boolean enableHeal()
	{
		return true;
	}

	@Range(
			max = 99,
			min = 5
	)

	@ConfigItem(
			keyName = "HPBottomThreshold",
			name = "Minimum HP",
			description = "You will START eating when your HP is at or below this number",
			position = 4,
			hidden = true,
			unhide = "enableHeal"
	)
	default int HPBottomThreshold()
	{
		return 10;
	}

}
