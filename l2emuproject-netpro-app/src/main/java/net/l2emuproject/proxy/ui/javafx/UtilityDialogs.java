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

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.javafx.main.view.WaitingIndicatorDialogController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * A set of convenience methods for utility dialogs.
 * 
 * @author _dev_
 */
public final class UtilityDialogs
{
	private UtilityDialogs()
	{
		// utility class
	}
	
	public static final <E> Optional<E> showChoiceDialog(Window ownerWindow, String title, String header, Set<E> choices)
	{
		return initNonModalUtilityDialog(new ChoiceDialog<>(choices.iterator().next(), choices), ownerWindow, title, header, null).showAndWait();
	}
	
	public static final <E> Optional<E> showChoiceDialog(Window ownerWindow, String title, String header, E... choices)
	{
		return initNonModalUtilityDialog(new ChoiceDialog<>(choices[0], choices), ownerWindow, title, header, null).showAndWait();
	}
	
	public static final Alert makeNonModalUtilityAlert(AlertType type, Window ownerWindow, String title, String header, String content, Object... contentTokens)
	{
		return initNonModalUtilityDialog(new Alert(type), ownerWindow, title, header, content, contentTokens);
	}
	
	public static final Alert makeNonModalUtilityAlert(AlertType type, Window ownerWindow, String title, Object[] titleTokens, String header, Object[] headerTokens, String content,
			Object... contentTokens)
	{
		return initNonModalUtilityDialog(new Alert(type), ownerWindow, title, titleTokens, header, headerTokens, content, contentTokens);
	}
	
	public static final <T, D extends Dialog<T>> D initNonModalUtilityDialog(D dialog, Window ownerWindow, String title, String header, String content, Object... contentTokens)
	{
		return initNonModalUtilityDialog(dialog, ownerWindow, title, null, header, null, content, contentTokens);
	}
	
	public static final <T, D extends Dialog<T>> D initNonModalUtilityDialog(D dialog, Window ownerWindow, String title, Object[] titleTokens, String header, Object[] headerTokens, String content,
			Object... contentTokens)
	{
		dialog.initModality(Modality.NONE);
		dialog.initOwner(ownerWindow);
		dialog.initStyle(StageStyle.UTILITY);
		
		dialog.setTitle(UIStrings.get(title, titleTokens));
		dialog.setHeaderText(UIStrings.get(header, headerTokens));
		if (content != null)
			dialog.setContentText(UIStrings.get(content, contentTokens));
		
		WindowTracker.getInstance().add(dialog);
		return dialog;
	}
	
	public static final ExceptionAlert wrapException(Throwable t, String title, Object[] titleTokens, String header, Object[] headerTokens, Window owner, Modality modality)
	{
		final ExceptionAlert alert = new ExceptionAlert(t, owner, UIStrings.get(title, titleTokens), UIStrings.get(header, headerTokens));
		alert.initModality(modality);
		return alert;
	}
	
	public static final WaitingIndicatorDialogController showWaitDialog(Window ownerWindow, String title, String description, Object... descriptionTokens)
	{
		try
		{
			final FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(WaitingIndicatorDialogController.class), UIStrings.getBundle());
			final Scene scene = new Scene(loader.load(), null);
			
			final Stage stage = new Stage(StageStyle.UTILITY);
			stage.initModality(Modality.NONE);
			stage.initOwner(ownerWindow);
			stage.setTitle(UIStrings.get(title));
			stage.setScene(scene);
			stage.getIcons().addAll(FXUtils.getIconListFX());
			stage.sizeToScene();
			stage.setResizable(false);
			
			final WaitingIndicatorDialogController controller = loader.getController();
			controller.setContentText(UIStrings.get(description, descriptionTokens));
			
			WindowTracker.getInstance().add(stage);
			stage.show();
			return controller;
		}
		catch (IOException e)
		{
			throw new AssertionError("Waiting dialog is missing", e);
		}
	}
}
