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
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;

/**
 * @author _dev_
 */
public final class CompilationErrorExpandableController implements Initializable
{
	@FXML
	private Accordion _container;
	
	@FXML
	private TitledPane _tpExpanded;
	
	@FXML
	private TextArea _taScriptList;
	
	@FXML
	private TextArea _taErrorLog;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		_container.setExpandedPane(_tpExpanded);
	}
	
	/**
	 * Sets the compilation error information.
	 * 
	 * @param scriptList list of script names
	 * @param errorLog list of compile errors
	 */
	public void setErroneousScripts(String scriptList, String errorLog)
	{
		_taScriptList.setText(scriptList);
		_taErrorLog.setText(errorLog);
	}
}
