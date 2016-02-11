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
package net.l2emuproject.proxy.ui.javafx.main.view;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Handles interactions with the splash screen.
 * 
 * @author _dev_
 */
public final class SplashScreenController
{
	@FXML
	private Label _labDescription;
	
	/**
	 * Binds the description label's text.
	 * 
	 * @param descriptionValue new description
	 */
	public void bindDescription(ObservableValue<? extends String> descriptionValue)
	{
		_labDescription.textProperty().bind(descriptionValue);
	}
}
