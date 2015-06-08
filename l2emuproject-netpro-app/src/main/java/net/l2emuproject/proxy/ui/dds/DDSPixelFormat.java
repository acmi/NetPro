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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import net.l2emuproject.util.BitMaskUtils;

/**
 * <pre>
 * struct DDS_PIXELFORMAT {
 *   DWORD dwSize;
 *   DWORD dwFlags;
 *   DWORD dwFourCC;
 *   DWORD dwRGBBitCount;
 *   DWORD dwRBitMask;
 *   DWORD dwGBitMask;
 *   DWORD dwBBitMask;
 *   DWORD dwABitMask;
 * };
 * </pre>
 * 
 * @author _dev_
 */
class DDSPixelFormat
{
	static final int DXT1 = fourCC("DXT1"), DXT2 = fourCC("DXT2"), DXT3 = fourCC("DXT3"), DXT4 = fourCC("DXT4"), DXT5 = fourCC("DXT5"), DX10 = fourCC("DX10");
	
	final int _size, _fourCC, _rgbBitCount, _rBitMask, _gBitMask, _bBitMask, _aBitMask;
	final Set<DDPF> _flags;
	
	DDSPixelFormat(int size, int flags, int fourCC, int rgbBitCount, int rBitMask, int gBitMask, int bBitMask, int aBitMask)
	{
		_size = size;
		_flags = BitMaskUtils.setOf(flags, DDPF.class);
		_fourCC = fourCC;
		_rgbBitCount = rgbBitCount;
		_rBitMask = rBitMask;
		_gBitMask = gBitMask;
		_bBitMask = bBitMask;
		_aBitMask = aBitMask;
	}
	
	@Override
	public String toString()
	{
		return "DDSPixelFormat[_size=" + _size + ", _flags=" + _flags + (_flags.contains(DDPF.FOURCC) ? ", _fourCC=" + fourChars(_fourCC) : "")
				+ (_flags.contains(DDPF.RGB) || _flags.contains(DDPF.LUMINANCE) || _flags.contains(DDPF.YUV) ? ", _rgbBitCount=" + _rgbBitCount + ", _rBitMask=" + _rBitMask : "")
				+ (_flags.contains(DDPF.RGB) || _flags.contains(DDPF.YUV) ? ", _gBitMask=" + _gBitMask + ", _bBitMask=" + _bBitMask : "")
				+ (_flags.contains(DDPF.ALPHAPIXELS) || _flags.contains(DDPF.ALPHA) ? ", _aBitMask=" + _aBitMask : "") + "]";
	}
	
	private static final String fourChars(int dword)
	{
		return new String(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(dword).array(), StandardCharsets.US_ASCII);
	}
	
	private static final int fourCC(String chars)
	{
		return ByteBuffer.wrap(chars.getBytes(StandardCharsets.US_ASCII), 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}
	
	/** Values which indicate what type of data is in the surface. */
	enum DDPF
	{
		/** Texture contains alpha data; dwRGBAlphaBitMask contains valid data. */
		ALPHAPIXELS,
		/** Used in some older DDS files for alpha channel only uncompressed data (dwRGBBitCount contains the alpha channel bitcount; dwABitMask contains valid data) */
		ALPHA,
		/** Texture contains compressed RGB data; dwFourCC contains valid data. */
		FOURCC, _8, _10, _20,
		/** Texture contains uncompressed RGB data; dwRGBBitCount and the RGB masks (dwRBitMask, dwGBitMask, dwBBitMask) contain valid data. */
		RGB, _80, _100,
		/**
		 * Used in some older DDS files for YUV uncompressed data (dwRGBBitCount contains the YUV bit count; dwRBitMask contains the Y mask, dwGBitMask contains the U mask, dwBBitMask contains the V
		 * mask)
		 */
		YUV, _400, _800, _1000, _2000, _4000, _8000, _10000,
		/**
		 * Used in some older DDS files for single channel color uncompressed data (dwRGBBitCount contains the luminance channel bit count; dwRBitMask contains the channel mask). Can be combined with
		 * DDPF_ALPHAPIXELS for a two channel DDS file.
		 */
		LUMINANCE,
	}
}
