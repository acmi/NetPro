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

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import org.google.jhsheets.filtered.operators.StringOperator;

/**
 * Contains methods for easy resource access.
 * 
 * @author _dev_
 */
public final class FXUtils
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
			icons.add(new javafx.scene.image.Image(FXUtils.class.getResource("icon-" + sz + ".png").toString()));
		return icons;
	}
	
	/**
	 * Encodes the image as a whole into PNG, then into Base64 and finally into an URI suitable for the HTML {@code <img>} tag.
	 * 
	 * @param image an image
	 * @return image as URI (image within the URI)
	 * @throws IIOException if there is a fault with an image writer
	 * @throws IOException in case of a general I/O error
	 */
	public static final String getImageSrcForWebEngine(RenderedImage image) throws IIOException, IOException
	{
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, "PNG", output);
		return "data:base64," + Base64.getMimeEncoder().encodeToString(output.toByteArray());
	}
	
	/**
	 * Tests a {@link String} value against the given filter.
	 * 
	 * @param actualValue cell value
	 * @param filter filter
	 * @return {@code true}, if cell should not be visible, {@code false} otherwise
	 */
	public static final boolean isHidden(String actualValue, StringOperator filter)
	{
		final String filterValue = filter.getValue();
		switch (filter.getType())
		{
			case EQUALS:
				if (!actualValue.equals(filterValue))
					return true;
				break;
			case NOTEQUALS:
				if (actualValue.equals(filterValue))
					return true;
				break;
			case CONTAINS:
				if (!actualValue.contains(filterValue))
					return true;
				break;
			case STARTSWITH:
				if (!actualValue.startsWith(filterValue))
					return true;
				break;
			case ENDSWITH:
				if (!actualValue.endsWith(filterValue))
					return true;
				break;
		}
		return false;
	}
}
