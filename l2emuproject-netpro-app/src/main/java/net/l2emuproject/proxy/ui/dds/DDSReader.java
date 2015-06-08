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
package net.l2emuproject.proxy.ui.dds;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.l2emuproject.proxy.ui.dds.DDSHeader.DDSCaps;
import net.l2emuproject.proxy.ui.dds.DDSHeader.DDSD;
import net.l2emuproject.proxy.ui.dds.DDSPixelFormat.DDPF;

/**
 * A minimal implementation of a DDS reader just for Lineage II crest/insignia textures.<BR>
 * <BR>
 * I could not find a fitting library due to licensing issues and had to do it myself according to the
 * <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/bb943991%28v=vs.85%29.aspx">specification</a>.
 * DXT1 decompression is implemented as described
 * <a href="https://www.opengl.org/wiki/S3_Texture_Compression#DXT1_Format">here</a>.
 * 
 * @author _dev_
 */
public class DDSReader
{
	private static final int MAGIC = 0x20534444;
	
	private static final Set<DDSD> UNSUPPORTED_HEADER_FLAGS = EnumSet.of(DDSD.MIPMAPCOUNT, DDSD.DEPTH);
	private static final Set<DDSCaps> UNSUPPORTED_CAPS = EnumSet.of(DDSCaps.COMPLEX, DDSCaps.MIPMAP);
	private static final Set<DDPF> UNSUPPORTED_PF_FLAGS = EnumSet.of(DDPF.ALPHA, DDPF.ALPHAPIXELS, DDPF.LUMINANCE, DDPF.RGB, DDPF.YUV);
	
	private static final int COMPONENT_RED = 0, COMPONENT_GREEN = 1, COMPONENT_BLUE = 2;
	
	/**
	 * Returns the first DXT1-compressed texture contained in the given DirectDrawSurface image.
	 * 
	 * @param dds DDS image
	 * @return contained texture
	 */
	public static final BufferedImage getCrestTexture(byte[] dds)
	{
		// Phase 1: Validation
		if (dds.length < 128)
			throw new IllegalArgumentException();
		
		final ByteBuffer buf = ByteBuffer.wrap(dds).order(ByteOrder.LITTLE_ENDIAN);
		if (buf.getInt() != MAGIC)
			throw new IllegalArgumentException();
		
		final DDSHeader header = readHeader(buf);
		if (header._size != 124)
			throw new IllegalArgumentException();
		
		final DDSPixelFormat pixelFormat = header._ddspf;
		if (pixelFormat._size != 32)
			throw new IllegalArgumentException();
		if (pixelFormat._flags.contains(DDPF.FOURCC) && pixelFormat._fourCC == DDSPixelFormat.DX10 && dds.length < 148)
			throw new IllegalArgumentException();
		
		// Phase 2: General case denial
		if (!Collections.disjoint(header._flags, UNSUPPORTED_HEADER_FLAGS) || !Collections.disjoint(header._caps, UNSUPPORTED_CAPS))
			throw new UnsupportedOperationException();
		if (!Collections.disjoint(pixelFormat._flags, UNSUPPORTED_PF_FLAGS))
			throw new UnsupportedOperationException();
		if (pixelFormat._flags.contains(DDPF.FOURCC) && pixelFormat._fourCC == DDSPixelFormat.DX10)
			throw new UnsupportedOperationException(); // cannot read DX10 header
			
		// Phase 3: Single format support
		if (!pixelFormat._flags.contains(DDPF.FOURCC) || pixelFormat._fourCC != DDSPixelFormat.DXT1)
			throw new UnsupportedOperationException(); // can only read DXT1 compressed images
			
		final int size = (Math.max(1, (header._width + 3) >> 2) * Math.max(1, (header._height + 3) >> 2)) << 3;
		if (buf.remaining() != size)
			throw new UnsupportedOperationException(); // can only read a single image
			
		final BufferedImage img = new BufferedImage(header._width, header._height, BufferedImage.TYPE_3BYTE_BGR);
		final int blocksInLine = img.getWidth() >> 2;
		for (int block = 0; buf.hasRemaining(); ++block)
		{
			// implements the S3TC variant 1 (DXT1)
			// this is a lossy compression, so half of the colors must be inferred from existing ones
			final int[][] availableColors = new int[4][3];
			final int color0 = buf.getChar(), color1 = buf.getChar();
			availableColors[0] = rgb565AsIndividualComponents(color0);
			availableColors[1] = rgb565AsIndividualComponents(color1);
			if (color0 > color1)
			{
				for (int individualComponent = COMPONENT_RED; individualComponent <= COMPONENT_BLUE; ++individualComponent)
				{
					availableColors[2][individualComponent] = ((availableColors[0][individualComponent] << 1) + availableColors[1][individualComponent]) / 3;
					availableColors[3][individualComponent] = ((availableColors[1][individualComponent] << 1) + availableColors[0][individualComponent]) / 3;
				}
			}
			else
			{
				for (int individualComponent = COMPONENT_RED; individualComponent <= COMPONENT_BLUE; ++individualComponent)
				{
					availableColors[2][individualComponent] = (availableColors[0][individualComponent] + availableColors[1][individualComponent]) >> 1;
					availableColors[3][individualComponent] = 0;
				}
			}
			final int blockRow = block / blocksInLine, blockCol = block % blocksInLine;
			int pixels = buf.getInt();
			for (int y = 0; y < 4; ++y)
			{
				for (int x = 0; x < 4; ++x)
				{
					final int[] colorComponents = availableColors[pixels & 3];
					img.setRGB((blockCol << 2) + x, (blockRow << 2) + y, individualComponentsAsARGB(colorComponents));
					pixels >>>= 2;
				}
			}
		}
		return img;
	}
	
