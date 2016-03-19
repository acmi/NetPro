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
package net.l2emuproject.proxy.ui.javafx;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.l2emuproject.lang.management.ShutdownManager;

import javafx.application.Platform;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

/**
 * As {@code StageHelper} and {@code FXRobotHelper} are internal (non-API) classes, this tracks created windows to promptly get rid of them during shutdown.
 * 
 * @author _dev_
 */
public class WindowTracker
{
	private final Set<WeakReference<Window>> _windows;
	private final Set<WeakReference<Dialog<?>>> _dialogs;
	
	WindowTracker()
	{
		_windows = new HashSet<>();
		_dialogs = new HashSet<>();
		
		ShutdownManager.addSpecialHook(Byte.MIN_VALUE, () ->
		{
			if (Platform.isFxApplicationThread())
				onShutdownFX();
			else
				onShutdown();
		});
	}
	
	/**
	 * Adds a window to be closed during shutdown. Must be called on the FX application thread.
	 * 
	 * @param window a window
	 */
	public void add(Window window)
	{
		_windows.add(new WeakReference<>(window));
	}
	
	/**
	 * Adds a window to be closed during shutdown. Must be called on the FX application thread.
	 * 
	 * @param dialog a window
	 */
	public void add(Dialog<?> dialog)
	{
		_dialogs.add(new WeakReference<>(dialog));
	}
	
	/** Removes expired references. Must be called on the FX application thread. */
	public void cleanup()
	{
		for (final Iterator<WeakReference<Dialog<?>>> it = _dialogs.iterator(); it.hasNext();)
			if (it.next().get() == null)
				it.remove();
		for (final Iterator<WeakReference<Window>> it = _windows.iterator(); it.hasNext();)
			if (it.next().get() == null)
				it.remove();
	}
	
	void onShutdown()
	{
		Platform.runLater(this::onShutdownFX);
	}
	
	void onShutdownFX()
	{
		for (final WeakReference<Dialog<?>> windowRef : _dialogs)
		{
			final Dialog<?> window = windowRef.get();
			if (window != null)
				window.hide();
		}
		_dialogs.clear();
		for (final WeakReference<Window> windowRef : _windows)
		{
			final Window window = windowRef.get();
			if (window != null)
				window.hide();
		}
		_windows.clear();
		
		Platform.exit();
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final WindowTracker getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final WindowTracker INSTANCE = new WindowTracker();
	}
}
