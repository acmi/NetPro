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

import org.apache.commons.lang3.StringUtils;

import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.util.StackTraceUtil;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;

/**
 * A preconfigured exception dialog.
 * 
 * @author _dev_
 */
public class ExceptionAlert extends Alert
{
	/**
	 * Constructs a generic exception dialog.
	 * 
	 * @param t any exception
	 */
	public ExceptionAlert(Throwable t)
	{
		super(AlertType.ERROR, StringUtils.isBlank(t.getLocalizedMessage()) ? UIStrings.get("generic.exception.nodesc") : t.getLocalizedMessage());
		initModality(Modality.APPLICATION_MODAL);
		setTitle(UIStrings.get("generic.exception.uncaught"));
		setHeaderText(t.getClass().getSimpleName());
		
		final TextArea taStackTrace = new TextArea(StackTraceUtil.traceToString(t));
		taStackTrace.setMaxHeight(Double.MAX_VALUE);
		taStackTrace.setMaxWidth(Double.MAX_VALUE);
		getDialogPane().setExpandableContent(taStackTrace);
	}
}