	// The arithmetic operations are done per-component, not on the integer value of the colors.
	private static final int[] rgb565AsIndividualComponents(int rgb565)
	{
		final int[] rgb = new int[3];
		rgb[COMPONENT_RED] = (rgb565 >> (5 + 6));
		rgb[COMPONENT_GREEN] = (rgb565 >> 5) & 0x3F;
		rgb[COMPONENT_BLUE] = rgb565 & 0x1F;
		return rgb;
	}
	
	// http://stackoverflow.com/a/9069480/1320710
	private static final int individualComponentsAsARGB(int[] rgb565)
	{
		final int red = (rgb565[COMPONENT_RED] * 527 + 23) >> 6;
		final int green = (rgb565[COMPONENT_GREEN] * 259 + 33) >> 6;
		final int blue = (rgb565[COMPONENT_BLUE] * 527 + 23) >> 6;
		return (0xFF << 24) | (red << 16) | (green << 8) | blue;
	}
	
	private static final DDSHeader readHeader(ByteBuffer buf)
	{
		final int size = buf.getInt(), flags = buf.getInt(), height = buf.getInt(), width = buf.getInt(), pitchOrLinearSize = buf.getInt(), depth = buf.getInt(), mipMapCount = buf.getInt();
		final int[] reserved1 = new int[DDSHeader.RESERVED1_SIZE];
		for (int i = 0; i < reserved1.length; ++i)
			reserved1[i] = buf.getInt();
		final DDSPixelFormat ddspf = readPixelFormat(buf);
		final int caps = buf.getInt(), caps2 = buf.getInt(), caps3 = buf.getInt(), caps4 = buf.getInt(), reserved2 = buf.getInt();
		return new DDSHeader(size, flags, height, width, pitchOrLinearSize, depth, mipMapCount, reserved1, ddspf, caps, caps2, caps3, caps4, reserved2);
	}
	
	private static final DDSPixelFormat readPixelFormat(ByteBuffer buf)
	{
		final int size = buf.getInt(), flags = buf.getInt(), fourCC = buf.getInt(), rgbBitCount = buf.getInt(), rBitMask = buf.getInt(), gBitMask = buf.getInt(), bBitMask = buf.getInt(), aBitMask = buf
				.getInt();
		return new DDSPixelFormat(size, flags, fourCC, rgbBitCount, rBitMask, gBitMask, bBitMask, aBitMask);
	}
}
