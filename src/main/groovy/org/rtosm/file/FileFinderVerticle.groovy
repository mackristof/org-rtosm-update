package org.rtosm.file

import org.vertx.groovy.platform.Verticle

/**
 * Created by https://github.com/mackristof on 09/11/2013.
 */
class FileFinderVerticle extends Verticle {

	def start() {
		vertx.eventBus.registerHandler("find-page"){ message ->
			container.logger.info("Find page Verticle received msg : $message.body")
			message.reply(new File('src/main/resources/html/'+(message.body == "/" ? "index.html" : message.body)).text)
		}
	}
}
