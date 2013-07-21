/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.devnexus.ting.core.service.impl;

import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devnexus.ting.core.dao.BackupDao;
import com.devnexus.ting.core.dao.SchemaMigrationDao;
import com.devnexus.ting.core.dao.SystemDao;
import com.devnexus.ting.core.model.Backup;
import com.devnexus.ting.core.model.SchemaMigration;
import com.devnexus.ting.core.service.SystemSetupService;

/**
 * @author Gunnar Hillert
 * @since 1.0
 */
@Service("systemSetupService")
@Transactional
public class SystemSetupServiceImpl implements SystemSetupService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemSetupServiceImpl.class);

	private @Autowired SchemaMigrationDao   schemaMigrationDao;
	private @Autowired BackupDao            backupDao;
	private @Autowired SystemDao            systemDao;

	private @Value("${database.jdbc.driverClassName}") String jdbcDriverClassName;
	private @Value("${database.jdbc.url}") String jdbcDatabaseUrl;

	@Override
	public void restore(final InputStream inputStream) {

		final Backup backup = backupDao.convertToBackupData(inputStream);

		this.restore(backup);

	}

	/** {@inheritDoc} */
	@Override
	public void loadAndRestoreSeedData() {
		final InputStream is = SystemSetupServiceImpl.class.getResourceAsStream("/data/seeddata.xml");
		restore(is);
	}

	/** {@inheritDoc} */
	@Override
	public void restore(final Backup backup) {

	   //TODO

	}

	@Override
	public void createDatabase() {
		LOGGER.warn("Create Database with Settings jdbcDriverClassName: '{}'; jdbcDatabaseUrl: '{}'", jdbcDriverClassName, jdbcDatabaseUrl);
		systemDao.createDatabase(false, null);
	}

	@Override
	public void updateDatabase() {
		systemDao.updateDatabase();
	}

	@Override
	public boolean isDatabaseSetup() {
		try {
			final List<SchemaMigration> migrations = schemaMigrationDao.getAll();

			if (migrations.isEmpty()) {
				return false;
			} else {
				return true;
			}
		} catch (InvalidDataAccessResourceUsageException e) {
			LOGGER.warn("Looks like the database has not been set up, yet.", e.getMessage());
			return false;
		}
	}

	@Override
	public Backup convertToBackupData(InputStream inputStream) {
		return backupDao.convertToBackupData(inputStream);
	}

}
