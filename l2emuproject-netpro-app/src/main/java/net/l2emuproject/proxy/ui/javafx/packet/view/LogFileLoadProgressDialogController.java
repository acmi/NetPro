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
package net.l2emuproject.proxy.ui.javafx.packet.view;

import java.text.NumberFormat;
import java.util.concurrent.Future;

import net.l2emuproject.proxy.ui.i18n.UIStrings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Window;

/**
 * Manages the dialog that is shown when loading a packet log file.
 * 
 * @author _dev_
 */
public final class LogFileLoadProgressDialogController
{
	@FXML
	private Label _labFilename;
	
	@FXML
	private ProgressBar _pbLoading;
	
	@FXML
	private Label _labPercentage;
	
	@FXML
	private Label _labAmount;
	
	@FXML
	private Button _btnCancel;
	
	private Future<?> _task;
	
	private Window getDialogWindow()
	{
		return _pbLoading.getScene().getWindow();
	}
	
	@FXML
	private void stopLoading(ActionEvent event)
	{
		if (event != null)
			getDialogWindow().hide();
		if (_task != null)
			_task.cancel(true);
	}
	
	/**
	 * Specifies the associated file name.
	 * 
	 * @param filename a file name
	 */
	public void setFilename(String filename)
	{
		_labFilename.setText(filename);
	}
	
	/**
	 * Updates log load progress.
	 * 
	 * @param loadedAmount packets already loaded
	 * @param totalAmount total amount of packets
	 */
	public void setLoadedAmount(int loadedAmount, int totalAmount)
	{
		final NumberFormat integerFormat = NumberFormat.getIntegerInstance(UIStrings.CURRENT_LOCALE);
		if (totalAmount < 0)
		{
			_pbLoading.setProgress(Double.NEGATIVE_INFINITY);
			_labPercentage.setText(UIStrings.get("generic.unavailable"));
			_labAmount.setText(UIStrings.get("generic.amountof", integerFormat.format(loadedAmount), UIStrings.get("generic.unavailable")));
			return;
		}
		
		final double progress = (double)loadedAmount / totalAmount;
		_pbLoading.setProgress(progress);
		_labPercentage.setText(NumberFormat.getPercentInstance(UIStrings.CURRENT_LOCALE).format(progress));
		_labAmount.setText(UIStrings.get("generic.amountof", integerFormat.format(loadedAmount), integerFormat.format(totalAmount)));
	}
	
	/**
	 * Assigns a task that can be cancelled when pressing the cancellation button.
	 * 
	 * @param task associated loading task
	 */
	public void setTask(Future<?> task)
	{
		_task = task;
		_btnCancel.setDisable(false);
		
		getDialogWindow().setOnHidden(e -> stopLoading(null));
	}
}
