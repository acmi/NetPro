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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.l2emuproject.util.StackTraceUtil;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

/**
 * Handles a dialog that can display the details of an arbitrary amount of exceptions.
 * 
 * @author _dev_
 */
public final class ExceptionSummaryDialogController implements Initializable
{
	@FXML
	private ListView<ExceptionEntry> _lvEntries;
	
	@FXML
	private TextArea _taStackTrace;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		_lvEntries.getSelectionModel().selectedItemProperty().addListener((obs, old, neu) -> _taStackTrace.setText(neu != null ? neu._textAreaContent : null));
	}
	
	/**
	 * Sets the exceptions to be displayed.
	 * 
	 * @param exceptions exception map
	 */
	public <V extends Throwable> void setAllExceptions(Map<?, V> exceptions)
	{
		setAllExceptions(exceptions, String::valueOf);
	}
	
	/**
	 * Sets the exceptions to be displayed.
	 * 
	 * @param exceptions exception map
	 * @param keyConverter entry name function
	 */
	public <K, V extends Throwable> void setAllExceptions(Map<K, V> exceptions, Function<K, String> keyConverter)
	{
		setAllExceptions(exceptions, keyConverter, StackTraceUtil::traceToString);
	}
	
	/**
	 * Sets the exceptions to be displayed.
	 * 
	 * @param exceptions exception map
	 * @param keyConverter entry name function
	 * @param valueConverter entry details function
	 */
	public <K, V extends Throwable> void setAllExceptions(Map<K, V> exceptions, Function<K, String> keyConverter, Function<Throwable, String> valueConverter)
	{
		setAllExceptions(exceptions, keyConverter, (k, v) -> valueConverter.apply(v));
	}
	
	/**
	 * Sets the exceptions to be displayed.
	 * 
	 * @param exceptions exception map
	 * @param keyConverter entry name function
	 * @param valueConverter entry details function
	 */
	public <K, V extends Throwable> void setAllExceptions(Map<K, V> exceptions, Function<K, String> keyConverter, BiFunction<K, Throwable, String> valueConverter)
	{
		if (exceptions.isEmpty())
		{
			_lvEntries.setItems(FXCollections.emptyObservableList());
			return;
		}
		
		final List<ExceptionEntry> items = new ArrayList<>(exceptions.size());
		for (final Entry<K, V> e : exceptions.entrySet())
			items.add(new ExceptionEntry(keyConverter.apply(e.getKey()), valueConverter.apply(e.getKey(), e.getValue())));
		_lvEntries.setItems(FXCollections.observableList(items));
		_lvEntries.getSelectionModel().selectFirst();
	}
	
	private static final class ExceptionEntry
	{
		private final String _listViewName;
		final String _textAreaContent;
		
		ExceptionEntry(String listViewName, String textAreaContent)
		{
			_listViewName = listViewName;
			_textAreaContent = textAreaContent;
		}
		
		@Override
		public String toString()
		{
			return _listViewName;
		}
	}
}
