package net.l2emuproject.proxy.network.meta;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static net.l2emuproject.network.protocol.ClientProtocolVersion.*;

import java.util.EnumSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.Test;

import net.l2emuproject.network.protocol.IProtocolVersion;

public class ProtocolTreeNodeTest {
	@Test
	public void testFromMap() {
		final Map<String, IProtocolVersion> id2CPV = new TreeMap<>();
		id2CPV.put("L2-EF", ETINAS_FATE);
		id2CPV.put("L2-CO", ORFEN);
		id2CPV.put("L2-EF-Taiwan", THE_FINAL_PROTOCOL_VERSION);
		id2CPV.put("L2-PPTS", PRELUDE_PTS);
		
		final Map<String, String> id2PID = new TreeMap<>();
		id2PID.put("L2-EF", null);
		id2PID.put("L2-CO", "L2-EF");
		id2PID.put("L2-EF-Taiwan", "L2-EF");
		id2PID.put("L2-PPTS", null);
		
		ProtocolTreeNode<IProtocolVersion> root = ProtocolTreeNode.fromMap(id2PID, id2CPV);
		assertThat(root.getProtocol(), is(nullValue()));
		assertThat(root.getChildren().stream().map(ProtocolTreeNode::getProtocol).collect(Collectors.toSet()), is(equalTo(EnumSet.of(PRELUDE_PTS, ETINAS_FATE))));
		assertThat(root.getChildren().iterator().next().getChildren().stream().map(ProtocolTreeNode::getProtocol).collect(Collectors.toSet()), is(equalTo(EnumSet.of(ORFEN, THE_FINAL_PROTOCOL_VERSION))));
	}
}
