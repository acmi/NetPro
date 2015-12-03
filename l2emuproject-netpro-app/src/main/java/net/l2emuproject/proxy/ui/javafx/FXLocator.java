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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains methods for easy resource access.
 * 
 * @author _dev_
 */
public final class FXLocator
{
	private static final int[] ICON_SIZES = { 16, 20, 24, 32, 40, 48, 64, 256 };
	
	/**
	 * Returns an URL of the specified view controller's FXML file.
	 * 
	 * @param viewController view controller
	 * @return FXML
	 */
	public static final URL getFXML(Class<?> viewController)
	{
		final String name = viewController.getSimpleName();
		return viewController.getResource(name.substring(0, name.lastIndexOf('C')) + ".fxml");
	}
	
	/**
	 * Returns the application icon list.
	 * 
	 * @return all application icons
	 */
	public static final List<? extends javafx.scene.image.Image> getIconListFX()
	{
		final List<javafx.scene.image.Image> icons = new ArrayList<>(ICON_SIZES.length);
		for (int sz : ICON_SIZES)
			icons.add(new javafx.scene.image.Image(FXLocator.class.getResource("icon-" + sz + ".png").toString()));
		return icons;
	}
}
