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
package interpreter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerTranslator;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword as a name/title color.
 * 
 * @author savormix
 */
public class BGR extends ScriptedFieldValueInterpreter implements IntegerTranslator
{
	@Override
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		final int w = 32, h = 14;
		final BufferedImage re = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = re.createGraphics();
		try
		{
			final int bgr = (int) value;
			final Color c = new Color(bgr & 0xFF, (bgr >> 8) & 0xFF, (bgr >> 16) & 0xFF);
			g.setColor(c);
			g.fillRect(0, 0, w, h);
			g.setColor(Color.BLACK);
			g.drawRect(0, 0, w - 1, h - 1);
		}
		finally
		{
			g.dispose();
		}
		return re;
	}
}
