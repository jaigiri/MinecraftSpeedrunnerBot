package com.daytrip.hack;

import com.daytrip.event.Events;
import com.daytrip.event.impl.EventKeypress;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class HackManager {
	private static final List<Hack> hacks = new ArrayList<>();

	public static void init() {
		Events.on(EventKeypress.class, event -> {
			EventKeypress keypress = (EventKeypress) event;
			if(keypress.keyAction == 0) {
				for(Hack hack : hacks) {
					if(hack.getKeyBind() == keypress.keyCode) {
						hack.toggleEnabled();
					}
				}
			}
		});
	}

	public static void register(Hack hack) {
		hacks.add(hack);
	}

	@Nullable
	public static <T extends Hack> T byClass(@Nonnull Class<T> tClass) {
		return (T) hacks.stream().filter(hack -> hack.getClass() == tClass).findFirst().orElse(null);
	}
}
