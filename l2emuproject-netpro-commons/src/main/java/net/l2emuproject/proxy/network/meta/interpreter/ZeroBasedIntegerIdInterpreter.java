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
package net.l2emuproject.proxy.network.meta.interpreter;

import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Allows to optimize memory usage.
 * 
 * @author _dev_
 */
public abstract class ZeroBasedIntegerIdInterpreter implements IntegerInterpreter
{
	private final InterpreterMetadata _metadata;
	private final Object[] _interpretations;
	
	/**
	 * Constructs this interpreter.
	 * 
	 * @param metadata metadata
	 * @param interpretations known ID interpretations
	 */
	protected ZeroBasedIntegerIdInterpreter(InterpreterMetadata metadata, Object... interpretations)
	{
		_metadata = metadata;
		_interpretations = interpretations;
	}
	
	/**
	 * Constructs this interpreter.
	 * 
	 * @param interpretations known ID interpretations
	 */
	protected ZeroBasedIntegerIdInterpreter(Object... interpretations)
	{
		this(InterpreterMetadata.DEFAULT_METADATA, interpretations);
	}
	
	@Override
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		final int idx = (int)(value - _metadata._offset);
		if (idx < 0 || idx >= _interpretations.length || _interpretations[idx] == null)
			return _metadata._unknownKeyInterpretation != null ? _metadata._unknownKeyInterpretation : value;
		
		return _interpretations[idx];
	}
	
	/** Provides a convenient way to pass metadata to this interpreter. */
	public static final class InterpreterMetadata
	{
		/** Default metadata with no offset and no default interpretation for unknown IDs. */
		public static final InterpreterMetadata DEFAULT_METADATA = new InterpreterMetadata();
		
		final int _offset;
		final Object _unknownKeyInterpretation;
		
		/**
		 * Constructs the metadata wrapper.
		 * 
		 * @param offset ID of interpretation at index 0
		 * @param unknownKeyInterpretation unknown ID interpretation or {@code null}
		 */
		public InterpreterMetadata(int offset, Object unknownKeyInterpretation)
		{
			_offset = offset;
			_unknownKeyInterpretation = unknownKeyInterpretation;
		}
		
		/**
		 * Constructs the metadata wrapper.
		 * 
		 * @param offset ID of interpretation at index 0
		 */
		public InterpreterMetadata(int offset)
		{
			this(offset, null);
		}
		
		/**
		 * Constructs the metadata wrapper.
		 * 
		 * @param unknownKeyInterpretation unknown ID interpretation or {@code null}
		 */
		public InterpreterMetadata(Object unknownKeyInterpretation)
		{
			this(0, unknownKeyInterpretation);
		}
		
		/** Constructs the default metadata wrapper. */
		private InterpreterMetadata()
		{
			this(0, null);
		}
	}
}
