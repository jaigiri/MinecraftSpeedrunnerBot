package com.daytrip.event.impl;

import com.daytrip.event.CancellableEvent;

import javax.annotation.Nullable;

public class EventClickMouse extends CancellableEvent {
	public Button button;

	public enum Button {
		LEFT(0),
		RIGHT(1),
		MIDDLE(2),
		;

		private final int id;

		Button(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		@Nullable
		public static Button byId(int id) {
			for(Button button : Button.values()) {
				if(button.id == id) return button;
			}
			return null;
		}
	}
}
