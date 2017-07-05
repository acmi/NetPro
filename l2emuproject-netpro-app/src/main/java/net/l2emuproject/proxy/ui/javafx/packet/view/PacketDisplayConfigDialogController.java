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

import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.WARNING;
import static net.l2emuproject.proxy.ui.javafx.UtilityDialogs.makeNonModalUtilityAlert;
import static net.l2emuproject.proxy.ui.javafx.UtilityDialogs.wrapException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.io.exception.DamagedFileException;
import net.l2emuproject.proxy.io.exception.InsufficientlyLargeFileException;
import net.l2emuproject.proxy.io.exception.UnknownFileTypeException;
import net.l2emuproject.proxy.io.packethiding.PacketHidingConfigFileUtils;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.javafx.UtilityDialogs;
import net.l2emuproject.proxy.ui.javafx.main.view.MainWindowController;
import net.l2emuproject.proxy.ui.javafx.main.view.WaitingIndicatorDialogController;
import net.l2emuproject.proxy.ui.javafx.packet.IPacketHidingConfig;
import net.l2emuproject.proxy.ui.javafx.packet.PacketHidingConfig;
import net.l2emuproject.proxy.ui.javafx.packet.ProtocolPacketHidingManager;
import net.l2emuproject.util.HexUtil;
import net.l2emuproject.util.StackTraceUtil;
import net.l2emuproject.util.concurrent.L2ThreadPool;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * Handles the packet hiding configuration view for both endpoint types.
 * 
 * @author _dev_
 */
public class PacketDisplayConfigDialogController implements IOConstants
{
	@FXML
	private PacketDisplayConfigTableViewController _clientPacketTableController;
	
	@FXML
	private PacketDisplayConfigTableViewController _serverPacketTableController;
	
	// when a new packet config is imported, we must be able to reconstruct the tables
	// and to update the associated tab (and other tabs in case of protocol)
	private Set<IPacketTemplate> _clientPackets, _serverPackets;
	private IProtocolVersion _protocolVersion;
	private ObjectProperty<IPacketHidingConfig> _tabHidingConfigProperty;
	private Runnable _onTabConfigChange, _onProtocolConfigChange;
	
	private File _lastOpenDirectory = IOConstants.PROTOCOL_PACKET_HIDING_DIR.toFile();
	
