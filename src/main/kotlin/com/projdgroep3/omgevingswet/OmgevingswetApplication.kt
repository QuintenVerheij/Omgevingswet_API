package com.projdgroep3.omgevingswet

import com.projdgroep3.omgevingswet.config.config
import com.projdgroep3.omgevingswet.config.server
import com.projdgroep3.omgevingswet.logic.Database.getDatabase
import com.projdgroep3.omgevingswet.models.db.addresses
import com.projdgroep3.omgevingswet.models.db.models
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.event.EventListener
import useraddresses
import users
import java.awt.Desktop
import java.io.IOException
import java.net.URI

@SpringBootApplication
class OmgevingswetApplication


	fun main(args: Array<String>) {
		runApplication<OmgevingswetApplication>(*args)

		transaction(getDatabase()) { SchemaUtils.create(users, addresses, useraddresses, models) }
		browse(config[server.baseUrl] + "/swagger-ui.html")
	}

	fun browse(url: String) {
		if (Desktop.isDesktopSupported()) {
			var desktop: Desktop = Desktop.getDesktop();
			try {
				desktop.browse(URI(url));
			} catch (e: IOException) {
				e.printStackTrace();
			}
		} else {
			var runtime = Runtime.getRuntime()
			try {
				runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
			} catch (e: IOException) {
				e.printStackTrace();
			}
		}
	}

