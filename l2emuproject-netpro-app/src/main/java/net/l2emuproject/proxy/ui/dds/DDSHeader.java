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

import java.util.Set;

import net.l2emuproject.util.BitMaskUtils;

/**
 * <pre>
 * typedef struct {
 *   DWORD           dwSize;
 *   DWORD           dwFlags;
 *   DWORD           dwHeight;
 *   DWORD           dwWidth;
 *   DWORD           dwPitchOrLinearSize;
 *   DWORD           dwDepth;
 *   DWORD           dwMipMapCount;
 *   DWORD           dwReserved1[11];
 *   DDS_PIXELFORMAT ddspf;
 *   DWORD           dwCaps;
 *   DWORD           dwCaps2;
 *   DWORD           dwCaps3;
 *   DWORD           dwCaps4;
 *   DWORD           dwReserved2;
 * } DDS_HEADER;
 * </pre>
 * 
 * @author _dev_
 */
class DDSHeader
{
	static final int RESERVED1_SIZE = 11;
	
	final int _size, _height, _width, _pitchOrLinearSize, _depth, _mipMapCount;
	final Set<DDSD> _flags;
	final int[] _reserved1 = new int[RESERVED1_SIZE];
	final DDSPixelFormat _ddspf;
	final Set<DDSCaps> _caps;
	final Set<DDSCaps2> _caps2;
	final int _caps3, _caps4, _reserved2;
	
	DDSHeader(int size, int flags, int height, int width, int pitchOrLinearSize, int depth, int mipMapCount, int[] reserved1, DDSPixelFormat ddspf, int caps, int caps2, int caps3, int caps4,
			int reserved2)
	{
		_size = size;
		_flags = BitMaskUtils.setOf(flags, DDSD.class);
		_height = height;
		_width = width;
		_pitchOrLinearSize = pitchOrLinearSize;
		_depth = depth;
		_mipMapCount = mipMapCount;
		System.arraycopy(reserved1, 0, _reserved1, 0, _reserved1.length);
		_ddspf = ddspf;
		_caps = BitMaskUtils.setOf(caps, DDSCaps.class);
		_caps2 = BitMaskUtils.setOf(caps2, DDSCaps2.class);
		_caps3 = caps3;
		_caps4 = caps4;
		_reserved2 = reserved2;
	}
	
	@Override
	public String toString()
	{
		return "DDSHeader[_size=" + _size + ", _flags=" + _flags + ", _height=" + _height + ", _width=" + _width
				+ (_flags.contains(DDSD.PITCH) || _flags.contains(DDSD.LINEARSIZE) ? ", _pitchOrLinearSize=" + _pitchOrLinearSize : "") + (_flags.contains(DDSD.DEPTH) ? ", _depth=" + _depth : "")
				+ (_flags.contains(DDSD.MIPMAPCOUNT) ? ", _mipMapCount=" + _mipMapCount : "")/* + ", _reserved1=" + Arrays.toString(_reserved1) + */
				+ ", _ddspf=" + _ddspf + ", _caps=" + _caps + ", _caps2=" + _caps2 + /*", _caps3=" + _caps3 + ", _caps4=" + _caps4 + ", _reserved2=" + _reserved2 + */"]";
	}
	
	/** Flags to indicate which members contain valid data. */
	enum DDSD
	{
		/** Required in every .dds file. */
		CAPS,
		/** Required in every .dds file. */
		HEIGHT,
		/** Required in every .dds file. */
		WIDTH,
		/** Required when pitch is provided for an uncompressed texture. */
		PITCH, _10, _20, _40, _80, _100, _200, _400, _800,
		/** Required in every .dds file. */
		PIXELFORMAT, _2000, _4000, _8000, _10000,
		/** Required in a mipmapped texture. */
		MIPMAPCOUNT, _40000,
		/** Required when pitch is provided for a compressed texture. */
		LINEARSIZE, _100000, _200000, _400000,
		/** Required in a depth texture. */
		DEPTH,
	}
	
	/** Specifies the complexity of the surfaces stored. */
	enum DDSCaps
	{
		_1, _2, _4,
		/** Optional; must be used on any file that contains more than one surface (a mipmap, a cubic environment map, or mipmapped volume texture). */
		COMPLEX, _10, _20, _40, _80, _100, _200, _400, _800,
		/** Required (not present in Lineage II crest textures!) */
		TEXTURE, _2000, _4000, _8000, _10000, _20000, _40000, _80000, _100000, _200000,
		/** Optional; should be used for a mipmap. */
		MIPMAP,
	}
	
	/** Additional detail about the surfaces stored. */
	enum DDSCaps2
	{
		_1, _2, _4, _8, _10, _20, _40, _80, _100,
		/** Required for a cube map. */
		CUBEMAP,
		/** Required when these surfaces are stored in a cube map. */
		CUBEMAP_POSITIVEX,
		/** Required when these surfaces are stored in a cube map. */
		CUBEMAP_NEGATIVEX,
		/** Required when these surfaces are stored in a cube map. */
		CUBEMAP_POSITIVEY,
		/** Required when these surfaces are stored in a cube map. */
		CUBEMAP_NEGATIVEY,
		/** Required when these surfaces are stored in a cube map. */
		CUBEMAP_POSITIVEZ,
		/** Required when these surfaces are stored in a cube map. */
		CUBEMAP_NEGATIVEZ, _10000, _20000, _40000, _80000, _100000,
		/** Required for a volume texture. */
		VOLUME,
	}
}
