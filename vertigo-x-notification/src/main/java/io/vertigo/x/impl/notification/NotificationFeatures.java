package io.vertigo.x.impl.notification;

import io.vertigo.core.config.Features;
import io.vertigo.x.notification.NotificationManager;
import io.vertigo.x.plugins.notification.redis.RedisNotificationPlugin;

/**
 * Defines extension notification.
 * @author pchretien
 */
public final class NotificationFeatures extends Features {

	/**
	 * Constructor.
	 */
	public NotificationFeatures() {
		super("x-notification");
	}

	/** {@inheritDoc} */
	@Override
	protected void setUp() {
		getModuleConfigBuilder()
				.addComponent(NotificationManager.class, NotificationManagerImpl.class);
	}

	/**
	 * @return Active redis plugin
	 */
	public NotificationFeatures withRedis() {
		getModuleConfigBuilder()
				.addPlugin(RedisNotificationPlugin.class);
		return this;
	}
}
