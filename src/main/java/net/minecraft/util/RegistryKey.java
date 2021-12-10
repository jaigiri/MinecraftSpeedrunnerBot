package net.minecraft.util;

import com.google.common.collect.Maps;
import net.minecraft.util.registry.Registry;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class RegistryKey<T> {
	private static final Map<String, RegistryKey<?>> UNIVERSAL_KEY_MAP = Collections.synchronizedMap(Maps.newIdentityHashMap());
	private final ResourceLocation parent;
	private final ResourceLocation location;

	private RegistryKey(ResourceLocation parent, ResourceLocation location) {
		this.parent = parent;
		this.location = location;
	}

	public static <T> RegistryKey<T> getOrCreateKey(RegistryKey<? extends Registry<T>> parent, ResourceLocation location) {
		return getOrCreateKey(parent.location, location);
	}

	public static <T> RegistryKey<Registry<T>> getOrCreateRootKey(ResourceLocation location) {
		return getOrCreateKey(Registry.ROOT, location);
	}

	private static <T> RegistryKey<T> getOrCreateKey(ResourceLocation parent, ResourceLocation location) {
		String s = (parent + ":" + location).intern();
		return (RegistryKey<T>) UNIVERSAL_KEY_MAP.computeIfAbsent(s, (p_240906_2_) -> new RegistryKey(parent, location));
	}

	public static <T> Function<ResourceLocation, RegistryKey<T>> getKeyCreator(RegistryKey<? extends Registry<T>> parent) {
		return (p_240907_1_) -> getOrCreateKey(parent, p_240907_1_);
	}

	public String toString() {
		return "ResourceKey[" + this.parent + " / " + this.location + ']';
	}

	public boolean isParent(RegistryKey<? extends Registry<?>> key) {
		return this.parent.equals(key.getLocation());
	}

	public ResourceLocation getLocation() {
		return this.location;
	}
}