	@FXML
	private void exportHidingConfig(ActionEvent event)
	{
		final Window wnd = ((Node)event.getSource()).getScene().getWindow();
		
		final ExportType exportType;
		if (_tabHidingConfigProperty != null)
		{
			final Optional<ExportType> result = UtilityDialogs.showChoiceDialog(wnd, "packetdc.export.choicedialog.title", "packetdc.export.choicedialog.header", ExportType.VALUES);
			if (!result.isPresent())
				return;
			
			exportType = result.get();
		}
		else
			exportType = ExportType.PROTOCOL_ONLY;
		
		final FileChooser fc = new FileChooser();
		fc.setTitle(UIStrings.get("open.nph.fileselect.title"));
		fc.getExtensionFilters().addAll(new ExtensionFilter(UIStrings.get("open.nph.fileselect.description"), "*." + PROTOCOL_PACKET_HIDING_EXTENSION),
				new ExtensionFilter(UIStrings.get("generic.filedlg.allfiles"), "*.*"));
		fc.setInitialDirectory(_lastOpenDirectory);
		
		final File selectedFile = fc.showSaveDialog(wnd);
		if (selectedFile == null)
			return;
		
		_lastOpenDirectory = selectedFile.getParentFile();
		
		final Path hidingConfigFile;
		{
			String filename = selectedFile.getName();
			if (!filename.contains(".") && !fc.getSelectedExtensionFilter().getExtensions().contains("*.*"))
				filename += "*." + PROTOCOL_PACKET_HIDING_EXTENSION;
			hidingConfigFile = selectedFile.toPath().resolveSibling(filename);
		}
		
		final WaitingIndicatorDialogController waitDialog = UtilityDialogs.showWaitDialog(wnd, "generic.waitdlg.title", "open.waitdlg.header", selectedFile.getName());
		final Future<?> preprocessTask = L2ThreadPool.submitLongRunning(() -> {
			final String filename = hidingConfigFile.getFileName().toString();
			final Map<EndpointType, Set<byte[]>> phc, tmp;
			switch (exportType)
			{
				case TAB_ONLY:
					phc = _tabHidingConfigProperty.get().getSaveableFormat();
					break;
				case PROTOCOL_ONLY:
					phc = ProtocolPacketHidingManager.getInstance().getHidingConfiguration(_protocolVersion).get().getSaveableFormat();
					break;
				case MERGE:
					phc = _tabHidingConfigProperty.get().getSaveableFormat();
					tmp = ProtocolPacketHidingManager.getInstance().getHidingConfiguration(_protocolVersion).get().getSaveableFormat();
					phc.computeIfAbsent(EndpointType.CLIENT, et -> new HashSet<>()).addAll(tmp.getOrDefault(EndpointType.CLIENT, Collections.emptySet()));
					phc.computeIfAbsent(EndpointType.SERVER, et -> new HashSet<>()).addAll(tmp.getOrDefault(EndpointType.SERVER, Collections.emptySet()));
					break;
				default:
					throw new AssertionError(exportType);
			}
			
			try
			{
				PacketHidingConfigFileUtils.saveHidingConfiguration(hidingConfigFile,
						new PacketHidingConfig(phc.getOrDefault(EndpointType.CLIENT, Collections.emptySet()), phc.getOrDefault(EndpointType.SERVER, Collections.emptySet())));
			}
			catch (final InterruptedException e)
			{
				// cancelled by user
			}
			catch (final IOException e)
			{
				final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, MainWindowController.class.getName());
				Platform.runLater(() -> wrapException(t, "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.io", null, wnd, Modality.NONE).show());
			}
			
			Platform.runLater(waitDialog::onWaitEnd);
		});
		waitDialog.setCancelAction(() -> preprocessTask.cancel(true));
	}
	
	@FXML
	private void importHidingConfig(ActionEvent event)
	{
		final FileChooser fc = new FileChooser();
		fc.setTitle(UIStrings.get("open.nph.fileselect.title"));
		fc.getExtensionFilters().addAll(new ExtensionFilter(UIStrings.get("open.nph.fileselect.description"), "*." + PROTOCOL_PACKET_HIDING_EXTENSION),
				new ExtensionFilter(UIStrings.get("generic.filedlg.allfiles"), "*.*"));
		fc.setInitialDirectory(_lastOpenDirectory);
		
		final Window wnd = ((Node)event.getSource()).getScene().getWindow();
		final File selectedFile = fc.showOpenDialog(wnd);
		if (selectedFile == null)
			return;
		
		_lastOpenDirectory = selectedFile.getParentFile();
		
		final WaitingIndicatorDialogController waitDialog = UtilityDialogs.showWaitDialog(wnd, "generic.waitdlg.title", "open.waitdlg.header", selectedFile.getName());
		final Future<?> preprocessTask = L2ThreadPool.submitLongRunning(() -> {
			final Path hidingConfigFile = selectedFile.toPath();
			final String filename = hidingConfigFile.getFileName().toString();
			
			IPacketHidingConfig tmp = null;
			try
			{
				tmp = PacketHidingConfigFileUtils.readHidingConfiguration(hidingConfigFile);
			}
			catch (final InterruptedException e)
			{
				// cancelled by user
			}
			catch (final IOException e)
			{
				final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, MainWindowController.class.getName());
				Platform.runLater(() -> wrapException(t, "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.io", null, wnd, Modality.NONE).show());
			}
			catch (final InsufficientlyLargeFileException e)
			{
				Platform.runLater(() -> makeNonModalUtilityAlert(WARNING, wnd, "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.toosmall", null,
						"open.nph.err.dialog.content.toosmall", filename).show());
			}
			catch (final UnknownFileTypeException e)
			{
				Platform.runLater(() -> makeNonModalUtilityAlert(WARNING, wnd, "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.wrongfile", null,
						"open.nph.err.dialog.content.wrongfile", filename, HexUtil.bytesToHexString(e.getMagic8Bytes(), " ")).show());
			}
			catch (final DamagedFileException e)
			{
				Platform.runLater(() -> makeNonModalUtilityAlert(ERROR, wnd, "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.damaged", null,
						"open.netpro.err.dialog.content.damaged", filename).show());
			}
			catch (final RuntimeException e)
			{
				final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, MainWindowController.class.getName());
				Platform.runLater(() -> wrapException(t, "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.runtime", null, wnd, Modality.NONE).show());
			}
			
			final IPacketHidingConfig config = tmp;
			Platform.runLater(() -> {
				waitDialog.onWaitEnd();
				
				if (config == null)
					return;
				
				if (_tabHidingConfigProperty != null)
				{
					_tabHidingConfigProperty.set(config);
					_onTabConfigChange.run();
				}
				else
				{
					ProtocolPacketHidingManager.getInstance().getHidingConfiguration(_protocolVersion).set(config);
					_onProtocolConfigChange.run();
				}
				setPacketTemplates(_clientPackets, _serverPackets, _tabHidingConfigProperty, _onTabConfigChange, _protocolVersion, _onProtocolConfigChange);
			});
		});
		waitDialog.setCancelAction(() -> preprocessTask.cancel(true));
	}
	
	private enum ExportType
	{
		TAB_ONLY("packetdc.export.choicedialog.tab"), PROTOCOL_ONLY("packetdc.export.choicedialog.protocol"), MERGE("packetdc.export.choicedialog.both");
		
		private final String _uiString;
		
		ExportType(String uiString)
		{
			_uiString = uiString;
		}
		
		@Override
		public String toString()
		{
			return UIStrings.get(_uiString);
		}
		
		static final ExportType[] VALUES = values();
	}
	
	/**
	 * Fills tables with given packet templates and binds the UI controls with underlying packet hiding configurations.
	 * 
	 * @param clientPackets known client packet templates
	 * @param serverPackets known server packet templates
	 * @param tabHidingConfigProperty packet hiding config of the associated tab (or {@code null})
	 * @param onTabConfigChange action to be taken when tab config changes
	 * @param protocolVersion associated protocol version
	 * @param onProtocolConfigChange action to be taken when protocol config changes
	 */
	public void setPacketTemplates(Set<IPacketTemplate> clientPackets, Set<IPacketTemplate> serverPackets, ObjectProperty<IPacketHidingConfig> tabHidingConfigProperty, Runnable onTabConfigChange,
			IProtocolVersion protocolVersion, Runnable onProtocolConfigChange)
	{
		_clientPackets = clientPackets;
		_serverPackets = serverPackets;
		_protocolVersion = protocolVersion;
		_tabHidingConfigProperty = tabHidingConfigProperty;
		_onTabConfigChange = onTabConfigChange;
		_onProtocolConfigChange = onProtocolConfigChange;
		
		_clientPacketTableController.setTemplates(EndpointType.CLIENT, clientPackets, tabHidingConfigProperty, onTabConfigChange, protocolVersion, onProtocolConfigChange);
		_serverPacketTableController.setTemplates(EndpointType.SERVER, serverPackets, tabHidingConfigProperty, onTabConfigChange, protocolVersion, onProtocolConfigChange);
	}
}
