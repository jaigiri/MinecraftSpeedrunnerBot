package com.daytrip.hack;

import net.minecraft.client.Minecraft;

public class Hack {
	protected final Minecraft minecraft;

	protected boolean enabled;

	protected int keyBind;
	protected final String name;

	public Hack(int keyBind, String name) {
		minecraft = Minecraft.getInstance();

		this.keyBind = keyBind;
		this.name = name;
	}

	public void toggleEnabled() {
		setEnabled(!enabled);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if(enabled) {
			enable();
		} else {
			disable();
		}
	}

	public void enable() {

	}

	public void disable() {

	}

	public int getKeyBind() {
		return keyBind;
	}

	public void setKeyBind(int keyBind) {
		this.keyBind = keyBind;
	}

	public String getName() {
		return name;
	}
}
