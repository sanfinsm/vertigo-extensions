package io.vertigo.x.impl.account;

import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.InputStreamBuilder;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.persona.security.UserSession;
import io.vertigo.persona.security.VSecurityManager;
import io.vertigo.x.account.Account;
import io.vertigo.x.account.AccountGroup;
import io.vertigo.x.account.AccountManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * @author pchretien
 */
public final class AccountManagerImpl implements AccountManager {
	private static final String X_ACCOUNT_ID = "X_ACCOUNT_ID";
	private final VSecurityManager securityManager;
	private final AccountStorePlugin accountStorePlugin;
	private final VFile defaultPhoto;

	/**
	 * Constructor.
	 * @param accountStorePlugin Account store plugin
	 * @param fileManager File Manager
	 * @param securityManager Security manager
	 */
	@Inject
	public AccountManagerImpl(final AccountStorePlugin accountStorePlugin, final FileManager fileManager, final VSecurityManager securityManager) {
		Assertion.checkNotNull(accountStorePlugin);
		Assertion.checkNotNull(fileManager);
		Assertion.checkNotNull(securityManager);
		//-----
		this.accountStorePlugin = accountStorePlugin;
		//TODO a remplacer par l'appel a fileManager en v0.9.1
		defaultPhoto = createFile("defaultPhoto.png", "image/png", AccountManagerImpl.class.getResource("defaultPhoto.png"), fileManager);
		this.securityManager = securityManager;
	}

	private static VFile createFile(final String fileName, final String typeMime, final URL ressourceUrl, final FileManager fileManager) {
		final long length;
		final long lastModified;
		final URLConnection conn;
		try {
			conn = ressourceUrl.openConnection();
			try {
				length = conn.getContentLength();
				lastModified = conn.getLastModified();
			} finally {
				conn.getInputStream().close();
			}
		} catch (final IOException e) {
			throw new RuntimeException("Can't get file size", e);
		}
		Assertion.checkArgument(length >= 0, "Can't get file size");
		final InputStreamBuilder inputStreamBuilder = new InputStreamBuilder() {
			@Override
			public InputStream createInputStream() throws IOException {
				return ressourceUrl.openStream();
			}
		};
		return fileManager.createFile(fileName, typeMime, new Date(lastModified), length, inputStreamBuilder);
	}

	/** {@inheritDoc} */
	@Override
	public void login(final URI<Account> accountURI) {
		final UserSession userSession = securityManager.getCurrentUserSession().get();
		userSession.putAttribute(X_ACCOUNT_ID, accountURI);
	}

	/** {@inheritDoc} */
	@Override
	public URI<Account> getLoggedAccount() {
		final UserSession userSession = securityManager.getCurrentUserSession().get();
		final URI<Account> accountUri = userSession.getAttribute(X_ACCOUNT_ID);
		Assertion.checkNotNull(accountUri, "Account was not logged");
		return accountUri;
	}

	/** {@inheritDoc} */
	@Override
	public long getAccountsCount() {
		return accountStorePlugin.getAccountsCount();
	}

	/** {@inheritDoc} */
	@Override
	public Account getAccount(final URI<Account> accountURI) {
		return accountStorePlugin.getAccount(accountURI);
	}

	/** {@inheritDoc} */
	@Override
	public Set<URI<AccountGroup>> getGroupURIs(final URI<Account> accountURI) {
		return accountStorePlugin.getGroupURIs(accountURI);
	}

	/** {@inheritDoc} */
	@Override
	public void saveAccounts(final List<Account> accounts) {
		accountStorePlugin.saveAccounts(accounts);
	}

	/** {@inheritDoc} */
	@Override
	public long getGroupsCount() {
		return accountStorePlugin.getGroupsCount();
	}

	/** {@inheritDoc} */
	@Override
	public Collection<AccountGroup> getAllGroups() {
		return accountStorePlugin.getAllGroups();
	}

	/** {@inheritDoc} */
	@Override
	public AccountGroup getGroup(final URI<AccountGroup> groupURI) {
		return accountStorePlugin.getGroup(groupURI);
	}

	/** {@inheritDoc} */
	@Override
	public Set<URI<Account>> getAccountURIs(final URI<AccountGroup> groupURI) {
		return accountStorePlugin.getAccountURIs(groupURI);
	}

	/** {@inheritDoc} */
	@Override
	public void saveGroup(final AccountGroup saveGroup) {
		accountStorePlugin.saveGroup(saveGroup);
	}

	/** {@inheritDoc} */
	@Override
	public void attach(final URI<Account> accountURI, final URI<AccountGroup> groupURI) {
		accountStorePlugin.attach(accountURI, groupURI);
	}

	/** {@inheritDoc} */
	@Override
	public void detach(final URI<Account> accountURI, final URI<AccountGroup> groupURI) {
		accountStorePlugin.detach(accountURI, groupURI);
	}

	/** {@inheritDoc} */
	@Override
	public void setPhoto(final URI<Account> accountURI, final VFile photo) {
		accountStorePlugin.setPhoto(accountURI, photo);
	}

	/** {@inheritDoc} */
	@Override
	public VFile getPhoto(final URI<Account> accountURI) {
		final Option<VFile> photo = accountStorePlugin.getPhoto(accountURI);
		return photo.getOrElse(defaultPhoto);
	}

}
