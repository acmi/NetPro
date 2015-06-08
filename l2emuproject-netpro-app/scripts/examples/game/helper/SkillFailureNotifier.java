/*
 * Copyright 2011-2015 L2EMU UNIQUE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package examples.game.helper;

import interpreter.ImmutableSystemMessage;

import java.util.List;

import util.packet.CommonPacketSender;
import util.packet.SystemMessageRecipient;

import eu.revengineer.simplejse.HasScriptDependencies;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerInterpreter;
import net.l2emuproject.proxy.script.ScriptedMetaclass;
import net.l2emuproject.proxy.script.game.PpeEnabledGameScript;
import net.l2emuproject.proxy.script.interpreter.L2SkillTranslator;

/**
 * Notifies the user about their skill usage failure ON SCREEN.
 * 
 * @author _dev_
 */
@HasScriptDependencies({ "interpreter.ImmutableSystemMessage", "util.packet.CommonPacketSender", "util.packet.SystemMessageRecipient" })
public class SkillFailureNotifier extends PpeEnabledGameScript implements SystemMessageRecipient
{
	private static final int SM_CASTING_INTERRUPTED = 27;
	private static final int SM_RESISTED_EFFECT = 139;
	
	private static final int POS_INTERRUPTED = 7, POS_RESISTED = 2;
	private static final boolean SMALL_FONT = true, FADE = false;
	private static final int DURATION = 1_500;
	
	@Override
	public String getName()
	{
		return "Skill failure notifier";
	}
	
	@Override
	public void handleClientPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		// nothing to do here
	}
	
	@Override
	public void handleServerPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		final ImmutableSystemMessage ism;
		try
		{
			ism = MetaclassRegistry.getInstance().getInterpreter(ScriptedMetaclass.getAlias(ImmutableSystemMessage.class), ImmutableSystemMessage.class);
		}
		catch (InvalidFieldValueInterpreterException e)
		{
			// disabled due to some reason
			return;
		}
		
		final int message = buf.readFirstInteger32(SYSMSG_ID);
		switch (message)
		{
			case SM_CASTING_INTERRUPTED:
				CommonPacketSender.sendImmutableScreenSystemMessage(client, SM_CASTING_INTERRUPTED, POS_INTERRUPTED, 0, SMALL_FONT, 0, 1, false, DURATION, FADE);
				break;
			case SM_RESISTED_EFFECT:
				final List<EnumeratedPayloadField> tokenTypes = buf.getFieldIndices(SYSMSG_TOKENS);
				final MMOBuffer buffer = buf.getMMOBuffer();
				
				String target,
				skill;
				{
					final int targetType = buf.readInteger32(tokenTypes.get(0));
					switch (targetType)
					{
						case SYSMSG_TOKEN_NPC:
							final int npcClassID = SystemMessageRecipient.getSysMsgNPCID(client.getProtocol(), buffer);
							try
							{
								target = String.valueOf(MetaclassRegistry.getInstance().getInterpreter("Npc", IntegerInterpreter.class).getInterpretation(npcClassID, null));
							}
							catch (InvalidFieldValueInterpreterException e)
							{
								target = String.valueOf(npcClassID);
							}
							break;
						case SYSMSG_TOKEN_STRING:
						case SYSMSG_TOKEN_PLAYER:
							target = buffer.readS();
							break;
						default:
							target = "Unknown[" + targetType + "]";
							break;
					}
				}
				{
					final int skillType = buf.readInteger32(tokenTypes.get(1));
					if (skillType == SYSMSG_TOKEN_SKILL)
					{
						final long skillNameID = SystemMessageRecipient.getSysMsgSkillNameID(client.getProtocol(), buffer);
						skill = L2SkillTranslator.getInterpretation(skillNameID, null);
					}
					else
						skill = "Unknown[" + skillType + "]";
				}
				CommonPacketSender.sendScreenMessage(client, POS_RESISTED, 0, SMALL_FONT, 0, 1, false, DURATION, FADE, ism.getRepresentation(SM_RESISTED_EFFECT, target, skill));
				break;
			default:
				return;
		}
	}
}
