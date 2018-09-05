package net.l2emuproject.proxy.network.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public enum L2ServerLocale {
	KOREA(Collections.singleton("korea")),
	ENGLISH(Collections.emptySet()),
	JAPAN(Collections.singleton("japan")),
	TAIWAN(Collections.singleton("taiwan")),
	CHINA(Collections.singleton("china")),
	THAILAND(Collections.singleton("thailand")),
	PHILIPPINE(Collections.singleton("philippine")),
	INDONESIA(Collections.singleton("indonesia")),
	RUSSIA(Collections.singleton("russia")),
	EUROPE(Collections.singleton("europe")),
	GERMANY(Collections.singleton("germany")),
	FRANCE(Collections.singleton("france")),
	POLAND(Collections.singleton("poland")),
	TURKEY(Collections.singleton("turkey"));

	private static final L2ServerLocale[] VALUES = values();
	public static final Set<String> ALL_LOCALES = Collections.unmodifiableSet(Arrays.stream(VALUES).map(L2ServerLocale::getAltModeSet).flatMap(Set::stream).collect(Collectors.toSet()));
	
	private Set<String> _altModeSet;
	
	private L2ServerLocale(Set<String> altModeSet) {
		_altModeSet = altModeSet;
	}
	
	public Set<String> getAltModeSet() {
		return _altModeSet;
	}
	
	public static L2ServerLocale valueOf(int localization) {
		if (localization < 0 || localization >= VALUES.length)
			return null;
		return VALUES[localization];
	}
}
