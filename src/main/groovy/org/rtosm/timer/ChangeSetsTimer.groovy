package org.rtosm.timer


import org.vertx.groovy.platform.Verticle

/**
 * Verticle to start changeset loader every minute
 * and regularly publish these results to client
 * @author https://github.com/mackristof
 *
 */


class ChangeSetsTimer extends Verticle {
	
	def start() {
		long periodic = 60000l
		
		
		vertx.setPeriodic(periodic) {
			vertx.eventBus.send("load-changesets", '') { response ->
				container.logger.info('message received from load-changesets')
				container.logger.info(response.body.ids)
				long init = 0l
				for (idChangeset in response.body.ids) {
					final idToTransfert = idChangeset 
					Long start = init+(periodic/response.body.ids.size()).longValue()
					init += (periodic/response.body.ids.size()).longValue()
					vertx.setTimer(start) {
						vertx.eventBus.publish("download-changeset-by-id", idToTransfert)
					}
				}
			}
		}
	}

}
