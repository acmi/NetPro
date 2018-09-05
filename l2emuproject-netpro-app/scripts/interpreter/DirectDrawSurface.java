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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.interpreter.ByteArrayTranslator;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.ui.dds.DDSReader;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Interprets a given byte array as a DirectDraw Surface image.
 * 
 * @author savormix
 */
public class DirectDrawSurface extends ScriptedFieldValueInterpreter implements ByteArrayTranslator
{
	private static final L2Logger LOG = L2Logger.getLogger(DirectDrawSurface.class);
	
	@Override
	public Object translate(byte[] value, IProtocolVersion protocol, ICacheServerID cacheContext)
	{
		try
		{
			return DDSReader.getCrestTexture(value);
		}
		catch (IllegalArgumentException e)
		{
			// Legacy clients used BMP crests
			try
			{
				return ImageIO.read(new ByteArrayInputStream(value));
			}
			catch (IOException e2)
			{
				LOG.error("Cannot read BMP image.", e);
			}
		}
		catch (Exception e)
		{
			LOG.error("Cannot read DDS image.", e);
		}
		return value;
	}
}
