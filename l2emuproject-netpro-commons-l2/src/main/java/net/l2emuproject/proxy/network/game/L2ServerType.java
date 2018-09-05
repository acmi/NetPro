package net.l2emuproject.proxy.network.game;

import java.util.Collections;
import java.util.Set;

public enum L2ServerType {
	LIVE(Collections.emptySet()),
	CLASSIC(Collections.singleton("classic")),
	ARENA(Collections.singleton("arena"));
	
	private Set<String> _altModeSet;
	
	private L2ServerType(Set<String> altModeSet) {
		_altModeSet = altModeSet;
	}
	
	public Set<String> getAltModeSet() {
		return _altModeSet;
	}
	
	public static L2ServerType valueOf(int serverType) {
		switch (serverType) {
		case 1:
			return CLASSIC;
		case 2:
			return ARENA;
		default:
			return LIVE;
		}
	}
}
