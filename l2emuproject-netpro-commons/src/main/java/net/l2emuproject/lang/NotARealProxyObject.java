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
package net.l2emuproject.lang;

/**
 * A wrapper class, designed to be represent an object of some 3rd party class
 * and allow it to implement unsolicited interfaces.
 * 
 * @author _dev_
 * @param <T> type of object to be wrapped
 */
public abstract class NotARealProxyObject<T>
{
	private final T _object;
	
	/**
	 * Creates a proxy object for {@code object}.
	 * 
	 * @param object an object
	 */
	protected NotARealProxyObject(T object)
	{
		if (object == null)
			throw new NullPointerException();
		
		_object = object;
	}
	
	/**
	 * Returns the wrapped object.
	 * 
	 * @return underlying object
	 */
	protected T get()
	{
		return _object;
	}
	
	@Override
	public int hashCode()
	{
		return _object.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof NotARealProxyObject)
			o = ((NotARealProxyObject<?>)o).get();
		
		return _object.equals(o);
	}
	
	@Override
	public String toString()
	{
		return _object.toString();
	}
}
