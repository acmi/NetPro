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

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Window;

/**
 * @author _dev_
 */
public final class WaitingIndicatorDialogController
{
	@FXML
	private Label _labDescription;
	
	@FXML
	private Button _btnCancel;
	
	/**
	 * Sets the description detailing the operation in progress.
	 * 
	 * @param contentText description
	 */
	public void setContentText(String contentText)
	{
		_labDescription.setText(contentText);
	}
	
	public void setCancelAction(Runnable cancelAction)
	{
		final Window window = getWindow();
		window.setOnHidden(e -> cancelAction.run());
		_btnCancel.setOnAction(e -> window.hide());
		_btnCancel.setDisable(false);
	}
	
	public void onWaitEnd()
	{
		final Window window = getWindow();
		window.setOnHidden(null);
		window.hide();
	}
	
	public Window getWindow()
	{
		return _btnCancel.getScene().getWindow();
	}
}
