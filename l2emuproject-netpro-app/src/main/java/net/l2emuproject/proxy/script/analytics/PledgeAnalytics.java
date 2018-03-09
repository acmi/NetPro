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
package net.l2emuproject.proxy.script.analytics;

import java.util.List;

import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.script.ScriptFieldAlias;
import net.l2emuproject.proxy.state.entity.L2ObjectInfo;
import net.l2emuproject.proxy.state.entity.L2ObjectInfoCache;
import net.l2emuproject.proxy.state.entity.ObjectInfo;
import net.l2emuproject.proxy.state.entity.PledgeInfo;
import net.l2emuproject.proxy.state.entity.cache.AllianceCrestInfoCache;
import net.l2emuproject.proxy.state.entity.cache.AllianceInfoCache;
import net.l2emuproject.proxy.state.entity.cache.PledgeCrestInfoCache;
import net.l2emuproject.proxy.state.entity.cache.PledgeInfoCache;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.state.entity.type.PledgeType;

/**
 * Allows pledge IDs to be interpreted in a log file.
 * 
 * @author savormix
 */
public class PledgeAnalytics extends PpeAnalyticsScript
{
	@ScriptFieldAlias
	private static final String PLEDGE_ID = "PIC_PLEDGE_ID";
	@ScriptFieldAlias
	private static final String PLEDGE_NAME = "PIC_PLEDGE_NAME";
	@ScriptFieldAlias
	private static final String PLEDGE_CREST_ID = "PIC_PLEDGE_CREST_ID";
	@ScriptFieldAlias
	private static final String PLEDGE_CREST_SIZE = "PIC_PLEDGE_CREST_SIZE";
	@ScriptFieldAlias
	private static final String PLEDGE_CREST_FULL = "PIC_PLEDGE_CREST";
	@ScriptFieldAlias
	private static final String ALLIANCE_ID = "AIC_ALLY_ID";
	@ScriptFieldAlias
	private static final String ALLIANCE_NAME = "AIC_ALLY_NAME";
	@ScriptFieldAlias
	private static final String ALLIANCE_CREST_ID = "AIC_ALLY_CREST_ID";
	@ScriptFieldAlias
	private static final String ALLIANCE_CREST_SIZE = "AIC_ALLY_CREST_SIZE";
	@ScriptFieldAlias
	private static final String ALLIANCE_CREST_FULL = "AIC_ALLY_CREST";
	
	@Override
	public void handleClientPacket(RandomAccessMMOBuffer buf, ICacheServerID cacheContext) throws RuntimeException
	{
		// none handled
	}
	
	@Override
	public void handleServerPacket(RandomAccessMMOBuffer buf, ICacheServerID cacheContext) throws RuntimeException
	{
		final List<EnumeratedPayloadField> ids = buf.getFieldIndices(PLEDGE_ID);
		final List<EnumeratedPayloadField> names = buf.getFieldIndices(PLEDGE_NAME);
		final List<EnumeratedPayloadField> cszs = buf.getFieldIndices(PLEDGE_CREST_SIZE);
		final List<EnumeratedPayloadField> crests = buf.getFieldIndices(PLEDGE_CREST_FULL);
		final List<EnumeratedPayloadField> cids = buf.getFieldIndices(PLEDGE_CREST_ID);
		final List<EnumeratedPayloadField> aids = buf.getFieldIndices(ALLIANCE_ID);
		final List<EnumeratedPayloadField> ans = buf.getFieldIndices(ALLIANCE_NAME);
		final List<EnumeratedPayloadField> acids = buf.getFieldIndices(ALLIANCE_CREST_ID);
		final List<EnumeratedPayloadField> acszs = buf.getFieldIndices(ALLIANCE_CREST_SIZE);
		final List<EnumeratedPayloadField> acrests = buf.getFieldIndices(ALLIANCE_CREST_FULL);
		
		if (!cszs.isEmpty())
		{
			for (int i = 0; i < cids.size(); ++i)
			{
				final int cid = buf.readInteger32(cids.get(i));
				final int sz = buf.readInteger32(cszs.get(i));
				final byte[] crest = buf.readBytes(crests.get(i), sz);
				
				PledgeCrestInfoCache.getInstance().getOrAdd(cid, cacheContext).setCrest(crest);
			}
			return;
		}
		if (!acszs.isEmpty())
		{
			for (int i = 0; i < acids.size(); ++i)
			{
				final int aid = buf.readInteger32(acids.get(i));
				final int sz = buf.readInteger32(acszs.get(i));
				final byte[] crest = buf.readBytes(acrests.get(i), sz);
				
				AllianceCrestInfoCache.getInstance().getOrAdd(aid, cacheContext).setCrest(crest);
			}
			return;
		}
		
		if (!names.isEmpty())
		{
			for (int i = 0; i < ids.size(); ++i)
			{
				final int id = buf.readInteger32(ids.get(i));
				final String name = buf.readString(names.get(i));
				
				final PledgeInfo pi = PledgeInfoCache.getInstance().getOrAdd(id, cacheContext);
				pi.setName(name);
				final ObjectInfo<L2ObjectInfo> oi = L2ObjectInfoCache.getOrAdd(id, cacheContext).setType(new PledgeType());
				oi.setName(name);
				
				final int allyId = pi.getAllianceID();
				if (allyId != 0 && !ans.isEmpty())
					AllianceInfoCache.getInstance().getOrAdd(allyId, cacheContext).setName(buf.readString(ans.get(i)));
			}
			return;
		}
		
		if (!ids.isEmpty() && !cids.isEmpty())
		{
			for (int i = 0; i < ids.size(); ++i)
			{
				final int id = buf.readInteger32(ids.get(i));
				final int cid = buf.readInteger32(cids.get(i));
				final int aid = aids.isEmpty() ? 0 : buf.readInteger32(aids.get(i));
				final int acid = acids.isEmpty() ? 0 : buf.readInteger32(acids.get(i));
				
				final PledgeInfo pi = PledgeInfoCache.getInstance().getOrAdd(id, cacheContext);
				pi.setCrestID(cid);
				if (aid != 0)
				{
					pi.setAllianceID(aid);
					if (acid != 0)
						AllianceInfoCache.getInstance().getOrAdd(aid, cacheContext).setCrestID(acid);
				}
			}
		}
	}
	
	@Override
	public String getName()
	{
		return "Analytics – Pledge/Ally ID & crest interpretation";
	}
	
	@Override
	public double getPriority()
	{
		return -Double.longBitsToDouble(0x7feffffffffffffcL);
	}
}
