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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

import net.l2emuproject.proxy.NetProInfo;
import net.l2emuproject.proxy.ui.i18n.UIStrings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;

/**
 * Properly initializes controls within the about dialog.
 * 
 * @author _dev_
 */
public class AboutDialogController implements Initializable
{
	@FXML
	private Label _labBuild;
	
	@FXML
	private void closeDialog(ActionEvent event)
	{
		_labBuild.getScene().getWindow().hide();
	}
	
	@FXML
	private void openLicense(ActionEvent event)
	{
		if (!Desktop.isDesktopSupported())
			return;
		
		final Desktop desktop = Desktop.getDesktop();
		try
		{
			if (desktop.isSupported(Desktop.Action.BROWSE))
				desktop.browse(URI.create(((Hyperlink)event.getSource()).getText()));
		}
		catch (IOException | IllegalArgumentException e)
		{
			// too bad
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		final String build;
		if (NetProInfo.isUnreleased())
			build = "norelease";
		else if (NetProInfo.isSnapshot())
			build = "snapshot";
		else
			build = "stable";
		
		_labBuild.setText(UIStrings.get("about.build." + build, NetProInfo.getRevisionNumber()));
	}
}
